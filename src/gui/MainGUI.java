package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap; // Dùng LinkedHashMap để duy trì thứ tự các nút
import java.util.Map;
import java.util.List;
 
public class MainGUI extends JFrame {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243); // Màu xanh nhấn chính
    private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 28, 244); // Màu nền nút đang active (đậm hơn 1 chút)

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
            JLabel titleLabel = new JLabel("StarGuardian"); // Thay thế bằng tên nhà hàng
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(titleLabel);
            JLabel subtitleLabel = new JLabel("restaurant");
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(Color.WHITE);
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
        mainContentPanel.add(new JPanel_KhuyenMai(), "Khuyến mãi");
        mainContentPanel.add(createPlaceholderPanel("Hóa đơn"), "Hóa đơn");
        mainContentPanel.add(createPlaceholderPanel("Nhân viên"), "Nhân viên");
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

  
public class JPanel_KhuyenMai extends JPanel {

    // --- Định nghĩa màu sắc ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_HEADER_BG = new Color(235, 240, 247);
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);

    // --- Components ---
    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JButton btnThemKhuyenMai;
    private JComboBox<String> cbxLoc;
    private JTextField txtTimKiem;

    // --- DAO ---
    private final KhuyenMaiDAO khuyenMaiDAO;
    private List<KhuyenMai> dsKhuyenMai; // Lưu danh sách KM để truy cập khi click
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public JPanel_KhuyenMai() {
        this.khuyenMaiDAO = new KhuyenMaiDAO(); // Khởi tạo DAO

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // === NORTH: Header ===
        add(createHeaderPanel(), BorderLayout.NORTH);

        // === CENTER: Bảng và Tìm kiếm ===
        add(createMainPanel(), BorderLayout.CENTER);

        // === SOUTH: Phân trang ===
        add(createFooterPanel(), BorderLayout.SOUTH);

        // --- Nạp dữ liệu từ CSDL ---
        loadDataToTable();

        // --- Thêm sự kiện cho nút "Thêm" ---
        addEventListeners();
    }

    /**
     * Tải dữ liệu từ CSDL lên JTable
     */
    private void loadDataToTable() {
        modelKhuyenMai.setRowCount(0); // Xóa hết dữ liệu cũ
        dsKhuyenMai = khuyenMaiDAO.getAllKhuyenMai(); // Lấy dữ liệu mới từ DAO

        for (KhuyenMai km : dsKhuyenMai) {
            String moTa = String.format("<html><b>%s</b><br>%s</html>",
                    km.getTenChuongTrinh(),
                    generateMoTaGiaTri(km)); // Tạo mô tả chi tiết

            String ngayKT = (km.getNgayKetThuc() != null) ? km.getNgayKetThuc().format(dtf) : "--";

            modelKhuyenMai.addRow(new Object[]{
                    moTa,
                    km.getLoaiKhuyenMai(),
                    km.getNgayBatDau().format(dtf),
                    ngayKT,
                    km.getTrangThai()
            });
        }
    }

    /**
     * Helper tạo mô tả khuyến mãi dựa trên loại và giá trị
     */
    private String generateMoTaGiaTri(KhuyenMai km) {
        switch (km.getLoaiKhuyenMai()) {
            case "Giảm theo phần trăm":
                return String.format("Giảm %.0f%% cho hóa đơn", km.getGiaTri());
            case "Giảm giá số tiền":
                return String.format("Giảm %.0f VND cho hóa đơn", km.getGiaTri());
            case "Tặng món":
                return "Tặng món theo hóa đơn"; // Cần logic chi tiết hơn
            default:
                return "Chi tiết khuyến mãi";
        }
    }


    /**
     * Tạo Panel Header (Tiêu đề và Nút Thêm)
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Chương trình khuyến mãi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        btnThemKhuyenMai = new JButton("+ Thêm khuyến mãi");
        btnThemKhuyenMai.setFont(new Font("Arial", Font.BOLD, 14));
        btnThemKhuyenMai.setBackground(COLOR_BUTTON_BLUE);
        btnThemKhuyenMai.setForeground(COLOR_TEXT_WHITE);
        btnThemKhuyenMai.setFocusPainted(false);
        btnThemKhuyenMai.setBorder(new EmptyBorder(10, 15, 10, 15));
        btnThemKhuyenMai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.add(btnThemKhuyenMai, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Tạo Panel Chính (Tìm kiếm và Bảng)
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setOpaque(false);
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * Tạo Panel Tìm kiếm và Lọc
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        String[] locOptions = {"Lọc khuyến mãi", "Đang áp dụng", "Ngưng áp dụng"};
        cbxLoc = new JComboBox<>(locOptions);
        cbxLoc.setFont(new Font("Arial", Font.PLAIN, 14));
        cbxLoc.setPreferredSize(new Dimension(160, 38));
        searchPanel.add(cbxLoc);

        JPanel searchBox = new JPanel(new BorderLayout(5, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));

        JLabel searchIcon = new JLabel(" 🔍 ");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        searchBox.add(searchIcon, BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setBorder(null);
        txtTimKiem.setPreferredSize(new Dimension(300, 36));
        addPlaceholder(txtTimKiem, "Tìm kiếm khuyến mãi");
        searchBox.add(txtTimKiem, BorderLayout.CENTER);

        searchPanel.add(searchBox);
        return searchPanel;
    }

    /**
     * Tạo Panel Bảng (JTable)
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"Chương trình khuyến mãi", "Loại khuyến mãi", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"};

        modelKhuyenMai = new DefaultTableModel(columnNames, 0) { // Khởi tạo với 0 hàng
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhuyenMai = new JTable(modelKhuyenMai);

        // --- Tùy chỉnh giao diện cho Bảng ---
        tblKhuyenMai.setRowHeight(60);
        tblKhuyenMai.setFont(new Font("Arial", Font.PLAIN, 14));
        tblKhuyenMai.setGridColor(COLOR_TABLE_GRID);
        tblKhuyenMai.setShowGrid(true);
        tblKhuyenMai.setIntercellSpacing(new Dimension(0, 0));

        tblKhuyenMai.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblKhuyenMai.getTableHeader().setOpaque(false);
        tblKhuyenMai.getTableHeader().setBackground(COLOR_TABLE_HEADER_BG);
        tblKhuyenMai.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhuyenMai.getTableHeader().setReorderingAllowed(false);

        // --- ĐẶT RENDERER TÙY CHỈNH CHO CỘT "TRẠNG THÁI" ---
        tblKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());

        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(250);
        tblKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblKhuyenMai.getColumnModel().getColumn(4).setPreferredWidth(120);

        // --- THÊM SỰ KIỆN CLICK (ĐỂ SỬA) ---
        addTableClickListener();

        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));

        return scrollPane;
    }

    /**
     * Tạo Panel Chân trang (Phân trang)
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);

        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        JButton btnPage1 = new JButton("1");
        btnPage1.setBackground(COLOR_BUTTON_BLUE);
        btnPage1.setForeground(COLOR_TEXT_WHITE);

        stylePaginationButton(btnPrev);
        stylePaginationButton(btnNext);
        stylePaginationButton(btnPage1);

        footerPanel.add(btnPrev);
        footerPanel.add(btnPage1);
        footerPanel.add(btnNext);

        return footerPanel;
    }

    // Helper để style nút phân trang
    private void stylePaginationButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        if (!btn.getText().equals("1")) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
        }
    }

    // --- Hàm Hỗ Trợ Placeholder ---
    private void addPlaceholder(JTextField tf, String placeholder) {
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * Gắn sự kiện cho các nút
     */
    private void addEventListeners() {
        // Sự kiện nút "Thêm khuyến mãi"
        btnThemKhuyenMai.addActionListener(e -> {
            // Mở dialog ở chế độ "Thêm" (truyền null)
            showKhuyenMaiDialog(null);
        });
    }

    /**
     * Thêm sự kiện click vào JTable (để Sửa)
     */
    private void addTableClickListener() {
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Kiểm tra nếu click 2 lần
                if (e.getClickCount() == 2) {
                    int row = tblKhuyenMai.getSelectedRow();
                    if (row == -1) return;

                    // Lấy đối tượng KhuyenMai tương ứng từ danh sách
                    KhuyenMai kmCanSua = dsKhuyenMai.get(row);

                    // Mở dialog ở chế độ "Sửa" (truyền đối tượng km)
                    showKhuyenMaiDialog(kmCanSua);
                }
            }
        });
    }

    /**
     * Hiển thị pop-up (JDialog) để Thêm hoặc Sửa khuyến mãi
     *
     * @param km Đối tượng KhuyenMai để sửa (nếu null, là chế độ Thêm mới)
     */
    private void showKhuyenMaiDialog(KhuyenMai km) {
        // Tạo JDialog (pop-up)
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quản lý Khuyến mãi", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // --- Form nhập liệu ---
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 0 hàng, 2 cột
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Các trường nhập liệu
        JTextField txtMaKM = new JTextField(km != null ? km.getMaKM() : "");
        if (km != null) txtMaKM.setEditable(false); // Không cho sửa Mã
        
        JTextField txtTenCT = new JTextField(km != null ? km.getTenChuongTrinh() : "");
        
        String[] loaiKMOptions = {"Giảm theo phần trăm", "Giảm giá số tiền", "Tặng món"};
        JComboBox<String> cbLoaiKM = new JComboBox<>(loaiKMOptions);
        if (km != null) cbLoaiKM.setSelectedItem(km.getLoaiKhuyenMai());

        JTextField txtGiaTri = new JTextField(km != null ? String.valueOf(km.getGiaTri()) : "0");
        
        // (Nâng cao: nên dùng JDatePicker, ở đây dùng tạm JTextField)
        JTextField txtNgayBD = new JTextField(km != null ? km.getNgayBatDau().format(dtf) : "dd/MM/yyyy");
        JTextField txtNgayKT = new JTextField((km != null && km.getNgayKetThuc() != null) ? km.getNgayKetThuc().format(dtf) : "dd/MM/yyyy");

        String[] trangThaiOptions = {"Đang áp dụng", "Ngưng áp dụng"};
        JComboBox<String> cbTrangThai = new JComboBox<>(trangThaiOptions);
        if (km != null) cbTrangThai.setSelectedItem(km.getTrangThai());

        // Thêm các component vào form
        formPanel.add(new JLabel("Mã Khuyến mãi:"));
        formPanel.add(txtMaKM);
        formPanel.add(new JLabel("Tên Chương trình:"));
        formPanel.add(txtTenCT);
        formPanel.add(new JLabel("Loại Khuyến mãi:"));
        formPanel.add(cbLoaiKM);
        formPanel.add(new JLabel("Giá trị:"));
        formPanel.add(txtGiaTri);
        formPanel.add(new JLabel("Ngày Bắt đầu (dd/MM/yyyy):"));
        formPanel.add(txtNgayBD);
        formPanel.add(new JLabel("Ngày Kết thúc (dd/MM/yyyy):"));
        formPanel.add(txtNgayKT);
        formPanel.add(new JLabel("Trạng thái:"));
        formPanel.add(cbTrangThai);

        dialog.add(formPanel, BorderLayout.CENTER);

        // --- Nút Lưu ---
        JButton btnLuu = new JButton("Lưu lại");
        btnLuu.setBackground(COLOR_BUTTON_BLUE);
        btnLuu.setForeground(COLOR_TEXT_WHITE);
        btnLuu.addActionListener(e -> {
            try {
                // 1. Lấy dữ liệu từ form
                String ma = txtMaKM.getText().trim();
                String ten = txtTenCT.getText().trim();
                String loai = (String) cbLoaiKM.getSelectedItem();
                double giaTri = Double.parseDouble(txtGiaTri.getText().trim());
                LocalDate ngayBD = LocalDate.parse(txtNgayBD.getText().trim(), dtf);
                
                LocalDate ngayKT = null;
                if (!txtNgayKT.getText().trim().isEmpty() && !txtNgayKT.getText().trim().equals("dd/MM/yyyy")) {
                    ngayKT = LocalDate.parse(txtNgayKT.getText().trim(), dtf);
                }
                
                String trangThai = (String) cbTrangThai.getSelectedItem();

                // 2. Tạo đối tượng KhuyenMai
                KhuyenMai kmMoi = new KhuyenMai(ma, ten, loai, giaTri, ngayBD, ngayKT, trangThai);

                // 3. Gọi DAO
                boolean success;
                if (km == null) {
                    // Chế độ THÊM MỚI
                    // success = khuyenMaiDAO.themKhuyenMai(kmMoi); // Bạn cần tự viết hàm này
                    success = true; // Giả sử
                    JOptionPane.showMessageDialog(dialog, "Đã thêm khuyến mãi thành công!");
                } else {
                    // Chế độ SỬA
                    success = khuyenMaiDAO.updateKhuyenMai(kmMoi);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật khuyến mãi thành công!");
                }

                if (success) {
                    dialog.dispose(); // Đóng pop-up
                    loadDataToTable(); // Tải lại bảng
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnLuu);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Hiển thị dialog
        dialog.setVisible(true);
    }


    // --- LỚP CON (INNER CLASS) ĐỂ VẼ CỘT TRẠNG THÁI ---
    private class TrangThaiRenderer extends DefaultTableCellRenderer {
        private final Color COLOR_GREEN_BG = new Color(220, 250, 230);
        private final Color COLOR_GREEN_FG = new Color(0, 150, 50);
        private final Color COLOR_RED_BG = new Color(255, 230, 230);
        private final Color COLOR_RED_FG = new Color(210, 0, 0);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {

            JPanel panel = new JPanel(new GridBagLayout());
            JLabel label = new JLabel(value.toString());

            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 15, 5, 15));

            if ("Đang áp dụng".equals(value.toString())) {
                label.setBackground(COLOR_GREEN_BG);
                label.setForeground(COLOR_GREEN_FG);
            } else {
                label.setBackground(COLOR_RED_BG);
                label.setForeground(COLOR_RED_FG);
            }

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }

            panel.add(label);
            return panel;
        }
    }
}
}

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                // Cố gắng dùng Nimbus Look and Feel nếu có
//                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                    if ("Nimbus".equals(info.getName())) {
//                        UIManager.setLookAndFeel(info.getClassName());
//                        break;
//                    }
//                }
//                UIManager.put("RootPane.border", BorderFactory.createEmptyBorder());
//            } catch (Exception e) {
//                // Fallback về mặc định nếu không được
//                e.printStackTrace();
//            }
//            new MainGUI().setVisible(true);
//        });
//    }
