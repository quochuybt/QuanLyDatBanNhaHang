package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.sql.Types;

// Thêm các import cần thiết cho Dashboard
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HoaDonDAO {
    private ChiTietHoaDonDAO chiTietDAO;
    public HoaDonDAO() {
        this.chiTietDAO = new ChiTietHoaDonDAO();
    }
    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        HoaDon hoaDon = null;

        // --- SỬA CÂU SQL ---
        // Liên kết HoaDon với DonDatMon (để lấy maBan)
        String sql = "SELECT hd.*, ddm.maKH FROM HoaDon hd " +
                "JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "WHERE ddm.maBan = ? AND hd.trangThai = N'Chưa thanh toán'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Lấy thông tin Hóa Đơn
                    hoaDon = createHoaDonFromResultSet(rs);

                    String maKH = rs.getString("maKH");
                    hoaDon.setMaKH(maKH);
                    // 2. Lấy thông tin Chi Tiết Hóa Đơn
                    List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(hoaDon.getMaDon());

                    // 3. Gán chi tiết vào hóa đơn
                    hoaDon.setDsChiTiet(dsChiTiet);
                    hoaDon.tinhLaiTongTienTuChiTiet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hoaDon; // Trả về null nếu không tìm thấy
    }
    // NOTE SỬA: Thêm hàm cập nhật maNV khi thanh toán
    public boolean capNhatNhanVien(String maHD, String maNV) {
        String sql = "UPDATE HoaDon SET maNV = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật NV cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean capNhatMaKM(String maHD, String maKM) {
        // Cập nhật cột maKM trong bảng HoaDon
        String sql = "UPDATE HoaDon SET maKM = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (maKM != null && !maKM.isEmpty()) {
                ps.setString(1, maKM); // Đặt mã KM
            } else {
                ps.setNull(1, java.sql.Types.NVARCHAR); // Đặt là NULL nếu maKM rỗng hoặc null
            }
            ps.setString(2, maHD); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật maKM cho Hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean thanhToanHoaDon(String maHD, float tienKhachDua, String hinhThucThanhToan) {
        String sql = "UPDATE HoaDon SET trangThai = N'Đã thanh toán', tienKhachDua = ?, hinhThucThanhToan = ? " +
                "WHERE maHD = ? AND trangThai = N'Chưa thanh toán'"; // Chỉ cập nhật HĐ chưa thanh toán
        Connection conn = null; // Khai báo ngoài try để dùng cho transaction (nếu cần)
        PreparedStatement ps = null;
        boolean success = false;

        try {
            conn = SQLConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setFloat(1, tienKhachDua);
            ps.setString(2, hinhThucThanhToan);
            ps.setString(3, maHD);

            int rowsAffected = ps.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thanh toán hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
            success = false;
        } finally {
            // Đóng PreparedStatement
            try { if (ps != null) ps.close(); } catch (SQLException ex) {}
            // Không đóng connection nếu nó được quản lý bởi Singleton
        }
        return success;
    }
    public boolean capNhatTongTien(String maHD, float tongTienMoi) {
        // Giả sử cột tổng tiền trong bảng HoaDon tên là tongTien
        String sql = "UPDATE HoaDon SET tongTien = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, tongTienMoi);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật tổng tiền cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Chuyển ResultSet thành đối tượng HoaDon.
     */
    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");

        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");

        String maDon = rs.getString("maDon");
        String maNV = rs.getString("maNV");
        String maKM = rs.getString("maKM");

        float tongTien = rs.getFloat("tongTien"); // Đây là tổng tiền GỐC (trước giảm giá)

        float tienKhachDua = rs.getFloat("tienKhachDua");

        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThucThanhToan, maDon, maNV, maKM);

        hd.setTienKhachDua(tienKhachDua);
        hd.setTongTienTuDB(tongTien); // Gán tổng tiền gốc

        // Cần tính toán lại tổng thanh toán thực tế nếu DB không lưu
        // Hoặc nếu DB có cột tongThanhToan, hãy lấy ở đây
        // float tongThanhToan = rs.getFloat("tongThanhToan");
        // hd.setTongThanhToan(tongThanhToan); // Cần setter trong entity HoaDon

        return hd;
    }

    /**
     * [SELECT] - Lấy toàn bộ danh sách hóa đơn từ CSDL.
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon ORDER BY ngayLap DESC";
        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    dsHoaDon.add(createHoaDonFromResultSet(rs));
                } catch (Exception e) {
                    System.err.println("Lỗi khi tạo HoaDon từ ResultSet (getAll): " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsHoaDon;
    }

    /**
     * [INSERT] - Thêm một hóa đơn mới vào CSDL.
     */
    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua,maNV, maKM, maDon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            ps.setFloat(3, hd.getTongTien()); // Tổng tiền gốc
            ps.setString(4, hd.getTrangThai());
            ps.setString(5, hd.getHinhThucThanhToan());
            ps.setFloat(6, hd.getTienKhachDua());
            ps.setString(7, hd.getMaNV());
            if (hd.getMaKM() != null) {
                ps.setString(8, hd.getMaKM());
            } else {
                ps.setNull(8, java.sql.Types.NVARCHAR);
            }
            ps.setString(9, hd.getMaDon());

            // Nếu CSDL của bạn có cột tongThanhToan, bạn cần thêm 1 tham số ? vào SQL
            // và thêm dòng này:
            // ps.setFloat(10, hd.getTongThanhToan());

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi ràng buộc: Mã HD đã tồn tại: " + hd.getMaHD());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * [SEARCH] - Tìm kiếm hóa đơn theo Mã HD.
     */
    public List<HoaDon> timHoaDon(String tuKhoa) {
        List<HoaDon> dsKetQua = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maHD LIKE ? ORDER BY ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsKetQua.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo HoaDon từ ResultSet (tìm kiếm): " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKetQua;
    }
    public HoaDon getHoaDonTheoMaDon(String maDon) {
        HoaDon hoaDon = null;
        String sql = "SELECT * FROM HoaDon WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        hoaDon = createHoaDonFromResultSet(rs);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo HoaDon từ ResultSet (theo mã đơn): " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm hóa đơn theo mã đơn " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return hoaDon;
    }

    // --- CÁC HÀM MỚI CHO DASHBOARD ---

    /**
     * Lấy tổng doanh thu theo từng ngày trong khoảng thời gian.
     * Chỉ tính các hóa đơn đã thanh toán.
     * @param startDate Ngày bắt đầu (bao gồm)
     * @param endDate Ngày kết thúc (bao gồm)
     * @return Map với Key là LocalDate, Value là tổng doanh thu ngày đó.
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();
        // Giả sử cột 'tongTien' trong DB LÀ tổng tiền cuối cùng khách trả (đã bao gồm giảm giá,...)
        // Nếu 'tongTien' trong DB là tổng tiền gốc (trước giảm giá), bạn cần tính toán lại
        // hoặc (tốt nhất) là lưu một cột 'tongThanhToan' trong bảng HoaDon và SUM cột đó.
        // Ví dụ này giả định 'tongTien' là tổng cuối cùng.
        String sql = "SELECT CAST(ngayLap AS DATE) AS Ngay, SUM(tongTien) AS DoanhThuNgay " +
                "FROM HoaDon " +
                "WHERE trangThai = N'Đã thanh toán' " +
                "AND ngayLap >= ? AND ngayLap < ? " +
                "GROUP BY CAST(ngayLap AS DATE) " +
                "ORDER BY Ngay";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate ngay = rs.getDate("Ngay").toLocalDate();
                    double doanhThu = rs.getDouble("DoanhThuNgay");
                    dailyRevenue.put(ngay, doanhThu);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching daily revenue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn doanh thu hàng ngày", e);
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching daily revenue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi lấy doanh thu", e);
        }
        return dailyRevenue;
    }

    /**
     * Đếm số lượng hóa đơn đã thanh toán trong khoảng thời gian.
     * @param startDate Ngày bắt đầu (bao gồm)
     * @param endDate Ngày kết thúc (bao gồm)
     * @return Số lượng hóa đơn.
     */
    public int getOrderCount(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        String sql = "SELECT COUNT(maHD) FROM HoaDon " +
                "WHERE trangThai = N'Đã thanh toán' " +
                "AND ngayLap >= ? AND ngayLap < ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while counting orders: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn số lượng hóa đơn", e);
        } catch (Exception e) {
            System.err.println("Unexpected error while counting orders: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi đếm hóa đơn", e);
        }
        return count;
    }

    /**
     * (MỚI) Lấy top nhân viên theo doanh thu trong khoảng thời gian.
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param limit Số lượng nhân viên top (ví dụ: 5)
     * @return Map<String, Double> (Key: Tên Nhân viên, Value: Tổng doanh thu)
     */
    public Map<String, Double> getTopStaffByRevenue(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Double> topStaff = new LinkedHashMap<>(); // Giữ thứ tự

        // Dùng cột maNV từ bảng HoaDon để tính doanh thu
        // Giả định 'tongTien' là tổng tiền cuối cùng
        String sql = "SELECT TOP (?) nv.hoTen, SUM(hd.tongTien) AS TongDoanhThu " +
                "FROM HoaDon hd " +
                "JOIN NhanVien nv ON hd.maNV = nv.maNV " + // Join với NhanVien để lấy hoTen
                "WHERE hd.trangThai = N'Đã thanh toán' " +
                "AND hd.ngayLap >= ? AND hd.ngayLap < ? " +
                "GROUP BY nv.hoTen " + // Nhóm theo tên (hoặc mã NV nếu muốn)
                "ORDER BY TongDoanhThu DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit); // Đặt tham số TOP
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenNV = rs.getString("hoTen");
                    double tongDoanhThu = rs.getDouble("TongDoanhThu");
                    topStaff.put(tenNV, tongDoanhThu);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching top staff: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn top nhân viên", e);
        }
        return topStaff;
    }

} // Kết thúc class HoaDonDAO