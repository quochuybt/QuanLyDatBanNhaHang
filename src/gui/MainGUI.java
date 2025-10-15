package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap; // Dùng LinkedHashMap để duy trì thứ tự các nút
import java.util.Map;

public class MainGUI extends JFrame {

    // --- Định nghĩa màu sắc từ mẫu ---
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243); // Màu xanh nhấn chính
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244); // Màu nền nút đang active (đậm hơn 1 chút)
    private static final Color COLOR_TEXT_WHITE = Color.WHITE; // Màu chữ trắng
    private static final Color COLOR_SEPARATOR = new Color(255, 255, 255, 255); // Màu đường kẻ phân cách

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);

    // Lưu trữ các nút menu để quản lý trạng thái active/inactive theo thứ tự
    private final Map<String, JPanel> menuButtons = new LinkedHashMap<>();
    private JPanel currentActiveButton = null;

    public MainGUI() {
        setTitle("Phần mềm quản lý cửa hàng tiện lợi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        getRootPane().setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0,0));



        // ===== TẠO CÁC THÀNH PHẦN GIAO DIỆN =====
        JPanel menuPanel = createMenuPanel();
        setupMainContentPanel();
        JPanel contentWrapperPanel = new JPanel(new BorderLayout());
        contentWrapperPanel.add(createHeaderPanel(), BorderLayout.NORTH); // Header ở trên
        contentWrapperPanel.add(mainContentPanel, BorderLayout.CENTER); // Nội dung chính ở giữa

        add(menuPanel, BorderLayout.WEST);
        add(contentWrapperPanel, BorderLayout.CENTER);

        // Mặc định hiển thị màn hình đầu tiên ("Màn hình chính")
        showCard("Màn hình chính");
    }
    /**
     * TẠO HEADER (NAVBAR) - PHIÊN BẢN CÓ THỂ ĐIỀU CHỈNH CHIỀU RỘNG
     */
    private JPanel createHeaderPanel() {
        // --- Panel bao bọc bên ngoài ---
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(COLOR_TEXT_WHITE);
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
        JLabel nameLabel = new JLabel("Lâm Đình Khoa");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);
        JLabel roleLabel = new JLabel("Quản lý");
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
        menuPanel.setPreferredSize(new Dimension(220, getHeight()));
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
            JLabel titleLabel = new JLabel("StarGuardian"); // Thay thế bằng tên nhà hàng
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(COLOR_TEXT_WHITE);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(titleLabel);
            JLabel subtitleLabel = new JLabel("restaurant");
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(COLOR_TEXT_WHITE);
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(subtitleLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            System.err.println("LỖI: Không tìm thấy logo tại /img/DangNhap+Logo/Logo.jpg");
            e.printStackTrace();
        }

        // --- Các nút chức năng ---
        // Sử dụng LinkedHashMap để duy trì thứ tự thêm vào
        LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();
        menuItems.put("Màn hình chính", "⌂"); // Icon Unicode
        menuItems.put("Danh mục món ăn", "🍽️");
        menuItems.put("Lịch làm việc", "📅");
        menuItems.put("Khuyến mãi", "🏷️");
        menuItems.put("Hóa đơn", "🧾");
        menuItems.put("Nhân viên", "👤");
        menuItems.put("Đăng xuất", "⎋");

        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            JPanel button = createMenuButton(entry.getKey(), entry.getValue());
            menuButtons.put(entry.getKey(), button); // Lưu lại để quản lý
            menuPanel.add(button);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 1))); // Khoảng cách nhỏ giữa các nút
        }

        return menuPanel;
    }

    /**
     * Helper để tạo một nút menu trên sidebar
     * @param text Tên nút
     * @param iconChar Ký tự Unicode làm icon (hoặc null nếu không có)
     */
    private JPanel createMenuButton(String text, String iconChar) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        buttonPanel.setBackground(COLOR_ACCENT_BLUE); // Màu nền mặc định
        buttonPanel.setMaximumSize(new Dimension(220, 50)); // Chiều rộng bằng sidebar
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder()); // Bỏ border
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_SEPARATOR)); // Đường kẻ dưới dày 1px


        // Icon
        if (iconChar != null && !iconChar.isEmpty()) {
            JLabel iconLabel = new JLabel(iconChar);
            iconLabel.setForeground(COLOR_TEXT_WHITE);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18)); // Font cho Unicode icons
            buttonPanel.add(iconLabel);
        }

        // Text
        JLabel label = new JLabel(text);
        label.setForeground(COLOR_TEXT_WHITE);
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
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder());
        mainContentPanel.add(createPlaceholderPanel("Màn hình chính"), "Màn hình chính");
        mainContentPanel.add(createPlaceholderPanel("Danh mục món ăn"), "Danh mục món ăn");
        mainContentPanel.add(createPlaceholderPanel("Lịch làm việc"), "Lịch làm việc");
        mainContentPanel.add(createPlaceholderPanel("Khuyến mãi"), "Khuyến mãi");
        mainContentPanel.add(createPlaceholderPanel("Hóa đơn"), "Hóa đơn");
        mainContentPanel.add(createPlaceholderPanel("Nhân viên"), "Nhân viên");
        mainContentPanel.add(createPlaceholderPanel("Đăng xuất"), "Đăng xuất"); // Mặc dù sẽ không hiển thị, nhưng vẫn cần cho CardLayout
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cố gắng dùng Nimbus Look and Feel nếu có
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                UIManager.put("RootPane.border", BorderFactory.createEmptyBorder());
            } catch (Exception e) {
                // Fallback về mặc định nếu không được
                e.printStackTrace();
            }
            new MainGUI().setVisible(true);
        });
    }
}