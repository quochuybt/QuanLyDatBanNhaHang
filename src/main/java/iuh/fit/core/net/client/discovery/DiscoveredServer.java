package iuh.fit.core.net.client.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveredServer {
    private String serviceName;
    private String host;
    private int port;
    private String version;
    private String discoverySource;
}
