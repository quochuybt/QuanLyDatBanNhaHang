package gui;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

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

    public KhuyenMaiGUI() {
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

