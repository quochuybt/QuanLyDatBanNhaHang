package gui;

import dao.NhanVienDAO;
import dao.PhanCongDAO;
import entity.NhanVien;
import entity.VaiTro;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NhanVienGUI extends JPanel {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final PhanCongDAO phanCongDAO = new PhanCongDAO();
    private DefaultTableModel model;
    private JTable table;
    private final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private final DecimalFormat hourFormat = new DecimalFormat("#,##0.0");

    private JComboBox<String> cmbSearchType;
    private JTextField txtSearchKeyword;
    private JButton btnSearch;
    private JButton btnCancelSearch;

    public NhanVienGUI() {

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(244, 247, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = createMainContentPanel();
        add(mainContent, BorderLayout.CENTER);

        btnSearch.addActionListener(this::handleSearch);
        btnCancelSearch.addActionListener(this::handleCancelSearch);

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

        cmbSearchType = new JComboBox<>(new String[]{"Tên NV", "SĐT"});
        searchPanel.add(cmbSearchType);

        txtSearchKeyword = new JTextField(20);
        searchPanel.add(txtSearchKeyword);

        btnSearch = new JButton("Tìm");
        searchPanel.add(btnSearch);

        btnCancelSearch = new JButton("Làm mới tìm kiếm");
        btnCancelSearch.setBackground(new Color(220, 220, 220));
        btnCancelSearch.setOpaque(true);
        btnCancelSearch.setBorderPainted(false);
        searchPanel.add(btnCancelSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Họ tên nhân viên", "Chức vụ", "Tổng số giờ làm", "Nhắc nhở", "Xem chi tiết"}, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getColumn("Xem chi tiết").setCellRenderer(new ButtonRenderer());

        // [KÍCH HOẠT]
        table.getColumn("Xem chi tiết").setCellEditor(new ButtonEditor(new JCheckBox(), table));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void loadDataToTable() {
        updateTableWithResults(nhanVienDAO.getAllNhanVien());
    }

    public void refreshTable() {
        loadDataToTable();
    }

    private void handleSearch(ActionEvent e) {
        String keyword = txtSearchKeyword.getText().trim();
        String searchType = (String) cmbSearchType.getSelectedItem();
        List<NhanVien> searchResults = null;

        if (keyword.isEmpty()) {
            loadDataToTable();
            return;
        }

        try {
            switch (searchType) {
                case "Tên NV":
                    searchResults = nhanVienDAO.searchNhanVienByName(keyword);
                    break;
                case "SĐT":
                    searchResults = nhanVienDAO.searchNhanVienBySdt(keyword);
                    break;
            }

            updateTableWithResults(searchResults);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thực hiện tìm kiếm: " + ex.getMessage(), "Lỗi Hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelSearch(ActionEvent e) {
        txtSearchKeyword.setText("");
        cmbSearchType.setSelectedIndex(0);
        loadDataToTable();
    }

    private void updateTableWithResults(List<NhanVien> results) {
        model.setRowCount(0);

        if (results == null || results.isEmpty()) {
            if (!txtSearchKeyword.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên nào phù hợp.", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        Map<String, Double> tongGioLamMap = phanCongDAO.getTongGioLamChoTatCaNV();

        for (NhanVien nv : results) {
            Double tongGio = tongGioLamMap.getOrDefault(nv.getManv(), 0.0);
            Object[] row = new Object[]{
                    nv.getHoten(),
                    nv.getVaiTro().name(),
                    hourFormat.format(tongGio),
                    "0",
                    nv.getManv()
            };
            model.addRow(row);
        }
    }


    // --- (Các lớp ButtonRenderer và ButtonEditor ) ---
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
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
        private final JButton button;
        private final JTable table;
        private boolean isPushed;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton("Xem chi tiết >>");

            // [ĐÃ SỬA]: Logic mở Dialog được chuyển vào đây
            button.addActionListener((ActionEvent e) -> {
                // 1. Phải dừng chỉnh sửa trước khi mở dialog
                fireEditingStopped();

                // 2. Lấy Mã NV an toàn
                int currentRow = table.convertRowIndexToModel(editingRow);
                int maNVColumnIndex = table.getColumn("Xem chi tiết").getModelIndex();
                String maNV = (String) table.getModel().getValueAt(currentRow, maNVColumnIndex);

                // 3. Mở Dialog
                ChiTietNhanVienDialog dialog = new ChiTietNhanVienDialog(NhanVienGUI.this, maNV);
                dialog.setVisible(true);
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
            // [ĐÃ SỬA]: Chỉ trả về giá trị để JTable kết thúc chỉnh sửa
            if (isPushed) {
                int maNVColumnIndex = table.getColumn("Xem chi tiết").getModelIndex();
                return table.getValueAt(editingRow, maNVColumnIndex);
            }
            isPushed = false;
            return super.getCellEditorValue();
        }
    }
}