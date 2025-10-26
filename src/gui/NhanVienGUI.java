package gui;

import dao.NhanVienDAO;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class NhanVienGUI extends JPanel {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private DefaultTableModel model;
    private JTable table;
    private final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public NhanVienGUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(244, 247, 252));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // LỖI XẢY RA Ở ĐÂY NẾU PHƯƠNG THỨC BỊ THIẾU
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = createMainContentPanel();
        add(mainContent, BorderLayout.CENTER);

        loadDataToTable();
    }

    // PHƯƠNG THỨC THIẾU 1: Tạo Header (Tiêu đề và nút Thêm NV)
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

    // PHƯƠNG THỨC THIẾU 2: Tạo Nội dung chính (Thanh tìm kiếm và Bảng)
    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Thanh tìm kiếm (Mô phỏng)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        searchPanel.add(new JComboBox<>(new String[]{"Tên NV", "Mã NV", "SĐT"}));
        searchPanel.add(new JTextField(20));
        searchPanel.add(new JButton("Tìm"));

        panel.add(searchPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
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

    public void loadDataToTable() {
        model.setRowCount(0);
        List<NhanVien> ds = nhanVienDAO.getAllNhanVien();

        for (NhanVien nv : ds) {
            Object[] row = new Object[]{
                    nv.getHoten(),
                    nv.getVaiTro().name(),
                    "195", // Giả lập Tổng số giờ làm
                    "0",   // Giả lập Nhắc nhở
                    nv.getManv()
            };
            model.addRow(row);
        }
    }

    public void refreshTable() {
        loadDataToTable();
    }

    // --- Lớp hỗ trợ cho cột "Xem chi tiết >>" (Renderer) ---
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

    // --- Lớp hỗ trợ cho cột "Xem chi tiết >>" (Editor - Xử lý click) ---
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final JTable table;
        private boolean isPushed;

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
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int selectedRow = table.getSelectedRow();
                // LẤY MÃ NV (đã lưu ở bước loadDataToTable vào cột này)
                String maNV = (String) table.getValueAt(selectedRow, table.getColumn("Xem chi tiết").getModelIndex());

                // MỞ HỘP THOẠI CHI TIẾT
                ChiTietNhanVienDialog dialog = new ChiTietNhanVienDialog(NhanVienGUI.this, maNV);
                dialog.setVisible(true);
            }
            isPushed = false;
            // Trả về giá trị đã click (mã NV)
            return table.getValueAt(table.getSelectedRow(), table.getColumn("Xem chi tiết").getModelIndex());
        }
    }
}