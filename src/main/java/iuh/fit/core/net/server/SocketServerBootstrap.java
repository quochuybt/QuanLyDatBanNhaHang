package iuh.fit.core.net.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.server.dispatch.CommandDispatcher;
import iuh.fit.core.net.server.discovery.MdnsAnnouncer;
import iuh.fit.core.net.server.discovery.UdpDiscoveryResponder;
import iuh.fit.core.net.server.heartbeat.HeartbeatMonitor;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServerBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServerBootstrap.class);

    private final int tcpPort;
    private final int udpPort;
    private final SessionRegistry sessionRegistry = new SessionRegistry();
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private volatile boolean running;
    private ServerSocket serverSocket;
    private HeartbeatMonitor heartbeatMonitor;
    private Thread udpThread;
    private UdpDiscoveryResponder udpDiscoveryResponder;
    private MdnsAnnouncer mdnsAnnouncer;

    public SocketServerBootstrap(int tcpPort, int udpPort) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
    }

    public void start() {
        try {
            // 1) Mở cổng TCP chính để nhận kết nối client
            running = true;
            serverSocket = new ServerSocket(tcpPort);

            // 2) Bật heartbeat monitor để dọn session chết/treo
            heartbeatMonitor = new HeartbeatMonitor(sessionRegistry, 30_000);
            heartbeatMonitor.start();

            // 3) Announce service qua mDNS (ưu tiên discovery)
            mdnsAnnouncer = new MdnsAnnouncer("StarGuardian Restaurant", tcpPort, "1.0");
            mdnsAnnouncer.start();

            // 4) Bật UDP responder làm fallback discovery
            udpDiscoveryResponder = new UdpDiscoveryResponder(udpPort, tcpPort, "StarGuardian Restaurant", "1.0");
            udpThread = new Thread(udpDiscoveryResponder, "udp-discovery-responder");
            udpThread.setDaemon(true);
            udpThread.start();

            CommandDispatcher dispatcher = new CommandDispatcher(sessionRegistry);

            while (running) {
                // 5) Với mỗi client mới: tạo session + giao handler xử lý riêng
                Socket socket = serverSocket.accept();
                LOGGER.info("[SocketServer] Đã có client kết nối: "
                        + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                ClientSession session = new ClientSession(socket);
                sessionRegistry.register(session);
                clientExecutor.submit(new ClientSessionHandler(session, sessionRegistry, dispatcher));
            }
        } catch (Exception e) {
            if (running) {
                if (e instanceof BindException) {
                    LOGGER.error("[SocketServer] Lỗi bind cổng TCP={} UDP={}: {}",
                            tcpPort, udpPort, e.getMessage(), e);
                    throw new RuntimeException("Không thể khởi động Socket Server: cổng TCP " + tcpPort
                            + " hoặc UDP " + udpPort + " đang được sử dụng", e);
                }
                LOGGER.error("[SocketServer] Không thể khởi động Socket Server: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể khởi động Socket Server", e);
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        // Dừng toàn bộ hạ tầng theo thứ tự an toàn
        running = false;
        if (udpDiscoveryResponder != null) {
            udpDiscoveryResponder.stop();
        }
        if (mdnsAnnouncer != null) {
            mdnsAnnouncer.stop();
        }
        if (heartbeatMonitor != null) {
            heartbeatMonitor.stop();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        clientExecutor.shutdownNow();
        sessionRegistry.allSessions().forEach(ClientSession::close);
    }
}
