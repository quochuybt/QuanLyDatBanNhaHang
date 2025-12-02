package dao;

import connectDB.SQLConnection;
import entity.MonAn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

public class MonAnDAO {

    // ... (Hàm createMonAnFromResultSet và các hàm get hiện có) ...
    private MonAn createMonAnFromResultSet(ResultSet rs) throws Exception {
        String maMonAn = rs.getString("maMonAn");
        String tenMon = rs.getString("tenMon");
        String moTa = rs.getString("moTa");
        float donGia = rs.getFloat("donGia");
        String donViTinh = rs.getString("donViTinh");
        String trangThai = rs.getString("trangThai");
        String hinhAnh = rs.getString("hinhAnh");
        String maDM = rs.getString("maDM"); // Lấy mã danh mục

        // Tạo đối tượng MonAn - Đảm bảo constructor entity/MonAn.java khớp
        MonAn monAn = new MonAn(maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM);
        return monAn;
    }

    public List<MonAn> getAllMonAn() {
        List<MonAn> dsMonAn = new ArrayList<>();
        String sql = "SELECT maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM FROM MonAn";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    dsMonAn.add(createMonAnFromResultSet(rs));
                } catch (Exception e) {
                    System.err.println("Lỗi khi tạo đối tượng MonAn từ ResultSet (getAll): " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi tải danh sách món ăn: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải danh sách món ăn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }

    public List<MonAn> getMonAnTheoDanhMuc(String maDM) {
        List<MonAn> dsMonAn = new ArrayList<>();
        String sql = "SELECT * FROM MonAn WHERE maDM = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsMonAn.add(createMonAnFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo đối tượng MonAn từ ResultSet (lọc theo DM): " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lọc món ăn theo danh mục " + maDM + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi lọc món ăn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }

    public String getTenMonByMa(String maMonAn) {
        String tenMon = maMonAn; // Giá trị mặc định
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
            System.err.println("Lỗi SQL khi lấy tên món ăn cho mã " + maMonAn + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi lấy tên món: " + e.getMessage());
        }
        return tenMon;
    }

    // --- CÁC HÀM MỚI CHO DASHBOARD ---

    public Map<String, Integer> getInventoryCountByCategory() {
        Map<String, Integer> countByCategory = new HashMap<>();
        // Đảm bảo tên bảng và cột đúng: DanhMucMon(madm, tendm), MonAn(maMonAn, maDM)
        String sql = "SELECT dm.tendm, COUNT(m.maMonAn) AS SoLuong " +
                "FROM MonAn m JOIN DanhMucMon dm ON m.maDM = dm.madm " +
                "GROUP BY dm.tendm";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String tenDM = rs.getString("tendm");
                int soLuong = rs.getInt("SoLuong");
                // Kiểm tra null cho tên danh mục nếu cần
                if (tenDM != null) {
                    countByCategory.put(tenDM, soLuong);
                } else {
                    countByCategory.put("Chưa phân loại", countByCategory.getOrDefault("Chưa phân loại", 0) + soLuong);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting items by category: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn số lượng món theo danh mục", e);
        } catch (Exception e) {
            System.err.println("Unexpected error counting items by category: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi đếm món ăn", e);
        }
        return countByCategory;
    }

    public int getLowStockCount() {
        int count = 0;
        // Logic đếm món sắp hết - Ví dụ: đếm các món có trạng thái là 'Hết món'
        String sql = "SELECT COUNT(maMonAn) FROM MonAn WHERE trangThai = N'Hết món'";
        // Hoặc nếu có cột số lượng:
        // String sql = "SELECT COUNT(maMonAn) FROM MonAn WHERE SoLuongConLai < 10"; // Giả sử ngưỡng là 10

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting low stock items: " + e.getMessage());
            e.printStackTrace();
            // Trả về 0 nếu có lỗi, không ném exception để dashboard không bị crash hoàn toàn
        } catch (Exception e) {
            System.err.println("Unexpected error counting low stock items: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }

    public boolean themMonAn(MonAn m) {
        String sql = "INSERT INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
            System.err.println("Lỗi thêm món ăn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin món ăn
     */
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
            System.err.println("Lỗi cập nhật món ăn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa món ăn
     */
    public boolean xoaMonAn(String maMon) {
        String sql = "DELETE FROM MonAn WHERE maMonAn = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maMon);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa món ăn: " + e.getMessage());
            return false;
        }
    }
} // Kết thúc class MonAnDAO