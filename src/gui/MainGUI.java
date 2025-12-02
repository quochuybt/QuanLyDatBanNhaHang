package gui;

import entity.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainGUI extends JFrame {
    // --- Constants ---
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244);

    // --- UI Components ---
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>(); // Giữ thứ tự nút menu
    private JPanel currentActiveButton = null; // Nút menu đang được chọn

    // --- User Information ---
    private final String userRole; // Vai trò (String: "QUANLY" or "NHANVIEN")
    private final String userName; // Tên hiển thị
    private final String maNVDangNhap; // Mã nhân viên đăng nhập

    // --- Child Panels ---
    private DanhSachBanGUI danhSachBanGUI; // Panel quản lý bàn (cho nhân viên)
    private KhachHangGUI khachHangGUI;   // Panel quản lý khách hàng (cho nhân viên)

    public MainGUI(String userRole, String userName, String maNVDangNhap) {
        this.userRole = userRole;
        this.userName = userName;
        this.maNVDangNhap = maNVDangNhap; // Lưu mã NV

        // --- Cài đặt cửa sổ chính ---
        setTitle("StarGuardian Restaurant - Quản lý Nhà hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình khi mở
        getRootPane().setBorder(BorderFactory.createEmptyBorder()); // Bỏ viền mặc định của JFrame
        setLayout(new BorderLayout(0, 0)); // Layout chính không có khoảng cách

        // ===== TẠO CÁC THÀNH PHẦN GIAO DIỆN =====
        JPanel menuPanel = createMenuPanel();           // Tạo menu bên trái
        setupMainContentPanel();                        // Khởi tạo các panel nội dung chính
        JPanel contentWrapperPanel = new JPanel(new BorderLayout()); // Panel bao bọc nội dung và header
        contentWrapperPanel.add(createHeaderPanel(), BorderLayout.NORTH); // Thêm header ở trên
        contentWrapperPanel.add(mainContentPanel, BorderLayout.CENTER);   // Thêm panel nội dung ở giữa

        // --- Thêm menu và nội dung vào JFrame ---
        add(menuPanel, BorderLayout.WEST);
        add(contentWrapperPanel, BorderLayout.CENTER);

        // --- Mở rộng cửa sổ ra toàn màn hình ---
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Hiển thị màn hình chính (Dashboard) mặc định
        showCard("Dashboard");
    }

    /**
     * Constructor phụ (nếu không truyền mã NV, ví dụ cho mục đích test).
     */
    public MainGUI(String userRole, String userName) {
        this(userRole, userName, null);
    }

    private JLabel createIconLabel(String iconPath, int width, int height) {
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
            if (originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                String fallbackChar = getFallbackIconChar(iconPath);
                iconLabel.setText(fallbackChar);
                iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
                iconLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải icon: " + iconPath + " - " + e.getMessage());
            String fallbackChar = getFallbackIconChar(iconPath);
            iconLabel.setText(fallbackChar);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
            iconLabel.setForeground(Color.WHITE);
        }
        return iconLabel;
    }

    private String getFallbackIconChar(String iconPath) {
        if (iconPath.contains("dashboard") || iconPath.contains("home")) return "⌂";
        else if (iconPath.contains("menu") || iconPath.contains("food")) return "🍽️";
        else if (iconPath.contains("schedule") || iconPath.contains("calendar")) return "📅";
        else if (iconPath.contains("promotion") || iconPath.contains("discount")) return "🏷️";
        else if (iconPath.contains("invoice") || iconPath.contains("bill")) return "🧾";
        else if (iconPath.contains("employee") || iconPath.contains("staff")) return "👥";
        else if (iconPath.contains("table") || iconPath.contains("chair")) return "🪑";
        else if (iconPath.contains("customer") || iconPath.contains("member")) return "🧑";
        else if (iconPath.contains("logout") || iconPath.contains("exit")) return "🚪";
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
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            JLabel logoLabel = new JLabel(resizedIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(logoLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        } catch (Exception e) {
            System.err.println("Lỗi tải logo: " + e.getMessage());
            JLabel errorLabel = new JLabel("Lỗi tải logo");
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(errorLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
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

        menuPanel.add(Box.createVerticalGlue());
        return menuPanel;
    }

    private JPanel createMenuButton(String text, String iconPath) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        buttonPanel.setBackground(COLOR_ACCENT_BLUE);
        buttonPanel.setMaximumSize(new Dimension(220, 50));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = createIconLabel(iconPath, 20, 20);
        buttonPanel.add(iconLabel);

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(label);

        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("Đăng xuất".equals(text)) {
                    int choice = JOptionPane.showConfirmDialog(
                            MainGUI.this,
                            "Bạn có chắc chắn muốn đăng xuất?",
                            "Xác nhận đăng xuất",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            new TaiKhoanGUI().setVisible(true);
                        });
                    }
                } else {
                    showCard(text);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_BUTTON_ACTIVE.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_ACCENT_BLUE);
                }
            }
        });

        return buttonPanel;
    }

    /**
     * Khởi tạo các panel con và thêm chúng vào CardLayout của mainContentPanel.
     * Đã sửa để phân biệt Dashboard cho Quản lý và Nhân viên.
     */
    private void setupMainContentPanel() {
        // [ĐÃ SỬA] Logic phân chia Dashboard dựa trên UserRole
        if ("QUANLY".equalsIgnoreCase(this.userRole)) {
            // Dashboard thống kê cho Quản Lý
            mainContentPanel.add(new DashboardQuanLyGUI(), "Dashboard");
        } else {
            // Dashboard cá nhân hóa cho Nhân Viên (Giao ca, Hiệu suất cá nhân)
            // Truyền mã NV và Tên NV vào để xử lý logic
            mainContentPanel.add(new DashboardNhanVienGUI(this.maNVDangNhap, this.userName), "Dashboard");
        }

        VaiTro vaiTroEnum;
        if (this.userRole != null && this.userRole.equalsIgnoreCase("QUANLY")) {
            vaiTroEnum = VaiTro.QUANLY;
        } else {
            vaiTroEnum = VaiTro.NHANVIEN;
        }

        // Panel Lịch làm việc (chung)
        mainContentPanel.add(new LichLamViecGUI(vaiTroEnum), "Lịch làm việc");
        // Panel Hóa đơn (chung)
        mainContentPanel.add(new HoaDonGUI(), "Hóa đơn");

        // --- Panels chỉ dành cho Quản lý ---
        if (VaiTro.QUANLY == vaiTroEnum) {
            mainContentPanel.add(new DanhMucMonGUI(), "Danh mục món ăn");
            mainContentPanel.add(new KhuyenMaiGUI(), "Khuyến mãi");
            mainContentPanel.add(new NhanVienGUI(), "Nhân viên");
        }
        // --- Panels chỉ dành cho Nhân viên ---
        else if (VaiTro.NHANVIEN == vaiTroEnum) {
            this.danhSachBanGUI = new DanhSachBanGUI(this, this.maNVDangNhap);
            mainContentPanel.add(danhSachBanGUI, "Danh sách bàn");
            this.khachHangGUI = new KhachHangGUI();
            mainContentPanel.add(this.khachHangGUI, "Thành viên");
        }
    }

    public void refreshKhachHangScreen() {
        if (khachHangGUI != null) {
            khachHangGUI.refreshKhachHangTable();
            System.out.println("MainGUI: Đã yêu cầu KhachHangGUI làm mới.");
        } else {
            System.err.println("MainGUI: KhachHangGUI chưa được khởi tạo.");
        }
    }

    private void showCard(String name) {
        cardLayout.show(mainContentPanel, name);
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_ACCENT_BLUE);
        }
        currentActiveButton = menuButtons.get(name);
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_BUTTON_ACTIVE);
        }
    }
}