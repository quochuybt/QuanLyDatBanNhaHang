package iuh.fit.gui;

import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.dto.NhanVienDTO;
import iuh.fit.core.net.client.NhanVienRemoteService;
import iuh.fit.core.entity.VaiTro;
import com.toedter.calendar.JDateChooser; // IMPORT THƯ VIỆN JCALENDAR

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

public class ThemNhanVienDialog extends JDialog {

    private final NhanVienRemoteService nhanVienRemoteService;
    private final NhanVienGUI parentPanel;

    private final JTextField txtHoTen = new JTextField(15);
    // 1. SỬ DỤNG JDATECHOOSER THAY CHO JTEXTFIELD
    private final JDateChooser txtNgaySinh = new JDateChooser();

    private final JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField txtSdt = new JTextField(10);
    private final JTextField txtDiaChi = new JTextField(30);
    private final JTextField txtLuong = new JTextField(8);
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JTextField txtTenTK = new JTextField(10);
    private final JPasswordField txtMatKhau = new JPasswordField(10);
    private final JTextField txtEmail = new JTextField(15);

    public ThemNhanVienDialog(NhanVienGUI parentPanel) {
        super(SwingUtilities.getWindowAncestor(parentPanel) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(parentPanel) : null,
                "Thêm Nhân Viên Mới", true);
        this.parentPanel = parentPanel;
        this.nhanVienRemoteService = parentPanel.getNhanVienRemoteService();
        if (this.nhanVienRemoteService == null) {
            throw new IllegalStateException("Không có kết nối remote cho chức năng nhân viên.");
        }
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

        // 2. THIẾT LẬP FORMAT CHO JDATECHOOSER
        txtNgaySinh.setDateFormatString("dd/MM/yyyy");
        // Tùy chọn: Set ngày mặc định (VD: cách đây 18 năm) để người dùng chọn nhanh hơn
        // txtNgaySinh.setDate(java.sql.Date.valueOf(LocalDate.now().minusYears(18)));

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
            String sdt = txtSdt.getText().trim();
            String luongStr = txtLuong.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String tenTK = txtTenTK.getText().trim();
            String matKhau = new String(txtMatKhau.getPassword());
            String email = txtEmail.getText().trim();

            if (hoTen.isEmpty()) {
                showError("Họ tên không được để trống.");
                txtHoTen.requestFocus();
                return;
            }
            if (!hoTen.matches("^[\\p{L}\\s]+$")) {
                showError("Họ tên không hợp lệ (chỉ được chứa chữ cái và khoảng trắng).");
                txtHoTen.requestFocus();
                return;
            }

            // 3. XỬ LÝ LẤY DỮ LIỆU TỪ JDATECHOOSER
            java.util.Date selectedDate = txtNgaySinh.getDate();
            if (selectedDate == null) {
                showError("Vui lòng chọn hoặc nhập ngày sinh hợp lệ.");
                txtNgaySinh.requestFocusInWindow();
                return;
            }

            // Convert từ java.util.Date sang java.time.LocalDate
            LocalDate ngaySinh = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Tính tuổi
            int age = Period.between(ngaySinh, LocalDate.now()).getYears();
            if (age < 18) {
                showError("Nhân viên phải từ đủ 18 tuổi trở lên.");
                txtNgaySinh.requestFocusInWindow();
                return;
            }

            if (sdt.isEmpty() || !sdt.matches("^0\\d{9}$")) {
                showError("Số điện thoại không hợp lệ (Phải có 10 chữ số và bắt đầu bằng số 0).");
                txtSdt.requestFocus();
                return;
            }

            if (diaChi.isEmpty()) {
                showError("Địa chỉ không được để trống.");
                txtDiaChi.requestFocus();
                return;
            }

            if (email.isEmpty() || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                showError("Email không hợp lệ (Ví dụ đúng: ten@gmail.com).");
                txtEmail.requestFocus();
                return;
            }

            if (tenTK.isEmpty() || tenTK.contains(" ") || tenTK.length() < 5) {
                showError("Tên tài khoản không được rỗng, không chứa khoảng trắng và phải từ 5 ký tự trở lên.");
                txtTenTK.requestFocus();
                return;
            }

            if (matKhau.isEmpty() || matKhau.length() < 6) {
                showError("Mật khẩu không được rỗng và phải từ 6 ký tự trở lên.");
                txtMatKhau.requestFocus();
                return;
            }

            if (luongStr.isEmpty()) {
                showError("Vui lòng nhập mức lương.");
                txtLuong.requestFocus();
                return;
            }
            float luong;
            try {
                luong = Float.parseFloat(luongStr);
                if (luong <= 0) {
                    showError("Lương phải là số lớn hơn 0.");
                    txtLuong.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Lương nhập vào phải là số.");
                txtLuong.requestFocus();
                return;
            }

            String gioiTinh = (String) cmbGioiTinh.getSelectedItem();
            VaiTro vaiTro = (VaiTro) cmbVaiTro.getSelectedItem();

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

            NhanVienDTO dto = NhanVienDTO.fromEntity(nv);
            dto.setTenTK(tenTK);
            nhanVienRemoteService.add(dto, tenTK, matKhau);

            JOptionPane.showMessageDialog(this, "Thêm Nhân viên và Tài khoản thành công!\nTên TK: " + tenTK + "\nMật khẩu: " + matKhau, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            parentPanel.refreshTable();
            dispose();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi dữ liệu", JOptionPane.WARNING_MESSAGE);
    }
}
