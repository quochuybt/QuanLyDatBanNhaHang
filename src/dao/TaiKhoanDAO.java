package dao; // Hoặc package DAO của bạn

import connectDB.SQLConnection; // <-- Dùng lớp kết nối của bạn
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap; // <-- Thêm import này
import java.util.Map;    // <-- Thêm import này

public class TaiKhoanDAO {

    public Map<String, String> checkLoginAndGetInfo(String tenTK, String plainPassword) throws RuntimeException {

        String cleanPassword = plainPassword.trim().toLowerCase();
        String cleanTenTK = tenTK.trim();
        String inputHashedPassword = "hashed_" + cleanPassword.hashCode();

        // --- SỬA 2: Thêm N.hoTen vào câu SELECT ---
        String sql = "SELECT T.matKhau, N.vaiTro, N.hoTen FROM TaiKhoan T " +
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
                        String hoTen = rs.getString("hoTen"); // <-- Lấy họ tên

                        // So sánh mật khẩu
                        if (inputHashedPassword.equals(dbHashedPassword)) {
                            // --- SỬA 3: Tạo Map và trả về ---
                            Map<String, String> userInfo = new HashMap<>();
                            userInfo.put("role", vaiTro);
                            userInfo.put("name", hoTen);
                            return userInfo; // Trả về Map chứa role và name
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