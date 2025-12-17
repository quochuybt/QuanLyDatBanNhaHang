package gui;

import dao.DanhMucMonDAO;
import entity.DanhMucMon;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyDanhMucDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private DanhMucMonDAO danhMucDAO;
    private boolean dataChanged = false;

    public QuanLyDanhMucDialog(Frame parent) {
        super(parent, "Quản Lý Danh Mục", true);
        this.danhMucDAO = new DanhMucMonDAO();
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
        List<DanhMucMon> list = danhMucDAO.getAllDanhMuc();
        for (DanhMucMon dm : list) {
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
            DanhMucMon dm = new DanhMucMon("", txtTen.getText(), txtMoTa.getText());
            if (danhMucDAO.themDanhMuc(dm)) {
                loadData();
                dataChanged = true;
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
            DanhMucMon dm = new DanhMucMon(ma, txtTen.getText(), txtMoTa.getText());
            if (danhMucDAO.capNhatDanhMuc(dm)) {
                loadData();
                dataChanged = true;
            }
        }
    }

    private void actionXoa() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String ma = model.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa? Các món ăn thuộc danh mục này có thể bị ảnh hưởng.", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (danhMucDAO.xoaDanhMuc(ma)) {
                loadData();
                dataChanged = true;
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa (Có thể đang chứa món ăn).");
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