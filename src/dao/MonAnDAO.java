package dao;

import connectDB.SQLConnection;
import entity.MonAn;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonAnDAO {
    private MonAn createMonAnFromResultSet(ResultSet rs) throws SQLException {
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

    public String getNextMaMonAn() {
        String sql = "SELECT MAX(maMonAn) FROM MonAn";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maxID = rs.getString(1);
                if (maxID != null && maxID.length() > 2) {
                    try {
                        int number = Integer.parseInt(maxID.substring(2));
                        return "MA" + (number + 1);
                    } catch (NumberFormatException e) {
                        return "MA100";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "MA100";
    }

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
    public List<MonAn> getMonAnDangKinhDoanh() {
        List<MonAn> dsMonAn = new ArrayList<>();

        String sql = "SELECT * FROM MonAn WHERE trangThai = N'Còn'";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dsMonAn.add(createMonAnFromResultSet(rs));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return dsMonAn;
    }

    public String getTenMonByMa(String maMonAn) {
        String tenMon = maMonAn;
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

    public boolean themMonAn(MonAn m) {
        String sql = "INSERT INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

}