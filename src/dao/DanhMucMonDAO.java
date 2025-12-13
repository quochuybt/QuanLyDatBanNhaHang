package dao;

import connectDB.SQLConnection;
import entity.DanhMucMon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonDAO {
    public List<DanhMucMon> getAllDanhMuc() {
        List<DanhMucMon> list = new ArrayList<>();
        String sql = "SELECT * FROM DanhMucMon";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DanhMucMon(
                        rs.getString("madm"),
                        rs.getString("tendm"),
                        rs.getString("mota")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}