package dao;

import connectDB.SQLConnection;
import entity.DanhMucMon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonDAO {

    /**
     * Lấy tất cả danh mục món ăn từ CSDL.
     * @return List<DanhMucMon>
     */
    public List<DanhMucMon> getAllDanhMuc() {
        List<DanhMucMon> dsDanhMuc = new ArrayList<>();
        String sql = "SELECT madm, tendm, mota, maNV FROM DanhMucMon"; // Lấy tất cả các cột

        // Sửa lỗi kết nối Singleton
        Connection conn = SQLConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    String maDM = rs.getString("madm");
                    String tenDM = rs.getString("tendm");
                    String moTa = rs.getString("mota");
                    // String maNV = rs.getString("maNV"); // Lấy maNV nếu cần

                    // Giả sử DanhMucMon có constructor (ma, ten, mota)
                    // Hoặc (ma, ten, mota, maNV)
                    DanhMucMon dm = new DanhMucMon(maDM, tenDM, moTa);
                    dsDanhMuc.add(dm);

                } catch (Exception e) {
                    System.err.println("Lỗi khi tạo đối tượng DanhMucMon: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        // Không đóng connection
        return dsDanhMuc;
    }

    // (Thêm các hàm thêm, xóa, sửa DanhMucMon nếu cần)
}