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
}