package iuh.fit.core.net.client.discovery;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class LanServerDiscoveryService {
    private final UdpDiscoveryClient udpDiscoveryClient;
    private final MdnsDiscoveryClient mdnsDiscoveryClient;
    private final TcpSubnetScanClient tcpSubnetScanClient;

    public LanServerDiscoveryService(int udpPort, int tcpPort) {
        this.udpDiscoveryClient = new UdpDiscoveryClient(udpPort);
        this.mdnsDiscoveryClient = new MdnsDiscoveryClient();
        this.tcpSubnetScanClient = new TcpSubnetScanClient(tcpPort);
    }

    public LanServerDiscoveryService(int udpPort) {
        this(udpPort, 9090);
    }
    public Map<String, List<DiscoveredServer>> discoverByStrategy(long mdnsTimeoutMs, long udpTimeoutMs) {
        Map<String, List<DiscoveredServer>> result = new LinkedHashMap<>();

        // 1. mDNS
        List<DiscoveredServer> mdns = mdnsDiscoveryClient.discover(mdnsTimeoutMs);
        result.put("mDNS", mdns);
        if (!mdns.isEmpty()) {
            result.put("UDP", new ArrayList<>());
            result.put("TCP Scan", new ArrayList<>());
            return result;
        }

        // 2. UDP broadcast
        List<DiscoveredServer> udp = udpDiscoveryClient.discover(udpTimeoutMs);
        result.put("UDP", udp);
        if (!udp.isEmpty()) {
            result.put("TCP Scan", new ArrayList<>());
            return result;
        }

        // 3. TCP subnet scan (fallback khi broadcast bị chặn)
        result.put("TCP Scan", tcpSubnetScanClient.scan());
        return result;
    }
}
