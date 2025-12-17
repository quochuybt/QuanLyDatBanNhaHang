package gui;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.VaiTro;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ThemNhanVienDialog extends JDialog {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final NhanVienGUI parentPanel;

    private final String DATE_PLACEHOLDER = "dd/MM/yyyy";
    private final JTextField txtHoTen = new JTextField(15);
    private final JTextField txtNgaySinh = new JTextField(8);
    private final JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField txtSdt = new JTextField(10);
    private final JTextField txtDiaChi = new JTextField(30);
    private final JTextField txtLuong = new JTextField(8);
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JTextField txtTenTK = new JTextField(10);
    private final JPasswordField txtMatKhau = new JPasswordField(10);
    private final JTextField txtEmail = new JTextField(15); // 🌟 THÊM: Trường nhập Email

    public ThemNhanVienDialog(NhanVienGUI parentPanel) {
        super(SwingUtilities.getWindowAncestor(parentPanel) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(parentPanel) : null,
                "Thêm Nhân Viên Mới", true);
        this.parentPanel = parentPanel;
        setupUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNgaySinh.setText(DATE_PLACEHOLDER);
        txtNgaySinh.setForeground(Color.GRAY);

        txtNgaySinh.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtNgaySinh.getText().equals(DATE_PLACEHOLDER)) {
                    txtNgaySinh.setText("");
                    txtNgaySinh.setForeground(Color.BLACK); // Màu nhập liệu thông thường
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtNgaySinh.getText().isEmpty()) {
                    txtNgaySinh.setForeground(Color.GRAY); // Màu Placeholder
                    txtNgaySinh.setText(DATE_PLACEHOLDER);
                }
            }
        });
        addComponent(mainPanel, new JLabel("Họ Tên:"), txtHoTen, gbc, 0, 0);
        addComponent(mainPanel, new JLabel("Ngày Sinh:"), txtNgaySinh, gbc, 0, 1);

        addComponent(mainPanel, new JLabel("Giới Tính:"), cmbGioiTinh, gbc, 1, 0);
        addComponent(mainPanel, new JLabel("SĐT:"), txtSdt, gbc, 1, 1);

        addComponent(mainPanel, new JLabel("Tên Tài khoản:"), txtTenTK, gbc, 2, 0);
        addComponent(mainPanel, new JLabel("Email:"), txtEmail, gbc, 2, 1);

        addComponent(mainPanel, new JLabel("Mật Khẩu Mặc Định:"), txtMatKhau, gbc, 3, 0);
        addComponent(mainPanel, new JLabel("Vai Trò:"), cmbVaiTro, gbc, 3, 1);

        addComponent(mainPanel, new JLabel("Lương:"), txtLuong, gbc, 4, 0);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0; mainPanel.add(new JLabel("Địa Chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(txtDiaChi, gbc);

        txtMatKhau.setText(generateDefaultPassword());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThem = new JButton("Thêm");
        btnThem.addActionListener(e -> themNhanVien());
        JButton btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());

        buttonPanel.add(btnThem);
        buttonPanel.add(btnHuy);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addComponent(JPanel panel, Component label, Component field, GridBagConstraints gbc, int row, int col) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        panel.add(field, gbc);
    }

    private String generateDefaultPassword() {
        return "123456";
    }

    private void themNhanVien() {
        try {
            String hoTen = txtHoTen.getText().trim();
            LocalDate ngaySinh;
            try {
                ngaySinh = LocalDate.parse(txtNgaySinh.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ (dd/MM/yyyy).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String gioiTinh = (String) cmbGioiTinh.getSelectedItem();
            String sdt = txtSdt.getText().trim();
            float luong = Float.parseFloat(txtLuong.getText().trim());
            String diaChi = txtDiaChi.getText().trim();
            VaiTro vaiTro = (VaiTro) cmbVaiTro.getSelectedItem();

            String tenTK = txtTenTK.getText().trim();
            String matKhau = new String(txtMatKhau.getPassword());
            String email = txtEmail.getText().trim(); // 🌟 THÊM: Lấy giá trị Email

            if(tenTK.isEmpty()){
                JOptionPane.showMessageDialog(this, "Tên Tài khoản không được rỗng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(matKhau.isEmpty()){
                JOptionPane.showMessageDialog(this, "Mật khẩu không được rỗng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(email.isEmpty()){
                JOptionPane.showMessageDialog(this, "Email không được rỗng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            NhanVien nv = new NhanVien(
                    hoTen,
                    ngaySinh,
                    gioiTinh,
                    sdt,
                    diaChi,
                    LocalDate.now(),
                    luong,
                    vaiTro,
                    email
            );

            boolean success = nhanVienDAO.addNhanVienAndAccount(nv, tenTK, matKhau);

            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm Nhân viên và Tài khoản thành công!\nTên TK: " + tenTK + "\nMật khẩu: " + matKhau, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                parentPanel.refreshTable();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm Nhân viên/Tài khoản thất bại. Có thể Tên TK, SĐT, hoặc Email đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}