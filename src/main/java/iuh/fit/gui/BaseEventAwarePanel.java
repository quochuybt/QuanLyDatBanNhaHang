package iuh.fit.gui;

import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.protocol.EventType;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base panel tự động đăng ký lắng nghe các business event từ server.
 * Subclass chỉ cần override {@link #onBusinessEvent(EventType)} để xử lý.
 */
public abstract class BaseEventAwarePanel extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEventAwarePanel.class);
    protected final SocketClientConnection connection;

    protected BaseEventAwarePanel(SocketClientConnection connection) {
        this.connection = Objects.requireNonNull(connection, "SocketClientConnection không được null.");
        registerEventListener();
    }

    protected BaseEventAwarePanel(LayoutManager layout, SocketClientConnection connection) {
        super(layout);
        this.connection = Objects.requireNonNull(connection, "SocketClientConnection không được null.");
        registerEventListener();
    }

    private void registerEventListener() {
        connection.addEventListener(event -> {
            if (event == null || event.getName() == null) return;
            try {
                EventType type = EventType.valueOf(event.getName());
                LOGGER.debug("[Event] {} received by {}", type, getClass().getSimpleName());
                onBusinessEvent(type);
            } catch (IllegalArgumentException ignored) {
                // event không thuộc EventType đã biết — bỏ qua
            }
        });
    }

    /**
     * Được gọi trên reader thread khi nhận event từ server.
     * Dùng {@link SwingUtilities#invokeLater} nếu cần cập nhật UI.
     */
    protected abstract void onBusinessEvent(EventType eventType);
}
