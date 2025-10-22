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
        int tienThoi = rs.getInt("tienThoi"); // Lấy tiền thối từ CSDL

        // ❌ KHÔNG CÒN LẤY CÁC TRƯỜNG KHÓA NGOẠI NỮA: maKH, maNV, maBan

        // Sử dụng Constructor mới (Hoặc Constructor cũ và setTienThoi)
        HoaDon hd = new HoaDon(ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, tienThoi);
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
        String sql = "SELECT maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, tienThoi FROM HoaDon ORDER BY ngayLap DESC";

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
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, tienThoi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            ps.setFloat(3, hd.getTongTien());
            ps.setString(4, hd.getTrangThai());
            ps.setString(5, hd.getHinhThucThanhToan());
            ps.setFloat(6, hd.getTienKhachDua());
            ps.setInt(7, hd.getTienThoi()); // THÊM tienThoi

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