package dao;

import connectDB.SQLConnection;
import entity.GiaoCa;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GiaoCaDAO {

    /**
     * [ĐÃ SỬA] Kiểm tra xem nhân viên có đang trong ca làm việc không
     * @return maGiaoCa nếu đang có ca, -1 nếu không có
     */
    public int getMaCaDangLamViec(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT TOP 1 maGiaoCa FROM LichSuGiaoCa " +
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

    /**
     * [ĐÃ SỬA] Bắt đầu ca mới cho nhân viên
     * Kiểm tra trùng lặp trước khi thêm
     */
    public boolean batDauCa(String maNV, double tienDauCa) {
        if (maNV == null || maNV.trim().isEmpty()) {
            System.err.println("Mã NV không hợp lệ");
            return false;
        }

        if (tienDauCa < 0) {
            System.err.println("Tiền đầu ca không được âm");
            return false;
        }

        // Kiểm tra xem đã có ca đang mở chưa
        if (getMaCaDangLamViec(maNV) > 0) {
            System.err.println("Nhân viên đang trong ca làm việc");
            return false;
        }

        String sql = "INSERT INTO LichSuGiaoCa (maNV, thoiGianBatDau, tienDauCa) " +
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

    /**
     * [ĐÃ SỬA] Lấy thông tin ca đang làm việc
     * Bao gồm validation và error handling
     */
    public GiaoCa getThongTinCaDangLam(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT TOP 1 * FROM LichSuGiaoCa " +
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

    /**
     * [ĐÃ SỬA HOÀN TOÀN] Kết thúc ca với tính toán chính xác
     * Sửa lỗi query tính tiền hệ thống
     */
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

            // Lấy thông tin ca hiện tại
            String sqlGetInfo = "SELECT maNV, thoiGianBatDau, tienDauCa FROM LichSuGiaoCa WHERE maGiaoCa = ?";
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

            // Tính tiền hệ thống (chỉ tính hóa đơn tiền mặt đã thanh toán)
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

            // Tính chênh lệch
            double chenhLech = tienCuoiCa - tienDauCa - tienHeThong;

            // Cập nhật kết thúc ca
            String sqlUpdate = "UPDATE LichSuGiaoCa SET " +
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

    /**
     * [ĐÃ SỬA] Lấy danh sách lịch sử giao ca
     */
    public List<GiaoCa> getLichSuGiaoCa(String maNV, int limit) {
        List<GiaoCa> list = new ArrayList<>();

        if (maNV == null || maNV.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT TOP (?) * FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setString(2, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GiaoCa gc = new GiaoCa(
                            rs.getInt("maGiaoCa"),
                            rs.getString("maNV"),
                            rs.getTimestamp("thoiGianBatDau").toLocalDateTime(),
                            rs.getDouble("tienDauCa")
                    );

                    Timestamp ketThuc = rs.getTimestamp("thoiGianKetThuc");
                    if (ketThuc != null) {
                        gc.setThoiGianKetThuc(ketThuc.toLocalDateTime());
                    }

                    gc.setTienCuoiCa(rs.getDouble("tienCuoiCa"));
                    gc.setTienHeThongTinh(rs.getDouble("tienHeThongTinh"));
                    gc.setGhiChu(rs.getString("ghiChu"));

                    list.add(gc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getLichSuGiaoCa: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * [ĐÃ SỬA] Tổng giờ làm theo tuần
     * Sửa lỗi tính toán thời gian
     */
    public double getTongGioLamTheoThang(String maNV, java.time.LocalDate startOfMonth) {
        if (maNV == null || maNV.trim().isEmpty() || startOfMonth == null) {
            return 0;
        }

        // Logic: Chỉ lấy các ca đã bắt đầu trong quá khứ hoặc hiện tại.
        // Nếu ca chưa kết thúc (NULL), dùng GETDATE() để tính, nhưng đảm bảo start <= GETDATE()
        String sql = "SELECT ISNULL(SUM(DATEDIFF(MINUTE, thoiGianBatDau, " +
                "CASE WHEN thoiGianKetThuc IS NULL THEN GETDATE() ELSE thoiGianKetThuc END)), 0) AS TongPhut " +
                "FROM LichSuGiaoCa " +
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

    /**
     * [ĐÃ SỬA LỖI ÂM GIỜ] Tổng giờ làm theo tuần
     */
    public double getTongGioLamTheoTuan(String maNV, java.time.LocalDate startOfWeek) {
        if (maNV == null || maNV.trim().isEmpty() || startOfWeek == null) {
            return 0;
        }

        String sql = "SELECT ISNULL(SUM(DATEDIFF(MINUTE, thoiGianBatDau, " +
                "CASE WHEN thoiGianKetThuc IS NULL THEN GETDATE() ELSE thoiGianKetThuc END)), 0) AS TongPhut " +
                "FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "AND CAST(thoiGianBatDau AS DATE) >= ? " +
                "AND CAST(thoiGianBatDau AS DATE) < DATEADD(DAY, 7, ?) " +
                "AND thoiGianBatDau <= GETDATE()"; // Chặn lỗi dữ liệu tương lai

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

    /**
     * [MỚI] Lấy danh sách 3 ca làm việc GẦN NHẤT ĐÃ HOÀN THÀNH (để hiển thị lịch sử)
     * Dùng để hiển thị chi tiết hiệu suất làm việc
     */
    public List<Map<String, Object>> get3CaLamGanNhat(String maNV) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (maNV == null) return list;

        String sql = "SELECT TOP 3 maGiaoCa, thoiGianBatDau, thoiGianKetThuc, chenhLech, " +
                "DATEDIFF(MINUTE, thoiGianBatDau, thoiGianKetThuc) / 60.0 AS GioLam " +
                "FROM LichSuGiaoCa " +
                "WHERE maNV = ? AND thoiGianKetThuc IS NOT NULL " +
                "AND thoiGianBatDau <= GETDATE() " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("ngay", rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
                map.put("gioLam", rs.getDouble("GioLam"));
                map.put("chenhLech", rs.getDouble("chenhLech"));
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * [ĐÃ SỬA HOÀN TOÀN] Lấy giờ làm theo ngày cho biểu đồ
     * Sử dụng CAST AS DATE và đảm bảo đúng format
     */
    // File: dao/GiaoCaDAO.java

    /**
     * [ĐÃ SỬA] Lấy giờ làm theo ngày cho biểu đồ
     * - Fix lỗi: Chỉ lấy dữ liệu quá khứ và hiện tại (<= GETDATE())
     * - Fix lỗi: Đảm bảo số lượng ngày trả về đúng bằng tham số soNgay
     */
    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();

        if (maNV == null || maNV.trim().isEmpty()) {
            return data;
        }

        // 1. Khởi tạo khung dữ liệu (trục X) cho 'soNgay' gần nhất
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

        // Loop ngược từ ngày xa nhất đến hôm nay để LinkedHashMap giữ đúng thứ tự
        for (int i = soNgay - 1; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            data.put(date.format(formatter), 0.0);
        }

        // 2. Query dữ liệu thực tế (Đã thêm điều kiện chặn ngày tương lai)
        String sql = "SELECT CAST(thoiGianBatDau AS DATE) AS Ngay, " +
                "SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) / 60.0 AS GioLam " +
                "FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "AND thoiGianBatDau >= DATEADD(DAY, -?, CAST(GETDATE() AS DATE)) " +
                "AND thoiGianBatDau <= GETDATE() " + // [QUAN TRỌNG] Chặn dữ liệu tương lai
                "GROUP BY CAST(thoiGianBatDau AS DATE) " +
                "ORDER BY CAST(thoiGianBatDau AS DATE)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setInt(2, soNgay); // Truyền tham số giới hạn ngày

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("Ngay");
                    java.time.LocalDate localDate = sqlDate.toLocalDate();
                    String dateStr = localDate.format(formatter);
                    double gioLam = rs.getDouble("GioLam");

                    // Chỉ cập nhật nếu ngày đó nằm trong map (an toàn)
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

    /**
     * [ĐÃ SỬA] Lấy 3 ca làm việc sắp tới
     */
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

    /**
     * [MỚI] Lấy tổng giờ làm theo khoảng thời gian tùy chỉnh
     */
    public double getTongGioLamTheoKhoangThoiGian(String maNV, java.time.LocalDate tuNgay, java.time.LocalDate denNgay) {
        if (maNV == null || maNV.trim().isEmpty() || tuNgay == null || denNgay == null) {
            return 0;
        }

        String sql = "SELECT ISNULL(SUM(DATEDIFF(MINUTE, thoiGianBatDau, " +
                "ISNULL(thoiGianKetThuc, GETDATE()))), 0) AS TongPhut " +
                "FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "AND CAST(thoiGianBatDau AS DATE) >= ? " +
                "AND CAST(thoiGianBatDau AS DATE) <= ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(tuNgay));
            ps.setDate(3, java.sql.Date.valueOf(denNgay));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double totalMinutes = rs.getDouble("TongPhut");
                    return totalMinutes / 60.0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTongGioLamTheoKhoangThoiGian: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * [MỚI] Lấy danh sách lịch sử giao ca chi tiết theo khoảng thời gian
     */
    public List<Map<String, Object>> getLichSuGiaoCaChiTiet(String maNV, java.time.LocalDate start, java.time.LocalDate end) {
        List<Map<String, Object>> list = new ArrayList<>();

        if (maNV == null || maNV.trim().isEmpty()) {
            return list;
        }

        String sql = "SELECT * FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "AND CAST(thoiGianBatDau AS DATE) >= ? " +
                "AND CAST(thoiGianBatDau AS DATE) <= ? " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new java.util.HashMap<>();

                    Timestamp tsStart = rs.getTimestamp("thoiGianBatDau");
                    Timestamp tsEnd = rs.getTimestamp("thoiGianKetThuc");

                    row.put("maGiaoCa", rs.getInt("maGiaoCa"));
                    row.put("thoiGianBatDau", tsStart != null ? tsStart.toLocalDateTime() : null);
                    row.put("thoiGianKetThuc", tsEnd != null ? tsEnd.toLocalDateTime() : null);
                    row.put("tienDauCa", rs.getDouble("tienDauCa"));

                    double tienCuoi = rs.getDouble("tienCuoiCa");
                    row.put("tienCuoiCa", rs.wasNull() ? null : tienCuoi);

                    double tienHeThong = rs.getDouble("tienHeThongTinh");
                    row.put("tienHeThongTinh", rs.wasNull() ? null : tienHeThong);

                    double chenhLech = rs.getDouble("chenhLech");
                    row.put("chenhLech", rs.wasNull() ? null : chenhLech);

                    row.put("ghiChu", rs.getString("ghiChu"));

                    // Giả định tên ca dựa vào giờ bắt đầu
                    if (tsStart != null) {
                        int hour = tsStart.toLocalDateTime().getHour();
                        String tenCa = (hour >= 6 && hour < 14) ? "Ca Sáng" :
                                (hour >= 14 && hour < 18) ? "Ca Chiều" : "Ca Tối";
                        row.put("tenCa", tenCa);
                    } else {
                        row.put("tenCa", "N/A");
                    }

                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getLichSuGiaoCaChiTiet: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * [MỚI] Tính tổng số giờ làm việc trong một khoảng thời gian (Alias cho hàm đã có)
     */
    public double getTongGioLamTheoKhoang(String maNV, java.time.LocalDate start, java.time.LocalDate end) {
        return getTongGioLamTheoKhoangThoiGian(maNV, start, end);
    }
}