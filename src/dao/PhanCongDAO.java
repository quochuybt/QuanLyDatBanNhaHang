package dao;

import connectDB.SQLConnection;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCong;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhanCongDAO {

    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        List<PhanCong> dsPhanCong = new ArrayList<>();
        String sql = "SELECT pc.ngayLam, c.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, nv.maNV, nv.hoTen " +
                "FROM PhanCongCa pc JOIN CaLam c ON pc.maCa = c.maCa JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.ngayLam BETWEEN ? AND ? ORDER BY pc.ngayLam, c.gioBatDau";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CaLam ca = new CaLam(rs.getString("maCa"), rs.getString("tenCa"), rs.getTime("gioBatDau").toLocalTime(), rs.getTime("gioKetThuc").toLocalTime());
                    NhanVien nv = new NhanVien();
                    nv.setManv(rs.getString("maNV"));
                    nv.setHoten(rs.getString("hoTen"));
                    LocalDate ngayLam = rs.getDate("ngayLam").toLocalDate();
                    PhanCong pc = new PhanCong(ca, nv, ngayLam);
                    dsPhanCong.add(pc);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách phân công: " + e.getMessage());
            e.printStackTrace();
        }
        return dsPhanCong;
    }
    public boolean themPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        String sql = "INSERT INTO PhanCongCa (maNV, maCa, ngayLam) VALUES (?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, maCa);
            ps.setDate(3, Date.valueOf(ngayLam));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm phân công: " + e.getMessage());
            return false;
        }
    }
    public boolean xoaPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        String sql = "DELETE FROM PhanCongCa WHERE maNV = ? AND maCa = ? AND ngayLam = ?";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, maCa);
            ps.setDate(3, Date.valueOf(ngayLam));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa phân công: " + e.getMessage());
            return false;
        }
    }


    public Map<String, Double> getTongGioLamChoTatCaNV() {
        Map<String, Double> tongGioLamMap = new HashMap<>();
        // Câu SQL tính tổng số phút làm việc của mỗi nhân viên
        // DATEDIFF(MINUTE, c.gioBatDau, c.gioKetThuc) tính số phút của 1 ca
        // SUM(...) tính tổng số phút
        // GROUP BY pc.maNV, nv.hoTen để tính riêng cho mỗi nhân viên
        String sql = "SELECT pc.maNV, SUM(DATEDIFF(MINUTE, c.gioBatDau, c.gioKetThuc)) AS TongSoPhut " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam c ON pc.maCa = c.maCa " +
                "GROUP BY pc.maNV";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maNV = rs.getString("maNV");
                int tongSoPhut = rs.getInt("TongSoPhut");
                // Chuyển tổng số phút sang tổng số giờ (dạng Double)
                double tongSoGio = tongSoPhut / 60.0;
                tongGioLamMap.put(maNV, tongSoGio);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng giờ làm: " + e.getMessage());
            e.printStackTrace();
            // Trả về map rỗng nếu có lỗi
            return new HashMap<>();
        }
        return tongGioLamMap;
    }
}