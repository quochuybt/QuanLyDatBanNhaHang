package dao;

import connectDB.SQLConnection;
import entity.Ban;
import entity.TrangThaiBan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap; // Thêm import này
import java.util.List;
import java.util.Map; // Thêm import này

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
    public boolean updateBan(Ban ban) {
        String sql = "UPDATE Ban SET tenBan = ?, soGhe = ?, trangThai = ?, gioMoBan = ?, khuVuc = ? WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ban.getTenBan());
            ps.setInt(2, ban.getSoGhe());

            // Chuyển Enum trạng thái thành String lưu vào DB
            String trangThaiDB;
            switch (ban.getTrangThai()) {
                case DANG_PHUC_VU:
                    trangThaiDB = "Đang có khách"; // Hoặc "Đang phục vụ" tùy CSDL
                    break;
                case DA_DAT_TRUOC:
                    trangThaiDB = "Đã đặt trước";
                    break;
                case TRONG:
                default:
                    trangThaiDB = "Trống";
                    break;
            }
            ps.setString(3, trangThaiDB);

            // Xử lý gioMoBan (có thể null)
            if (ban.getGioMoBan() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(ban.getGioMoBan()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            ps.setString(5, ban.getKhuVuc());
            ps.setString(6, ban.getMaBan()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Ban> getDanhSachBanTrong() {
        List<Ban> dsBanTrong = new ArrayList<>();
        String sql = "SELECT * FROM Ban WHERE trangThai = N'Trống' ORDER BY maBan"; // Lọc theo trạng thái 'Trống'

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maBan = rs.getString("maBan");
                String tenBan = rs.getString("tenBan");
                int soGhe = rs.getInt("soGhe");
                // String trangThaiStr = rs.getString("trangThai"); // Biết chắc là Trống rồi
                Timestamp gioMoBanTS = rs.getTimestamp("gioMoBan");
                String khuVuc = rs.getString("khuVuc");

                LocalDateTime gioMoBan = null; // Bàn trống thì giờ mở = null
                // Không cần kiểm tra gioMoBanTS vì bàn trống thường là null

                // Dùng constructor của Ban
                Ban ban = new Ban(maBan, tenBan, soGhe, TrangThaiBan.TRONG, gioMoBan, khuVuc);
                dsBanTrong.add(ban);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsBanTrong;
    }
    public String getTenBanByMa(String maBan) {
        String tenBan = maBan; // Giá trị mặc định
        String sql = "SELECT tenBan FROM Ban WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenBan = rs.getString("tenBan");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tenBan;
    }
    public Ban getBanByMa(String maBan) {
        Ban ban = null;
        String sql = "SELECT * FROM Ban WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenBan = rs.getString("tenBan");
                    int soGhe = rs.getInt("soGhe");
                    String trangThaiStr = rs.getString("trangThai");
                    Timestamp gioMoBanTS = rs.getTimestamp("gioMoBan");
                    String khuVuc = rs.getString("khuVuc");

                    TrangThaiBan trangThai = convertStringToTrangThai(trangThaiStr);
                    LocalDateTime gioMoBan = (gioMoBanTS != null) ? gioMoBanTS.toLocalDateTime() : null;

                    ban = new Ban(maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ban;
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

    // --- HÀM MỚI CHO DASHBOARD ---
    /**
     * (MỚI) Đếm số lượng bàn theo từng trạng thái (real-time).
     * @return Map<String, Integer> (Key: Tên trạng thái, Value: Số lượng)
     */
    public Map<String, Integer> getTableStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        // Khởi tạo các giá trị mặc định dựa trên CSDL của bạn
        counts.put("Trống", 0);
        counts.put("Đang có khách", 0);
        counts.put("Đã đặt trước", 0);

        String sql = "SELECT trangThai, COUNT(maBan) AS SoLuong FROM Ban GROUP BY trangThai";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String trangThai = rs.getString("trangThai");
                int soLuong = rs.getInt("SoLuong");
                if (trangThai != null) {
                    // Ghi đè giá trị 0 nếu tìm thấy
                    // Cần đảm bảo key khớp chính xác với CSDL (ví dụ: "Đang có khách")
                    counts.put(trangThai, soLuong);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while counting table status: " + e.getMessage());
            e.printStackTrace();
            // Ném lỗi để báo cho SwingWorker biết
            throw new RuntimeException("Lỗi truy vấn trạng thái bàn", e);
        }
        return counts;
    }

} // Kết thúc class BanDAO