package iuh.fit.core.net.server.heartbeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatMonitor.class);

    // Registry chứa toàn bộ session đang mở trên server.
    // Monitor sẽ duyệt registry định kỳ để loại session quá hạn heartbeat.
    private final SessionRegistry sessionRegistry;

    // Ngưỡng timeout (milliseconds). Nếu quá thời gian này không có tín hiệu mới
    // từ client (PING/COMMAND), session được xem là mất kết nối.
    private final long timeoutMs;

    // Scheduler chạy nền 1 luồng, định kỳ kiểm tra session sống/chết.
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public HeartbeatMonitor(SessionRegistry sessionRegistry, long timeoutMs) {
        this.sessionRegistry = sessionRegistry;
        this.timeoutMs = timeoutMs;
    }

    public void start() {
        // Mỗi 5 giây quét toàn bộ session:
        // - nếu session quá hạn timeout => remove khỏi registry + đóng kết nối.
        // Cơ chế này giúp tự dọn tài nguyên khi client rớt mạng hoặc tắt app đột ngột.
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (ClientSession session : sessionRegistry.allSessions()) {
                if (now - session.getLastSeenAt() > timeoutMs) {
                    LOGGER.info("[SocketServer] Dọn session timeout: session=" + session.getSessionId()
                            + ", user=" + (session.getTenTK() != null ? session.getTenTK() : "(ẩn danh)")
                            + ", quá " + timeoutMs + "ms không có heartbeat");
                    sessionRegistry.remove(session);
                    session.close();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        // Dừng scheduler ngay khi server shutdown.
        scheduler.shutdownNow();
    }
}
