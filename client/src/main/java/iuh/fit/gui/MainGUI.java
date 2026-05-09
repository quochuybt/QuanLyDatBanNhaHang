package iuh.fit.gui;

import iuh.fit.core.net.client.SocketClientConnection;

/**
 * MainGUI compatibility wrapper for migration plan.
 * DashboardGUI is the current CardLayout container.
 */
public class MainGUI extends DashboardGUI {

    public MainGUI(String userRole, String userName, String maNV, SocketClientConnection connection) {
        super(userRole, userName, maNV, connection);
    }
}