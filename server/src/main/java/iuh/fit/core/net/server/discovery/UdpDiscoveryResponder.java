package iuh.fit.core.net.server.discovery;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpDiscoveryResponder implements Runnable {
    public static final String REQUEST_TOKEN = "SG_DISCOVER_REQUEST";

    private final int udpPort;
    private final int tcpPort;
    private final String serviceName;
    private final String version;
    private volatile boolean running = true;

    public UdpDiscoveryResponder(int udpPort, int tcpPort, String serviceName, String version) {
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
        this.serviceName = serviceName;
        this.version = version;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(udpPort, InetAddress.getByName("0.0.0.0"))) {
            socket.setBroadcast(true);
            byte[] buf = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String req = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
                if (!REQUEST_TOKEN.equals(req)) {
                    continue;
                }

                String localIp = InetAddress.getLocalHost().getHostAddress();
                String response = "SG_DISCOVER_RESPONSE|" + serviceName + "|" + localIp + "|" + tcpPort + "|" + version;
                byte[] data = response.getBytes(StandardCharsets.UTF_8);

                DatagramPacket responsePacket = new DatagramPacket(
                        data,
                        data.length,
                        packet.getAddress(),
                        packet.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (Exception ignored) {
        }
    }

    public void stop() {
        running = false;
    }
}
