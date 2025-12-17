package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // Thêm import này
import java.sql.Timestamp;  // Thêm import này
import java.time.LocalDate; // Thêm import này
import java.util.ArrayList;
import java.util.LinkedHashMap; // Thêm import này
import java.util.List;
import java.util.Map; // Thêm import này

public class ChiTietHoaDonDAO {

    public List<ChiTietHoaDon> getChiTietTheoMaDon(String maDon) {
        List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

        String sql = "SELECT ct.maDon, ct.maMonAn, m.tenMon, ct.soLuong, ct.donGia " +
                "FROM ChiTietHoaDon ct " +
                "JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                "WHERE ct.maDon = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maMon = rs.getString("maMonAn");
                    String tenMon = rs.getString("tenMon");
                    int soLuong = rs.getInt("soLuong");
                    float donGia = rs.getFloat("donGia");

                    ChiTietHoaDon ct = new ChiTietHoaDon(maDon, maMon, tenMon, soLuong, donGia);
                    dsChiTiet.add(ct);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsChiTiet;
    }
    public boolean themChiTiet(ChiTietHoaDon ct) {
        String sql = "INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES (?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ct.getMaDon());
            ps.setString(2, ct.getMaMon());
            ps.setInt(3, ct.getSoluong());
            ps.setFloat(4, ct.getDongia()); // Lưu đơn giá tại thời điểm thêm

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean xoaChiTiet(String maDon, String maMon) {
        String sql = "DELETE FROM ChiTietHoaDon WHERE maDon = ? AND maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);
            ps.setString(2, maMon);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean suaChiTiet(ChiTietHoaDon ct) {
        String sql = "UPDATE ChiTietHoaDon SET soLuong = ? WHERE maDon = ? AND maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ct.getSoluong());
            ps.setString(2, ct.getMaDon());
            ps.setString(3, ct.getMaMon());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Integer> topItems = new LinkedHashMap<>();

        String sqlTop = "SELECT TOP (?) m.tenMon, SUM(ct.soLuong) AS TongSoLuong " +
                "FROM ChiTietHoaDon ct " +
                "JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                "JOIN DonDatMon ddm ON ct.maDon = ddm.maDon " +
                "JOIN HoaDon hd ON ddm.maDon = hd.maDon " +
                "WHERE hd.trangThai = N'Đã thanh toán' " +
                "AND hd.ngayLap >= ? AND hd.ngayLap < ? " +
                "GROUP BY m.tenMon " +
                "ORDER BY TongSoLuong DESC";


        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlTop)) {

            ps.setInt(1, limit); // Tham số cho TOP (?)
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenMon = rs.getString("tenMon");
                    int tongSoLuong = rs.getInt("TongSoLuong");
                    topItems.put(tenMon, tongSoLuong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn món bán chạy", e);
        }
        return topItems;
    }


    public java.util.List<String> getTopMonBanChayTrongNgay() {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT TOP 3 m.tenMon, SUM(ct.soLuong) as SL " +
                "FROM ChiTietHoaDon ct " +
                "JOIN HoaDon hd ON ct.maDon = hd.maDon " +
                "JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                "WHERE CAST(hd.ngayLap AS DATE) = CAST(GETDATE() AS DATE) " +
                "GROUP BY m.tenMon ORDER BY SL DESC";
        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                list.add("#" + rank + " " + rs.getString("tenMon") + " (" + rs.getInt("SL") + " suất)");
                rank++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (list.isEmpty()) list.add("Chưa có dữ liệu hôm nay");
        return list;
    }

    public Map<String, Integer> getLeastSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Integer> result = new LinkedHashMap<>();

        String sql = "SELECT TOP (?) ma.tenMon, SUM(ct.soLuong) as SoLuongBan " +
                "FROM ChiTietHoaDon ct " +
                "JOIN MonAn ma ON ct.maMonAn = ma.maMonAn " + // <--- SỬA TẠI ĐÂY
                "JOIN HoaDon hd ON ct.maDon = hd.maDon " +
                "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? " +
                "AND hd.trangThai = N'Đã thanh toán' " +
                "GROUP BY ma.tenMon " +
                "ORDER BY SoLuongBan ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("tenMon"), rs.getInt("SoLuongBan"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}