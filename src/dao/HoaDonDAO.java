package dao;

import connectDB.SQLConnection;
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

    /**
     * Chuyển ResultSet thành đối tượng HoaDon (ĐÃ LOẠI BỎ KHÓA NGOẠI).
     */
    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");

        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        float tongTien = rs.getFloat("tongTien");
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");
        float tienKhachDua = rs.getFloat("tienKhachDua");

        // ❌ KHÔNG CÒN LẤY CÁC TRƯỜNG KHÓA NGOẠI NỮA: maKH, maNV, maBan

        // Sử dụng Constructor mới (Hoặc Constructor cũ và setTienThoi)
        HoaDon hd = new HoaDon(ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua);
        hd.setMaHD(maHD);

        return hd;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SELECT] - Lấy toàn bộ danh sách hóa đơn từ CSDL.
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        // Cập nhật câu lệnh SQL: BỎ maKH, maNV, maBan, THÊM tienThoi
        String sql = "SELECT maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua FROM HoaDon ORDER BY ngayLap DESC";

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
        String sql = "SELECT maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua " +
                "FROM HoaDon WHERE maHD LIKE ? ORDER BY ngayLap DESC";

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