package iuh.fit.core.net.server.session;

import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final Map<String, ClientSession> bySessionId = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> byUsername = new ConcurrentHashMap<>();

    public void register(ClientSession session) {
        bySessionId.put(session.getSessionId(), session);
    }

    public synchronized void bindUser(String tenTK, ClientSession newSession) {
        ClientSession oldSession = byUsername.get(tenTK);
        if (oldSession != null && oldSession != newSession) {
            try {
                oldSession.send(MessageEnvelope.event(
                        EventType.SESSION_KICKED.name(),
                        JsonCodec.toJsonNode(Map.of("reason", "Đăng nhập từ thiết bị khác"))
                ));
            } catch (IOException ignored) {
            } finally {
                remove(oldSession);
                oldSession.close();
            }
        }

        newSession.setTenTK(tenTK);
        byUsername.put(tenTK, newSession);
        System.out.println("[SocketServer] Người dùng đã đăng nhập: " + tenTK + " (session=" + newSession.getSessionId() + ")");
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
}
