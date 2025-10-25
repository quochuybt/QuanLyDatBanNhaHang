package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon; // Đảm bảo entity ChiTietHoaDon đúng

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonDAO {

    /**
     * Helper: Chuyển ResultSet thành đối tượng ChiTietHoaDon
     */
    private ChiTietHoaDon createChiTietFromResultSet(ResultSet rs) throws Exception {
        // Lấy đúng tên cột từ CSDL (từ câu INSERT của bạn là maDon, maMonAn, soLuong, donGia)
        String maDon = rs.getString("maDon");
        String maMonAn = rs.getString("maMonAn"); // Hoặc maMon nếu tên cột khác
        int soLuong = rs.getInt("soLuong");
        float donGia = rs.getFloat("donGia");

        // Giả sử constructor ChiTietHoaDon là (maMonAn, maDon, soLuong, donGia)
        // Nếu khác, hãy điều chỉnh cho phù hợp
        return new ChiTietHoaDon(maMonAn, maDon, soLuong, donGia);
    }

    /**
     * Lấy danh sách Chi Tiết Hóa Đơn theo Mã Đơn Đặt Món (maDon)
     * Kèm theo Tên Món Ăn để hiển thị dễ hơn.
     * @param maDon Mã đơn đặt món cần xem chi tiết.
     * @return Danh sách các chi tiết (bao gồm tên món).
     */
    public List<ChiTietHoaDon> getChiTietByMaDon(String maDon) {
        List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();
        // Câu SQL join với bảng MonAn để lấy tên món
         // Đảm bảo tên bảng và tên cột khớp CSDL: ChiTietHoaDon, MonAn, maMonAn, tenMon
        String sql = "SELECT ct.maDon, ct.maMonAn, ct.soLuong, ct.donGia " +
                     "FROM ChiTietHoaDon ct " +
                     "WHERE ct.maDon = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsChiTiet.add(createChiTietFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi đọc chi tiết hóa đơn cho mã đơn " + maDon + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi lấy chi tiết hóa đơn cho mã đơn " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return dsChiTiet;
    }
}