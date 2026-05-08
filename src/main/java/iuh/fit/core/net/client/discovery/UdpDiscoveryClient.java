package iuh.fit.core.net.client.discovery;

import iuh.fit.core.net.server.discovery.UdpDiscoveryResponder;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UdpDiscoveryClient {
    private final int udpPort;

    public UdpDiscoveryClient(int udpPort) {
        this.udpPort = udpPort;
    }

    public List<DiscoveredServer> discover(long timeoutMs) {
        List<DiscoveredServer> results = new ArrayList<>();
        long endAt = System.currentTimeMillis() + timeoutMs;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(400);

            byte[] req = UdpDiscoveryResponder.REQUEST_TOKEN.getBytes(StandardCharsets.UTF_8);
            DatagramPacket requestPacket = new DatagramPacket(
                    req,
                    req.length,
                    InetAddress.getByName("255.255.255.255"),
                    udpPort
            );
            socket.send(requestPacket);

            while (System.currentTimeMillis() < endAt) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    socket.receive(response);

                    String msg = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                    if (!msg.startsWith("SG_DISCOVER_RESPONSE|")) {
                        continue;
                    }

                    String[] parts = msg.split("\\|");
                    if (parts.length < 5) {
                        continue;
                    }

                    DiscoveredServer server = DiscoveredServer.builder()
                            .serviceName(parts[1])
                            .host(parts[2])
                            .port(Integer.parseInt(parts[3]))
                            .version(parts[4])
                            .discoverySource("UDP")
                            .build();

                    boolean existed = results.stream()
                            .anyMatch(s -> s.getHost().equals(server.getHost()) && s.getPort() == server.getPort());
                    if (!existed) {
                        results.add(server);
                    }
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (Exception ignored) {
        }

        return results;
    }
}
