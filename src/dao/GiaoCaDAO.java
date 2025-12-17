package dao;

import connectDB.SQLConnection;
import entity.GiaoCa;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GiaoCaDAO {

    public int getMaCaDangLamViec(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT TOP 1 maGiaoCa FROM GiaoCa " +
                "WHERE maNV = ? AND thoiGianKetThuc IS NULL " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("maGiaoCa");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getMaCaDangLamViec: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public boolean batDauCa(String maNV, double tienDauCa) {
        if (maNV == null || maNV.trim().isEmpty()) {
            System.err.println("Mã NV không hợp lệ");
            return false;
        }

        if (tienDauCa < 0) {
            System.err.println("Tiền đầu ca không được âm");
            return false;
        }

        if (getMaCaDangLamViec(maNV) > 0) {
            System.err.println("Nhân viên đang trong ca làm việc");
            return false;
        }

        String sql = "INSERT INTO GiaoCa (maNV, thoiGianBatDau, tienDauCa) " +
                "VALUES (?, GETDATE(), ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setDouble(2, tienDauCa);

            int result = ps.executeUpdate();
            if (result > 0) {
                System.out.println("Bắt đầu ca thành công cho NV: " + maNV);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi batDauCa: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public GiaoCa getThongTinCaDangLam(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT TOP 1 * FROM GiaoCa " +
                "WHERE maNV = ? AND thoiGianKetThuc IS NULL " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GiaoCa gc = new GiaoCa(
                            rs.getInt("maGiaoCa"),
                            rs.getString("maNV"),
                            rs.getTimestamp("thoiGianBatDau").toLocalDateTime(),
                            rs.getDouble("tienDauCa")
                    );
                    return gc;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getThongTinCaDangLam: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean ketThucCa(int maGiaoCa, double tienCuoiCa, String ghiChu) {
        if (maGiaoCa <= 0) {
            System.err.println("Mã giao ca không hợp lệ");
            return false;
        }

        if (tienCuoiCa < 0) {
            System.err.println("Tiền cuối ca không được âm");
            return false;
        }

        Connection conn = null;
        try {
            conn = SQLConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlGetInfo = "SELECT maNV, thoiGianBatDau, tienDauCa FROM GiaoCa WHERE maGiaoCa = ?";
            String maNV = null;
            LocalDateTime thoiGianBatDau = null;
            double tienDauCa = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlGetInfo)) {
                ps.setInt(1, maGiaoCa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maNV = rs.getString("maNV");
                        thoiGianBatDau = rs.getTimestamp("thoiGianBatDau").toLocalDateTime();
                        tienDauCa = rs.getDouble("tienDauCa");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            String sqlTinhTien = "SELECT ISNULL(SUM(tongTien - ISNULL(giamGia, 0)), 0) AS TienHeThong " +
                    "FROM HoaDon " +
                    "WHERE maNV = ? " +
                    "AND trangThai = N'Đã thanh toán' " +
                    "AND hinhThucThanhToan = N'Tiền mặt' " +
                    "AND ngayLap >= ? " +
                    "AND ngayLap <= GETDATE()";

            double tienHeThong = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlTinhTien)) {
                ps.setString(1, maNV);
                ps.setTimestamp(2, Timestamp.valueOf(thoiGianBatDau));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tienHeThong = rs.getDouble("TienHeThong");
                    }
                }
            }

            double chenhLech = tienCuoiCa - tienDauCa - tienHeThong;

            String sqlUpdate = "UPDATE GiaoCa SET " +
                    "thoiGianKetThuc = GETDATE(), " +
                    "tienCuoiCa = ?, " +
                    "tienHeThongTinh = ?, " +
                    "chenhLech = ?, " +
                    "ghiChu = ? " +
                    "WHERE maGiaoCa = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, tienCuoiCa);
                ps.setDouble(2, tienHeThong);
                ps.setDouble(3, chenhLech);
                ps.setString(4, ghiChu);
                ps.setInt(5, maGiaoCa);

                int result = ps.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    System.out.println("Kết thúc ca thành công. Chênh lệch: " + chenhLech);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi ketThucCa: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public double getTongGioLamTheoThang(String maNV, java.time.LocalDate startOfMonth) {
        if (maNV == null || maNV.trim().isEmpty() || startOfMonth == null) {
            return 0;
        }

        String sql = "SELECT ISNULL(SUM(DATEDIFF(MINUTE, thoiGianBatDau, " +
                "CASE WHEN thoiGianKetThuc IS NULL THEN GETDATE() ELSE thoiGianKetThuc END)), 0) AS TongPhut " +
                "FROM GiaoCa " +
                "WHERE maNV = ? " +
                "AND MONTH(thoiGianBatDau) = ? " +
                "AND YEAR(thoiGianBatDau) = ? " +
                "AND thoiGianBatDau <= GETDATE()";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setInt(2, startOfMonth.getMonthValue());
            ps.setInt(3, startOfMonth.getYear());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalMinutes = rs.getDouble("TongPhut");
                    return totalMinutes / 60.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTongGioLamTheoThang: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public double getTongGioLamTheoTuan(String maNV, java.time.LocalDate startOfWeek) {
        if (maNV == null || maNV.trim().isEmpty() || startOfWeek == null) {
            return 0;
        }

        String sql = "SELECT ISNULL(SUM(DATEDIFF(MINUTE, thoiGianBatDau, " +
                "CASE WHEN thoiGianKetThuc IS NULL THEN GETDATE() ELSE thoiGianKetThuc END)), 0) AS TongPhut " +
                "FROM GiaoCa " +
                "WHERE maNV = ? " +
                "AND CAST(thoiGianBatDau AS DATE) >= ? " +
                "AND CAST(thoiGianBatDau AS DATE) < DATEADD(DAY, 7, ?) " +
                "AND thoiGianBatDau <= GETDATE()";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(startOfWeek));
            ps.setDate(3, java.sql.Date.valueOf(startOfWeek));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalMinutes = rs.getDouble("TongPhut");
                    return totalMinutes / 60.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTongGioLamTheoTuan: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getNhanVienDangLamViecChiTiet() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT nv.hoTen, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM GiaoCa ls " +
                "JOIN NhanVien nv ON ls.maNV = nv.maNV " +
                "LEFT JOIN PhanCongCa pc ON ls.maNV = pc.maNV AND pc.ngayLam = CAST(GETDATE() AS DATE) " +
                "LEFT JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE ls.thoiGianKetThuc IS NULL " +
                "ORDER BY ls.thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String tenNV = rs.getString("hoTen");
                String tenCa = rs.getString("tenCa");
                Time start = rs.getTime("gioBatDau");
                Time end = rs.getTime("gioKetThuc");

                if (tenCa != null && start != null && end != null) {
                    String timeStr = start.toString().substring(0, 5) + " - " + end.toString().substring(0, 5);
                    list.add(String.format("%s (%s: %s)", tenNV, tenCa, timeStr));
                } else {
                    list.add(tenNV + " (Ca bổ sung)");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Double> getTopStaffByWorkHours(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Double> result = new LinkedHashMap<>();

        String sql = "SELECT TOP (?) nv.hoTen, " +
                "SUM(DATEDIFF(MINUTE, ls.thoiGianBatDau, " +
                "    CASE " +
                "        WHEN ls.thoiGianKetThuc IS NULL THEN GETDATE() " +
                "        WHEN ls.thoiGianKetThuc > GETDATE() THEN GETDATE() " +
                "        ELSE ls.thoiGianKetThuc " +
                "    END)) / 60.0 AS TongGio " +
                "FROM GiaoCa ls " +
                "JOIN NhanVien nv ON ls.maNV = nv.maNV " +
                "WHERE ls.thoiGianBatDau >= ? " +
                "AND ls.thoiGianBatDau < ? " +
                "AND ls.thoiGianBatDau <= GETDATE() " +
                "GROUP BY nv.hoTen " +
                "HAVING SUM(DATEDIFF(MINUTE, ls.thoiGianBatDau, " +
                "    CASE " +
                "        WHEN ls.thoiGianKetThuc IS NULL THEN GETDATE() " +
                "        WHEN ls.thoiGianKetThuc > GETDATE() THEN GETDATE() " +
                "        ELSE ls.thoiGianKetThuc " +
                "    END)) > 0 " +
                "ORDER BY TongGio DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double hours = rs.getDouble("TongGio");
                    if (hours > 0) {
                        result.put(rs.getString("hoTen"), hours);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getTopStaffByWorkHours: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();

        if (maNV == null || maNV.trim().isEmpty()) {
            return data;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

        for (int i = soNgay - 1; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            data.put(date.format(formatter), 0.0);
        }

        String sql = "SELECT CAST(thoiGianBatDau AS DATE) AS Ngay, " +
                "SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) / 60.0 AS GioLam " +
                "FROM GiaoCa " +
                "WHERE maNV = ? " +
                "AND thoiGianBatDau >= DATEADD(DAY, -?, CAST(GETDATE() AS DATE)) " +
                "AND thoiGianBatDau <= GETDATE() " +
                "GROUP BY CAST(thoiGianBatDau AS DATE) " +
                "ORDER BY CAST(thoiGianBatDau AS DATE)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setInt(2, soNgay);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("Ngay");
                    java.time.LocalDate localDate = sqlDate.toLocalDate();
                    String dateStr = localDate.format(formatter);
                    double gioLam = rs.getDouble("GioLam");

                    if (data.containsKey(dateStr)) {
                        data.put(dateStr, gioLam);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getGioLamTheoNgay: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public List<String> getCacCaLamSapToi(String maNV) {
        List<String> list = new ArrayList<>();

        if (maNV == null || maNV.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT TOP 3 pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM PhanCongCa pc " +
                "JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.maNV = ? " +
                "AND pc.ngayLam >= CAST(GETDATE() AS DATE) " +
                "ORDER BY pc.ngayLam ASC, cl.gioBatDau ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

                while (rs.next()) {
                    String ngay = rs.getDate("ngayLam").toLocalDate().format(dtf);
                    String ca = rs.getString("tenCa");
                    String gioBD = rs.getTime("gioBatDau").toString().substring(0, 5);
                    String gioKT = rs.getTime("gioKetThuc").toString().substring(0, 5);

                    list.add(String.format("%s: %s (%s - %s)", ngay, ca, gioBD, gioKT));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getCacCaLamSapToi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<entity.GiaoCa> getLichSuGiaoCa(java.time.LocalDate tuNgay, java.time.LocalDate denNgay) {
        java.util.List<entity.GiaoCa> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM GiaoCa WHERE CAST(thoiGianBatDau AS DATE) BETWEEN ? AND ? ORDER BY thoiGianBatDau DESC";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tuNgay));
            ps.setDate(2, java.sql.Date.valueOf(denNgay));

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalDateTime kt = rs.getTimestamp("thoiGianKetThuc") != null ?
                            rs.getTimestamp("thoiGianKetThuc").toLocalDateTime() : null;

                    list.add(new entity.GiaoCa(
                            rs.getInt("maGiaoCa"),
                            rs.getString("maNV"),
                            rs.getTimestamp("thoiGianBatDau").toLocalDateTime(),
                            kt,
                            rs.getDouble("tienDauCa"),
                            rs.getDouble("tienCuoiCa"),
                            rs.getDouble("tienHeThongTinh"),
                            rs.getDouble("chenhLech"),
                            rs.getString("ghiChu")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}