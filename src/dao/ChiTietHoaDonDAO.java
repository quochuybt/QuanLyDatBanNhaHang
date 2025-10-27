package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
}