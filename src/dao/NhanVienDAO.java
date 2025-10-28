package dao;

import connectDB.SQLConnection;
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

    // LƯU Ý: Đã xóa toPascalCase. Ghi/Đọc Vai trò (trường hợp này là chữ hoa) được xử lý bằng name()/toUpperCase().

    // =================================================================
    // CÁC HÀM XỬ LÝ ĐỌC CSDL (SELECT)
    // =================================================================

    /**
     * Lấy danh sách nhân viên từ CSDL.
     */
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
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
                nv.setTenTK(rs.getString("tenTK"));
                ds.add(nv);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn CSDL NhanVien: " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Lấy thông tin chi tiết nhân viên dựa trên mã NV.
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
                    nv.setTenTK(rs.getString("tenTK"));
                    return nv;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =================================================================
    // CÁC HÀM XỬ LÝ GHI CSDL (INSERT/UPDATE/DELETE)
    // =================================================================

    /**
     * THÊM NHÂN VIÊN VÀ TÀI KHOẢN (Đã sửa lỗi thứ tự INSERT Khóa ngoại)
     */
    public boolean addNhanVienAndAccount(NhanVien nv, String tenTK, String plainPassword) {
        Connection conn = null;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. THÊM TÀI KHOẢN (PK) TRƯỚC
            String sqlTK = "INSERT INTO TaiKhoan (tenTK, matKhau, trangThai) VALUES (?, ?, 1)";
            String hashedPass = "hashed_" + plainPassword.trim().toLowerCase().hashCode();

            try (PreparedStatement pstmtTK = conn.prepareStatement(sqlTK)) {
                pstmtTK.setString(1, tenTK);
                pstmtTK.setString(2, hashedPass);

                if (pstmtTK.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. THÊM NHÂN VIÊN (FK) SAU
            String sqlNV = "INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtNV = conn.prepareStatement(sqlNV)) {
                pstmtNV.setString(1, nv.getManv());
                pstmtNV.setString(2, nv.getHoten());
                pstmtNV.setDate(3, java.sql.Date.valueOf(nv.getNgaysinh()));
                pstmtNV.setString(4, nv.getGioitinh());
                pstmtNV.setString(5, nv.getSdt());
                pstmtNV.setString(6, nv.getDiachi());
                pstmtNV.setDate(7, java.sql.Date.valueOf(nv.getNgayvaolam()));
                pstmtNV.setFloat(8, nv.getLuong());
                pstmtNV.setString(9, nv.getVaiTro().name()); // Ghi NHANVIEN/QUANLY
                pstmtNV.setString(10, tenTK);

                if (pstmtNV.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
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
    }

    /**
     * CẬP NHẬT THÔNG TIN NHÂN VIÊN VÀ TÀI KHOẢN (Đã sửa lỗi Khóa ngoại)
     */
    public boolean updateNhanVienAndAccount(NhanVien nv, String oldTenTK, String newTenTK, String newPlainPassword) {
        Connection conn = null;
        boolean isSuccess = false;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // BƯỚC 1: XỬ LÝ VIỆC ĐỔI TÊN TÀI KHOẢN (PK -> FK)
            if (!oldTenTK.equals(newTenTK)) {

                // 1a. Cập nhật Khóa chính (TaiKhoan) TRƯỚC
                String sqlUpdateTK = "UPDATE TaiKhoan SET tenTK = ? WHERE tenTK = ?";
                try (PreparedStatement pstmtUpdateTK = conn.prepareStatement(sqlUpdateTK)) {
                    pstmtUpdateTK.setString(1, newTenTK);
                    pstmtUpdateTK.setString(2, oldTenTK);
                    if (pstmtUpdateTK.executeUpdate() == 0) throw new SQLException("Update TaiKhoan failed: could not change tenTK.");
                }

                // 1b. Cập nhật Khóa ngoại (NhanVien) SAU
                String sqlUpdateNhanVienFK = "UPDATE NhanVien SET tenTK = ? WHERE maNV = ?";
                try (PreparedStatement pstmtUpdateNhanVienFK = conn.prepareStatement(sqlUpdateNhanVienFK)) {
                    pstmtUpdateNhanVienFK.setString(1, newTenTK);
                    pstmtUpdateNhanVienFK.setString(2, nv.getManv());
                    if (pstmtUpdateNhanVienFK.executeUpdate() == 0) throw new SQLException("Update NhanVien FK failed: tenTK was changed in TaiKhoan but not NhanVien.");
                }
            }

            // BƯỚC 2: CẬP NHẬT THÔNG TIN CHUNG CỦA NHÂN VIÊN VÀ MẬT KHẨU
            // 2a. Cập nhật NhanVien
            String sqlNV = "UPDATE NhanVien SET hoTen=?, ngaySinh=?, gioiTinh=?, sdt=?, diaChi=?, luong=?, vaiTro=?, tenTK=? WHERE maNV=?";
            try (PreparedStatement pstmtNV = conn.prepareStatement(sqlNV)) {
                pstmtNV.setString(1, nv.getHoten());
                pstmtNV.setDate(2, java.sql.Date.valueOf(nv.getNgaysinh()));
                pstmtNV.setString(3, nv.getGioitinh());
                pstmtNV.setString(4, nv.getSdt());
                pstmtNV.setString(5, nv.getDiachi());
                pstmtNV.setFloat(6, nv.getLuong());
                pstmtNV.setString(7, nv.getVaiTro().name());
                pstmtNV.setString(8, newTenTK);
                pstmtNV.setString(9, nv.getManv());

                if (pstmtNV.executeUpdate() == 0) throw new SQLException("Update NhanVien failed, no rows affected.");
            }

            // 2b. Đổi Mật khẩu
            if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
                String sqlUpdatePass = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
                String hashedPass = "hashed_" + newPlainPassword.trim().toLowerCase().hashCode();
                try (PreparedStatement pstmtUpdatePass = conn.prepareStatement(sqlUpdatePass)) {
                    pstmtUpdatePass.setString(1, hashedPass);
                    pstmtUpdatePass.setString(2, newTenTK);
                    if (pstmtUpdatePass.executeUpdate() == 0) throw new SQLException("Update MatKhau failed: could not update password.");
                }
            }

            conn.commit();
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

    /**
     * XÓA NHÂN VIÊN (Transaction - Hard Delete)
     */
    public boolean deleteNhanVienAndAccount(String maNV, String tenTK) {
        Connection conn = null;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // BƯỚC 1: XÓA DỮ LIỆU LIÊN QUAN (BẮT BUỘC)
            // (Giả sử bạn đã thiết lập ON DELETE CASCADE trong DB hoặc phải xóa thủ công tại đây)

            // 1a. Xóa PhanCongCa (FK_PhanCong_NhanVien)
            String sqlDeletePCC = "DELETE FROM PhanCongCa WHERE maNV = ?";
            try (PreparedStatement pstmtPCC = conn.prepareStatement(sqlDeletePCC)) {
                pstmtPCC.setString(1, maNV);
                pstmtPCC.executeUpdate();
            }

            // 1b. Xóa DonDatMon (FK_DonDatMon_NhanVien)
            String sqlDeleteDDM = "DELETE FROM DonDatMon WHERE maNV = ?";
            try (PreparedStatement pstmtDDM = conn.prepareStatement(sqlDeleteDDM)) {
                pstmtDDM.setString(1, maNV);
                pstmtDDM.executeUpdate();
            }

            // 2. Xóa Nhân viên (FK)
            String sqlDeleteNV = "DELETE FROM NhanVien WHERE maNV = ?";
            try (PreparedStatement pstmtNV = conn.prepareStatement(sqlDeleteNV)) {
                pstmtNV.setString(1, maNV);
                if (pstmtNV.executeUpdate() == 0) {
                    throw new SQLException("Xóa NhanVien thất bại.");
                }
            }

            // 3. Xóa Tài khoản (PK) SAU (Sau khi FK đã được xóa)
            String sqlDeleteTK = "DELETE FROM TaiKhoan WHERE tenTK = ?";
            try (PreparedStatement pstmtTK = conn.prepareStatement(sqlDeleteTK)) {
                pstmtTK.setString(1, tenTK);
                if (pstmtTK.executeUpdate() == 0) {
                    // LƯU Ý: Không ném exception ở đây nếu tài khoản đã bị xóa do ON DELETE CASCADE, chỉ cần log
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
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
    }
    public String getTenNhanVienByMa(String maNV) {
        // Giả sử tên cột trong bảng NhanVien là 'maNV' và 'hoTen'
        String sql = "SELECT hoTen FROM NhanVien WHERE maNV = ?";
        String tenNV = "N/A (Lỗi CSDL)";

        // Nếu mã NV là null, trả về lỗi ngay
        if (maNV == null || maNV.trim().isEmpty()) {
            return "N/A (Thiếu Mã NV)";
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenNV = rs.getString("hoTen"); // Lấy giá trị cột hoTen
                } else {
                    tenNV = "N/A (" + maNV + ")"; // Không tìm thấy mã NV này
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tên NV " + maNV + ": " + e.getMessage());
            e.printStackTrace();
            // tenNV vẫn là "N/A (Lỗi CSDL)"
        }
        return tenNV;
    }
}