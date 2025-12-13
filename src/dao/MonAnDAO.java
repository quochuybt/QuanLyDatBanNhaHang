package dao;

import connectDB.SQLConnection;
import entity.MonAn;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonAnDAO {

    // --- 1. HÀM HỖ TRỢ (PRIVATE) ---

    /**
     * Map dữ liệu từ ResultSet sang Object MonAn để tái sử dụng, tránh viết lặp lại code.
     */
    private MonAn createMonAnFromResultSet(ResultSet rs) throws SQLException {
        // Thứ tự constructor: ma, ten, mota, dongia, dvt, trangthai, hinhanh, madm
        return new MonAn(
                rs.getString("maMonAn"),
                rs.getString("tenMon"),
                rs.getString("moTa"),
                rs.getFloat("donGia"),
                rs.getString("donViTinh"),
                rs.getString("trangThai"),
                rs.getString("hinhAnh"),
                rs.getString("maDM")
        );
    }

    // --- 2. HÀM SINH MÃ TỰ ĐỘNG (QUAN TRỌNG) ---
    /**
     * Lấy mã lớn nhất trong DB (VD: MA105) và trả về mã kế tiếp (MA106).
     * Thay thế cho biến static nextId để tránh trùng lặp khi tắt app.
     */
    public String getNextMaMonAn() {
        String sql = "SELECT MAX(maMonAn) FROM MonAn";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maxID = rs.getString(1); // VD: "MA105"
                if (maxID != null && maxID.length() > 2) {
                    try {
                        int number = Integer.parseInt(maxID.substring(2));
                        return "MA" + (number + 1);
                    } catch (NumberFormatException e) {
                        return "MA100"; // Fallback nếu mã cũ lỗi format
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "MA100"; // Mặc định nếu bảng rỗng
    }

    // --- 3. CÁC HÀM GET DỮ LIỆU ---

    public List<MonAn> getAllMonAn() {
        List<MonAn> dsMonAn = new ArrayList<>();
        String sql = "SELECT * FROM MonAn";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dsMonAn.add(createMonAnFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsMonAn;
    }

    /**
     * KHÔI PHỤC: Lấy danh sách món theo mã danh mục
     */
    public List<MonAn> getMonAnTheoDanhMuc(String maDM) {
        List<MonAn> dsMonAn = new ArrayList<>();
        String sql = "SELECT * FROM MonAn WHERE maDM = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsMonAn.add(createMonAnFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc món theo danh mục: " + e.getMessage());
        }
        return dsMonAn;
    }

    /**
     * KHÔI PHỤC: Lấy tên món ăn dựa vào mã
     */
    public String getTenMonByMa(String maMonAn) {
        String tenMon = maMonAn; // Giá trị mặc định nếu không tìm thấy
        String sql = "SELECT tenMon FROM MonAn WHERE maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMonAn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenMon = rs.getString("tenMon");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenMon;
    }

    /**
     * KHÔI PHỤC: Lấy đơn giá dựa vào mã
     */
    public float getDonGiaByMa(String maMon) {
        float donGia = 0;
        String sql = "SELECT donGia FROM MonAn WHERE maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    donGia = rs.getFloat("donGia");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return donGia;
    }

    // --- 4. CÁC HÀM THÊM / XÓA / SỬA ---

    public boolean themMonAn(MonAn m) {
        String sql = "INSERT INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Logic quan trọng: Nếu mã chưa có, tự sinh mã mới từ DB
            if (m.getMaMonAn() == null || m.getMaMonAn().isEmpty() || m.getMaMonAn().equals("Tự động tạo")) {
                m.setMaMonAn(getNextMaMonAn());
            }

            ps.setString(1, m.getMaMonAn());
            ps.setString(2, m.getTenMon());
            ps.setString(3, m.getMota());
            ps.setFloat(4, m.getDonGia());
            ps.setString(5, m.getDonViTinh());
            ps.setString(6, m.getTrangThai());
            ps.setString(7, m.getHinhAnh());
            ps.setString(8, m.getMaDM());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm món: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatMonAn(MonAn m) {
        String sql = "UPDATE MonAn SET tenMon=?, moTa=?, donGia=?, donViTinh=?, trangThai=?, hinhAnh=?, maDM=? " +
                "WHERE maMonAn=?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getTenMon());
            ps.setString(2, m.getMota());
            ps.setFloat(3, m.getDonGia());
            ps.setString(4, m.getDonViTinh());
            ps.setString(5, m.getTrangThai());
            ps.setString(6, m.getHinhAnh());
            ps.setString(7, m.getMaDM());
            ps.setString(8, m.getMaMonAn());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật món: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaMonAn(String maMon) {
        String sql = "DELETE FROM MonAn WHERE maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // --- 5. CÁC HÀM DASHBOARD / THỐNG KÊ (KHÔI PHỤC) ---

    public Map<String, Integer> getInventoryCountByCategory() {
        Map<String, Integer> countByCategory = new HashMap<>();
        String sql = "SELECT dm.tendm, COUNT(m.maMonAn) AS SoLuong " +
                "FROM MonAn m JOIN DanhMucMon dm ON m.maDM = dm.madm " +
                "GROUP BY dm.tendm";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tenDM = rs.getString("tendm");
                int soLuong = rs.getInt("SoLuong");
                if (tenDM != null) {
                    countByCategory.put(tenDM, soLuong);
                } else {
                    countByCategory.put("Chưa phân loại", countByCategory.getOrDefault("Chưa phân loại", 0) + soLuong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countByCategory;
    }

    public int getLowStockCount() {
        int count = 0;
        String sql = "SELECT COUNT(maMonAn) FROM MonAn WHERE trangThai = N'Hết món'";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

} // End Class