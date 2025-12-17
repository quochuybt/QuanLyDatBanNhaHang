package dao;

import connectDB.SQLConnection;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCong;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class PhanCongDAO {

    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        List<PhanCong> dsPhanCong = new ArrayList<>();
        String sql = "SELECT pc.ngayLam, c.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, nv.maNV, nv.hoTen " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam c ON pc.maCa = c.maCa " +
                "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.ngayLam BETWEEN ? AND ? " +
                "ORDER BY pc.ngayLam, c.gioBatDau";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CaLam ca = new CaLam(
                            rs.getString("maCa"),
                            rs.getString("tenCa"),
                            rs.getTime("gioBatDau").toLocalTime(),
                            rs.getTime("gioKetThuc").toLocalTime()
                    );
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
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                double tongSoGio = tongSoPhut / 60.0;
                tongGioLamMap.put(maNV, tongSoGio);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính tổng giờ làm: " + e.getMessage());
            e.printStackTrace();
        }
        return tongGioLamMap;
    }

    public entity.CaLam getCaLamViecCuaNhanVien(String maNV, LocalDate date) {
        String sql = "SELECT cl.maCa, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.maNV = ? AND pc.ngayLam = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new entity.CaLam(
                            rs.getString("maCa"),
                            rs.getString("tenCa"),
                            rs.getTime("gioBatDau").toLocalTime(),
                            rs.getTime("gioKetThuc").toLocalTime()
                    );
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String[] getThongTinCaTruocSau(String maNV, LocalDate today) {
        String[] result = new String[]{"<html><center>-- Trống --</center></html>", "<html><center>-- Trống --</center></html>"};

        String sqlPrev = "SELECT TOP 1 pc.maCa, pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.ngayLam < CAST(GETDATE() AS DATE) " +
                "OR (pc.ngayLam = CAST(GETDATE() AS DATE) AND cl.gioKetThuc <= CAST(GETDATE() AS TIME)) " +
                "ORDER BY pc.ngayLam DESC, cl.gioKetThuc DESC";

        String sqlNext = "SELECT TOP 1 pc.maCa, pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.ngayLam > CAST(GETDATE() AS DATE) " +
                "OR (pc.ngayLam = CAST(GETDATE() AS DATE) AND cl.gioBatDau > CAST(GETDATE() AS TIME)) " +
                "ORDER BY pc.ngayLam ASC, cl.gioBatDau ASC";

        try (Connection conn = SQLConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(sqlPrev);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCa = rs.getString("maCa");
                    LocalDate ngayLam = rs.getDate("ngayLam").toLocalDate();
                    String tenCa = rs.getString("tenCa");
                    Time start = rs.getTime("gioBatDau");
                    Time end = rs.getTime("gioKetThuc");

                    // Lấy danh sách nhân viên trong ca này
                    List<String> dsTenNV = getDsNhanVienTrongCa(conn, maCa, ngayLam);
                    result[0] = formatShiftInfo(ngayLam, tenCa, start, end, dsTenNV);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlNext);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maCa = rs.getString("maCa");
                    LocalDate ngayLam = rs.getDate("ngayLam").toLocalDate();
                    String tenCa = rs.getString("tenCa");
                    Time start = rs.getTime("gioBatDau");
                    Time end = rs.getTime("gioKetThuc");

                    List<String> dsTenNV = getDsNhanVienTrongCa(conn, maCa, ngayLam);
                    result[1] = formatShiftInfo(ngayLam, tenCa, start, end, dsTenNV);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<String> getDsNhanVienTrongCa(Connection conn, String maCa, LocalDate ngayLam) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT nv.hoTen FROM PhanCongCa pc " +
                "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.maCa = ? AND pc.ngayLam = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCa);
            ps.setDate(2, java.sql.Date.valueOf(ngayLam));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("hoTen"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String formatShiftInfo(LocalDate date, String tenCa, Time start, Time end, List<String> names) {

        LocalDate today = LocalDate.now();
        String dateStr;
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));

        dayOfWeek = dayOfWeek.replace("Thứ Hai", "thứ 2").replace("Thứ Ba", "thứ 3")
                .replace("Thứ Tư", "thứ 4").replace("Thứ Năm", "thứ 5")
                .replace("Thứ Sáu", "thứ 6").replace("Thứ Bảy", "thứ 7")
                .replace("Chủ Nhật", "CN");

        if (date.isEqual(today)) {
            dateStr = "Hôm nay (" + dayOfWeek + ")";
        } else if (date.isEqual(today.minusDays(1))) {
            dateStr = "Hôm qua (" + dayOfWeek + ")";
        } else if (date.isEqual(today.plusDays(1))) {
            dateStr = "Ngày mai (" + dayOfWeek + ")";
        } else {
            dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM")) + " (" + dayOfWeek + ")";
        }

        String timeStr = start.toString().substring(0, 2) + "h-" + end.toString().substring(0, 2) + "h";

        String staffListStr;
        if (names.isEmpty()) {
            staffListStr = "<i style='color:gray'>Chưa phân công</i>";
        } else {
            staffListStr = String.join(", ", names);
        }

        return "<html><div style='text-align: center; width: 180px;'>" +
                "<span style='color:#2980b9; font-weight:bold; font-size:10px;'>" + dateStr + "</span><br/>" +
                "<span style='color:#2c3e50; font-weight:bold; font-size:11px;'>" + tenCa + " " + timeStr + "</span><br/>" +
                "<div style='margin-top:4px; color:#555; font-size:12px;'>" + staffListStr + "</div>" +
                "</div></html>";
    }
}