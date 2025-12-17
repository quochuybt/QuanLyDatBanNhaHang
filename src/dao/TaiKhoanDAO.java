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

        String sql = "SELECT T.matKhau, T.trangThai, N.vaiTro, N.hoTen, N.maNV FROM TaiKhoan T " +
                "JOIN NhanVien N ON T.tenTK = N.tenTK " +
                "WHERE T.tenTK = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cleanTenTK);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbHashedPassword = rs.getString("matKhau").trim();

                    int trangThai = rs.getInt("trangThai");

                    if (inputHashedPassword.equals(dbHashedPassword)) {

                        if (trangThai == 0) {
                            Map<String, String> lockedInfo = new HashMap<>();
                            lockedInfo.put("status", "LOCKED");
                            return lockedInfo;
                        }

                        String vaiTro = rs.getString("vaiTro");
                        String hoTen = rs.getString("hoTen");
                        String maNV = rs.getString("maNV");

                        Map<String, String> userInfo = new HashMap<>();
                        userInfo.put("role", vaiTro);
                        userInfo.put("name", hoTen);
                        userInfo.put("maNV", maNV);
                        return userInfo;
                    }

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn CSDL khi đăng nhập", e);
        }

        return null;
    }

    public boolean updatePassword(String tenTK, String newPlainPassword) {
        String sqlUpdatePass = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
        String cleanTenTK = tenTK.trim();

        String hashedPass = "hashed_" + newPlainPassword.trim().toLowerCase().hashCode();

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement pstmtUpdatePass = conn.prepareStatement(sqlUpdatePass)) {

            pstmtUpdatePass.setString(1, hashedPass);
            pstmtUpdatePass.setString(2, cleanTenTK);

            int rowsAffected = pstmtUpdatePass.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}