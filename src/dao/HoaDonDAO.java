package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import entity.HoaDon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        // Dùng Constructor mới của HoaDon (đã bỏ maBan)
        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThucThanhToan, maDon, maNV, maKM);
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
// --- ADDED REQUIRED FOREIGN KEYS ---
            // You need to get these values from the HoaDon object
            // For example:
            // ps.setString(7, hd.getMaNV()); // Replace with your actual getter
            // ps.setString(8, hd.getMaKM()); // Replace with your actual getter, handle nulls
            // ps.setString(9, hd.getMaDon()); // Replace with your actual getter

            // Placeholder - Replace with actual FK getters from your HoaDon entity
            ps.setNull(7, java.sql.Types.NVARCHAR); // Placeholder for maNV
            ps.setNull(8, java.sql.Types.NVARCHAR); // Placeholder for maKM
            ps.setNull(9, java.sql.Types.NVARCHAR); // Placeholder for maDon

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
}