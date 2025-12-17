package dao;

import connectDB.SQLConnection;
import entity.CaLam;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CaLamDAO {

    public List<CaLam> getAllCaLam() {
        List<CaLam> dsCaLam = new ArrayList<>();
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc FROM CaLam ORDER BY gioBatDau";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String maCa = rs.getString("maCa");
                String tenCa = rs.getString("tenCa");
                LocalTime gioBatDau = rs.getTime("gioBatDau").toLocalTime();
                LocalTime gioKetThuc = rs.getTime("gioKetThuc").toLocalTime();
                dsCaLam.add(new CaLam(maCa, tenCa, gioBatDau, gioKetThuc));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsCaLam;
    }

}