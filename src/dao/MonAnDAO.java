package dao;

import connectDB.SQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import entity.MonAn;

public class MonAnDAO {

    private MonAn createMonAnFromResultSet(ResultSet rs) throws Exception {
        String maMonAn = rs.getString("maMonAn");
        String tenMon = rs.getString("tenMon");
        String moTa = rs.getString("moTa");
        float donGia = rs.getFloat("donGia");
        String donViTinh = rs.getString("donViTinh");
        String trangThai = rs.getString("trangThai");
        String hinhAnh = rs.getString("hinhAnh");
        String maDM = rs.getString("maDM"); // Lấy mã danh mục

        // ---- QUAN TRỌNG ----
        // Kiểm tra lại constructor trong entity/MonAn.java của bạn.
        // Code này giả định bạn có constructor 7 tham số KHÔNG BAO GỒM maDM.
        // Nếu constructor của bạn khác (ví dụ: cần maDM), hãy sửa lại dòng dưới đây.
        MonAn monAn = new MonAn(maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM);

        // Nếu MonAn có hàm setMaDM, bạn có thể dùng nó ở đây:
        // monAn.setMaDM(maDM); // Giả sử có hàm setMaDM

        return monAn;
    }

    public List<MonAn> getAllMonAn() {
        List<MonAn> dsMonAn = new ArrayList<>();
        // Lấy tất cả các cột cần thiết, bao gồm cả maDM
        String sql = "SELECT maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM FROM MonAn";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    dsMonAn.add(createMonAnFromResultSet(rs));
                } catch (Exception e) {
                    System.err.println("Lỗi khi tạo đối tượng MonAn từ ResultSet: " + e.getMessage());
                    // Bỏ qua món ăn bị lỗi hoặc xử lý khác nếu cần
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách món ăn: " + e.getMessage());
            e.printStackTrace(); // In chi tiết lỗi ra console
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
        } catch (Exception e) {
            System.err.println("Lỗi khi lọc món ăn theo danh mục " + maDM + ": " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }
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