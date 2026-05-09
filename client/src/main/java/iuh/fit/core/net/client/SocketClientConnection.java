package iuh.fit.core.net.client;

import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.protocol.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
public class SocketClientConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClientConnection.class);
    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Thread readerThread;
    // Cờ trạng thái chạy theo kiểu atomic để nhiều thread (UI thread + reader thread)
    // có thể đọc/ghi an toàn mà không bị race condition.
    // - true  : kết nối đang hoạt động
    // - false : đã ngắt hoặc chưa kết nối
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Lưu các request đang chờ phản hồi từ server.
    // Key = messageId của request đã gửi.
    // Value = CompletableFuture sẽ được complete khi reader thread nhận RESPONSE
    // có correlationId tương ứng.
    // Dùng ConcurrentHashMap để an toàn khi nhiều thread cùng truy cập.
    private final Map<String, CompletableFuture<MessageEnvelope>> pendingResponses = new ConcurrentHashMap<>();
    private final List<ClientEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService eventDispatcher = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "socket-event-dispatcher");
        t.setDaemon(true);
        return t;
    });
    private final ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "socket-ping-scheduler");
        t.setDaemon(true);
        return t;
    });

    public SocketClientConnection(String host, int port, int connectTimeoutMs, int readTimeoutMs) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    public synchronized void connect() {
        if (running.get()) {
            return;
        }

        try {
            // Thiết lập kết nối TCP persistent đến server
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            socket.setSoTimeout(readTimeoutMs);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            running.set(true);
            startReaderLoop();
            // Gửi PING mỗi 10 giây để giữ session sống trên server
            pingScheduler.scheduleAtFixedRate(this::sendPing, 10, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            closeResources();
            throw new RuntimeException("Không thể kết nối server " + host + ":" + port, e);
        }
    }

    public MessageEnvelope sendCommand(String actionName, Object payload, long timeoutMs) {
        if (!running.get()) {
            connect();
        }

        // Mỗi command tạo messageId riêng để match với response qua correlationId
        MessageEnvelope request = MessageEnvelope.command(actionName, JsonCodec.toJsonNode(payload));
        CompletableFuture<MessageEnvelope> future = new CompletableFuture<>();
        pendingResponses.put(request.getMessageId(), future);

        try {
            send(request);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Timeout trả về lỗi chuẩn để UI xử lý thân thiện
            pendingResponses.remove(request.getMessageId());
            return MessageEnvelope.responseFail(
                    request.getMessageId(),
                    ErrorCode.SERVER_UNREACHABLE,
                    "Quá thời gian chờ phản hồi từ server"
            );
        } catch (Exception e) {
            pendingResponses.remove(request.getMessageId());
            LOGGER.error("[SocketClient] Gửi command thất bại: {} | {}", actionName, e.getMessage(), e);
            throw new RuntimeException("Gửi command thất bại", e);
        }
    }

    public void sendPing() {
        if (!running.get()) {
            return;
        }
        try {
            send(MessageEnvelope.ping());
        } catch (Exception ignored) {
        }
    }

    public synchronized void disconnect() {
        running.set(false);
        pingScheduler.shutdown();
        eventDispatcher.shutdown();
        closeResources();
    }

    public void addEventListener(ClientEventListener listener) {
        listeners.add(listener);
    }

    private void startReaderLoop() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while (running.get()) {
                    try {
                        line = in.readLine();
                        if (line == null) break; // server đóng kết nối
                        MessageEnvelope message = JsonCodec.fromJson(line, MessageEnvelope.class);
                        handleIncoming(message);
                    } catch (SocketTimeoutException ignored) {
                        // Timeout đọc bình thường — không có data, tiếp tục loop
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    LOGGER.warn("[SocketClient] Reader thread exception: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
                    listeners.forEach(l -> l.onDisconnected(e));
                }
            } finally {
                running.set(false);
                closeResources();
                LOGGER.warn("[SocketClient] Reader loop kết thúc, complete {} pending futures", pendingResponses.size());
                pendingResponses.forEach((k, f) -> f.completeExceptionally(new RuntimeException("Mất kết nối server")));
                pendingResponses.clear();
            }
        }, "socket-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void handleIncoming(MessageEnvelope message) {
        if (message == null || message.getType() == null) {
            return;
        }

        if (message.getType() == MessageType.RESPONSE) {
            // RESPONSE: complete future đang chờ theo correlationId
            String correlationId = message.getCorrelationId();
            if (correlationId != null) {
                CompletableFuture<MessageEnvelope> f = pendingResponses.remove(correlationId);
                if (f != null) {
                    f.complete(message);
                }
            }
            return;
        }

        if (message.getType() == MessageType.EVENT) {
            // Dispatch event sang thread riêng để không block reader thread
            eventDispatcher.submit(() -> listeners.forEach(l -> l.onEvent(message)));
        }
    }

    private synchronized void send(MessageEnvelope request) throws IOException {
        out.write(JsonCodec.toJson(request));
        out.write("\n");
        out.flush();
    }

    private synchronized void closeResources() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
        in = null;
        out = null;
        socket = null;
    }
}
