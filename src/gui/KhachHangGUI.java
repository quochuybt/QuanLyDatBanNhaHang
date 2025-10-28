package gui;

import dao.KhachHangDAO;
import entity.HangThanhVien;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Đổi tên từ JPanel_ThanhVien thành KhachHangGUI
public class KhachHangGUI extends JPanel {

    // --- Định nghĩa màu sắc ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);

    // --- Components Form ---
    private JTextField txtMaKH, txtTenKH, txtSDT, txtEmail, txtDiaChi, txtTongChiTieu;
    private JComboBox<String> cbGioiTinh, cbHangTV;
    private JTextField txtNgaySinh, txtNgayThamGia;
    private JButton btnThem, btnXoa, btnSua, btnTimKiem, btnLamMoiForm;

    // --- Components Bảng ---
    private JTable tblKhachHang;
    private DefaultTableModel modelKhachHang;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0' VND'");

    // --- DAO & Data ---
    private final KhachHangDAO khachHangDAO;
    private List<KhachHang> dsKhachHang;
    private KhachHang khachHangDangChon = null;

    public KhachHangGUI() {
        this.khachHangDAO = new KhachHangDAO();

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // === NORTH: Header (Thao tác & Form) ===
        add(createHeaderPanel(), BorderLayout.NORTH);

        // === CENTER: Bảng ===
        add(createTablePanel(), BorderLayout.CENTER);

        // --- Gán sự kiện ---
        addEventListeners();

        // --- Tải dữ liệu từ CSDL lên bảng ---
        loadDataToTable(khachHangDAO.getAllKhachHang());

        // --- Thiết lập form và trạng thái mặc định ---
        lamMoiForm();
    }

    // =========================================================================
    // I. LOGIC TẢI DỮ LIỆU & RENDER
    // =========================================================================

    /**
     * Tải dữ liệu từ danh sách (được lấy từ DAO) lên JTable
     */
    public void refreshKhachHangTable() {
        System.out.println("KhachHangGUI: Yêu cầu làm mới bảng khách hàng..."); // Debug
        try {
            // 1. Gọi DAO để lấy danh sách khách hàng mới nhất
            List<KhachHang> dsKhachHangMoi = khachHangDAO.getAllKhachHang();

            // 2. Gọi hàm loadDataToTable để cập nhật JTable
            loadDataToTable(dsKhachHangMoi);

            // 3. (Tùy chọn) Có thể gọi làm mới form nếu muốn
            // lamMoiForm(); // Bỏ comment nếu muốn form cũng reset

        } catch (Exception e) {
            System.err.println("Lỗi khi làm mới bảng khách hàng: " + e.getMessage());
            e.printStackTrace();
            // Có thể hiển thị thông báo lỗi cho người dùng
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi làm mới danh sách khách hàng.",
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadDataToTable(List<KhachHang> listKH) {
        modelKhachHang.setRowCount(0);
        dsKhachHang = listKH;

        int stt = 1;
        for (KhachHang kh : dsKhachHang) {
            String tongChiTieuStr = currencyFormat.format(kh.getTongChiTieu());

            modelKhachHang.addRow(new Object[]{
                    stt++,
                    kh.getMaKH(),
                    kh.getTenKH(),
                    kh.getGioitinh(),
                    kh.getSdt(),
                    kh.getEmail(),
                    tongChiTieuStr,
                    kh.getHangThanhVien().toString()
            });
        }
    }

    // ... (Lớp HangThanhVienRenderer giữ nguyên) ...
    private class HangThanhVienRenderer extends DefaultTableCellRenderer {

        private Color getBackgroundColor(HangThanhVien hang) {
            switch (hang) {
                case DIAMOND: return new Color(255, 240, 255);
                case GOLD: return new Color(255, 245, 204);
                case SILVER: return new Color(240, 240, 245);
                case BRONZE: return new Color(250, 240, 230);
                case MEMBER: return new Color(230, 240, 255);
                case NONE: default: return new Color(255, 255, 255);
            }
        }

        private Color getForegroundColor(HangThanhVien hang) {
            switch (hang) {
                case DIAMOND: return new Color(180, 0, 180);
                case GOLD: return new Color(200, 160, 0);
                case SILVER: return new Color(100, 100, 150);
                case BRONZE: return new Color(150, 100, 50);
                case MEMBER: return new Color(40, 100, 180);
                case NONE: default: return Color.GRAY.darker();
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            JPanel panel = new JPanel(new GridBagLayout());
            HangThanhVien hang = HangThanhVien.valueOf(value.toString());

            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 15, 5, 15));
            label.setBackground(getBackgroundColor(hang));
            label.setForeground(getForegroundColor(hang));

            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }

            panel.add(label);
            return panel;
        }
    }


    // =========================================================================
    // II. LOGIC THAO TÁC (CRUD & EVENT)
    // =========================================================================

    /**
     * Gắn sự kiện cho các nút
     */
    private void addEventListeners() {
        btnLamMoiForm.addActionListener(e -> lamMoiForm());

        btnThem.addActionListener(e -> themKhachHang());

        btnSua.addActionListener(e -> suaKhachHang());

        btnXoa.addActionListener(e -> xoaKhachHang());

        btnTimKiem.addActionListener(e -> timKhachHang());

        tblKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhachHang.getSelectedRow();
                if (row == -1) return;

                khachHangDangChon = dsKhachHang.get(row);
                hienThiChiTiet(khachHangDangChon);
                tblKhachHang.clearSelection();
            }
        });
    }

    /**
     * Lấy dữ liệu từ Form và kiểm tra tính hợp lệ
     */
    private KhachHang getKhachHangTuForm(boolean isNew) throws Exception {
        String ma = txtMaKH.getText().trim();
        String ten = txtTenKH.getText().trim();
        String gioiTinh = (String) cbGioiTinh.getSelectedItem();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String ngaySinhStr = txtNgaySinh.getText().trim();
        String ngayTGStr = txtNgayThamGia.getText().trim();

        if (ten.isEmpty()) throw new Exception("Tên khách hàng không được rỗng!");
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) throw new Exception("Số điện thoại không hợp lệ (10 chữ số)!");
        if (ngaySinhStr.isEmpty()) throw new Exception("Ngày sinh không được rỗng!");

        LocalDate ngaySinh = LocalDate.parse(ngaySinhStr, dtf);
        LocalDate ngayThamGia = LocalDate.parse(ngayTGStr, dtf);

        float tongChiTieu = 0.0f;
        HangThanhVien hangTV = HangThanhVien.NONE;

        if (!isNew && khachHangDangChon != null) {
            tongChiTieu = khachHangDangChon.getTongChiTieu();
            hangTV = khachHangDangChon.getHangThanhVien();
        }

        KhachHang kh = new KhachHang(ma, ten, gioiTinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
        kh.capNhatHangThanhVien();

        return kh;
    }

    /**
     * Xử lý sự kiện Thêm Khách hàng (Sử dụng DAO CSDL)
     */
    private void themKhachHang() {
        try {
            KhachHang khMoi = getKhachHangTuForm(true);

            boolean success = khachHangDAO.themKhachHang(khMoi);

            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable(khachHangDAO.getAllKhachHang());
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thất bại (Mã KH có thể bị trùng hoặc lỗi CSDL)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sự kiện Sửa Khách hàng (Sử dụng DAO CSDL)
     */
    private void suaKhachHang() {
        if (khachHangDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            KhachHang khCapNhat = getKhachHangTuForm(false);

            khCapNhat.setTongChiTieu(khachHangDangChon.getTongChiTieu());
            khCapNhat.setHangThanhVien(khachHangDangChon.getHangThanhVien());
            khCapNhat.capNhatHangThanhVien();

            boolean success = khachHangDAO.updateKhachHang(khCapNhat);

            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable(khachHangDAO.getAllKhachHang());
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sự kiện Xóa Khách hàng (Sử dụng DAO CSDL)
     */
    private void xoaKhachHang() {
        if (khachHangDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng " + khachHangDangChon.getTenKH() + " (Mã: " + khachHangDangChon.getMaKH() + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = khachHangDAO.xoaKhachHang(khachHangDangChon.getMaKH());

                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadDataToTable(khachHangDAO.getAllKhachHang());
                    lamMoiForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thất bại (Có thể do ràng buộc khóa ngoại hoặc không tìm thấy mã)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xử lý sự kiện Tìm kiếm (Sử dụng DAO CSDL)
     */
    private void timKhachHang() {
        String tuKhoa = JOptionPane.showInputDialog(this, "Nhập Tên hoặc Số điện thoại để tìm kiếm:", "Tìm kiếm khách hàng", JOptionPane.PLAIN_MESSAGE);

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            List<KhachHang> ketQua = khachHangDAO.timKhachHang(tuKhoa.trim());

            if (ketQua.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng nào phù hợp.", "Kết quả tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
                loadDataToTable(khachHangDAO.getAllKhachHang());
            } else {
                loadDataToTable(ketQua);
                lamMoiForm();
            }
        } else {
            loadDataToTable(khachHangDAO.getAllKhachHang());
            lamMoiForm();
        }
    }


    // =========================================================================
    // III. KHỞI TẠO GIAO DIỆN & HELPER
    // =========================================================================

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JPanel formDetailPanel = createFormDetailPanel();
        headerPanel.add(formDetailPanel, BorderLayout.CENTER);

        JPanel buttonGroupPanel = createButtonGroupPanel();
        headerPanel.add(buttonGroupPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFormDetailPanel() {
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_TABLE_GRID),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Các ô nhập liệu sẽ lấp đầy chiều ngang
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        // --- Thiết lập trọng số MỚI ---
        // Cột Labels (0 và 2) sẽ có trọng số nhỏ để chia đều không gian thừa Label
        final double WEIGHT_LABEL = 0.01; // Trọng số nhỏ, chỉ để cân bằng chiều rộng 2 cột Label
        // Cột Input Fields (1 và 3) sẽ có trọng số lớn để chia đều không gian Input
        final double WEIGHT_INPUT = 1.0;

        // Hàng 0: Mã khách hàng / Ngày sinh
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Mã khách hàng:"), gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtMaKH = new JTextField(20); txtMaKH.setEditable(false); formContainer.add(txtMaKH, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Ngày sinh (dd/MM/yyyy):"), gbc);

        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtNgaySinh = new JTextField(); formContainer.add(txtNgaySinh, gbc);

        // Hàng 1: Tên khách hàng / Địa chỉ
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Tên khách hàng:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtTenKH = new JTextField(); formContainer.add(txtTenKH, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtDiaChi = new JTextField(); formContainer.add(txtDiaChi, gbc);

        // Hàng 2: Giới tính / Ngày tham gia
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cbGioiTinh.setPreferredSize(new Dimension(0, 30));
        formContainer.add(cbGioiTinh, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Ngày tham gia (dd/MM/yyyy):"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtNgayThamGia = new JTextField(); txtNgayThamGia.setEditable(false); formContainer.add(txtNgayThamGia, gbc);

        // Hàng 3: Số điện thoại / Tổng chi tiêu
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtSDT = new JTextField(); formContainer.add(txtSDT, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Tổng chi tiêu:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtTongChiTieu = new JTextField(); txtTongChiTieu.setEditable(false); formContainer.add(txtTongChiTieu, gbc);

        // Hàng 4: Email / Hạng thành viên
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtEmail = new JTextField(); formContainer.add(txtEmail, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL;
        formContainer.add(new JLabel("Hạng thành viên:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        cbHangTV = new JComboBox<>(getHangThanhVienOptions());
        cbHangTV.setEnabled(false);
        cbHangTV.setPreferredSize(new Dimension(0, 30));
        formContainer.add(cbHangTV, gbc);

        return formContainer;
    }

    private String[] getHangThanhVienOptions() {
        HangThanhVien[] values = HangThanhVien.values();
        String[] options = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            options[i] = values[i].toString();
        }
        return options;
    }

    private JPanel createButtonGroupPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        btnLamMoiForm = createStyledButton("🔄 Làm mới", COLOR_ACCENT_BLUE.brighter());
        btnThem = createStyledButton(" Thêm", new Color(0, 150, 50));
        btnXoa = createStyledButton(" Xóa", new Color(210, 0, 0));
        btnSua = createStyledButton(" Sửa", COLOR_BUTTON_BLUE);
        btnTimKiem = createStyledButton(" Tìm kiếm", Color.LIGHT_GRAY.darker());

        Dimension buttonSize = new Dimension(150, 40);
        btnLamMoiForm.setMaximumSize(buttonSize);
        btnThem.setMaximumSize(buttonSize);
        btnXoa.setMaximumSize(buttonSize);
        btnSua.setMaximumSize(buttonSize);
        btnTimKiem.setMaximumSize(buttonSize);

        btnLamMoiForm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnThem.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnXoa.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSua.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTimKiem.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(btnLamMoiForm);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnThem);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnSua);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnXoa);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnTimKiem);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(COLOR_TEXT_WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY.brighter()),
                new EmptyBorder(8, 15, 8, 15)
        ));
        return button;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"STT", "Mã khách hàng", "Tên khách hàng", "Giới tính", "Số điện thoại", "Email", "Tổng chi tiêu", "Hạng thành viên"};

        modelKhachHang = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhachHang = new JTable(modelKhachHang);

        tblKhachHang.setRowHeight(35);
        tblKhachHang.setFont(new Font("Arial", Font.PLAIN, 14));
        tblKhachHang.setGridColor(COLOR_TABLE_GRID);
        tblKhachHang.setShowGrid(true);
        tblKhachHang.setIntercellSpacing(new Dimension(0, 0));

        tblKhachHang.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblKhachHang.getTableHeader().setOpaque(false);
        tblKhachHang.getTableHeader().setBackground(new Color(235, 240, 247));
        tblKhachHang.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhachHang.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblKhachHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKhachHang.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tblKhachHang.getColumnModel().getColumn(7).setCellRenderer(new HangThanhVienRenderer());

        tblKhachHang.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblKhachHang.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblKhachHang.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(tblKhachHang);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));

        return scrollPane;
    }

    private void hienThiChiTiet(KhachHang kh) {
        if (kh == null) return;

        txtMaKH.setText(kh.getMaKH());
        txtTenKH.setText(kh.getTenKH());
        cbGioiTinh.setSelectedItem(kh.getGioitinh());
        txtSDT.setText(kh.getSdt());
        txtEmail.setText(kh.getEmail());
        txtDiaChi.setText(kh.getDiaChi());

        txtNgaySinh.setText(kh.getNgaySinh() != null ? kh.getNgaySinh().format(dtf) : "");
        txtNgayThamGia.setText(kh.getNgayThamGia() != null ? kh.getNgayThamGia().format(dtf) : "");

        txtTongChiTieu.setText(currencyFormat.format(kh.getTongChiTieu()));
        cbHangTV.setSelectedItem(kh.getHangThanhVien().toString());
    }

    private void lamMoiForm() {
        khachHangDangChon = null;
        try {
            txtMaKH.setText(new KhachHang().getMaKH());
        } catch (Exception e) {
            txtMaKH.setText("KHXXXXXX000");
        }

        txtTenKH.setText("");
        cbGioiTinh.setSelectedItem("Nam");
        txtSDT.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");

        txtNgaySinh.setText(LocalDate.of(2000, 1, 1).format(dtf));
        txtNgayThamGia.setText(LocalDate.now().format(dtf));

        txtTongChiTieu.setText(currencyFormat.format(0.0f));
        cbHangTV.setSelectedItem(HangThanhVien.NONE.toString());

        tblKhachHang.clearSelection();
    }
}