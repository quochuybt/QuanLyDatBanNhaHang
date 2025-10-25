package dao;

import connectDB.SQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MonAnDAO {

    /**
     * Lấy tên món ăn dựa vào Mã Món Ăn.
     * @param maMonAn Mã món ăn cần tìm tên.
     * @return Tên món ăn, hoặc mã món nếu không tìm thấy tên.
     */
    public String getTenMonByMa(String maMonAn) {
        String tenMon = maMonAn; // Giá trị mặc định nếu không tìm thấy
        String sql = "SELECT tenMon FROM MonAn WHERE maMonAn = ?"; // Đảm bảo tên bảng/cột đúng

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maMonAn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenMon = rs.getString("tenMon");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên món ăn cho mã " + maMonAn + ": " + e.getMessage());
            // Không ném lỗi, chỉ in ra console và trả về mã món
        }
        return tenMon;
    }
}