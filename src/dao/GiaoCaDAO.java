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
     * Kiểm tra xem nhân viên có đang trong ca làm việc không (Ca chưa kết thúc)
     * @return maGiaoCa nếu đang có ca, -1 nếu không có
     */
    public int getMaCaDangLamViec(String maNV) {
        String sql = "SELECT TOP 1 maGiaoCa FROM LichSuGiaoCa " +
                "WHERE maNV = ? AND thoiGianKetThuc IS NULL " +
                "ORDER BY thoiGianBatDau DESC";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("Lỗi getMaCaDangLamViec: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Bắt đầu ca mới cho nhân viên
     */
    public boolean batDauCa(String maNV, double tienDauCa) {
        String sql = "INSERT INTO LichSuGiaoCa (maNV, thoiGianBatDau, tienDauCa) " +
                "VALUES (?, GETDATE(), ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDouble(2, tienDauCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi batDauCa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy thông tin ca đang làm việc (bao gồm tienDauCa và thoiGianBatDau)
     */
    public GiaoCa getThongTinCaDangLam(String maNV) {
        String sql = "SELECT TOP 1 * FROM LichSuGiaoCa " +
                "WHERE maNV = ? AND thoiGianKetThuc IS NULL " +
                "ORDER BY thoiGianBatDau DESC";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                GiaoCa gc = new GiaoCa(
                        rs.getInt("maGiaoCa"),
                        rs.getString("maNV"),
                        rs.getTimestamp("thoiGianBatDau").toLocalDateTime(),
                        rs.getDouble("tienDauCa")
                );
                return gc;
            }
        } catch (Exception e) {
            System.err.println("Lỗi getThongTinCaDangLam: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Kết thúc ca (Tính tiền hệ thống từ các hóa đơn trong khoảng thời gian ca)
     */
    // [ĐÃ SỬA LỖI] Sửa truy vấn con tính tiền hệ thống: Dùng SUM(tongTien)
    public boolean ketThucCa(int maGiaoCa, double tienCuoiCa, String ghiChu) {
        String sql = "UPDATE LichSuGiaoCa SET " +
                "thoiGianKetThuc = GETDATE(), " +
                "tienCuoiCa = ?, " +
                "ghiChu = ?, " +
                "tienHeThongTinh = (" +
                "   SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon " + // Sửa ở đây
                "   WHERE maNV = (SELECT maNV FROM LichSuGiaoCa WHERE maGiaoCa = ?) " +
                "   AND trangThai = N'Đã thanh toán' " +
                "   AND hinhThucThanhToan = N'Tiền mặt' " +
                "   AND ngayLap >= (SELECT thoiGianBatDau FROM LichSuGiaoCa WHERE maGiaoCa = ?) " +
                "   AND ngayLap <= GETDATE()" +
                "), " +
                "chenhLech = ? - (SELECT tienDauCa FROM LichSuGiaoCa WHERE maGiaoCa = ?) - (" +
                "   SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon " + // Sửa ở đây
                "   WHERE maNV = (SELECT maNV FROM LichSuGiaoCa WHERE maGiaoCa = ?) " +
                "   AND trangThai = N'Đã thanh toán' " +
                "   AND hinhThucThanhToan = N'Tiền mặt' " +
                "   AND ngayLap >= (SELECT thoiGianBatDau FROM LichSuGiaoCa WHERE maGiaoCa = ?) " +
                "   AND ngayLap <= GETDATE()" +
                ") " +
                "WHERE maGiaoCa = ?";

        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, tienCuoiCa);
            ps.setString(2, ghiChu);
            // Các tham số cho subquery tính toán
            ps.setInt(3, maGiaoCa); ps.setInt(4, maGiaoCa);
            ps.setDouble(5, tienCuoiCa);
            ps.setInt(6, maGiaoCa);
            ps.setInt(7, maGiaoCa); ps.setInt(8, maGiaoCa);
            // Tham số WHERE chính
            ps.setInt(9, maGiaoCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * Lấy danh sách lịch sử giao ca gần nhất của nhân viên
     */
    public List<GiaoCa> getLichSuGiaoCa(String maNV, int limit) {
        List<GiaoCa> list = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM LichSuGiaoCa " +
                "WHERE maNV = ? " +
                "ORDER BY thoiGianBatDau DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setString(2, maNV);
            ResultSet rs = ps.executeQuery();
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
        } catch (Exception e) {
            System.err.println("Lỗi getLichSuGiaoCa: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tổng giờ làm theo tuần
     */
    public double getTongGioLamTheoTuan(String maNV, java.time.LocalDate startOfWeek) {
        String sql = "SELECT SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) " +
                "FROM LichSuGiaoCa WHERE maNV = ? " +
                "AND thoiGianBatDau >= ? AND thoiGianBatDau < DATEADD(day, 7, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(startOfWeek));
            ps.setDate(3, java.sql.Date.valueOf(startOfWeek));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double totalMinutes = rs.getDouble(1);
                return totalMinutes / 60.0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi getTongGioLamTheoTuan: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy tổng giờ làm theo tháng
     */
    public double getTongGioLamTheoThang(String maNV, java.time.LocalDate startOfMonth) {
        String sql = "SELECT SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) " +
                "FROM LichSuGiaoCa WHERE maNV = ? " +
                "AND thoiGianBatDau >= ? " +
                "AND thoiGianBatDau < DATEADD(MONTH, 1, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(startOfMonth));
            ps.setDate(3, java.sql.Date.valueOf(startOfMonth));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double totalMinutes = rs.getDouble(1);
                return totalMinutes / 60.0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi getTongGioLamTheoThang: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy giờ làm theo ngày cho biểu đồ (7 ngày hoặc 30 ngày gần nhất)
     */
    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT FORMAT(thoiGianBatDau, 'dd/MM') as Ngay, " +
                "SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) / 60.0 as GioLam " +
                "FROM LichSuGiaoCa " +
                "WHERE maNV = ? AND thoiGianBatDau >= DATEADD(DAY, -?, GETDATE()) " +
                "GROUP BY FORMAT(thoiGianBatDau, 'dd/MM'), CAST(thoiGianBatDau AS DATE) " +
                "ORDER BY CAST(thoiGianBatDau AS DATE)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setInt(2, soNgay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("Ngay"), rs.getDouble("GioLam"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi getGioLamTheoNgay: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Lấy 3 ca làm việc sắp tới (tương lai)
     */
    public List<String> getCacCaLamSapToi(String maNV) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT TOP 3 pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa = cl.maCa " +
                "WHERE pc.maNV = ? AND pc.ngayLam >= CAST(GETDATE() AS DATE) " +
                "ORDER BY pc.ngayLam ASC, cl.gioBatDau ASC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            while (rs.next()) {
                String ngay = rs.getDate("ngayLam").toLocalDate().format(dtf);
                String ca = rs.getString("tenCa");
                String gio = rs.getTime("gioBatDau").toString().substring(0, 5) +
                        "-" + rs.getTime("gioKetThuc").toString().substring(0, 5);
                list.add(ngay + ": " + ca + " (" + gio + ")");
            }
        } catch (Exception e) {
            System.err.println("Lỗi getCacCaLamSapToi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tổng giờ làm trong khoảng thời gian tùy chỉnh
     */
    public double getTongGioLamTheoKhoangThoiGian(String maNV, java.time.LocalDate tuNgay, java.time.LocalDate denNgay) {
        String sql = "SELECT SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) " +
                "FROM LichSuGiaoCa WHERE maNV = ? " +
                "AND thoiGianBatDau >= ? " +
                "AND thoiGianBatDau <= ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(tuNgay));
            ps.setDate(3, java.sql.Date.valueOf(denNgay.plusDays(1)));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double totalMinutes = rs.getDouble(1);
                return totalMinutes / 60.0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi getTongGioLamTheoKhoangThoiGian: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // -----------------------------------------------------------
    // THÊM 2 HÀM NÀY VÀO GiaoCaDAO.java ĐỂ HẾT LỖI ĐỎ
    // -----------------------------------------------------------

    /**
     * Lấy danh sách lịch sử giao ca chi tiết theo khoảng thời gian (Dùng cho bảng Lịch sử)
     */
    public java.util.List<java.util.Map<String, Object>> getLichSuGiaoCaChiTiet(String maNV, java.time.LocalDate start, java.time.LocalDate end) {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();

        // Truy vấn lấy lịch sử giao ca trong khoảng thời gian
        String sql = "SELECT * FROM LichSuGiaoCa WHERE maNV = ? " +
                "AND CAST(thoiGianBatDau AS DATE) >= ? AND CAST(thoiGianBatDau AS DATE) <= ? " +
                "ORDER BY thoiGianBatDau DESC";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                java.sql.Timestamp tsStart = rs.getTimestamp("thoiGianBatDau");
                java.sql.Timestamp tsEnd = rs.getTimestamp("thoiGianKetThuc");

                row.put("thoiGianBatDau", tsStart.toLocalDateTime());
                row.put("thoiGianKetThuc", tsEnd != null ? tsEnd.toLocalDateTime() : null);
                row.put("tienDauCa", rs.getDouble("tienDauCa"));

                // Xử lý các giá trị có thể null
                double tienCuoi = rs.getDouble("tienCuoiCa");
                row.put("tienCuoiCa", rs.wasNull() ? null : tienCuoi);

                double chenhLech = rs.getDouble("chenhLech");
                row.put("chenhLech", rs.wasNull() ? null : chenhLech);

                // Giả định tên ca dựa vào giờ bắt đầu
                int hour = tsStart.toLocalDateTime().getHour();
                String tenCa = (hour >= 6 && hour < 14) ? "Ca Sáng" :
                        (hour >= 14 && hour < 18) ? "Ca Chiều" : "Ca Tối";
                row.put("tenCa", tenCa);

                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính tổng số giờ làm việc trong một khoảng thời gian
     */
    public double getTongGioLamTheoKhoang(String maNV, java.time.LocalDate start, java.time.LocalDate end) {
        String sql = "SELECT SUM(DATEDIFF(MINUTE, thoiGianBatDau, ISNULL(thoiGianKetThuc, GETDATE()))) FROM LichSuGiaoCa WHERE maNV = ? AND CAST(thoiGianBatDau AS DATE) >= ? AND CAST(thoiGianBatDau AS DATE) <= ?";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1) / 60.0;
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}