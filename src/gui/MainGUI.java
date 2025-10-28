package gui;

import entity.NhanVien;
import entity.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainGUI extends JFrame {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244);



    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);

    // Lưu trữ các nút menu để quản lý trạng thái active/inactive theo thứ tự
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>();
    private JPanel currentActiveButton = null;

    private final String userRole;
    private final String userName;
    private final String maNVDangNhap; // <--- 🌟 BIẾN MỚI: LƯU MÃ NV

    private DanhSachBanGUI danhSachBanGUI;
    private KhachHangGUI khachHangGUI;


    public MainGUI(String userRole, String userName) {
        this(userRole, userName, null);
    }

    public MainGUI(String userRole, String userName, String maNVDangNhap) { // <--- 🌟 CONSTRUCTOR MỚI
        this.userRole = userRole;
        this.userName = userName;
        this.maNVDangNhap = maNVDangNhap; // <--- 🌟 LƯU MÃ NV
        setTitle("Phần mềm quản lý cửa hàng tiện lợi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0, 0));

        // ===== TẠO CÁC THÀNH PHẦN GIAO DIỆN =====
        JPanel menuPanel = createMenuPanel();
        setupMainContentPanel();
        JPanel contentWrapperPanel = new JPanel(new BorderLayout());
        contentWrapperPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        contentWrapperPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(menuPanel, BorderLayout.WEST);
        add(contentWrapperPanel, BorderLayout.CENTER);

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        showCard("Màn hình chính");
    }

    private JPanel createHeaderPanel() {
        // --- Panel bao bọc bên ngoài ---
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);
        headerContainer.setBorder(new EmptyBorder(0, 10, 0, 0));
        headerContainer.setPreferredSize(new Dimension(0, 50));

        // --- Panel vẽ thanh màu xanh ---
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

        // --- Panel thông tin người dùng với góc bo tròn ---
        JPanel userInfoPanel = new JPanel(new BorderLayout(0, 0)) {
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

        // --- Icon người dùng ---
        JLabel userIconLabel;
        try {
            ImageIcon userIcon = new ImageIcon(getClass().getResource("/img/user_icon.png"));
            Image scaledImage = userIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            userIconLabel = new JLabel(new ImageIcon(scaledImage));
        } catch (Exception e) {
            userIconLabel = new JLabel("👤");
            userIconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));
        }

        // --- Panel chứa Tên và Vai trò ---
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(this.userName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);

        JLabel roleLabel = new JLabel(this.userRole);
        roleLabel.setForeground(Color.BLACK);
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


        // --- Logo ---
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/DangNhap+Logo/Logo.jpg"));
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(250, 200, Image.SCALE_DEFAULT);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            JLabel logoLabel = new JLabel(resizedIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(logoLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Các nút chức năng ---
        LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();
        if ("QUANLY".equals(this.userRole)) {
            menuItems.put("Màn hình chính", "⌂");
            menuItems.put("Danh mục món ăn", "🍽️");
            menuItems.put("Lịch làm việc", "📅");
            menuItems.put("Khuyến mãi", "🏷️");
            menuItems.put("Hóa đơn", "🧾");
            menuItems.put("Nhân viên", "👤");
        } else if ("NHANVIEN".equals(this.userRole)) {
            menuItems.put("Màn hình chính", "⌂");
            menuItems.put("Danh sách bàn", "🪑");
            menuItems.put("Thành viên", "🧑");
            menuItems.put("Lịch làm việc", "📅");
            menuItems.put("Hóa đơn", "🧾");
        }
        menuItems.put("Đăng xuất", "⎋");

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            JPanel button = createMenuButton(entry.getKey(), entry.getValue());
            menuButtons.put(entry.getKey(), button);
            menuPanel.add(button);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 1)));
        }

        return menuPanel;
    }

    private JPanel createMenuButton(String text, String iconChar) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        buttonPanel.setBackground(COLOR_ACCENT_BLUE);
        buttonPanel.setMaximumSize(new Dimension(220, 50));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));


        // Icon
        if (iconChar != null && !iconChar.isEmpty()) {
            JLabel iconLabel = new JLabel(iconChar);
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
            buttonPanel.add(iconLabel);
        }

        // Text
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(label);

        // --- Xử lý sự kiện Click và Hover ---
        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (text.equals("Đăng xuất")) {

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

    private void setupMainContentPanel() {
        // Chung
        mainContentPanel.add(createPlaceholderPanel("Màn hình chính"), "Màn hình chính");
        VaiTro vaiTroEnum;
        if (this.userRole != null && this.userRole.equalsIgnoreCase("QUANLY")) {
            vaiTroEnum = VaiTro.QUANLY;
        } else {
            vaiTroEnum = VaiTro.NHANVIEN;
        }
        // Giả định LichLamViecGUI không cần maNV
        mainContentPanel.add(new LichLamViecGUI(vaiTroEnum), "Lịch làm việc");
        mainContentPanel.add(new HoaDonGUI(), "Hóa đơn");

        // Chỉ Quản lý
        mainContentPanel.add(createPlaceholderPanel("Danh mục món ăn"), "Danh mục món ăn");
        mainContentPanel.add(new KhuyenMaiGUI(), "Khuyến mãi");
        mainContentPanel.add(new NhanVienGUI(), "Nhân viên");

        // Chỉ Nhân viên
        // 🌟 SỬA: Cập nhật constructor DanhSachBanGUI để truyền maNVDangNhap
        this.danhSachBanGUI = new DanhSachBanGUI(this, this.maNVDangNhap);
        mainContentPanel.add(danhSachBanGUI, "Danh sách bàn");
        this.khachHangGUI = new KhachHangGUI();
        mainContentPanel.add(this.khachHangGUI, "Thành viên");
    }

    public void refreshKhachHangScreen() {
        if (khachHangGUI != null) {
            khachHangGUI.refreshKhachHangTable();
            System.out.println("MainGUI: Đã yêu cầu KhachHangGUI làm mới.");
        } else {
            System.err.println("MainGUI: KhachHangGUI chưa được khởi tạo (có thể do vai trò không phải Nhân viên?).");
        }
    }
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(244, 247, 252));
        JLabel label = new JLabel("Đây là giao diện " + name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    private void showCard(String name) {
        // 1. Chuyển panel hiển thị
        cardLayout.show(mainContentPanel, name);

        // 2. Cập nhật màu sắc cho nút trong menu
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_ACCENT_BLUE);
        }

        currentActiveButton = menuButtons.get(name);
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_BUTTON_ACTIVE);
        }
    }
}