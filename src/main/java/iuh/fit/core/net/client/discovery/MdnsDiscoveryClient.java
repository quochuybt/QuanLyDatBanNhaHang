package iuh.fit.core.net.client.discovery;

import iuh.fit.core.net.server.discovery.MdnsAnnouncer;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MdnsDiscoveryClient {

    public List<DiscoveredServer> discover(long timeoutMs) {
        Map<String, DiscoveredServer> dedup = new LinkedHashMap<>();

        try (JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost())) {
            ServiceInfo[] services = jmdns.list(MdnsAnnouncer.SERVICE_TYPE, timeoutMs);
            for (ServiceInfo service : services) {
                String[] addresses = service.getHostAddresses();
                if (addresses == null || addresses.length == 0) {
                    continue;
                }

                String host = addresses[0];
                int port = service.getPort();
                String version = service.getPropertyString("version");
                String key = host + ":" + port;

                dedup.putIfAbsent(key, DiscoveredServer.builder()
                        .serviceName(service.getName())
                        .host(host)
                        .port(port)
                        .version(version != null ? version : "unknown")
                        .discoverySource("mDNS")
                        .build());
            }
        } catch (Exception ignored) {
        }

        return new ArrayList<>(dedup.values());
    }
}
