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
import java.util.List;

public class KhuyenMaiGUI extends JPanel {

    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_BUTTON_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_BUTTON_RED = new Color(220, 53, 69);
    private static final Color COLOR_TEXT_WHITE = Color.WHITE;
    private static final Color COLOR_TABLE_HEADER_BG = new Color(235, 240, 247);
    private static final Color COLOR_TABLE_GRID = new Color(220, 220, 220);
    private static final Font FONT_TEXT = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 14);

    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JButton btnThemKhuyenMai;
    private JButton btnXoaKhuyenMai;
    private JComboBox<String> cbxLoc;
    private JTextField txtTimKiem;

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

        loadDataToTable();
        addEventListeners();
        addSearchAndFilterListeners();
    }

    private void loadDataToTable() {
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai();
        updateTable(ds);
    }

    private void updateTable(List<KhuyenMai> ds) {
        modelKhuyenMai.setRowCount(0);
        this.dsKhuyenMai = ds;

        if (ds == null) return;

        for (KhuyenMai km : dsKhuyenMai) {
            String moTa = String.format("<html><b>%s</b><br>%s<br><i style='color:gray'>ĐK: >%.0f VNĐ</i></html>",
                    km.getTenChuongTrinh(),
                    generateMoTaGiaTri(km),
                    km.getDieuKienApDung());

            String ngayKT = (km.getNgayKetThuc() != null) ? km.getNgayKetThuc().format(dtf) : "Vô thời hạn";

            String soLuongHienThi = km.getSoLuotDaDung() + " / " +
                    (km.getSoLuongGioiHan() > 0 ? km.getSoLuongGioiHan() : "∞");

            modelKhuyenMai.addRow(new Object[]{
                    moTa,
                    km.getLoaiKhuyenMai(),
                    km.getNgayBatDau().format(dtf),
                    ngayKT,
                    soLuongHienThi,
                    km.getTrangThai()
            });
        }
    }

    private String generateMoTaGiaTri(KhuyenMai km) {
        switch (km.getLoaiKhuyenMai()) {
            case "Giảm theo phần trăm":
                return String.format("Giảm %.0f%% hóa đơn", km.getGiaTri());
            case "Giảm giá số tiền":
                return String.format("Giảm %.0f VNĐ", km.getGiaTri());
            case "Tặng món":
                return "Tặng: " + km.getMoTa();
            default:
                return km.getMoTa();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý Khuyến mãi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnXoaKhuyenMai = new JButton("Ngưng áp dụng");
        setupButton(btnXoaKhuyenMai, COLOR_BUTTON_RED);
        buttonPanel.add(btnXoaKhuyenMai);

        btnThemKhuyenMai = new JButton("+ Thêm khuyến mãi");
        setupButton(btnThemKhuyenMai, COLOR_BUTTON_BLUE);
        buttonPanel.add(btnThemKhuyenMai);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setOpaque(false);
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        String[] locOptions = {"Lọc khuyến mãi", "Đang áp dụng", "Ngưng áp dụng"};
        cbxLoc = new JComboBox<>(locOptions);
        cbxLoc.setFont(FONT_TEXT);
        cbxLoc.setPreferredSize(new Dimension(160, 38));
        searchPanel.add(cbxLoc);

        JPanel searchBox = new JPanel(new BorderLayout(5, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));
        searchBox.add(new JLabel(" 🔍 "), BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(FONT_TEXT);
        txtTimKiem.setBorder(null);
        txtTimKiem.setPreferredSize(new Dimension(300, 36));
        addPlaceholder(txtTimKiem, "Tìm kiếm khuyến mãi");
        searchBox.add(txtTimKiem, BorderLayout.CENTER);

        searchPanel.add(searchBox);
        return searchPanel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = {"Chương trình", "Loại", "Ngày Bắt đầu", "Ngày Kết thúc", "Đã dùng / Tổng", "Trạng thái"};

        modelKhuyenMai = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblKhuyenMai = new JTable(modelKhuyenMai);

        tblKhuyenMai.setRowHeight(70);
        tblKhuyenMai.setFont(FONT_TEXT);
        tblKhuyenMai.setGridColor(COLOR_TABLE_GRID);
        tblKhuyenMai.setShowGrid(true);
        tblKhuyenMai.getTableHeader().setFont(FONT_BOLD);
        tblKhuyenMai.getTableHeader().setBackground(COLOR_TABLE_HEADER_BG);
        tblKhuyenMai.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tblKhuyenMai.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());

        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(300);
        tblKhuyenMai.getColumnModel().getColumn(4).setPreferredWidth(100);
        addTableClickListener();

        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));
        return scrollPane;
    }

    private JPanel createFooterPanel() {
        return new JPanel();
    }

    private void addEventListeners() {
        btnThemKhuyenMai.addActionListener(e -> showKhuyenMaiDialog(null)); // Thêm mới
        btnXoaKhuyenMai.addActionListener(e -> xoaKhuyenMai()); // Ngưng áp dụng
    }

    private void addSearchAndFilterListeners() {
        txtTimKiem.addActionListener(e -> thucHienTimKiemVaLoc());
        cbxLoc.addActionListener(e -> thucHienTimKiemVaLoc());
    }

    private void thucHienTimKiemVaLoc() {
        String tuKhoa = txtTimKiem.getText().trim();
        String trangThai = (String) cbxLoc.getSelectedItem();
        List<KhuyenMai> ketQua = khuyenMaiDAO.timKiemVaLoc(tuKhoa, trangThai);
        updateTable(ketQua);
    }

    private void xoaKhuyenMai() {
        int selectedRow = tblKhuyenMai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần ngưng áp dụng.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn ngưng áp dụng khuyến mãi này không?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int modelRow = tblKhuyenMai.convertRowIndexToModel(selectedRow);
                KhuyenMai km = dsKhuyenMai.get(modelRow);

                if ("Ngưng áp dụng".equals(km.getTrangThai())) {
                    JOptionPane.showMessageDialog(this, "Khuyến mãi này đã ngưng áp dụng rồi.");
                    return;
                }

                km.setTrangThai("Ngưng áp dụng");
                if (khuyenMaiDAO.updateKhuyenMai(km)) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái thành công.");
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addTableClickListener() {
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblKhuyenMai.getSelectedRow();
                    if (row != -1) {
                        int modelRow = tblKhuyenMai.convertRowIndexToModel(row);
                        showKhuyenMaiDialog(dsKhuyenMai.get(modelRow));
                    }
                }
            }
        });
    }

    private void showKhuyenMaiDialog(KhuyenMai km) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                (km == null ? "Thêm Khuyến Mãi Mới" : "Cập Nhật Khuyến Mãi"), true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtMaKM = new JTextField(km != null ? km.getMaKM() : "");
        if (km != null) txtMaKM.setEditable(false);

        JTextField txtTenCT = new JTextField(km != null ? km.getTenChuongTrinh() : "");
        JTextField txtMoTa = new JTextField(km != null ? km.getMoTa() : "");

        JComboBox<String> cbLoaiKM = new JComboBox<>(new String[]{"Giảm theo phần trăm", "Giảm giá số tiền", "Tặng món"});
        if (km != null) cbLoaiKM.setSelectedItem(km.getLoaiKhuyenMai());

        JTextField txtGiaTri = new JTextField(km != null ? String.valueOf(km.getGiaTri()) : "0");
        JTextField txtDieuKien = new JTextField(km != null ? String.valueOf(km.getDieuKienApDung()) : "0");

        String slText = (km != null && km.getSoLuongGioiHan() > 0) ? String.valueOf(km.getSoLuongGioiHan()) : "";
        JTextField txtSoLuong = new JTextField(slText);
        txtSoLuong.setToolTipText("Để trống hoặc nhập 0 nếu không giới hạn số lượng");

        JDateChooser dcNgayBD = new JDateChooser();
        dcNgayBD.setDateFormatString("dd/MM/yyyy");
        if (km != null) dcNgayBD.setDate(Date.valueOf(km.getNgayBatDau()));

        JDateChooser dcNgayKT = new JDateChooser();
        dcNgayKT.setDateFormatString("dd/MM/yyyy");
        if (km != null && km.getNgayKetThuc() != null) dcNgayKT.setDate(Date.valueOf(km.getNgayKetThuc()));

        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Ngưng áp dụng"});
        if (km != null) cbTrangThai.setSelectedItem(km.getTrangThai());
        formPanel.add(new JLabel("Mã khuyến mãi (*):")); formPanel.add(txtMaKM);
        formPanel.add(new JLabel("Tên chương trình (*):")); formPanel.add(txtTenCT);
        formPanel.add(new JLabel("Mô tả chi tiết:")); formPanel.add(txtMoTa);
        formPanel.add(new JLabel("Loại khuyến mãi:")); formPanel.add(cbLoaiKM);
        formPanel.add(new JLabel("Giá trị giảm:")); formPanel.add(txtGiaTri);
        formPanel.add(new JLabel("Đơn tối thiểu (VNĐ):")); formPanel.add(txtDieuKien);

        formPanel.add(new JLabel("Số lượng giới hạn (Trống = Vô hạn):")); formPanel.add(txtSoLuong);

        formPanel.add(new JLabel("Ngày bắt đầu (*):")); formPanel.add(dcNgayBD);
        formPanel.add(new JLabel("Ngày kết thúc:")); formPanel.add(dcNgayKT);
        formPanel.add(new JLabel("Trạng thái:")); formPanel.add(cbTrangThai);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton btnLuu = new JButton("Lưu lại");
        setupButton(btnLuu, COLOR_BUTTON_BLUE);

        btnLuu.addActionListener(e -> {
            try {
                String ma = txtMaKM.getText().trim();
                String ten = txtTenCT.getText().trim();
                if (ma.isEmpty() || ten.isEmpty()) throw new Exception("Mã và Tên không được để trống.");
                if (dcNgayBD.getDate() == null) throw new Exception("Ngày bắt đầu không được để trống.");

                double giaTri = Double.parseDouble(txtGiaTri.getText().trim());
                double dieuKien = Double.parseDouble(txtDieuKien.getText().trim());
                if (giaTri < 0 || dieuKien < 0) throw new Exception("Giá trị tiền không được âm.");

                int soLuongGioiHan = 0;
                if (!txtSoLuong.getText().trim().isEmpty()) {
                    try {
                        soLuongGioiHan = Integer.parseInt(txtSoLuong.getText().trim());
                        if (soLuongGioiHan < 0) throw new Exception("Số lượng không được âm.");
                    } catch (NumberFormatException ne) {
                        throw new Exception("Số lượng giới hạn phải là số nguyên.");
                    }
                }

                LocalDate ngayBD = dcNgayBD.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate ngayKT = null;
                if (dcNgayKT.getDate() != null) {
                    ngayKT = dcNgayKT.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (ngayKT.isBefore(ngayBD)) throw new Exception("Ngày kết thúc phải sau ngày bắt đầu.");
                }

                KhuyenMai kmMoi = new KhuyenMai(
                        ma, ten, txtMoTa.getText(),
                        (String)cbLoaiKM.getSelectedItem(),
                        giaTri, dieuKien,
                        ngayBD, ngayKT,
                        (String)cbTrangThai.getSelectedItem()
                );

                kmMoi.setSoLuongGioiHan(soLuongGioiHan);
                if (km != null) {
                    kmMoi.setSoLuotDaDung(km.getSoLuotDaDung());
                }

                boolean success = (km == null) ? khuyenMaiDAO.themKhuyenMai(kmMoi) : khuyenMaiDAO.updateKhuyenMai(kmMoi);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Lưu thành công!");
                    dialog.dispose();
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lưu thất bại. Kiểm tra lại mã hoặc kết nối.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đúng định dạng số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnLuu);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void setupButton(JButton btn, Color bg) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(COLOR_TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addPlaceholder(JTextField tf, String text) {
        tf.setText(text);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(tf.getText().equals(text)) { tf.setText(""); tf.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if(tf.getText().isEmpty()) { tf.setText(text); tf.setForeground(Color.GRAY); } }
        });
    }

    private class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if ("Đang áp dụng".equals(value)) {
                label.setForeground(new Color(0, 150, 0));
            } else {
                label.setForeground(COLOR_BUTTON_RED);
            }
            return label;
        }
    }
}