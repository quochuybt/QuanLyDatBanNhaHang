package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap; // Dùng LinkedHashMap để duy trì thứ tự các nút
import java.util.Map;
 
public class MainGUI extends JFrame {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243); // Màu xanh nhấn chính
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244); // Màu nền nút đang active (đậm hơn 1 chút)



    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);

    // Lưu trữ các nút menu để quản lý trạng thái active/inactive theo thứ tự
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>();
    private JPanel currentActiveButton = null;
    // --- THAY ĐỔI 1: Thêm biến lưu vai trò ---
    private final String userRole;
    private final String userName;

    public MainGUI(String userRole, String userName) {
        this.userRole = userRole;
        this.userName = userName;
        setTitle("Phần mềm quản lý cửa hàng tiện lợi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0, 0));

        // ===== TẠO CÁC THÀNH PHẦN GIAO DIỆN =====
        JPanel menuPanel = createMenuPanel(); // Tạo menu trước (dùng userRole)
        setupMainContentPanel();              // Tạo content sau
        JPanel contentWrapperPanel = new JPanel(new BorderLayout());
        contentWrapperPanel.add(createHeaderPanel(), BorderLayout.NORTH); // Tạo header (dùng userName, userRole)
        contentWrapperPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(menuPanel, BorderLayout.WEST); // Menu bên trái
        add(contentWrapperPanel, BorderLayout.CENTER); // Nội dung chính bên phải

        // Mặc định hiển thị màn hình đầu tiên ("Màn hình chính")
        showCard("Màn hình chính");
    }

    private JPanel createHeaderPanel() {
        // --- Panel bao bọc bên ngoài ---
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);
        headerContainer.setBorder(new EmptyBorder(0, 10, 0, 0)); // Padding
        headerContainer.setPreferredSize(new Dimension(0, 50)); // Set chiều cao tổng thể

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
        // Set kích thước ưa thích cho panel user để thanh xanh biết chừa chỗ
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
        JLabel nameLabel = new JLabel(this.userName); // <-- Lấy tên từ biến đã lưu
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);

        JLabel roleLabel = new JLabel(this.userRole);
        roleLabel.setForeground(Color.BLACK);
        textPanel.add(nameLabel);
        textPanel.add(roleLabel);

        userInfoPanel.add(userIconLabel, BorderLayout.WEST);
        userInfoPanel.add(textPanel, BorderLayout.CENTER);

        // Đặt panel user vào bên phải của thanh xanh
        blueBarPanel.add(userInfoPanel, BorderLayout.EAST);

        // Đặt thanh xanh vào container
        headerContainer.add(blueBarPanel, BorderLayout.CENTER);

        return headerContainer;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        // Sử dụng màu nền xanh từ hình ảnh sidebar
        menuPanel.setBackground(COLOR_ACCENT_BLUE);
        menuPanel.setPreferredSize(new Dimension(220, 0));
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Bỏ padding ngang để các nút tràn ra


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
        // Sử dụng LinkedHashMap để duy trì thứ tự thêm vào
        LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();
        if ("QuanLy".equals(this.userRole)) {
            // Quản lý: Hiển thị tất cả các mục như cũ
            menuItems.put("Màn hình chính", "⌂"); // Icon Unicode
            menuItems.put("Danh mục món ăn", "🍽️");
            menuItems.put("Lịch làm việc", "📅");
            menuItems.put("Khuyến mãi", "🏷️");
            menuItems.put("Hóa đơn", "🧾");
            menuItems.put("Nhân viên", "👤");
        } else if ("NhanVien".equals(this.userRole)) {
            // Nhân viên: Hiển thị các mục bạn yêu cầu
            menuItems.put("Màn hình chính", "⌂");
            menuItems.put("Danh sách bàn", "🪑"); // (Icon ví dụ)
            menuItems.put("Thành viên", "🧑"); // (Icon ví dụ)
            menuItems.put("Lịch làm việc", "📅");
            menuItems.put("Hóa đơn", "🧾");
        }
        menuItems.put("Đăng xuất", "⎋");

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            JPanel button = createMenuButton(entry.getKey(), entry.getValue());
            menuButtons.put(entry.getKey(), button); // Lưu lại để quản lý
            menuPanel.add(button);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 1))); // Khoảng cách nhỏ giữa các nút
        }

        return menuPanel;
    }

    private JPanel createMenuButton(String text, String iconChar) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        buttonPanel.setBackground(COLOR_ACCENT_BLUE); // Màu nền mặc định
        buttonPanel.setMaximumSize(new Dimension(220, 50)); // Chiều rộng bằng sidebar
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE)); // Đường kẻ dưới dày 1px


        // Icon
        if (iconChar != null && !iconChar.isEmpty()) {
            JLabel iconLabel = new JLabel(iconChar);
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18)); // Font cho Unicode icons
            buttonPanel.add(iconLabel);
        }

        // Text
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Font theo mẫu
        buttonPanel.add(label);

        // --- Xử lý sự kiện Click và Hover ---
        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Xử lý sự kiện click
                if (text.equals("Đăng xuất")) {
                    JOptionPane.showMessageDialog(MainGUI.this, "Đăng xuất thành công!");
                    // Thực hiện logic đăng xuất, ví dụ: đóng cửa sổ này và mở cửa sổ đăng nhập
                    dispose();
                    // Mở lại cửa sổ đăng nhập
                    SwingUtilities.invokeLater(() -> {
                        new TaiKhoanGUI().setVisible(true);
                    });
                } else {
                    showCard(text);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Hiệu ứng khi di chuột vào (nếu không phải nút đang active)
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_BUTTON_ACTIVE.brighter()); // Hơi sáng hơn màu active
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Trả về màu cũ khi di chuột ra (nếu không phải nút đang active)
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_ACCENT_BLUE); // Trở về màu nền sidebar
                }
            }
        });

        return buttonPanel;
    }

    private void setupMainContentPanel() {
        // Chung
        mainContentPanel.add(createPlaceholderPanel("Màn hình chính"), "Màn hình chính");
        mainContentPanel.add(createPlaceholderPanel("Lịch làm việc"), "Lịch làm việc");
        mainContentPanel.add(new HoaDonGUI(), "Hóa đơn");

        // Chỉ Quản lý
        mainContentPanel.add(createPlaceholderPanel("Danh mục món ăn"), "Danh mục món ăn");
        mainContentPanel.add(new KhuyenMaiGUI(), "Khuyến mãi");
        mainContentPanel.add(createPlaceholderPanel("Nhân viên"), "Nhân viên");

        // Chỉ Nhân viên
        mainContentPanel.add(new DanhSachBanGUI(), "Danh sách bàn");
        mainContentPanel.add(new KhachHangGUI(), "Thành viên");
    }

    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(244, 247, 252)); // Màu nền chính
        JLabel label = new JLabel("Đây là giao diện " + name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    private void showCard(String name) {
        // 1. Chuyển panel hiển thị
        cardLayout.show(mainContentPanel, name);

        // 2. Cập nhật màu sắc cho nút trong menu
        // Đặt lại màu cho nút active cũ về màu nền sidebar
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_ACCENT_BLUE);
        }

        // Cập nhật nút active mới và đặt màu active
        currentActiveButton = menuButtons.get(name);
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_BUTTON_ACTIVE);
        }
    }
}
