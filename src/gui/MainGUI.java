package gui;

import dao.GiaoCaDAO;
import entity.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;          // [MỚI] Import URI
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.Timer;

public class MainGUI extends JFrame {

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>();
    private JPanel currentActiveButton = null;

    private final String userRole;
    private final String userName;
    private final String maNVDangNhap;

    private DanhSachBanGUI danhSachBanGUI;
    private KhachHangGUI khachHangGUI;

    private final GiaoCaDAO giaoCaDAO = new GiaoCaDAO();

    // [MỚI] Link HDSD
    private static final String HDSD_URL = "https://huyhkhanh205.github.io/HDSD/index.html";

    public MainGUI(String userRole, String userName, String maNVDangNhap) {
        this.userRole = userRole;
        this.userName = userName;
        this.maNVDangNhap = maNVDangNhap;

        setTitle("StarGuardian Restaurant - Quản lý Nhà hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0, 0));

        try {
            this.setIconImage(util.AppResource.getAppIcon());
        } catch (Exception e) {
        }

        JPanel menuPanel = createMenuPanel();
        setupMainContentPanel();
        JPanel contentWrapperPanel = new JPanel(new BorderLayout());
        contentWrapperPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentWrapperPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(menuPanel, BorderLayout.WEST);
        add(contentWrapperPanel, BorderLayout.CENTER);

        // Cài đặt phím tắt "?" (Shift + /) để mở HDSD
        setupHelpShortcut();

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        showCard("Dashboard");
    }

    public MainGUI(String userRole, String userName) {
        this(userRole, userName, null);
    }

    // --- Hàm thiết lập phím tắt ---
    private void setupHelpShortcut() {
        JRootPane rootPane = this.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(keyStroke, "openHelp");
        actionMap.put("openHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUserManual();
            }
        });
    }

    // --- [CẬP NHẬT] Hàm mở Link Web Hướng dẫn sử dụng ---
    private void openUserManual() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(HDSD_URL));
            } else {
                // Fallback nếu hệ điều hành không hỗ trợ mở trình duyệt
                JOptionPane.showMessageDialog(this,
                        "Hệ thống không hỗ trợ mở trình duyệt tự động.\n" +
                                "Vui lòng truy cập thủ công: \n" + HDSD_URL,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                // Copy link vào clipboard cho tiện
                java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(HDSD_URL);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi mở đường dẫn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createIconLabel(String iconPath, int width, int height) {
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
            if (originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                iconLabel.setText(getFallbackIconChar(iconPath));
                iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
                iconLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            iconLabel.setText(getFallbackIconChar(iconPath));
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
            iconLabel.setForeground(Color.WHITE);
        }
        return iconLabel;
    }

    private String getFallbackIconChar(String iconPath) {
        if (iconPath.contains("dashboard")) return "⌂";
        else if (iconPath.contains("menu")) return "🍽️";
        else if (iconPath.contains("schedule")) return "📅";
        else if (iconPath.contains("invoice")) return "🧾";
        else if (iconPath.contains("employee")) return "👥";
        else if (iconPath.contains("table")) return "🪑";
        else if (iconPath.contains("customer")) return "🧑";
        else if (iconPath.contains("logout")) return "🚪";
        else if (iconPath.contains("help")) return "❓";
        return "⚪";
    }

    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);
        headerContainer.setBorder(new EmptyBorder(0, 10, 0, 0));
        headerContainer.setPreferredSize(new Dimension(0, 50));

        JPanel blueBarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_ACCENT_BLUE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        blueBarPanel.setOpaque(false);

        JLabel lblCurrentTime = new JLabel();
        lblCurrentTime.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCurrentTime.setForeground(Color.WHITE);
        lblCurrentTime.setBorder(new EmptyBorder(0, 20, 0, 0));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy - HH:mm:ss", new Locale("vi", "VN"));
        Timer timer = new Timer(1000, e -> lblCurrentTime.setText(LocalDateTime.now().format(dtf)));
        timer.setInitialDelay(0);
        timer.start();

        blueBarPanel.add(lblCurrentTime, BorderLayout.WEST);

        JPanel userInfoPanel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        userInfoPanel.setOpaque(false);
        userInfoPanel.setBorder(new EmptyBorder(5, 10, 5, 15));
        userInfoPanel.setPreferredSize(new Dimension(210, 0));

        JLabel userIconLabel = createIconLabel("/img/icon/account_circle.png", 24, 24);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(this.userName != null ? this.userName : "N/A");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);

        JLabel roleLabel = new JLabel(this.userRole != null ? this.userRole : "N/A");
        roleLabel.setForeground(Color.DARK_GRAY);

        textPanel.add(nameLabel);
        textPanel.add(roleLabel);

        userInfoPanel.add(userIconLabel, BorderLayout.WEST);
        userInfoPanel.add(textPanel, BorderLayout.CENTER);

        blueBarPanel.add(userInfoPanel, BorderLayout.EAST);
        headerContainer.add(blueBarPanel, BorderLayout.CENTER);

        return headerContainer;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(COLOR_ACCENT_BLUE);
        menuPanel.setPreferredSize(new Dimension(220, 0));
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/DangNhap+Logo/Logo.jpg"));
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(180, 140, Image.SCALE_SMOOTH);
            menuPanel.add(new JLabel(new ImageIcon(resizedImage)) {{setAlignmentX(Component.CENTER_ALIGNMENT);}});
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        } catch (Exception e) {
            System.err.println("Lỗi tải logo: " + e.getMessage());
        }

        LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();
        if ("QUANLY".equalsIgnoreCase(this.userRole)) {
            menuItems.put("Dashboard", "/img/icon/dashboard.png");
            menuItems.put("Danh mục món ăn", "/img/icon/dining.png");
            menuItems.put("Lịch làm việc", "/img/icon/calendar_month.png");
            menuItems.put("Khuyến mãi", "/img/icon/percent_discount.png");
            menuItems.put("Hóa đơn", "/img/icon/receipt_long.png");
            menuItems.put("Nhân viên", "/img/icon/group.png");
        } else if ("NHANVIEN".equalsIgnoreCase(this.userRole)) {
            menuItems.put("Dashboard", "/img/icon/dashboard.png");
            menuItems.put("Danh sách bàn", "/img/icon/dine_lamp.png");
            menuItems.put("Thành viên", "/img/icon/diversity_3.png");
            menuItems.put("Lịch làm việc", "/img/icon/calendar_month.png");
            menuItems.put("Hóa đơn", "/img/icon/receipt_long.png");
        }
        menuItems.put("Đăng xuất", "/img/icon/logout.png");

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            JPanel button = createMenuButton(entry.getKey(), entry.getValue());
            menuButtons.put(entry.getKey(), button);
            menuPanel.add(button);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 1)));
        }

        // Đẩy nút HDSD xuống đáy
        menuPanel.add(Box.createVerticalGlue());

        JPanel helpButton = createMenuButton("Hướng dẫn sử dụng", "/img/icon/help.png");
        menuPanel.add(helpButton);

        return menuPanel;
    }

    private JPanel createMenuButton(String text, String iconPath) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        buttonPanel.setBackground(COLOR_ACCENT_BLUE);
        buttonPanel.setMaximumSize(new Dimension(220, 50));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(createIconLabel(iconPath, 20, 20));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(label);

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("Đăng xuất".equals(text)) {
                    if ("NHANVIEN".equalsIgnoreCase(userRole)) {
                        int maCa = giaoCaDAO.getMaCaDangLamViec(maNVDangNhap);
                        if (maCa > 0) {
                            JOptionPane.showMessageDialog(MainGUI.this,
                                    "Bạn chưa kết thúc ca làm việc!\n" +
                                            "Vui lòng quay lại Dashboard để kết thúc ca và kiểm tiền trước khi đăng xuất.",
                                    "Cảnh báo chưa kết ca",
                                    JOptionPane.WARNING_MESSAGE);
                            showCard("Dashboard");
                            return;
                        }
                    }
                    int choice = JOptionPane.showConfirmDialog(MainGUI.this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        dispose();
                        SwingUtilities.invokeLater(() -> new TaiKhoanGUI().setVisible(true));
                    }
                }
                // [CẬP NHẬT] Xử lý mở HDSD
                else if ("Hướng dẫn sử dụng".equals(text)) {
                    openUserManual();
                }
                else {
                    showCard(text);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (buttonPanel != currentActiveButton) buttonPanel.setBackground(COLOR_BUTTON_ACTIVE.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (buttonPanel != currentActiveButton) buttonPanel.setBackground(COLOR_ACCENT_BLUE);
            }
        });

        return buttonPanel;
    }

    private void setupMainContentPanel() {
        if ("QUANLY".equalsIgnoreCase(this.userRole)) {
            mainContentPanel.add(new DashboardQuanLyGUI(), "Dashboard");
        } else {
            mainContentPanel.add(new DashboardNhanVienGUI(this.maNVDangNhap, this.userName), "Dashboard");
        }

        VaiTro vaiTroEnum = ("QUANLY".equalsIgnoreCase(this.userRole)) ? VaiTro.QUANLY : VaiTro.NHANVIEN;

        mainContentPanel.add(new LichLamViecGUI(vaiTroEnum), "Lịch làm việc");
        mainContentPanel.add(new HoaDonGUI(), "Hóa đơn");

        if (VaiTro.QUANLY == vaiTroEnum) {
            mainContentPanel.add(new DanhMucMonGUI(), "Danh mục món ăn");
            mainContentPanel.add(new KhuyenMaiGUI(), "Khuyến mãi");
            mainContentPanel.add(new NhanVienGUI(), "Nhân viên");
        } else if (VaiTro.NHANVIEN == vaiTroEnum) {
            this.danhSachBanGUI = new DanhSachBanGUI(this, this.maNVDangNhap);
            mainContentPanel.add(danhSachBanGUI, "Danh sách bàn");
            this.khachHangGUI = new KhachHangGUI();
            mainContentPanel.add(this.khachHangGUI, "Thành viên");
        }
    }

    private void showCard(String name) {
        cardLayout.show(mainContentPanel, name);
        if (currentActiveButton != null) currentActiveButton.setBackground(COLOR_ACCENT_BLUE);
        currentActiveButton = menuButtons.get(name);
        if (currentActiveButton != null) currentActiveButton.setBackground(COLOR_BUTTON_ACTIVE);
    }
}