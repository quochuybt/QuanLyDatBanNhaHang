package iuh.fit.core.net.client;

import iuh.fit.core.net.client.discovery.DiscoveredServer;

/**
 * Context dùng chung để các màn hình GUI truy cập kết nối socket hiện tại.
 */
public final class NetClientContext {
    private static SocketClientConnection connection;
    private static DiscoveredServer server;

    private NetClientContext() {
    }

    public static synchronized void set(SocketClientConnection conn, DiscoveredServer discoveredServer) {
        connection = conn;
        server = discoveredServer;
    }

    public static synchronized SocketClientConnection getConnection() {
        return connection;
    }

    public static synchronized DiscoveredServer getServer() {
        return server;
    }

    public static synchronized boolean isReady() {
        return connection != null;
    }

    public static synchronized void clear() {
        connection = null;
        server = null;
    }
}
