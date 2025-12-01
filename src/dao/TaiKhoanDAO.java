package dao;

import connectDB.SQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TaiKhoanDAO {

    public Map<String, String> checkLoginAndGetInfo(String tenTK, String plainPassword) throws RuntimeException {

        String cleanPassword = plainPassword.trim().toLowerCase();
        String cleanTenTK = tenTK.trim();
        String inputHashedPassword = "hashed_" + cleanPassword.hashCode();

        // 🌟 SỬA: Thêm N.maNV vào câu SELECT
        String sql = "SELECT T.matKhau, N.vaiTro, N.hoTen, N.maNV FROM TaiKhoan T " +
                "JOIN NhanVien N ON T.tenTK = N.tenTK " +
                "WHERE T.tenTK = ? AND T.trangThai = 1";

        try {
            Connection conn = SQLConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cleanTenTK);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String dbHashedPassword = rs.getString("matKhau").trim();
                        String vaiTro = rs.getString("vaiTro");
                        String hoTen = rs.getString("hoTen");
                        String maNV = rs.getString("maNV"); // 🌟 LẤY MÃ NV

                        // So sánh mật khẩu
                        if (inputHashedPassword.equals(dbHashedPassword)) {
                            // 🌟 SỬA: Tạo Map và trả về
                            Map<String, String> userInfo = new HashMap<>();
                            userInfo.put("role", vaiTro);
                            userInfo.put("name", hoTen);
                            userInfo.put("maNV", maNV); // 🌟 TRẢ VỀ MÃ NV
                            return userInfo; // Trả về Map chứa role, name, và maNV
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn CSDL khi đăng nhập", e);
        }

        return null; // Trả về null nếu thất bại
    }
}