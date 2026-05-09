package iuh.fit.gui;

import iuh.fit.core.entity.VaiTro;
import iuh.fit.core.net.client.SocketClientConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class DashboardGUI extends JFrame {

    private static final Color COLOR_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_DARK_BLUE = new Color(40, 28, 244);

    private final String userRole;
    private final String userName;
    private final String maNV;
    private final SocketClientConnection connection;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>();
    private JPanel activeMenuButton;

    public DashboardGUI(String userRole, String userName, String maNV, SocketClientConnection connection) {
        this.userRole = userRole;
        this.userName = userName;
        this.maNV = maNV;
        this.connection = Objects.requireNonNull(connection, "SocketClientConnection không được null.");

        setTitle("StarGuardian Restaurant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMenu(), BorderLayout.WEST);

        setupContentCards();

        add(contentPanel, BorderLayout.CENTER);

        showCard("Dashboard");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };

        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 50));
        header.setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTime.setForeground(Color.WHITE);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
                "EEEE, dd/MM/yyyy - HH:mm:ss",
                new Locale("vi", "VN")
        );

        Timer timer = new Timer(1000, e -> lblTime.setText(LocalDateTime.now().format(dtf)));
        timer.setInitialDelay(0);
        timer.start();

        header.add(lblTime, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel(userName + "  |  " + userRole);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setForeground(Color.WHITE);

        userPanel.add(lblUser);
        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COLOR_BLUE);
        menu.setPreferredSize(new Dimension(220, 0));
        menu.setBorder(new EmptyBorder(20, 0, 10, 0));

        JLabel lblApp = new JLabel("StarGuardian");
        lblApp.setFont(new Font("Arial", Font.BOLD, 18));
        lblApp.setForeground(Color.WHITE);
        lblApp.setAlignmentX(Component.CENTER_ALIGNMENT);

        menu.add(lblApp);
        menu.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] items = "QUANLY".equalsIgnoreCase(userRole)
                ? new String[]{"Dashboard", "Danh mục món ăn", "Nhân viên", "Lịch làm việc", "Hóa đơn", "Khuyến mãi"}
                : new String[]{"Dashboard", "Danh sách bàn", "Thành viên", "Lịch làm việc", "Hóa đơn"};

        for (String item : items) {
            JPanel btn = createMenuBtn(item);
            menuButtons.put(item, btn);
            menu.add(btn);
            menu.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        menu.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(COLOR_DARK_BLUE);
        btnLogout.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setMaximumSize(new Dimension(200, 40));

        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn đăng xuất?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new ServerConnectionGUI().setVisible(true));
            }
        });

        menu.add(btnLogout);
        menu.add(Box.createRigidArea(new Dimension(0, 10)));

        return menu;
    }

    private JPanel createMenuBtn(String text) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        btn.setBackground(COLOR_BLUE);
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);

        btn.add(lbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeMenuButton) {
                    btn.setBackground(COLOR_DARK_BLUE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeMenuButton) {
                    btn.setBackground(COLOR_BLUE);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showCard(text);
            }
        });

        return btn;
    }

    private void setupContentCards() {
        boolean isManager = "QUANLY".equalsIgnoreCase(userRole);

        if (isManager) {
            addCardSafe("Dashboard", () -> new DashboardQuanLyGUI(connection));
            addCardSafe("Danh mục món ăn", () -> new DanhMucMonGUI(connection));
            addCardSafe("Nhân viên", () -> new NhanVienGUI(connection));
            addCardSafe("Lịch làm việc", () -> new LichLamViecGUI(VaiTro.QUANLY, connection));
            addCardSafe("Hóa đơn", () -> new HoaDonGUI(connection));
            addCardSafe("Khuyến mãi", () -> new KhuyenMaiGUI(connection));
        } else {
            addCardSafe("Dashboard", () -> new DashboardNhanVienGUI(maNV, userName, connection));
            addCardSafe("Danh sách bàn", () -> new DanhSachBanGUI(this, maNV, connection));
            addCardSafe("Thành viên", () -> new KhachHangGUI(connection));
            addCardSafe("Lịch làm việc", () -> new LichLamViecGUI(VaiTro.NHANVIEN,connection));
            addCardSafe("Hóa đơn", () -> new HoaDonGUI(connection));
        }
    }

    private void addCardSafe(String cardName, java.util.function.Supplier<JPanel> supplier) {
        try {
            contentPanel.add(supplier.get(), cardName);
        } catch (Exception ex) {
            ex.printStackTrace();
            contentPanel.add(buildErrorCard(cardName, ex), cardName);
        }
    }

    private JPanel buildErrorCard(String cardName, Exception ex) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 245, 245));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(
                "Không thể tải màn hình: " + cardName
                        + "\n\n" + ex.getClass().getSimpleName()
                        + ": " + ex.getMessage()
        );
        area.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(area, BorderLayout.CENTER);

        return panel;
    }

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);

        JPanel btn = menuButtons.get(cardName);

        if (btn != null) {
            if (activeMenuButton != null) {
                activeMenuButton.setBackground(COLOR_BLUE);
            }

            activeMenuButton = btn;
            activeMenuButton.setBackground(COLOR_DARK_BLUE);
        }
    }
}
