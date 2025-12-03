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

    /**
     * Lấy thông tin ca làm việc hiện tại của nhân viên (Giờ bắt đầu, Giờ kết thúc chuẩn)
     */
    public CaLam getCaLamViecCuaNhanVien(String maNV, LocalDate ngay) {
        String sql = "SELECT cl.* FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.maNV = ? AND pc.ngayLam = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CaLam(
                            rs.getString("maCa"),
                            rs.getString("tenCa"),
                            rs.getTime("gioBatDau").toLocalTime(),
                            rs.getTime("gioKetThuc").toLocalTime()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getCaLamViecCuaNhanVien: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin nhân viên làm ca trước và ca sau so với thời điểm hiện tại trong ngày.
     * @param maNVHienTai Mã nhân viên hiện tại (để không lấy chính họ)
     * @param ngayLam Ngày cần tra cứu (thường là LocalDate.now())
     * @return String[] { "Thông tin ca trước", "Thông tin ca sau" }
     */
    // [ĐÃ SỬA LỖI] Ép kiểu tham số thời gian thành TIME để so sánh
    // Thêm/Sửa trong PhanCongDAO

    /**
     * [ĐÃ SỬA] Lấy thông tin nhân viên làm ca trước và ca sau
     * Sửa lỗi cast thời gian
     */
    public String[] getThongTinCaTruocSau(String maNVHienTai, java.time.LocalDate ngayLam) {
        String[] result = {"-- Trống --", "-- Trống --"};

        if (maNVHienTai == null || maNVHienTai.trim().isEmpty() || ngayLam == null) {
            return result;
        }

        java.time.LocalTime now = java.time.LocalTime.now();
        String timeStr = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        // SQL Ca Trước
        String sqlPrev = "SELECT TOP 1 cl.tenCa, cl.gioBatDau, cl.gioKetThuc, nv.hoTen " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.ngayLam = ? " +
                "AND pc.maNV != ? " +
                "AND cl.gioKetThuc <= CAST(? AS TIME) " +
                "ORDER BY cl.gioKetThuc DESC";
        // SQL Ca Sau
        String sqlNext = "SELECT TOP 1 cl.tenCa, cl.gioBatDau, cl.gioKetThuc, nv.hoTen " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.ngayLam = ? " +
                "AND pc.maNV != ? " +
                "AND cl.gioBatDau >= CAST(? AS TIME) " +
                "ORDER BY cl.gioBatDau ASC";

        try (Connection conn = SQLConnection.getConnection()) {
            // Ca Trước
            try (PreparedStatement ps = conn.prepareStatement(sqlPrev)) {
                ps.setDate(1, Date.valueOf(ngayLam));
                ps.setString(2, maNVHienTai);
                ps.setString(3, timeStr);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String time = rs.getTime("gioBatDau").toString().substring(0, 5) +
                                "-" + rs.getTime("gioKetThuc").toString().substring(0, 5);
                        result[0] = "<html>" + rs.getString("hoTen") +
                                "<br><i style='font-size:10px'>(" + rs.getString("tenCa") + " " + time + ")</i></html>";
                    }
                }
            }

            // Ca Sau
            try (PreparedStatement ps = conn.prepareStatement(sqlNext)) {
                ps.setDate(1, Date.valueOf(ngayLam));
                ps.setString(2, maNVHienTai);
                ps.setString(3, timeStr);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String time = rs.getTime("gioBatDau").toString().substring(0, 5) +
                                "-" + rs.getTime("gioKetThuc").toString().substring(0, 5);
                        result[1] = "<html>" + rs.getString("hoTen") +
                                "<br><i style='font-size:10px'>(" + rs.getString("tenCa") + " " + time + ")</i></html>";
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getThongTinCaTruocSau: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}