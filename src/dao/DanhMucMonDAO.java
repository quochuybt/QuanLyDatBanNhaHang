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

    public boolean themDanhMuc(DanhMucMon dm) {
        String sql = "INSERT INTO DanhMucMon (maDM, tenDM, moTa) VALUES (?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Tự động sinh mã nếu chưa có
            if(dm.getMadm() == null || dm.getMadm().isEmpty()) {
                dm.setMadm(generateNewMaDM());
            }

            ps.setString(1, dm.getMadm());
            ps.setString(2, dm.getTendm());
            ps.setString(3, dm.getMota());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatDanhMuc(DanhMucMon dm) {
        String sql = "UPDATE DanhMucMon SET tenDM = ?, moTa = ? WHERE maDM = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dm.getTendm());
            ps.setString(2, dm.getMota());
            ps.setString(3, dm.getMadm());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoaDanhMuc(String maDM) {
        String sql = "DELETE FROM DanhMucMon WHERE maDM = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateNewMaDM() {
        String sql = "SELECT MAX(maDM) FROM DanhMucMon";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maxID = rs.getString(1);
                if (maxID != null) {
                    int num = Integer.parseInt(maxID.replace("DM", ""));
                    return String.format("DM%04d", num + 1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "DM0001";
    }
}