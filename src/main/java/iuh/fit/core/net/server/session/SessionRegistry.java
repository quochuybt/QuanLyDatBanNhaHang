package iuh.fit.core.net.server.session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SessionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRegistry.class);

    private final Map<String, ClientSession> bySessionId = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> byUsername = new ConcurrentHashMap<>();

    public void register(ClientSession session) {
        bySessionId.put(session.getSessionId(), session);
    }

    public synchronized void bindUser(String tenTK, ClientSession newSession) {
        ClientSession oldSession = byUsername.get(tenTK);
        if (oldSession != null && oldSession != newSession) {
            try {
                CompletableFuture<Void> kickFuture = CompletableFuture.runAsync(() -> {
                    try {
                        oldSession.send(MessageEnvelope.event(
                                EventType.SESSION_KICKED.name(),
                                JsonCodec.toJsonNode(Map.of("reason", "Đăng nhập từ thiết bị khác"))
                        ));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                kickFuture.get(500, TimeUnit.MILLISECONDS);

            } catch (TimeoutException te) {
                LOGGER.warn("[SocketServer] Timeout khi gửi SESSION_KICKED tới session cũ={} user={}",
                        oldSession.getSessionId(), tenTK);
            } catch (Exception ex) {
                LOGGER.warn("[SocketServer] Lỗi gửi SESSION_KICKED tới session cũ={} user={} : {}",
                        oldSession.getSessionId(), tenTK, ex.getMessage(), ex);
            } finally {
                remove(oldSession);
                oldSession.close();
            }
        }

        newSession.setTenTK(tenTK);
        byUsername.put(tenTK, newSession);
        LOGGER.info("[SocketServer] Người dùng đã đăng nhập: " + tenTK + " (session=" + newSession.getSessionId() + ")");
    }

    public synchronized void remove(ClientSession session) {
        if (session == null) return;
        bySessionId.remove(session.getSessionId());
        if (session.getTenTK() != null) {
            ClientSession mapped = byUsername.get(session.getTenTK());
            if (mapped == session) {
                byUsername.remove(session.getTenTK());
            }
        }
    }

    public Collection<ClientSession> allSessions() {
        return bySessionId.values();
    }

    public void broadcastEvent(EventType eventType, Map<String, Object> payload) {
        MessageEnvelope event = MessageEnvelope.event(
                eventType.name(),
                JsonCodec.toJsonNode(payload != null ? payload : Map.of())
        );

        for (ClientSession s : allSessions()) {
            try {
                s.send(event);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Broadcast payload chuẩn cho realtime business event.
     */
    public void broadcastBusinessEvent(
            EventType eventType,
            String sourceCommand,
            String entityType,
            String entityId,
            String changeType,
            String actor,
            Map<String, Object> data
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("eventType", eventType.name());
        payload.put("sourceCommand", sourceCommand);
        payload.put("entityType", entityType);
        payload.put("entityId", entityId);
        payload.put("changeType", changeType);
        payload.put("occurredAt", LocalDateTime.now().toString());
        payload.put("actor", actor != null && !actor.isBlank() ? actor : "system");
        payload.put("data", data != null ? data : Map.of());

        broadcastEvent(eventType, payload);
    }

    /**
     * Rút gọn cho các thông báo nhanh.
     */
    public void broadcastBusinessEvent(EventType eventType, String message) {
        broadcastBusinessEvent(
                eventType,
                "MANUAL",
                "NOTIFICATION",
                "NONE",
                "UPDATE",
                "system",
                Map.of("message", message)
        );
    }
}
