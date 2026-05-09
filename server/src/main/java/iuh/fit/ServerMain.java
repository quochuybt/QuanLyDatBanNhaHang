package iuh.fit;

import iuh.fit.core.net.server.SocketServerBootstrap;

public class ServerMain {
    public static void main(String[] args) {
        int tcpPort = 9090;
        int udpPort = 9091;

        SocketServerBootstrap server = new SocketServerBootstrap(tcpPort, udpPort);
        server.start();
    }
}
