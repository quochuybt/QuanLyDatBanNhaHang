package iuh.fit.gui;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.service.DanhMucMonService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyDanhMucDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private final DanhMucMonService danhMucService;
    private boolean dataChanged = false;

    public QuanLyDanhMucDialog(Frame parent) {
        super(parent, "Quản Lý Danh Mục", true);
        this.danhMucService = new DanhMucMonService();
        setSize(800, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        String[] headers = {"Mã DM", "Tên Danh Mục", "Mô Tả"};
        model = new DefaultTableModel(headers, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnDong = new JButton("Đóng");

        styleButton(btnThem, new Color(40, 167, 69));
        styleButton(btnSua, new Color(255, 193, 7));
        styleButton(btnXoa, new Color(220, 53, 69));

        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnDong);
        add(pnlButtons, BorderLayout.SOUTH);

        // Events
        btnThem.addActionListener(e -> actionThem());
        btnSua.addActionListener(e -> actionSua());
        btnXoa.addActionListener(e -> actionXoa());
        btnDong.addActionListener(e -> dispose());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<DanhMucMonDTO> list = danhMucService.getAllDanhMuc();
        for (DanhMucMonDTO dm : list) {
            model.addRow(new Object[]{dm.getMadm(), dm.getTendm(), dm.getMota()});
        }
    }

    private void actionThem() {
        JTextField txtTen = new JTextField();
        JTextField txtMoTa = new JTextField();
        Object[] message = {"Tên danh mục:", txtTen, "Mô tả:", txtMoTa};

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm danh mục mới", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được để trống!");
                return;
            }
            try {
                DanhMucMonDTO dto = DanhMucMonDTO.builder()
                        .tendm(txtTen.getText().trim())
                        .mota(txtMoTa.getText().trim())
                        .build();
                if (danhMucService.themDanhMuc(dto)) {
                    loadData();
                    dataChanged = true;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionSua() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String ma = model.getValueAt(row, 0).toString();
        String tenCu = model.getValueAt(row, 1).toString();
        String moTaCu = model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "";

        JTextField txtTen = new JTextField(tenCu);
        JTextField txtMoTa = new JTextField(moTaCu);
        Object[] message = {"Tên danh mục:", txtTen, "Mô tả:", txtMoTa};

        int option = JOptionPane.showConfirmDialog(this, message, "Sửa danh mục", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                DanhMucMonDTO dto = DanhMucMonDTO.builder()
                        .madm(ma)
                        .tendm(txtTen.getText().trim())
                        .mota(txtMoTa.getText().trim())
                        .build();
                if (danhMucService.capNhatDanhMuc(dto)) {
                    loadData();
                    dataChanged = true;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String ma = model.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa? Các món ăn thuộc danh mục này có thể bị ảnh hưởng.", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DanhMucMonDTO dto = DanhMucMonDTO.builder().madm(ma).build();
                if (danhMucService.xoaDanhMuc(dto)) {
                    loadData();
                    dataChanged = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa (Có thể đang chứa món ăn).");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    public boolean isDataChanged() {
        return dataChanged;
    }
}
