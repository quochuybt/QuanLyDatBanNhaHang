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

        // Hàng 3: Tên Tài khoản | Mật khẩu mới
        addComponent(mainPanel, new JLabel("Tên Tài khoản:"), txtTenTK, gbc, 3, 0);
        addComponent(mainPanel, new JLabel("Nhập Mật khẩu MỚI (để trống nếu không đổi):"), txtMatKhauMoi, gbc, 3, 1);

        // Hàng 4: Lương | Vai Trò
        addComponent(mainPanel, new JLabel("Lương:"), txtLuong, gbc, 4, 0);
        addComponent(mainPanel, new JLabel("Vai Trò:"), cmbVaiTro, gbc, 4, 1);

        // Hàng 5: Địa chỉ
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1; gbc.weightx = 0; mainPanel.add(new JLabel("Địa Chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(txtDiaChi, gbc);


        // --- PANEL NÚT BẤM ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnLuu = new JButton("Lưu Thay Đổi");
        btnLuu.addActionListener(e -> capNhatNhanVien());

        JButton btnXoa = new JButton("Xóa Nhân Viên");
        btnXoa.setBackground(Color.RED);
        btnXoa.setForeground(Color.WHITE);
        btnXoa.addActionListener(e -> xoaNhanVien());

        JButton btnHuy = new JButton("Đóng");
        btnHuy.addActionListener(e -> dispose());

        buttonPanel.add(btnXoa);
        buttonPanel.add(btnLuu);
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
                    hoTen, ngaySinh, gioiTinh, sdt, diaChi, nhanVienGoc.getNgayvaolam(), luong, vaiTro
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

    private void xoaNhanVien() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn XÓA vĩnh viễn nhân viên này và tài khoản liên quan?\n\nHành động này không thể hoàn tác.",
                "Xác nhận Xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            String maNV = nhanVienGoc.getManv();
            String tenTK = nhanVienGoc.getTenTK();

            try {
                boolean success = nhanVienDAO.deleteNhanVienAndAccount(maNV, tenTK);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Đã xóa nhân viên và tài khoản thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    parentPanel.refreshTable();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại. Vui lòng kiểm tra các ràng buộc khác (như Hóa đơn, Đơn đặt món, PhanCongCa) hoặc lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}