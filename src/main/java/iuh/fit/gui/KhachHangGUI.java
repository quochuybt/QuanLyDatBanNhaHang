package iuh.fit.gui;

import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.entity.KhachHang;
import iuh.fit.core.service.KhachHangService;
import com.toedter.calendar.JDateChooser; // IMPORT THƯ VIỆN JCALENDAR

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class KhachHangGUI extends JPanel {

    private static KhachHangGUI instance;

    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);

    private JTextField txtMaKH, txtTenKH, txtSDT, txtEmail, txtDiaChi, txtTongChiTieu;
    private JComboBox<String> cbGioiTinh, cbHangTV;
    private JTextField txtNgayThamGia;

    // ĐỔI SANG DÙNG JDATECHOOSER
    private JDateChooser txtNgaySinh;

    private JButton btnThem, btnSua, btnTimKiem, btnLamMoiForm;

    private JTable tblKhachHang;
    private DefaultTableModel modelKhachHang;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0' VND'");

    private final KhachHangService khachHangService;
    private List<KhachHang> dsKhachHang;
    private KhachHang khachHangDangChon = null;

    public KhachHangGUI() {
        this.khachHangService = new KhachHangService();
        instance = this;

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        addEventListeners();
        loadDataToTable(khachHangService.findAll());
        lamMoiForm();
    }

    public static void reloadKhachHangTableIfAvailable() {
        if (instance != null) {
            SwingUtilities.invokeLater(instance::refreshKhachHangTable);
        }
    }

    public void refreshKhachHangTable() {
        try {
            loadDataToTable(khachHangService.findAll());
            revalidate();
            repaint();
        } catch (Exception e) {
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
            modelKhachHang.addRow(new Object[]{
                    stt++,
                    kh.getMaKH(),
                    kh.getTenKH(),
                    kh.getGioitinh(),
                    kh.getSdt(),
                    kh.getEmail(),
                    currencyFormat.format(kh.getTongChiTieu()),
                    kh.getHangThanhVien().toString()
            });
        }
    }

    private class HangThanhVienRenderer extends DefaultTableCellRenderer {
        private Color getBackgroundColor(HangThanhVien hang) {
            return switch (hang) {
                case DIAMOND -> new Color(255, 240, 255);
                case GOLD -> new Color(255, 245, 204);
                case SILVER -> new Color(240, 240, 245);
                case BRONZE -> new Color(250, 240, 230);
                case MEMBER -> new Color(230, 240, 255);
                case NONE -> new Color(255, 255, 255);
            };
        }

        private Color getForegroundColor(HangThanhVien hang) {
            return switch (hang) {
                case DIAMOND -> new Color(180, 0, 180);
                case GOLD -> new Color(200, 160, 0);
                case SILVER -> new Color(100, 100, 150);
                case BRONZE -> new Color(150, 100, 50);
                case MEMBER -> new Color(40, 100, 180);
                case NONE -> Color.GRAY.darker();
            };
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            HangThanhVien hang = HangThanhVien.valueOf(value.toString());
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 15, 5, 15));
            label.setBackground(getBackgroundColor(hang));
            label.setForeground(getForegroundColor(hang));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(label);
            return panel;
        }
    }

    private void addEventListeners() {
        btnLamMoiForm.addActionListener(e -> {
            refreshKhachHangTable();
            lamMoiForm();
        });
        btnThem.addActionListener(e -> themKhachHang());
        btnSua.addActionListener(e -> suaKhachHang());
        btnTimKiem.addActionListener(e -> timKhachHang());

        tblKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhachHang.getSelectedRow();
                if (row == -1 && tblKhachHang.getRowCount() > 0) {
                    row = tblKhachHang.rowAtPoint(e.getPoint());
                }
                if (row == -1) return;

                String maKHTuBang = (String) modelKhachHang.getValueAt(row, 1);
                khachHangDangChon = dsKhachHang.stream().filter(kh -> kh.getMaKH().equals(maKHTuBang)).findFirst().orElse(null);
                hienThiChiTiet(khachHangDangChon);
            }
        });
    }

    private KhachHang getKhachHangTuForm(boolean isNew) throws Exception {
        String ma = txtMaKH.getText().trim();
        String ten = txtTenKH.getText().trim();
        String gioiTinh = (String) cbGioiTinh.getSelectedItem();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String ngayTGStr = txtNgayThamGia.getText().trim();

        if (ten.isEmpty()) throw new Exception("Tên khách hàng không được rỗng!");
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) throw new Exception("Số điện thoại không hợp lệ (10 chữ số)!");

        // XỬ LÝ DỮ LIỆU TỪ JDATECHOOSER
        Date selectedDate = txtNgaySinh.getDate();
        if (selectedDate == null) {
            throw new Exception("Ngày sinh không được rỗng hoặc sai định dạng!");
        }
        // Convert từ java.util.Date sang java.time.LocalDate
        LocalDate ngaySinh = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate ngayThamGia = LocalDate.parse(ngayTGStr, dtf);

        if (isNew) {
            KhachHang kh = new KhachHang(ten, gioiTinh, sdt, ngaySinh, diaChi, email);
            kh.setNgayThamGia(ngayThamGia);
            kh.capNhatHangThanhVien();
            return kh;
        }

        float tongChiTieu = khachHangDangChon != null ? khachHangDangChon.getTongChiTieu() : 0f;
        HangThanhVien hangTV = khachHangDangChon != null ? khachHangDangChon.getHangThanhVien() : HangThanhVien.MEMBER;
        KhachHang kh = new KhachHang(ma, ten, gioiTinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
        kh.capNhatHangThanhVien();
        return kh;
    }

    private void themKhachHang() {
        try {
            KhachHang khMoi = getKhachHangTuForm(true);
            khachHangService.addKhachHang(khMoi);
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshKhachHangTable();
            lamMoiForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void suaKhachHang() {
        if (khachHangDangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            KhachHang khCapNhat = getKhachHangTuForm(false);
            khachHangService.update(khCapNhat);
            JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            refreshKhachHangTable();
            lamMoiForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void timKhachHang() {
        String tuKhoa = JOptionPane.showInputDialog(this, "Nhập Tên hoặc Số điện thoại để tìm kiếm:", "Tìm kiếm khách hàng", JOptionPane.PLAIN_MESSAGE);
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            List<KhachHang> ketQua = khachHangService.search(tuKhoa.trim());
            if (ketQua.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng nào phù hợp.", "Kết quả tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
            } else {
                loadDataToTable(ketQua);
                lamMoiForm();
            }
        } else {
            loadDataToTable(khachHangService.findAll());
            lamMoiForm();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.add(createFormDetailPanel(), BorderLayout.CENTER);
        headerPanel.add(createButtonGroupPanel(), BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFormDetailPanel() {
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID), new EmptyBorder(10, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        final double WEIGHT_LABEL = 0.01;
        final double WEIGHT_INPUT = 1.0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Mã khách hàng:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtMaKH = new JTextField(20); txtMaKH.setEditable(false); formContainer.add(txtMaKH, gbc);
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Ngày sinh (dd/MM/yyyy):"), gbc);

        // KHỞI TẠO VÀ FORMAT JDATECHOOSER
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT;
        txtNgaySinh = new JDateChooser();
        txtNgaySinh.setDateFormatString("dd/MM/yyyy");
        formContainer.add(txtNgaySinh, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Tên khách hàng:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtTenKH = new JTextField(); formContainer.add(txtTenKH, gbc);
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtDiaChi = new JTextField(); formContainer.add(txtDiaChi, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"}); cbGioiTinh.setPreferredSize(new Dimension(0, 30)); formContainer.add(cbGioiTinh, gbc);
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Ngày tham gia (dd/MM/yyyy):"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtNgayThamGia = new JTextField(); txtNgayThamGia.setEditable(false); formContainer.add(txtNgayThamGia, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtSDT = new JTextField(); formContainer.add(txtSDT, gbc);
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Tổng chi tiêu:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtTongChiTieu = new JTextField(); txtTongChiTieu.setEditable(false); formContainer.add(txtTongChiTieu, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; txtEmail = new JTextField(); formContainer.add(txtEmail, gbc);
        gbc.gridx = 2; gbc.gridy = row; gbc.weightx = WEIGHT_LABEL; formContainer.add(new JLabel("Hạng thành viên:"), gbc);
        gbc.gridx = 3; gbc.gridy = row; gbc.weightx = WEIGHT_INPUT; cbHangTV = new JComboBox<>(getHangThanhVienOptions()); cbHangTV.setEnabled(false); cbHangTV.setPreferredSize(new Dimension(0, 30)); formContainer.add(cbHangTV, gbc);

        return formContainer;
    }

    private String[] getHangThanhVienOptions() {
        HangThanhVien[] values = HangThanhVien.values();
        String[] options = new String[values.length];
        for (int i = 0; i < values.length; i++) options[i] = values[i].toString();
        return options;
    }

    private JPanel createButtonGroupPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        btnLamMoiForm = createStyledButton("🔄 Làm mới", COLOR_ACCENT_BLUE.brighter());
        btnThem = createStyledButton(" Thêm", new Color(0, 150, 50));
        btnSua = createStyledButton(" Sửa", COLOR_BUTTON_BLUE);
        btnTimKiem = createStyledButton(" Tìm kiếm", Color.LIGHT_GRAY.darker());

        Dimension buttonSize = new Dimension(150, 40);
        btnLamMoiForm.setMaximumSize(buttonSize);
        btnThem.setMaximumSize(buttonSize);
        btnSua.setMaximumSize(buttonSize);
        btnTimKiem.setMaximumSize(buttonSize);

        btnLamMoiForm.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnThem.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSua.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTimKiem.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(btnLamMoiForm);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnThem);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnSua);
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
        return button;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"STT", "Mã khách hàng", "Tên khách hàng", "Giới tính", "Số điện thoại", "Email", "Tổng chi tiêu", "Hạng thành viên"};
        modelKhachHang = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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

        // CONVERT LocalDate SANG Date ĐỂ HIỂN THỊ LÊN JDATECHOOSER
        if (kh.getNgaySinh() != null) {
            Date date = Date.from(kh.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant());
            txtNgaySinh.setDate(date);
        } else {
            txtNgaySinh.setDate(null);
        }

        txtNgayThamGia.setText(kh.getNgayThamGia() != null ? kh.getNgayThamGia().format(dtf) : "");
        txtTongChiTieu.setText(currencyFormat.format(kh.getTongChiTieu()));
        cbHangTV.setSelectedItem(kh.getHangThanhVien().toString());
    }

    private void lamMoiForm() {
        khachHangDangChon = null;
        txtMaKH.setText("(Tự động)");
        txtTenKH.setText("");
        cbGioiTinh.setSelectedItem("Nam");
        txtSDT.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");

        // SET NULL CHO JDATECHOOSER ĐỂ LÀM TRỐNG NGÀY
        txtNgaySinh.setDate(null);

        txtNgayThamGia.setText(LocalDate.now().format(dtf));
        txtTongChiTieu.setText(currencyFormat.format(0.0f));
        cbHangTV.setSelectedItem(HangThanhVien.MEMBER.toString());
        tblKhachHang.clearSelection();
    }
}