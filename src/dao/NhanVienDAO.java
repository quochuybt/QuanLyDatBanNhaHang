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

    // Hàm tiện ích để ánh xạ ResultSet vào đối tượng NhanVien
    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setManv(rs.getString("manv"));
        nv.setHoten(rs.getString("hoTen"));
        // Lưu ý: Sử dụng LocalDate, cần dùng rs.getDate().toLocalDate()
        nv.setNgaysinh(rs.getDate("ngaySinh").toLocalDate());
        nv.setGioitinh(rs.getString("gioiTinh"));
        nv.setSdt(rs.getString("sdt"));
        nv.setDiachi(rs.getString("diaChi"));
        nv.setNgayvaolam(rs.getDate("ngayVaoLam").toLocalDate());
        nv.setLuong(rs.getFloat("luong"));
        // Đọc enum từ chuỗi trong DB (cần TRIM để loại bỏ khoảng trắng dư thừa)
        nv.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro").toUpperCase().trim()));
        nv.setTenTK(rs.getString("tenTK"));
        return nv;
    }

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
                ds.add(mapResultSetToNhanVien(rs)); // Tái sử dụng hàm ánh xạ
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
                    return mapResultSetToNhanVien(rs); // Tái sử dụng hàm ánh xạ
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm kiếm nhân viên theo Tên (Tìm kiếm tương đối - LIKE).
     */
    public List<NhanVien> searchNhanVienByName(String keyword) {
        List<NhanVien> ds = new ArrayList<>();
        // Sử dụng LIKE để tìm kiếm gần đúng, và LOWER() để không phân biệt chữ hoa/thường
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK FROM NhanVien WHERE LOWER(hoTen) LIKE ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword.toLowerCase().trim() + "%"); // Đặt tham số cho LIKE

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn tìm kiếm NhanVien theo Tên: " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Lấy danh sách nhân viên theo Vai trò (Chính xác).
     */
    public List<NhanVien> getNhanVienByRole(VaiTro vaiTro) {
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK FROM NhanVien WHERE vaiTro = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vaiTro.name()); // Vai trò (Enum) được lưu bằng name()

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn NhanVien theo Vai trò: " + e.getMessage(), e);
        }
        return ds;
    }

    // =================================================================
    // CÁC HÀM XỬ LÝ GHI CSDL (INSERT/UPDATE/DELETE)
    // =================================================================

    /**
     * THÊM NHÂN VIÊN VÀ TÀI KHOẢN
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
     * CẬP NHẬT THÔNG TIN NHÂN VIÊN VÀ TÀI KHOẢN
     * [ĐÃ SỬA LỖI]: Đảo ngược thứ tự update FK trước, PK sau để tránh lỗi REFERENCES constraint.
     */
    public boolean updateNhanVienAndAccount(NhanVien nv, String oldTenTK, String newTenTK, String newPlainPassword) {
        Connection conn = null;
        boolean isSuccess = false;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // BƯỚC 1: XỬ LÝ VIỆC ĐỔI TÊN TÀI KHOẢN (PK -> FK)
            if (!oldTenTK.equals(newTenTK)) {

                // 1a. Cập nhật Khóa ngoại (NhanVien.tenTK) TRƯỚC (từ OLD_TK sang NEW_TK)
                String sqlUpdateNhanVienFK = "UPDATE NhanVien SET tenTK = ? WHERE maNV = ? AND tenTK = ?";
                try (PreparedStatement pstmtUpdateNhanVienFK = conn.prepareStatement(sqlUpdateNhanVienFK)) {
                    pstmtUpdateNhanVienFK.setString(1, newTenTK);
                    pstmtUpdateNhanVienFK.setString(2, nv.getManv());
                    pstmtUpdateNhanVienFK.setString(3, oldTenTK);
                    if (pstmtUpdateNhanVienFK.executeUpdate() == 0) {
                        throw new SQLException("Update NhanVien FK failed: tenTK was NOT updated.");
                    }
                }

                // 1b. Cập nhật Khóa chính (TaiKhoan.tenTK) SAU (từ OLD_TK sang NEW_TK)
                String sqlUpdateTK = "UPDATE TaiKhoan SET tenTK = ? WHERE tenTK = ?";
                try (PreparedStatement pstmtUpdateTK = conn.prepareStatement(sqlUpdateTK)) {
                    pstmtUpdateTK.setString(1, newTenTK);
                    pstmtUpdateTK.setString(2, oldTenTK);
                    if (pstmtUpdateTK.executeUpdate() == 0) {
                        throw new SQLException("Update TaiKhoan failed: could not change tenTK.");
                    }
                }
            }

            // BƯỚC 2: CẬP NHẬT THÔNG TIN CHUNG CỦA NHÂN VIÊN VÀ MẬT KHẨU

            // 2a. Cập nhật NhanVien (bao gồm các thông tin cá nhân và tenTK mới/cũ)
            String sqlNV = "UPDATE NhanVien SET hoTen=?, ngaySinh=?, gioiTinh=?, sdt=?, diaChi=?, luong=?, vaiTro=?, tenTK=? WHERE maNV=?";
            try (PreparedStatement pstmtNV = conn.prepareStatement(sqlNV)) {
                pstmtNV.setString(1, nv.getHoten());
                pstmtNV.setDate(2, java.sql.Date.valueOf(nv.getNgaysinh()));
                pstmtNV.setString(3, nv.getGioitinh());
                pstmtNV.setString(4, nv.getSdt());
                pstmtNV.setString(5, nv.getDiachi());
                pstmtNV.setFloat(6, nv.getLuong());
                pstmtNV.setString(7, nv.getVaiTro().name());
                // Luôn sử dụng newTenTK (hoặc oldTenTK nếu không đổi tên)
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

            // BƯỚC 1: XÓA DỮ LIỆU LIÊN QUAN (Giả định FK cần xóa thủ công)

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
                    // Log: Có thể tài khoản đã bị xóa nếu DB có ON DELETE CASCADE
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

    /**
     * Lấy tên nhân viên dựa trên mã NV (Không thay đổi)
     */
    public String getTenNhanVienByMa(String maNV) {
        String sql = "SELECT hoTen FROM NhanVien WHERE maNV = ?";
        String tenNV = "N/A (Lỗi CSDL)";

        if (maNV == null || maNV.trim().isEmpty()) {
            return "N/A (Thiếu Mã NV)";
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenNV = rs.getString("hoTen");
                } else {
                    tenNV = "N/A (" + maNV + ")";
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tên NV " + maNV + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tenNV;
    }
}