package iuh.fit.core.net.client.discovery;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class LanServerDiscoveryService {
    private final UdpDiscoveryClient udpDiscoveryClient;
    private final MdnsDiscoveryClient mdnsDiscoveryClient;

    public LanServerDiscoveryService(int udpPort) {
        this.udpDiscoveryClient = new UdpDiscoveryClient(udpPort);
        this.mdnsDiscoveryClient = new MdnsDiscoveryClient();
    }

    public Map<String, List<DiscoveredServer>> discoverByStrategy(long mdnsTimeoutMs, long udpTimeoutMs) {
        Map<String, List<DiscoveredServer>> result = new LinkedHashMap<>();
        List<DiscoveredServer> mdns = mdnsDiscoveryClient.discover(mdnsTimeoutMs);
        result.put("mDNS", mdns);

        if (mdns.isEmpty()) {
            result.put("UDP", udpDiscoveryClient.discover(udpTimeoutMs));
        } else {
            result.put("UDP", new ArrayList<>());
        }
        return result;
    }
}
