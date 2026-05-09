package iuh.fit.core.net.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.protocol.MessageType;
import iuh.fit.core.net.server.dispatch.CommandDispatcher;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

public class ClientSessionHandler implements Runnable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSessionHandler.class);
    private final ClientSession session;
    private final SessionRegistry sessionRegistry;
    private final CommandDispatcher commandDispatcher;

    public ClientSessionHandler(ClientSession session, SessionRegistry sessionRegistry, CommandDispatcher commandDispatcher) {
        this.session = session;
        this.sessionRegistry = sessionRegistry;
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = session.getIn().readLine()) != null) {
                // Mỗi message nhận được => cập nhật heartbeat session
                session.touch();
                LOGGER.debug("[SocketServer] Nhận raw data từ session={}: {}", session.getSessionId(), line.length() > 200 ? line.substring(0, 200) + "..." : line);
                MessageEnvelope request = JsonCodec.fromJson(line, MessageEnvelope.class);

                // Ping/Pong giúp duy trì và kiểm tra kết nối sống
                if (request.getType() == MessageType.PING) {
                    session.send(MessageEnvelope.pong(request.getMessageId()));
                    continue;
                }

                // Command được route qua dispatcher đến handler tương ứng
                if (request.getType() == MessageType.COMMAND) {
                    long startedAt = System.currentTimeMillis();

                    LOGGER.info("[SocketServer] Nhận command: " + request.getName()
                            + " (messageId=" + request.getMessageId() + ", session=" + session.getSessionId() + ")");

                    MessageEnvelope response = commandDispatcher.dispatch(session, request);

                    long afterHandler = System.currentTimeMillis();
                    LOGGER.info("[SocketServer] Xử lý xong command={} (messageId={}, session={}, durationHandlerMs={})",
                            request.getName(),
                            request.getMessageId(),
                            session.getSessionId(),
                            (afterHandler - startedAt));

                    session.send(response);

                    long finishedAt = System.currentTimeMillis();
                    LOGGER.info("[SocketServer] Trả response cho command: " + request.getName()
                            + " (success=" + response.isSuccess()
                            + ", correlationId=" + response.getCorrelationId()
                            + ", totalDurationMs=" + (finishedAt - startedAt) + ")");
                }
            }
        } catch (Exception ex) {
            LOGGER.error("[SocketServer] Lỗi xử lý session={} : {}", session.getSessionId(), ex.getMessage(), ex);
        } finally {
            // Luôn dọn session khi client disconnect/lỗi
            sessionRegistry.remove(session);
            session.close();
        }
    }
}
