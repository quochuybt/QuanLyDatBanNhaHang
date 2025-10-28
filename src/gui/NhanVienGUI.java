package gui;

import dao.NhanVienDAO;
import dao.PhanCongDAO; // [1] Import PhanCongDAO
import entity.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat; // [2] Import DecimalFormat để định dạng số giờ
import java.util.List;
import java.util.Map; // [3] Import Map

public class NhanVienGUI extends JPanel {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final PhanCongDAO phanCongDAO = new PhanCongDAO(); // [4] Khởi tạo PhanCongDAO
    private DefaultTableModel model;
    private JTable table;
    private final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private final DecimalFormat hourFormat = new DecimalFormat("#,##0.0"); // [5] Định dạng giờ làm (ví dụ: 195.5)

    public NhanVienGUI() {

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(244, 247, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        JPanel mainContent = createMainContentPanel();
        add(mainContent, BorderLayout.CENTER);
        loadDataToTable();
    }

    private JPanel createHeaderPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lblTitle = new JLabel("Nhân viên");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(lblTitle, BorderLayout.WEST);
        JButton btnAdd = new JButton(" + Thêm nhân viên");
        btnAdd.setBackground(COLOR_ACCENT_BLUE);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnAdd.addActionListener(e -> {
            ThemNhanVienDialog dialog = new ThemNhanVienDialog(this);
            dialog.setVisible(true);
        });
        panel.add(btnAdd, BorderLayout.EAST);
        return panel;
    }

    private JPanel createMainContentPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(new JComboBox<>(new String[]{"Tên NV", "Mã NV", "SĐT"}));
        searchPanel.add(new JTextField(20));
        searchPanel.add(new JButton("Tìm"));
        panel.add(searchPanel, BorderLayout.NORTH);
        model = new DefaultTableModel(new String[]{"Họ tên nhân viên", "Chức vụ", "Tổng số giờ làm", "Nhắc nhở", "Xem chi tiết"}, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getColumn("Xem chi tiết").setCellRenderer(new ButtonRenderer());
        table.getColumn("Xem chi tiết").setCellEditor(new ButtonEditor(new JCheckBox(), table));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * [SỬA] Cập nhật hàm loadDataToTable
     */
    public void loadDataToTable() {
        model.setRowCount(0); // Xóa dữ liệu cũ

        // [6] Gọi DAO để lấy tổng giờ làm cho TẤT CẢ nhân viên (chỉ gọi 1 lần)
        Map<String, Double> tongGioLamMap = phanCongDAO.getTongGioLamChoTatCaNV();

        // Lấy danh sách nhân viên
        List<NhanVien> ds = nhanVienDAO.getAllNhanVien();

        for (NhanVien nv : ds) {
            // [7] Lấy tổng giờ làm từ Map (mặc định là 0 nếu không có)
            Double tongGio = tongGioLamMap.getOrDefault(nv.getManv(), 0.0);

            Object[] row = new Object[]{
                    nv.getHoten(),
                    nv.getVaiTro().name(), // Hiển thị tên Enum (QUANLY/NHANVIEN)
                    hourFormat.format(tongGio), // [8] Hiển thị tổng giờ đã định dạng
                    "0",   // Giả lập Nhắc nhở
                    nv.getManv() // Lưu mã NV vào cột Xem chi tiết
            };
            model.addRow(row);
        }
    }

    public void refreshTable() {
        loadDataToTable();
    }

    // --- (Các lớp ButtonRenderer và ButtonEditor ) ---
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        // ... (code giữ nguyên) ...
        public ButtonRenderer() {
            setOpaque(true);
            setText("Xem chi tiết >>");
            setForeground(new Color(0, 0, 255));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.PLAIN, 14));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Xem chi tiết >>");
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        // ... (code giữ nguyên) ...
        private final JButton button;
        private final JTable table;
        private boolean isPushed;
        private int editingRow;
        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton("Xem chi tiết >>");
            button.addActionListener((ActionEvent e) -> {
                fireEditingStopped();
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            isPushed = true;
            this.editingRow = row;
            return button;
        }
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int maNVColumnIndex = table.getColumn("Xem chi tiết").getModelIndex();
                String maNV = (String) table.getValueAt(editingRow, maNVColumnIndex);
                ChiTietNhanVienDialog dialog = new ChiTietNhanVienDialog(NhanVienGUI.this, maNV);
                dialog.setVisible(true);
            }
            isPushed = false;
            return table.getValueAt(editingRow, table.getColumn("Xem chi tiết").getModelIndex());
        }
    }
}