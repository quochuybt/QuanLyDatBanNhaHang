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

    /**
     * Lấy tất cả ChiTietHoaDon (kèm Tên Món) theo maDon
     */
    public List<ChiTietHoaDon> getChiTietTheoMaDon(String maDon) {
        List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();

        // Câu lệnh SQL JOIN 2 bảng ChiTietHoaDon và MonAn
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

                    // Dùng constructor mới (có tenMon)
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
        // Giả định bảng ChiTietHoaDon có các cột: maDon, maMonAn, soLuong, donGia
        String sql = "INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES (?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ct.getMaDon());
            ps.setString(2, ct.getMaMon());
            ps.setInt(3, ct.getSoluong());
            ps.setFloat(4, ct.getDongia()); // Lưu đơn giá tại thời điểm thêm

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Lỗi này có thể xảy ra nếu maDon hoặc maMonAn không tồn tại trong bảng cha,
            // hoặc cặp (maDon, maMonAn) đã tồn tại (nếu có UNIQUE constraint)
            System.err.println("Lỗi ràng buộc khi thêm chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm chi tiết hóa đơn: " + e.getMessage());
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
            System.err.println("Lỗi khi xóa chi tiết hóa đơn (maDon=" + maDon + ", maMon=" + maMon + "): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean suaChiTiet(ChiTietHoaDon ct) {
        // Cập nhật số lượng. Đơn giá có thể không cần cập nhật nếu giá món không đổi.
        // Thành tiền sẽ tự tính lại.
        String sql = "UPDATE ChiTietHoaDon SET soLuong = ? WHERE maDon = ? AND maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ct.getSoluong());
            ps.setString(2, ct.getMaDon());
            ps.setString(3, ct.getMaMon());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi sửa chi tiết hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // --- HÀM MỚI CHO DASHBOARD ---
    /**
     * (MỚI) Lấy danh sách các món bán chạy nhất trong khoảng thời gian.
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param limit Số lượng món ăn hàng đầu cần lấy (ví dụ: 5)
     * @return Map<String, Integer> (Key: Tên món, Value: Tổng số lượng bán)
     */
    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        // Dùng LinkedHashMap để giữ thứ tự top bán chạy
        Map<String, Integer> topItems = new LinkedHashMap<>();

        // Câu SQL này JOIN 4 bảng:
        // 1. ChiTietHoaDon (ct) - để lấy soLuong
        // 2. MonAn (m) - để lấy tenMon
        // 3. DonDatMon (ddm) - để liên kết ct với hd
        // 4. HoaDon (hd) - để lọc theo trangThai = 'Đã thanh toán' và ngayLap
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
            System.err.println("SQL Error while fetching top selling items: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn món bán chạy", e);
        }
        return topItems;
    }


    // Trong ChiTietHoaDonDAO.java
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
    /**
     * [MỚI] Lấy top món ăn có số lượng bán thấp nhất (Top Bad Sellers)
     */
    public Map<String, Integer> getLeastSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Integer> result = new LinkedHashMap<>();

        // SỬA LỖI Ở DÒNG JOIN: ma.maMon -> ma.maMonAn
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
} // Kết thúc class ChiTietHoaDonDAO