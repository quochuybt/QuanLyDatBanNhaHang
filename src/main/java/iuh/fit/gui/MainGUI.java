package iuh.fit.gui;

/**
 * MainGUI compatibility wrapper for migration plan.
 * DashboardGUI is the current CardLayout container.
 */
public class MainGUI extends DashboardGUI {

    public MainGUI(String userRole, String userName, String maNV) {
        super(userRole, userName, maNV);
    }

    public MainGUI(String userRole, String userName) {
        super(userRole, userName, null);
    }
}
