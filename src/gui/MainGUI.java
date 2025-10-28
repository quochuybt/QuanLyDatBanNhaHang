package gui;

import entity.NhanVien; // Cần import NhanVien
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
    // Khai báo các panel khác nếu cần truy cập từ MainGUI

    /**
     * Constructor chính, nhận vai trò, tên và mã nhân viên.
     * @param userRole String đại diện vai trò ("QUANLY" hoặc "NHANVIEN")
     * @param userName Tên hiển thị của người dùng
     * @param maNVDangNhap Mã nhân viên đăng nhập
     */
    public MainGUI(String userRole, String userName, String maNVDangNhap) {
        this.userRole = userRole;
        this.userName = userName;
        this.maNVDangNhap = maNVDangNhap; // Lưu mã NV

        // --- Cài đặt cửa sổ chính ---
        setTitle("StarGuardian Restaurant - Quản lý Nhà hàng"); // Đổi tiêu đề nếu cần
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
        showCard("Màn hình chính");
    }

    /**
     * Constructor phụ (nếu không truyền mã NV, ví dụ cho mục đích test).
     * @param userRole String đại diện vai trò
     * @param userName Tên hiển thị
     */
    public MainGUI(String userRole, String userName) {
        this(userRole, userName, null); // Gọi constructor chính với maNVDangNhap là null
    }


    /**
     * Tạo panel header hiển thị thông tin người dùng.
     * @return JPanel header
     */
    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(Color.WHITE);
        headerContainer.setBorder(new EmptyBorder(0, 10, 0, 0)); // Lề trái
        headerContainer.setPreferredSize(new Dimension(0, 50)); // Chiều cao cố định

        // Panel vẽ thanh màu xanh bo góc
        JPanel blueBarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_ACCENT_BLUE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Bo góc 20px
            }
        };
        blueBarPanel.setOpaque(false); // Để vẽ được bo góc

        // Panel thông tin người dùng (nền trắng, bo góc, viền xám)
        JPanel userInfoPanel = new JPanel(new BorderLayout(10, 0)) { // Khoảng cách ngang 10px
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ nền trắng bo góc
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Vẽ viền xám nhạt bo góc
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        userInfoPanel.setOpaque(false);
        userInfoPanel.setBorder(new EmptyBorder(5, 10, 5, 15)); // Padding bên trong
        userInfoPanel.setPreferredSize(new Dimension(210, 0)); // Chiều rộng cố định

        // Icon người dùng
        JLabel userIconLabel;
        try {
            // Cố gắng tải icon từ resources
            ImageIcon userIcon = new ImageIcon(getClass().getResource("/img/user_icon.png")); // Đảm bảo có file này
            // Kiểm tra xem icon có tải được không
            if (userIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = userIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                userIconLabel = new JLabel(new ImageIcon(scaledImage));
            } else {
                throw new Exception("Icon not loaded"); // Ném lỗi nếu không tải được
            }
        } catch (Exception e) {
            // Nếu lỗi, dùng ký tự thay thế
            System.err.println("Không tìm thấy user_icon.png, dùng ký tự thay thế.");
            userIconLabel = new JLabel("👤"); // Ký tự người dùng
            userIconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24)); // Font hỗ trợ ký tự
        }

        // Panel chứa Tên và Vai trò (xếp dọc)
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        JLabel nameLabel = new JLabel(this.userName != null ? this.userName : "N/A"); // Hiển thị tên (có kiểm tra null)
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);
        JLabel roleLabel = new JLabel(this.userRole != null ? this.userRole : "N/A"); // Hiển thị vai trò (có kiểm tra null)
        roleLabel.setForeground(Color.DARK_GRAY); // Màu chữ xám đậm hơn
        textPanel.add(nameLabel);
        textPanel.add(roleLabel);

        // Gắn icon và text vào userInfoPanel
        userInfoPanel.add(userIconLabel, BorderLayout.WEST);
        userInfoPanel.add(textPanel, BorderLayout.CENTER);

        // Gắn userInfoPanel vào blueBarPanel (căn phải)
        blueBarPanel.add(userInfoPanel, BorderLayout.EAST);
        // Gắn blueBarPanel vào headerContainer
        headerContainer.add(blueBarPanel, BorderLayout.CENTER);

        return headerContainer;
    }

    /**
     * Tạo panel menu bên trái với logo và các nút chức năng.
     * @return JPanel menu
     */
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        menuPanel.setBackground(COLOR_ACCENT_BLUE);
        menuPanel.setPreferredSize(new Dimension(220, 0)); // Chiều rộng cố định
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding trên dưới

        // --- Logo ---
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/DangNhap+Logo/Logo.jpg")); // Đường dẫn logo
            Image originalImage = originalIcon.getImage();
            // Thay đổi kích thước logo nếu cần
            Image resizedImage = originalImage.getScaledInstance(180, 140, Image.SCALE_SMOOTH); // Ví dụ: 180x140
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            JLabel logoLabel = new JLabel(resizedIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa logo
            menuPanel.add(logoLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách dưới logo
        } catch (Exception e) {
            System.err.println("Lỗi tải logo: " + e.getMessage());
            // Có thể thêm JLabel hiển thị lỗi thay thế logo
            JLabel errorLabel = new JLabel("Lỗi tải logo");
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(errorLabel);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        // --- Các nút chức năng ---
        LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();
        // Xác định các mục menu dựa trên vai trò người dùng
        if ("QUANLY".equalsIgnoreCase(this.userRole)) { // Dùng equalsIgnoreCase cho an toàn
            menuItems.put("Màn hình chính", "⌂");      // Ký tự Home
            menuItems.put("Danh mục món ăn", "🍽️"); // Ký tự dao nĩa
            menuItems.put("Lịch làm việc", "📅");   // Ký tự lịch
            menuItems.put("Khuyến mãi", "🏷️");     // Ký tự tag
            menuItems.put("Hóa đơn", "🧾");        // Ký tự hóa đơn
            menuItems.put("Nhân viên", "👥");      // Ký tự nhóm người (thay vì 1 người)
        } else if ("NHANVIEN".equalsIgnoreCase(this.userRole)) {
            menuItems.put("Màn hình chính", "⌂");
            menuItems.put("Danh sách bàn", "🪑");    // Ký tự ghế
            menuItems.put("Thành viên", "🧑");       // Ký tự người lớn
            menuItems.put("Lịch làm việc", "📅");
            menuItems.put("Hóa đơn", "🧾");
        }
        // Nút Đăng xuất luôn có
        menuItems.put("Đăng xuất", "🚪"); // Ký tự cửa ra

        // Tạo và thêm các nút vào menuPanel
        for (Map.Entry<String, String> entry : menuItems.entrySet()) {
            JPanel button = createMenuButton(entry.getKey(), entry.getValue());
            menuButtons.put(entry.getKey(), button); // Lưu lại để quản lý trạng thái active
            menuPanel.add(button);
            menuPanel.add(Box.createRigidArea(new Dimension(0, 1))); // Khoảng cách nhỏ giữa các nút
        }

        menuPanel.add(Box.createVerticalGlue()); // Đẩy các nút lên trên nếu còn trống

        return menuPanel;
    }

    /**
     * Tạo một nút bấm cho menu bên trái.
     * @param text Tên chức năng hiển thị
     * @param iconChar Ký tự icon (có thể là Emoji hoặc ký tự đặc biệt)
     * @return JPanel hoạt động như một nút bấm
     */
    private JPanel createMenuButton(String text, String iconChar) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12)); // Căn trái, padding
        buttonPanel.setBackground(COLOR_ACCENT_BLUE);
        buttonPanel.setMaximumSize(new Dimension(220, 50)); // Chiều cao cố định
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi con trỏ khi rê chuột
        // buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE)); // Bỏ border

        // Icon
        if (iconChar != null && !iconChar.isEmpty()) {
            JLabel iconLabel = new JLabel(iconChar);
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18)); // Font hỗ trợ ký tự đặc biệt
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
                // Xử lý khi bấm nút
                if ("Đăng xuất".equals(text)) {
                    // Hiển thị hộp thoại xác nhận đăng xuất
                    int choice = JOptionPane.showConfirmDialog(
                            MainGUI.this,
                            "Bạn có chắc chắn muốn đăng xuất?",
                            "Xác nhận đăng xuất",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    // Nếu người dùng chọn Yes
                    if (choice == JOptionPane.YES_OPTION) {

                        // --- SỬA: Gọi hàm đóng kết nối CSDL ---
                        connectDB.SQLConnection.closeConnection(); // Đóng kết nối Singleton
                        // --- KẾT THÚC SỬA ---

                        dispose(); // Đóng cửa sổ MainGUI hiện tại
                        // Mở lại cửa sổ đăng nhập (TaiKhoanGUI)
                        SwingUtilities.invokeLater(() -> {
                            new TaiKhoanGUI().setVisible(true);
                        });
                    }
                } else {
                    // Nếu không phải nút Đăng xuất, chuyển sang card tương ứng
                    showCard(text);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Đổi màu nền khi rê chuột vào (nếu nút đó không phải là nút đang active)
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_BUTTON_ACTIVE.brighter()); // Màu sáng hơn màu active
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Trả lại màu nền mặc định khi rê chuột ra (nếu nút đó không phải là nút đang active)
                if (buttonPanel != currentActiveButton) {
                    buttonPanel.setBackground(COLOR_ACCENT_BLUE);
                }
            }
        });

        return buttonPanel;
    }

    /**
     * Khởi tạo các panel con và thêm chúng vào CardLayout của mainContentPanel.
     */
    private void setupMainContentPanel() {
        // --- Panel chung cho mọi vai trò ---
        mainContentPanel.add(new DashboardGUI(), "Màn hình chính"); // Panel Dashboard

        VaiTro vaiTroEnum; // Chuyển String role thành Enum VaiTro
        if (this.userRole != null && this.userRole.equalsIgnoreCase("QUANLY")) {
            vaiTroEnum = VaiTro.QUANLY;
        } else {
            vaiTroEnum = VaiTro.NHANVIEN; // Mặc định là nhân viên nếu không phải quản lý
        }
        // Panel Lịch làm việc (chung)
        mainContentPanel.add(new LichLamViecGUI(vaiTroEnum), "Lịch làm việc");
        // Panel Hóa đơn (chung)
        mainContentPanel.add(new HoaDonGUI(), "Hóa đơn");

        // --- Panels chỉ dành cho Quản lý ---
        if (VaiTro.QUANLY == vaiTroEnum) {
            // <<< --- SỬA Ở ĐÂY --- >>>
            // Thay thế placeholder bằng class GUI thật
            mainContentPanel.add(new DanhMucMonGUI(), "Danh mục món ăn");
            // <<< --- KẾT THÚC SỬA --- >>>

            mainContentPanel.add(new KhuyenMaiGUI(), "Khuyến mãi");
            mainContentPanel.add(new NhanVienGUI(), "Nhân viên");
        }
        // --- Panels chỉ dành cho Nhân viên ---
        else if (VaiTro.NHANVIEN == vaiTroEnum) {
            // Khởi tạo và thêm DanhSachBanGUI (truyền mã NV)
            this.danhSachBanGUI = new DanhSachBanGUI(this, this.maNVDangNhap);
            mainContentPanel.add(danhSachBanGUI, "Danh sách bàn");
            // Khởi tạo và thêm KhachHangGUI
            this.khachHangGUI = new KhachHangGUI();
            mainContentPanel.add(this.khachHangGUI, "Thành viên");
        }
    }


    /**
     * Làm mới dữ liệu trên màn hình quản lý khách hàng (Thành viên).
     * Được gọi từ các panel con (ví dụ: ManHinhDatBanGUI) khi cần cập nhật.
     */
    public void refreshKhachHangScreen() {
        if (khachHangGUI != null) {
            khachHangGUI.refreshKhachHangTable(); // Gọi hàm làm mới của KhachHangGUI
            System.out.println("MainGUI: Đã yêu cầu KhachHangGUI làm mới."); // Log
        } else {
            // Ghi log nếu panel chưa được tạo (thường do vai trò không phù hợp)
            System.err.println("MainGUI: KhachHangGUI chưa được khởi tạo (vai trò có thể không phải Nhân viên?).");
        }
    }

    /**
     * Tạo một panel trống với tên chức năng (dùng làm placeholder).
     * @param name Tên chức năng
     * @return JPanel placeholder
     */
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new GridBagLayout()); // Dùng GridBagLayout để căn giữa dễ dàng
        panel.setBackground(new Color(244, 247, 252)); // Màu nền nhạt
        JLabel label = new JLabel("Giao diện chức năng: " + name); // Text hiển thị
        label.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Font chữ
        panel.add(label); // Thêm label vào giữa panel
        return panel;
    }

    /**
     * Hiển thị panel con tương ứng với tên chức năng và cập nhật trạng thái active của nút menu.
     * @param name Tên của card (phải khớp với tên đã dùng khi add vào CardLayout và tên nút menu)
     */
    private void showCard(String name) {
        // 1. Chuyển đổi panel hiển thị trong CardLayout
        cardLayout.show(mainContentPanel, name);

        // 2. Cập nhật màu sắc nút menu
        // Đặt lại màu nền cho nút đang active trước đó (nếu có)
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_ACCENT_BLUE); // Màu nền mặc định
        }

        // Tìm và đặt màu nền active cho nút mới được chọn
        currentActiveButton = menuButtons.get(name);
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(COLOR_BUTTON_ACTIVE); // Màu nền khi active
        }
    }

} // Kết thúc class MainGUI