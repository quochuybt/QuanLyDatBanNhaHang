package dao;

import connectDB.SQLConnection;
import entity.Ban;
import entity.TrangThaiBan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BanDAO {

    /**
     * Chuyển đổi chuỗi trạng thái từ CSDL (ví dụ: "Đang có khách")
     * sang kiểu Enum (TrangThaiBan.DANG_PHUC_VU).
     */
    private TrangThaiBan convertStringToTrangThai(String trangThaiDB) {
        if (trangThaiDB == null) {
            return TrangThaiBan.TRONG;
        }
        switch (trangThaiDB) {
            case "Đang có khách": // Giống trong SQL script
            case "Đang phục vụ": // Tên dùng trong logic GUI
                return TrangThaiBan.DANG_PHUC_VU;
            case "Đã đặt trước":
                return TrangThaiBan.DA_DAT_TRUOC;
            case "Trống":
            default:
                return TrangThaiBan.TRONG;
        }
    }

    /**
     * Lấy toàn bộ danh sách Bàn từ CSDL
     */
    public List<Ban> getAllBan() {
        List<Ban> dsBan = new ArrayList<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = SQLConnection.getConnection();
            String sql = "SELECT maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc FROM Ban";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String maBan = rs.getString("maBan");
                String tenBan = rs.getString("tenBan");
                int soGhe = rs.getInt("soGhe");
                String trangThaiStr = rs.getString("trangThai");
                Timestamp gioMoBanTS = rs.getTimestamp("gioMoBan");
                String khuVuc = rs.getString("khuVuc");

                // Chuyển đổi kiểu dữ liệu
                TrangThaiBan trangThai = convertStringToTrangThai(trangThaiStr);
                LocalDateTime gioMoBan = null;
                if (gioMoBanTS != null) {
                    gioMoBan = gioMoBanTS.toLocalDateTime();
                }

                // Sử dụng constructor mới (sẽ được thêm ở Bước 2)
                // để tạo đối tượng Ban từ dữ liệu DB
                Ban ban = new Ban(maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc);
                dsBan.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Đóng tài nguyên
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Không đóng 'con' nếu nó được quản lý bởi ConnectDB singleton
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dsBan;
    }

    /**
     * Lấy số thứ tự (phần số) lớn nhất của mã bàn (ví dụ: BAN10 -> 10)
     * Dùng để cập nhật lại bộ đếm static trong class Ban
     */
    public int getSoThuTuBanLonNhat() {
        int maxSoThuTu = 0;
        Connection con = SQLConnection.getConnection();
        // Câu lệnh này có thể cần điều chỉnh tùy theo hệ CSDL (VD: MSSQL, MySQL)
        // Đây là cho MSSQL (giống cú pháp T-SQL bạn cung cấp)
        String sql = "SELECT MAX(CAST(SUBSTRING(maBan, 4, LEN(maBan) - 3) AS INT)) FROM Ban WHERE maBan LIKE 'BAN[0-9]%'";
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                maxSoThuTu = rs.getInt(1); // Lấy kết quả từ cột đầu tiên
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return maxSoThuTu;
    }
}