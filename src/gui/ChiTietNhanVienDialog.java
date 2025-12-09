package gui;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.VaiTro;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ChiTietNhanVienDialog extends JDialog {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final NhanVienGUI parentPanel;
    private final NhanVien nhanVienGoc;

    private final JTextField txtHoTen = new JTextField(15);
    private final JTextField txtNgaySinh = new JTextField(8);
    private final JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField txtSdt = new JTextField(10);
    private final JTextField txtDiaChi = new JTextField(30);
    private final JTextField txtLuong = new JTextField(8);
    private final JComboBox<VaiTro> cmbVaiTro = new JComboBox<>(VaiTro.values());
    private final JTextField txtTenTK = new JTextField(10);
    private final JPasswordField txtMatKhauMoi = new JPasswordField(10);
    private final JTextField txtEmail = new JTextField(15); // Trường nhập Email
    private int accountStatus;

    public ChiTietNhanVienDialog(NhanVienGUI parentPanel, String maNV) {
        super(SwingUtilities.getWindowAncestor(parentPanel) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(parentPanel) : null,
                "Chi Tiết Nhân Viên", true);
        this.parentPanel = parentPanel;

        this.nhanVienGoc = nhanVienDAO.getChiTietNhanVien(maNV);

        if (nhanVienGoc == null) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy nhân viên có mã: " + maNV, "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        this.accountStatus = nhanVienDAO.getAccountStatus(nhanVienGoc.getTenTK());
        setupUI();
        loadData();
        pack();
        setLocationRelativeTo(null);
    }

    private void loadData() {
        txtHoTen.setText(nhanVienGoc.getHoten());
        txtNgaySinh.setText(nhanVienGoc.getNgaysinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cmbGioiTinh.setSelectedItem(nhanVienGoc.getGioitinh());
        txtSdt.setText(nhanVienGoc.getSdt());
        txtDiaChi.setText(nhanVienGoc.getDiachi());
        txtLuong.setText(String.valueOf(nhanVienGoc.getLuong()));
        cmbVaiTro.setSelectedItem(nhanVienGoc.getVaiTro());

        txtTenTK.setText(nhanVienGoc.getTenTK());
        txtMatKhauMoi.setText("");
        txtEmail.setText(nhanVienGoc.getEmail()); // Load Email
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hiển thị mã NV
        JLabel lblMaNV = new JLabel("Mã NV: " + nhanVienGoc.getManv());
        lblMaNV.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.WEST; mainPanel.add(lblMaNV, gbc);

        // Hàng 1: Họ Tên | Ngày Sinh
        addComponent(mainPanel, new JLabel("Họ Tên:"), txtHoTen, gbc, 1, 0);
        addComponent(mainPanel, new JLabel("Ngày Sinh:"), txtNgaySinh, gbc, 1, 1);

        // Hàng 2: Giới Tính | SĐT
        addComponent(mainPanel, new JLabel("Giới Tính:"), cmbGioiTinh, gbc, 2, 0);
        addComponent(mainPanel, new JLabel("SĐT:"), txtSdt, gbc, 2, 1);

        // Hàng 3: Tên Tài khoản | Email
        addComponent(mainPanel, new JLabel("Tên Tài khoản:"), txtTenTK, gbc, 3, 0);
        addComponent(mainPanel, new JLabel("Email:"), txtEmail, gbc, 3, 1);

        // Hàng 4: Mật khẩu mới | Vai Trò
        addComponent(mainPanel, new JLabel("Nhập Mật khẩu MỚI (để trống nếu không đổi):"), txtMatKhauMoi, gbc, 4, 0);
        addComponent(mainPanel, new JLabel("Vai Trò:"), cmbVaiTro, gbc, 4, 1);

        // 🌟 SỬA: Hàng 5: Lương
        addComponent(mainPanel, new JLabel("Lương:"), txtLuong, gbc, 5, 0);
        // Hàng 5, Cột 1 để trống, hoặc có thể dùng để căn chỉnh nếu cần:
        // mainPanel.add(Box.createRigidArea(new Dimension(100, 20)), gbc);

        // Hàng 6: Địa chỉ
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1; gbc.weightx = 0; mainPanel.add(new JLabel("Địa Chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(txtDiaChi, gbc);


        // --- PANEL NÚT BẤM ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnLuu = new JButton("Lưu Thay Đổi");
        btnLuu.addActionListener(e -> capNhatNhanVien());

        // 🌟 NÚT TẠM NGƯNG
        JButton btnTamNgung = new JButton("Tạm Ngưng Hoạt Động");
        btnTamNgung.setBackground(Color.ORANGE);
        btnTamNgung.addActionListener(e -> tamNgungNhanVien());

        // 🌟 NÚT KÍCH HOẠT LẠI
        JButton btnKichHoat = new JButton("KÍCH HOẠT LẠI");
        btnKichHoat.setBackground(new Color(0, 150, 0)); // Màu xanh lá cây
        btnKichHoat.addActionListener(e -> kichHoatNhanVien()); // Gọi hàm mới

        // --- Logic Hiển thị nút ---
        // Nếu Vai trò là QUANLY thì không cho phép tạm ngưng/kích hoạt
        if (nhanVienGoc.getVaiTro() == VaiTro.NHANVIEN) {
            if (accountStatus == 1) {
                // Đang hoạt động -> Hiển thị nút Tạm Ngưng
                buttonPanel.add(btnTamNgung);
            } else if (accountStatus == 0) {
                // Đang tạm ngưng -> Hiển thị nút Kích Hoạt Lại
                buttonPanel.add(btnKichHoat);
            }
            // Nếu accountStatus là -1 (lỗi) thì không hiển thị nút nào liên quan đến trạng thái
        }

        buttonPanel.add(btnLuu);

        JButton btnHuy = new JButton("Đóng");
        btnHuy.addActionListener(e -> dispose());
        buttonPanel.add(btnHuy);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addComponent(JPanel panel, Component label, Component field, GridBagConstraints gbc, int row, int col) {
        gbc.gridx = col * 2; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0; gbc.gridwidth = 1; panel.add(label, gbc);
        gbc.gridx = col * 2 + 1; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0; gbc.gridwidth = 1; panel.add(field, gbc);
    }

    private void capNhatNhanVien() {
        try {
            // 1. Lấy và Validate dữ liệu
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
            String email = txtEmail.getText().trim(); // Lấy giá trị Email mới

            String oldTenTK = nhanVienGoc.getTenTK();
            String newTenTK = txtTenTK.getText().trim();
            String newMatKhau = new String(txtMatKhauMoi.getPassword());

            if(newTenTK.isEmpty()){
                JOptionPane.showMessageDialog(this, "Tên tài khoản không được rỗng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Tạo đối tượng NhanVien mới (dùng validation)
            NhanVien nvUpdate = new NhanVien(
                    nhanVienGoc.getManv(),
                    hoTen, ngaySinh, gioiTinh, sdt, diaChi, nhanVienGoc.getNgayvaolam(), luong, vaiTro, email // Truyền email
            );

            // 3. Thực hiện cập nhật
            boolean success = nhanVienDAO.updateNhanVienAndAccount(nvUpdate, oldTenTK, newTenTK, newMatKhau);

            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                parentPanel.refreshTable();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Vui lòng kiểm tra lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
    private void kichHoatNhanVien() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn KÍCH HOẠT LẠI tài khoản của nhân viên này không?",
                "Xác nhận Kích Hoạt",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            String tenTK = nhanVienGoc.getTenTK();

            try {
                boolean success = nhanVienDAO.activateNhanVienAccount(tenTK);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đã KÍCH HOẠT LẠI tài khoản nhân viên thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    parentPanel.refreshTable();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Kích hoạt lại thất bại. Vui lòng kiểm tra lại kết nối CSDL hoặc tên tài khoản.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi kích hoạt lại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    private void tamNgungNhanVien() {
        // Kiểm tra điều kiện chỉ cho phép tạm ngưng VaiTro.NHANVIEN
        if (nhanVienGoc.getVaiTro() != VaiTro.NHANVIEN) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ có thể TẠM NGƯNG hoạt động đối với nhân viên có Vai trò NHANVIEN (hiện tại là " + nhanVienGoc.getVaiTro().name() + ").",
                    "Lỗi Tác Vụ",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn TẠM NGƯNG HOẠT ĐỘNG của nhân viên này?\n\nTài khoản của họ sẽ bị vô hiệu hóa, không thể đăng nhập.",
                "Xác nhận Tạm Ngưng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            String maNV = nhanVienGoc.getManv();
            String tenTK = nhanVienGoc.getTenTK();
            VaiTro vaiTro = nhanVienGoc.getVaiTro();

            try {
                // 🌟 GỌI PHƯƠNG THỨC MỚI
                boolean success = nhanVienDAO.suspendNhanVienAndAccount(maNV, tenTK, vaiTro);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đã TẠM NGƯNG HOẠT ĐỘNG nhân viên và vô hiệu hóa tài khoản thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    parentPanel.refreshTable();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Tạm ngưng thất bại. Vui lòng kiểm tra lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException e) {
                // Bắt lỗi nếu vai trò không phải NHANVIEN (mặc dù đã kiểm tra ở trên)
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi Tác Vụ", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi tạm ngưng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}