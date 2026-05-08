package iuh.fit.core.net.server.session;

import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Getter
public class ClientSession {
    private final String sessionId;
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private volatile long lastSeenAt;
    @Setter
    private volatile String tenTK;

    public ClientSession(Socket socket) throws IOException {
        this.sessionId = UUID.randomUUID().toString();
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.lastSeenAt = System.currentTimeMillis();
    }

    public void touch() {
        this.lastSeenAt = System.currentTimeMillis();
    }

    public synchronized void send(MessageEnvelope message) throws IOException {
        out.write(JsonCodec.toJson(message));
        out.write("\n");
        out.flush();
    }

    public synchronized void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }
}
