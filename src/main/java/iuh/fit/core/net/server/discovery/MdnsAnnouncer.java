package iuh.fit.core.net.server.discovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class MdnsAnnouncer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MdnsAnnouncer.class);

    public static final String SERVICE_TYPE = "_starguardian._tcp.local.";

    private final String serviceName;
    private final int tcpPort;
    private final String version;

    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    public MdnsAnnouncer(String serviceName, int tcpPort, String version) {
        this.serviceName = serviceName;
        this.tcpPort = tcpPort;
        this.version = version;
    }

    public void start() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            jmdns = JmDNS.create(addr, serviceName.replace(" ", "-") + "-mdns");
            serviceInfo = ServiceInfo.create(
                    SERVICE_TYPE,
                    serviceName,
                    tcpPort,
                    "app=StarGuardian;env=lan;version=" + version
            );
            jmdns.registerService(serviceInfo);
            LOGGER.info("[SocketServer] Đã công bố mDNS: " + serviceName + " @ " + addr.getHostAddress() + ":" + tcpPort);
        } catch (IOException e) {
            LOGGER.info("[SocketServer] Công bố mDNS thất bại, sẽ dùng UDP fallback. Lý do: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (jmdns != null) {
                if (serviceInfo != null) {
                    jmdns.unregisterService(serviceInfo);
                }
                jmdns.close();
            }
        } catch (Exception ignored) {
        }
    }
}
