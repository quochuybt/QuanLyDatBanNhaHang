package gui;


import com.toedter.calendar.JDateChooser;
import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

    // --- (Các hằng số màu sắc ) ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_BUTTON_RED = new Color(220, 53, 69);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_HEADER_BG = new Color(235, 240, 247);
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);

    // --- Components ---
    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JButton btnThemKhuyenMai;
    private JButton btnXoaKhuyenMai;
    private JComboBox<String> cbxLoc;
    private JTextField txtTimKiem;

    // --- DAO ---
    private final KhuyenMaiDAO khuyenMaiDAO;
    private List<KhuyenMai> dsKhuyenMai;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhuyenMaiGUI() {
        this.khuyenMaiDAO = new KhuyenMaiDAO();

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        // khuyenMaiDAO.autoUpdateExpiredStatuses(); // Đã chuyển vào bên trong getAllKhuyenMai() của DAO

        loadDataToTable(); // Nạp dữ liệu ban đầu
        addEventListeners();
        addSearchAndFilterListeners();
    }

    /**
     * Tải dữ liệu ban đầu (tất cả) từ CSDL lên JTable
     */
    private void loadDataToTable() {
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai();
        updateTable(ds);
    }

    /**
     * cập nhật bảng từ một danh sách
     */
    private void updateTable(List<KhuyenMai> ds) {

        modelKhuyenMai.setRowCount(0);
        this.dsKhuyenMai = ds;

        if (ds == null) return;

        for (KhuyenMai km : dsKhuyenMai) {
            String moTa = String.format("<html><b>%s</b><br>%s</html>",
                    km.getTenChuongTrinh(),
                    generateMoTaGiaTri(km));

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
                return "Tặng món (Chi tiết: " + km.getMoTa() + ")";
            default:
                return km.getMoTa();
        }
    }

    /**
     * Tạo Panel Header (Tiêu đề và Nút Thêm, Xóa)
     */
    private JPanel createHeaderPanel() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Chương trình khuyến mãi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnXoaKhuyenMai = new JButton("Xóa");
        btnXoaKhuyenMai.setFont(new Font("Arial", Font.BOLD, 14));
        btnXoaKhuyenMai.setBackground(COLOR_BUTTON_RED);
        btnXoaKhuyenMai.setForeground(COLOR_TEXT_WHITE);
        btnXoaKhuyenMai.setFocusPainted(false);
        btnXoaKhuyenMai.setBorder(new EmptyBorder(10, 15, 10, 15));
        btnXoaKhuyenMai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnXoaKhuyenMai);

        btnThemKhuyenMai = new JButton("+ Thêm khuyến mãi");
        btnThemKhuyenMai.setFont(new Font("Arial", Font.BOLD, 14));
        btnThemKhuyenMai.setBackground(COLOR_BUTTON_BLUE);
        btnThemKhuyenMai.setForeground(COLOR_TEXT_WHITE);
        btnThemKhuyenMai.setFocusPainted(false);
        btnThemKhuyenMai.setBorder(new EmptyBorder(10, 15, 10, 15));
        btnThemKhuyenMai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(btnThemKhuyenMai);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
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

        modelKhuyenMai = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKhuyenMai = new JTable(modelKhuyenMai);

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
        tblKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(250);
        tblKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblKhuyenMai.getColumnModel().getColumn(4).setPreferredWidth(120);

        addTableClickListener();

        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));
        return scrollPane;
    }

    /**
     * Tạo Panel Chân trang (Phân trang) - Tạm thời giữ nguyên
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

    private void stylePaginationButton(JButton btn) {

        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        if (!btn.getText().equals("1")) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
        }
    }

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
     * Gắn sự kiện cho các nút Thêm, Xóa
     */
    private void addEventListeners() {

        btnThemKhuyenMai.addActionListener(e -> {
            showKhuyenMaiDialog(null);
        });
        btnXoaKhuyenMai.addActionListener(e -> {
            xoaKhuyenMai();
        });
    }

    /**
     * Gắn sự kiện cho Tìm kiếm và Lọc
     */
    private void addSearchAndFilterListeners() {

        txtTimKiem.addActionListener(e -> thucHienTimKiemVaLoc());
        cbxLoc.addActionListener(e -> thucHienTimKiemVaLoc());
    }

    /**
     * Hàm thực hiện gọi DAO để tìm kiếm và lọc
     */
    private void thucHienTimKiemVaLoc() {
        String tuKhoa = txtTimKiem.getText().trim();
        String trangThai = (String) cbxLoc.getSelectedItem();

        List<KhuyenMai> ketQua = khuyenMaiDAO.timKiemVaLoc(tuKhoa, trangThai);
        updateTable(ketQua);
    }

    /**
     * Hàm xử lý logic xóa
     */
    /**
     *  Hàm này sẽ cập nhật trạng thái thành "Ngưng áp dụng" thay vì XÓA vĩnh viễn
     */
    private void xoaKhuyenMai() {
        int selectedRow = tblKhuyenMai.getSelectedRow();
        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần cập nhật trạng thái.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //  nội dung hộp thoại xác nhận
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn 'Ngưng áp dụng' khuyến mãi này không?\n(Các hóa đơn cũ vẫn sẽ được giữ lại)", "Xác nhận cập nhật",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Lấy đối tượng KhuyenMai từ danh sách
                int modelRow = tblKhuyenMai.convertRowIndexToModel(selectedRow);
                KhuyenMai kmCanCapNhat = dsKhuyenMai.get(modelRow);

                //  Kiểm tra nếu nó đã "Ngưng áp dụng" rồi
                if (kmCanCapNhat.getTrangThai().equals("Ngưng áp dụng")) {
                    JOptionPane.showMessageDialog(this, "Khuyến mãi này đã ở trạng thái 'Ngưng áp dụng'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Cập nhật trạng thái của đối tượng
                kmCanCapNhat.setTrangThai("Ngưng áp dụng");

                //Gọi hàm UPDATE thay vì DELETE
                boolean success = khuyenMaiDAO.updateKhuyenMai(kmCanCapNhat);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái khuyến mãi thành 'Ngưng áp dụng'.");
                    loadDataToTable(); // Tải lại toàn bộ dữ liệu
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Thêm sự kiện click vào JTable (để Sửa)
     */
    private void addTableClickListener() {

        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblKhuyenMai.getSelectedRow();
                    if (row == -1) return;

                    int modelRow = tblKhuyenMai.convertRowIndexToModel(row);
                    KhuyenMai kmCanSua = dsKhuyenMai.get(modelRow);

                    showKhuyenMaiDialog(kmCanSua);
                }
            }
        });
    }

    /**
     * Hiển thị pop-up (JDialog) để Thêm hoặc Sửa
     * [CẬP NHẬT] Thay thế JTextField ngày bằng JDateChooser
     */
    private void showKhuyenMaiDialog(KhuyenMai km) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quản lý Khuyến mãi", true);
        dialog.setSize(480, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Các trường nhập liệu ---
        JTextField txtMaKM = new JTextField(km != null ? km.getMaKM() : "");
        if (km != null) txtMaKM.setEditable(false);

        JTextField txtTenCT = new JTextField(km != null ? km.getTenChuongTrinh() : "");
        JTextField txtMoTa = new JTextField(km != null ? km.getMoTa() : "");

        String[] loaiKMOptions = {"Giảm theo phần trăm", "Giảm giá số tiền", "Tặng món"};
        JComboBox<String> cbLoaiKM = new JComboBox<>(loaiKMOptions);
        if (km != null) cbLoaiKM.setSelectedItem(km.getLoaiKhuyenMai());

        JTextField txtGiaTri = new JTextField(km != null ? String.valueOf(km.getGiaTri()) : "0");
        JTextField txtDieuKienApDung = new JTextField(km != null ? String.valueOf(km.getDieuKienApDung()) : "0");
        // [SỬA] Thay thế JTextField bằng JDateChooser
        JDateChooser dcNgayBD = new JDateChooser();
        dcNgayBD.setDateFormatString("dd/MM/yyyy");
        if (km != null) {
            dcNgayBD.setDate(Date.valueOf(km.getNgayBatDau())); // Chuyển LocalDate -> java.sql.Date
        }

        JDateChooser dcNgayKT = new JDateChooser();
        dcNgayKT.setDateFormatString("dd/MM/yyyy");
        if (km != null && km.getNgayKetThuc() != null) {
            dcNgayKT.setDate(Date.valueOf(km.getNgayKetThuc()));
        }

        String[] trangThaiOptions = {"Đang áp dụng", "Ngưng áp dụng"};
        JComboBox<String> cbTrangThai = new JComboBox<>(trangThaiOptions);
        if (km != null) cbTrangThai.setSelectedItem(km.getTrangThai());

        // --- Thêm component vào form ---
        formPanel.add(new JLabel("Mã Khuyến mãi:"));
        formPanel.add(txtMaKM);
        formPanel.add(new JLabel("Tên Chương trình:"));
        formPanel.add(txtTenCT);
        formPanel.add(new JLabel("Mô tả:"));
        formPanel.add(txtMoTa);
        formPanel.add(new JLabel("Loại Khuyến mãi:"));
        formPanel.add(cbLoaiKM);
        formPanel.add(new JLabel("Giá trị:"));
        formPanel.add(txtGiaTri);
        formPanel.add(new JLabel("Điều kiện áp dụng (VNĐ):"));
        formPanel.add(txtDieuKienApDung);
        formPanel.add(new JLabel("Ngày Bắt đầu:")); // Sửa label
        formPanel.add(dcNgayBD); // Thêm JDateChooser
        formPanel.add(new JLabel("Ngày Kết thúc:")); // Sửa label
        formPanel.add(dcNgayKT); // Thêm JDateChooser
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
                String moTa = txtMoTa.getText().trim();
                String loai = (String) cbLoaiKM.getSelectedItem();
                String giaTriStr = txtGiaTri.getText().trim();
                String dieuKienStr = txtDieuKienApDung.getText().trim();

                // Lấy ngày từ JDateChooser
                java.util.Date utilNgayBD = dcNgayBD.getDate();
                if (utilNgayBD == null) {
                    JOptionPane.showMessageDialog(dialog, "Ngày bắt đầu không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate ngayBD = utilNgayBD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


                java.util.Date utilNgayKT = dcNgayKT.getDate();
                LocalDate ngayKT = null;
                if (utilNgayKT != null) {

                    ngayKT = utilNgayKT.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }

                String trangThai = (String) cbTrangThai.getSelectedItem();

                // Kiểm tra logic
                if(ma.isEmpty() || ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Mã và Tên không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double giaTri;
                double dieuKienApDung = 0;
                try {
                    giaTri = Double.parseDouble(giaTriStr);
                    if (!dieuKienStr.isEmpty()) { // Chỉ parse nếu có nhập
                        dieuKienApDung = Double.parseDouble(dieuKienStr);
                    }
                    if (giaTri < 0 || dieuKienApDung < 0) throw new NumberFormatException("Giá trị không được âm.");
                } catch (NumberFormatException exNum) {
                    throw new Exception("Giá trị giảm hoặc Điều kiện áp dụng phải là số hợp lệ.");
                }
                if(ngayKT != null && ngayKT.isBefore(ngayBD)) {
                    JOptionPane.showMessageDialog(dialog, "Ngày kết thúc không được trước ngày bắt đầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. Tạo đối tượng KhuyenMai
                KhuyenMai kmMoi = new KhuyenMai(ma, ten, moTa, loai, giaTri,dieuKienApDung, ngayBD, ngayKT, trangThai);

                // 3. Gọi DAO
                boolean success;
                if (km == null) {
                    // Chế độ THÊM MỚI
                    success = khuyenMaiDAO.themKhuyenMai(kmMoi);
                    if(success) JOptionPane.showMessageDialog(dialog, "Đã thêm khuyến mãi thành công!");
                    else JOptionPane.showMessageDialog(dialog, "Thêm thất bại (Có thể trùng mã).", "Lỗi", JOptionPane.ERROR_MESSAGE);

                } else {
                    // Chế độ SỬA
                    success = khuyenMaiDAO.updateKhuyenMai(kmMoi);
                    if(success) JOptionPane.showMessageDialog(dialog, "Đã cập nhật khuyến mãi thành công!");
                    else JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

                if (success) {
                    dialog.dispose();
                    loadDataToTable(); // Tải lại bảng (đã tự động cập nhật trạng thái)
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi định dạng số (Giá trị): " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // In lỗi ra console để debug
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnLuu);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

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