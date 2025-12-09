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
        nv.setEmail(rs.getString("email")); // 🌟 THÊM: Đọc cột email
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
        // 🌟 SỬA SQL: Thêm cột email
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email FROM NhanVien";
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
        // 🌟 SỬA SQL: Thêm cột email
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email FROM NhanVien WHERE maNV = ?";
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
        // 🌟 SỬA SQL: Thêm cột email
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email FROM NhanVien WHERE LOWER(hoTen) LIKE ?";
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
        // 🌟 SỬA SQL: Thêm cột email
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email FROM NhanVien WHERE vaiTro = ?";
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

    /**
     * 🌟 THÊM: Hàm mới để lấy email dựa trên tên tài khoản
     */
    public String getEmailByTenTK(String tenTK) {
        String sql = "SELECT email FROM NhanVien WHERE tenTK = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenTK.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy email của TK " + tenTK + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy hoặc lỗi
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
            // 🌟 SỬA SQL: Thêm cột email
            String sqlNV = "INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                pstmtNV.setString(11, nv.getEmail()); // 🌟 THÊM: Set email

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
            // 🌟 SỬA SQL: Thêm cột email
            String sqlNV = "UPDATE NhanVien SET hoTen=?, ngaySinh=?, gioiTinh=?, sdt=?, diaChi=?, luong=?, vaiTro=?, tenTK=?, email=? WHERE maNV=?";
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
                pstmtNV.setString(9, nv.getEmail()); // 🌟 THÊM: Set email
                pstmtNV.setString(10, nv.getManv());

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
    public List<NhanVien> searchNhanVienBySdt(String keyword) {
        List<NhanVien> ds = new ArrayList<>();
        // 🌟 SỬA SQL: Thêm cột email
        String sql = "SELECT manv, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, tenTK, email FROM NhanVien WHERE sdt LIKE ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Đặt tham số cho LIKE, %keyword% cho phép tìm kiếm bất kỳ đâu trong SĐT
            pstmt.setString(1, "%" + keyword.trim() + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapResultSetToNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            // Nên ném RuntimeException để xử lý ở tầng trên
            throw new RuntimeException("Lỗi truy vấn tìm kiếm NhanVien theo SĐT: " + e.getMessage(), e);
        }
        return ds;
    }
    public boolean suspendNhanVienAndAccount(String maNV, String tenTK, VaiTro vaiTro) {
        Connection conn = null;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra Vai trò: CHỈ TẠM NGƯNG NHANVIEN
            if (vaiTro != VaiTro.NHANVIEN) {
                throw new IllegalArgumentException("Chỉ có thể tạm ngưng nhân viên có Vai trò NHANVIEN.");
            }

            // 2. Vô hiệu hóa Tài khoản (TaiKhoan.trangThai = 0)
            String sqlUpdateTK = "UPDATE TaiKhoan SET trangThai = 0 WHERE tenTK = ?";
            try (PreparedStatement pstmtTK = conn.prepareStatement(sqlUpdateTK)) {
                pstmtTK.setString(1, tenTK);
                if (pstmtTK.executeUpdate() == 0) {
                    throw new SQLException("Vô hiệu hóa Tài khoản thất bại hoặc Tài khoản không tồn tại.");
                }
            }

            // 3. Cập nhật trạng thái (nếu có cột trangThai trong NhanVien, nếu không thì bỏ qua)
            // Nếu không có cột trạng thái trong NhanVien, bước này không cần thiết.

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
    public int getAccountStatus(String tenTK) {
        String sql = "SELECT trangThai FROM TaiKhoan WHERE tenTK = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenTK.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("trangThai");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy trạng thái TK " + tenTK + ": " + e.getMessage());
        }
        return -1; // Trả về -1 nếu lỗi hoặc không tìm thấy
    }
    public boolean activateNhanVienAccount(String tenTK) {
        String sqlUpdateTK = "UPDATE TaiKhoan SET trangThai = 1 WHERE tenTK = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmtTK = conn.prepareStatement(sqlUpdateTK)) {

            pstmtTK.setString(1, tenTK);
            return pstmtTK.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi kích hoạt lại TK " + tenTK + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}