package iuh.fit.core.net.client.discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Quét toàn bộ subnet hiện tại, thử kết nối TCP đến tcpPort.
 * Dùng làm fallback khi UDP broadcast và mDNS đều thất bại.
 */
public class TcpSubnetScanClient {

    private final int tcpPort;
    private final int connectTimeoutMs;

    public TcpSubnetScanClient(int tcpPort) {
        this.tcpPort = tcpPort;
        this.connectTimeoutMs = 300;
    }

    public List<DiscoveredServer> scan() {
        List<DiscoveredServer> results = new ArrayList<>();

        try {
            String localIp = getLocalWifiIp();
            if (localIp == null) return results;

            // Lấy prefix subnet (ví dụ: "192.168.43.")
            String[] parts = localIp.split("\\.");
            if (parts.length != 4) return results;
            String prefix = parts[0] + "." + parts[1] + "." + parts[2] + ".";

            ExecutorService pool = Executors.newFixedThreadPool(50);
            List<Future<DiscoveredServer>> futures = new ArrayList<>();

            for (int i = 1; i <= 254; i++) {
                final String ip = prefix + i;
                if (ip.equals(localIp)) continue; // bỏ qua chính mình

                futures.add(pool.submit(() -> {
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(ip, tcpPort), connectTimeoutMs);
                        return DiscoveredServer.builder()
                                .serviceName("StarGuardian Restaurant")
                                .host(ip)
                                .port(tcpPort)
                                .version("N/A")
                                .discoverySource("TCP Scan")
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);

            for (Future<DiscoveredServer> f : futures) {
                try {
                    DiscoveredServer s = f.get(0, TimeUnit.MILLISECONDS);
                    if (s != null) results.add(s);
                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        return results;
    }

    /**
     * Lấy IP thực để xác định subnet.
     * Ưu tiên: WiFi > Ethernet > bất kỳ IP non-loopback nào.
     */
    private String getLocalWifiIp() {
        try {
            // Thử lấy IP bằng cách kết nối UDP ra ngoài (không gửi gói tin thực)
            // Đây là cách đáng tin cậy nhất để lấy IP đang dùng
            try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 80);
                String ip = socket.getLocalAddress().getHostAddress();
                if (ip != null && !ip.equals("0.0.0.0") && !ip.startsWith("127.")) {
                    return ip;
                }
            } catch (Exception ignored) {}

            // Fallback: duyệt interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(interfaces)) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                String name = ni.getName().toLowerCase();
                if (name.contains("tailscale") || name.contains("openvpn")
                        || name.contains("bluetooth") || name.contains("vmware")
                        || name.contains("virtualbox")) continue;

                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) continue;
                    String ip = addr.getHostAddress();
                    if (ip.contains(":")) continue; // bỏ IPv6
                    if (ip.startsWith("127.") || ip.startsWith("169.254.")) continue;
                    return ip;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
