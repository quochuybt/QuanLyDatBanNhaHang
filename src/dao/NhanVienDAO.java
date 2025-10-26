package dao;

import connectDB.SQLConnection; // Dùng lớp kết nối của bạn
import entity.NhanVien;
import entity.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    /**
     * Lấy danh sách nhân viên từ CSDL.
     * Dữ liệu mẫu của bạn KHÔNG có cột trangThai, nên tôi đã bỏ điều kiện WHERE trangThai = 1
     */
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        // LƯU Ý: Đã thêm cột tenTK vào SELECT để lấy thông tin tài khoản
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK FROM NhanVien";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setManv(rs.getString("manv"));
                nv.setHoten(rs.getString("hoTen"));
                nv.setNgaysinh(rs.getDate("ngaySinh").toLocalDate());
                nv.setGioitinh(rs.getString("gioiTinh"));
                nv.setSdt(rs.getString("sdt"));
                nv.setDiachi(rs.getString("diaChi"));
                nv.setNgayvaolam(rs.getDate("ngayVaoLam").toLocalDate());
                nv.setLuong(rs.getFloat("luong"));
                nv.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro").toUpperCase().trim()));
                // THÊM: Lưu tên tài khoản vào NhanVien (cần thêm thuộc tính tenTK vào entity NhanVien nếu muốn)
                // Hiện tại ta chỉ cần mã NV để truy vấn chi tiết sau.
                ds.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy thông tin chi tiết nhân viên (bao gồm tên TK) dựa trên mã NV.
     * Cần thiết cho màn hình chi tiết.
     */
    public NhanVien getChiTietNhanVien(String maNV) {
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK FROM NhanVien WHERE maNV = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNV);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setManv(rs.getString("manv"));
                    nv.setHoten(rs.getString("hoTen"));
                    nv.setNgaysinh(rs.getDate("ngaySinh").toLocalDate());
                    nv.setGioitinh(rs.getString("gioiTinh"));
                    nv.setSdt(rs.getString("sdt"));
                    nv.setDiachi(rs.getString("diaChi"));
                    nv.setNgayvaolam(rs.getDate("ngayVaoLam").toLocalDate());
                    nv.setLuong(rs.getFloat("luong"));
                    nv.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro").toUpperCase().trim()));
                    // LƯU Ý: Cần thêm setTenTK vào entity NhanVien nếu muốn lưu tên TK.
                    // Nếu không có, ta sẽ lấy tên TK qua một phương thức khác nếu cần.
                    return nv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * THÊM NHÂN VIÊN (Giữ nguyên logic transaction cũ)
     */
    public boolean addNhanVienAndAccount(NhanVien nv, String tenTK, String plainPassword) {
        // [Giữ nguyên code addNhanVienAndAccount từ câu trả lời trước]
        // ... (phần code này phải được giữ nguyên) ...
        return false; // Thay bằng code thật
    }

    /**
     * CẬP NHẬT THÔNG TIN NHÂN VIÊN VÀ TÀI KHOẢN (bao gồm đổi Tên TK, Mật khẩu)
     * Đây là một Transaction phức tạp hơn.
     */
    public boolean updateNhanVienAndAccount(NhanVien nv, String oldTenTK, String newTenTK, String newPlainPassword) {
        Connection conn = null;
        boolean isSuccess = false;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Cập nhật NhanVien
            String sqlNV = "UPDATE NhanVien SET hoTen=?, ngaySinh=?, gioiTinh=?, sdt=?, diaChi=?, luong=?, vaiTro=?, tenTK=? WHERE maNV=?";
            try (PreparedStatement pstmtNV = conn.prepareStatement(sqlNV)) {
                pstmtNV.setString(1, nv.getHoten());
                pstmtNV.setDate(2, java.sql.Date.valueOf(nv.getNgaysinh()));
                pstmtNV.setString(3, nv.getGioitinh());
                pstmtNV.setString(4, nv.getSdt());
                pstmtNV.setString(5, nv.getDiachi());
                pstmtNV.setFloat(6, nv.getLuong());
                pstmtNV.setString(7, nv.getVaiTro().name());
                pstmtNV.setString(8, newTenTK); // Cập nhật tên TK mới vào bảng NV
                pstmtNV.setString(9, nv.getManv());

                if (pstmtNV.executeUpdate() == 0) throw new SQLException("Update NhanVien failed, no rows affected.");
            }

            // 2. Cập nhật TaiKhoan
            // 2a. Đổi Tên TK (Nếu khác)
            if (!oldTenTK.equals(newTenTK)) {
                String sqlUpdateTK = "UPDATE TaiKhoan SET tenTK = ? WHERE tenTK = ?";
                try (PreparedStatement pstmtUpdateTK = conn.prepareStatement(sqlUpdateTK)) {
                    pstmtUpdateTK.setString(1, newTenTK);
                    pstmtUpdateTK.setString(2, oldTenTK);
                    if (pstmtUpdateTK.executeUpdate() == 0) throw new SQLException("Update TaiKhoan failed: could not change tenTK.");
                }
            }

            // 2b. Đổi Mật khẩu (Nếu có mật khẩu mới được cung cấp)
            if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
                String sqlUpdatePass = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
                String hashedPass = "hashed_" + newPlainPassword.trim().toLowerCase().hashCode(); // Logic băm giống TaiKhoanDAO
                try (PreparedStatement pstmtUpdatePass = conn.prepareStatement(sqlUpdatePass)) {
                    pstmtUpdatePass.setString(1, hashedPass);
                    pstmtUpdatePass.setString(2, newTenTK); // Dùng tên TK mới nhất
                    if (pstmtUpdatePass.executeUpdate() == 0) throw new SQLException("Update MatKhau failed: could not update password.");
                }
            }

            conn.commit(); // Thành công
            isSuccess = true;
        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }
}