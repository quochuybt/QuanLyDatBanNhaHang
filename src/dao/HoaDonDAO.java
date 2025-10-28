package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.sql.Types;

public class HoaDonDAO {
    private ChiTietHoaDonDAO chiTietDAO;
    public HoaDonDAO() {
        this.chiTietDAO = new ChiTietHoaDonDAO();
    }
    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        HoaDon hoaDon = null;

        // --- SỬA CÂU SQL ---
        // Liên kết HoaDon với DonDatMon (để lấy maBan)
        String sql = "SELECT hd.* FROM HoaDon hd " +
                "JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "WHERE ddm.maBan = ? AND hd.trangThai = N'Chưa thanh toán'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Lấy thông tin Hóa Đơn
                    // (Hàm createHoaDonFromResultSet sẽ được sửa ở bước 2.2)
                    hoaDon = createHoaDonFromResultSet(rs);

                    // 2. Lấy thông tin Chi Tiết Hóa Đơn
                    List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(hoaDon.getMaDon());

                    // 3. Gán chi tiết vào hóa đơn
                    hoaDon.setDsChiTiet(dsChiTiet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hoaDon; // Trả về null nếu không tìm thấy
    }
    // NOTE SỬA: Thêm hàm cập nhật maNV khi thanh toán
    public boolean capNhatNhanVien(String maHD, String maNV) {
        String sql = "UPDATE HoaDon SET maNV = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật NV cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean thanhToanHoaDon(String maHD, float tienKhachDua, String hinhThucThanhToan) {
        String sql = "UPDATE HoaDon SET trangThai = N'Đã thanh toán', tienKhachDua = ?, hinhThucThanhToan = ? " +
                "WHERE maHD = ? AND trangThai = N'Chưa thanh toán'"; // Chỉ cập nhật HĐ chưa thanh toán
        Connection conn = null; // Khai báo ngoài try để dùng cho transaction (nếu cần)
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = SQLConnection.getConnection();
            // --- Bắt đầu Transaction (Tùy chọn nhưng nên có nếu cập nhật nhiều bảng) ---
            // conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql);
            ps.setFloat(1, tienKhachDua);
            ps.setString(2, hinhThucThanhToan);
            ps.setString(3, maHD);

            int rowsAffected = ps.executeUpdate();
            success = (rowsAffected > 0);

            // --- Kết thúc Transaction (nếu dùng) ---
            // if (success) {
            //     conn.commit();
            // } else {
            //     conn.rollback();
            // }

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thanh toán hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
            // --- Rollback nếu lỗi (nếu dùng transaction) ---
            // try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            success = false;
        } finally {
            // Đóng PreparedStatement
            try { if (ps != null) ps.close(); } catch (SQLException ex) {}
            // --- Reset AutoCommit và đóng Connection (nếu dùng transaction) ---
            // try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) {}
            // Không đóng connection nếu nó được quản lý bởi Singleton
        }
        return success;
    }
    public boolean capNhatTongTien(String maHD, float tongTienMoi) {
        // Giả sử cột tổng tiền trong bảng HoaDon tên là tongTien
        String sql = "UPDATE HoaDon SET tongTien = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, tongTienMoi);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật tổng tiền cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Chuyển ResultSet thành đối tượng HoaDon (ĐÃ LOẠI BỎ KHÓA NGOẠI).
     */
    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");

        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");

        // --- CÁC CỘT NÀY TỒN TẠI TRONG HOADON (ĐÚNG) ---
        String maDon = rs.getString("maDon");
        String maNV = rs.getString("maNV");
        String maKM = rs.getString("maKM");

        float tongTien = rs.getFloat("tongTien");

        // 🌟 BỔ SUNG: LẤY TIỀN KHÁCH ĐƯA TỪ CSDL
        float tienKhachDua = rs.getFloat("tienKhachDua");

        // Dùng Constructor mới của HoaDon (đã bỏ maBan)
        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThucThanhToan, maDon, maNV, maKM);

        // 🌟 BỔ SUNG: GÁN GIÁ TRỊ VỪA LẤY
        hd.setTienKhachDua(tienKhachDua);
        hd.setTongTienTuDB(tongTien);
        return hd;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SELECT] - Lấy toàn bộ danh sách hóa đơn từ CSDL.
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        // Cập nhật câu lệnh SQL: BỎ maKH, maNV, maBan, THÊM tienThoi
        String sql = "SELECT * FROM HoaDon ORDER BY ngayLap DESC";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dsHoaDon.add(createHoaDonFromResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsHoaDon;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [INSERT] - Thêm một hóa đơn mới vào CSDL.
     */
    public boolean themHoaDon(HoaDon hd) {
        // Cập nhật câu lệnh SQL: BỎ maKH, maNV, maBan, THÊM tienThoi
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua,maNV, maKM, maDon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            ps.setFloat(3, hd.getTongTien());
            ps.setString(4, hd.getTrangThai());
            ps.setString(5, hd.getHinhThucThanhToan());
            ps.setFloat(6, hd.getTienKhachDua());
            ps.setString(7, hd.getMaNV());
            if (hd.getMaKM() != null) {
                ps.setString(8, hd.getMaKM());
            } else {
                ps.setNull(8, java.sql.Types.NVARCHAR); // Nếu maKM là null
            } // Placeholder for maKM
            ps.setString(9, hd.getMaDon()); // Placeholder for maDon

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi ràng buộc: Mã HD đã tồn tại.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SEARCH] - Tìm kiếm hóa đơn theo Mã HD.
     */
    public List<HoaDon> timHoaDon(String tuKhoa) {
        List<HoaDon> dsKetQua = new ArrayList<>();
        // Chỉ tìm kiếm theo Mã HD (Không còn Mã NV/Bàn)
        String sql = "SELECT * FROM HoaDon WHERE maHD LIKE ? ORDER BY ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsKetQua.add(createHoaDonFromResultSet(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKetQua;
    }
    public HoaDon getHoaDonTheoMaDon(String maDon) {
        HoaDon hoaDon = null;
        // Tìm hóa đơn có maDon khớp
        String sql = "SELECT * FROM HoaDon WHERE maDon = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Dùng lại hàm helper để tạo object
                    hoaDon = createHoaDonFromResultSet(rs);

                    // Lấy luôn chi tiết hóa đơn nếu có (tùy chọn, nhưng hữu ích)
                    // Vì hóa đơn mới tạo thường chưa có chi tiết ngay
                    // List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(hoaDon.getMaDon());
                    // hoaDon.setDsChiTiet(dsChiTiet);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm hóa đơn theo mã đơn " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return hoaDon; // Trả về null nếu không tìm thấy
    }
}