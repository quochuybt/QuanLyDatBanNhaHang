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

    private static final int ITEMS_PER_PAGE = 15;

    public HoaDonDAO() {
        this.chiTietDAO = new ChiTietHoaDonDAO();
    }

    public int getTotalHoaDonCount(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        int count = 0;
        String sql = "SELECT COUNT(hd.maHD) FROM HoaDon hd WHERE 1=1";

        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }

        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm hóa đơn: " + e.getMessage());
        }
        return count;
    }

    public List<HoaDon> getHoaDonByPage(int page, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        int offset = (page - 1) * ITEMS_PER_PAGE;

        String sql = "SELECT hd.*, b.tenBan " +
                "FROM HoaDon hd " +
                "LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "LEFT JOIN Ban b ON ddm.maBan = b.maBan " +
                "WHERE 1=1";

        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }

        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        sql += " ORDER BY hd.ngayLap DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"; // SQL Server Syntax

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            ps.setInt(index++, offset);
            ps.setInt(index++, ITEMS_PER_PAGE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsHoaDon.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi dòng dữ liệu: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy hóa đơn theo trang: " + e.getMessage());
            e.printStackTrace();
        }
        return dsHoaDon;
    }

    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        HoaDon hoaDon = null;

        String sql = "SELECT hd.*, ddm.maKH FROM HoaDon hd " +
                "JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "WHERE ddm.maBan = ? AND hd.trangThai = N'Chưa thanh toán'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hoaDon = createHoaDonFromResultSet(rs);

                    String maKH = rs.getString("maKH");
                    hoaDon.setMaKH(maKH);
                    List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(hoaDon.getMaDon());

                    hoaDon.setDsChiTiet(dsChiTiet);
                    hoaDon.tinhLaiTongTienTuChiTiet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hoaDon;
    }

    public boolean capNhatMaKM(String maHD, String maKM) {
        String sql = "UPDATE HoaDon SET maKM = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (maKM != null && !maKM.isEmpty()) {
                ps.setString(1, maKM);
            } else {
                ps.setNull(1, java.sql.Types.NVARCHAR);
            }
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật maKM cho Hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean thanhToanHoaDon(String maHD, double tongTien, double tienKhachDua, String hinhThucTT, double tienGiamGia, String maKM, String tenBanGhiLai) {

        String sqlUpdateHD = "UPDATE HoaDon SET trangThai = N'Đã thanh toán', ngayLap = GETDATE(), tongTien = ?, tienKhachDua = ?, hinhThucThanhToan = ?, giamGia = ?, maKM = ?, tenBan = ? WHERE maHD = ?";
        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false);

            String maDonHienTai = null;
            String maBanChinh = null;
            String sqlGetInfo = "SELECT d.maDon, d.maBan FROM HoaDon h JOIN DonDatMon d ON h.maDon = d.maDon WHERE h.maHD = ?";
            try (java.sql.PreparedStatement psInfo = conn.prepareStatement(sqlGetInfo)) {
                psInfo.setString(1, maHD);
                java.sql.ResultSet rsInfo = psInfo.executeQuery();
                if (rsInfo.next()) {
                    maDonHienTai = rsInfo.getString("maDon");
                    maBanChinh = rsInfo.getString("maBan");
                }
            }

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlUpdateHD)) {
                ps.setDouble(1, tongTien);
                ps.setDouble(2, tienKhachDua);
                ps.setString(3, hinhThucTT);
                ps.setDouble(4, tienGiamGia);
                ps.setString(5, maKM);

                ps.setNString(6, tenBanGhiLai);

                ps.setString(7, maHD);
                if (ps.executeUpdate() <= 0) { conn.rollback(); return false; }
            }

            String sqlCloseDon = "UPDATE DonDatMon SET trangThai = N'Đã thanh toán' WHERE maDon = ?";
            try (java.sql.PreparedStatement psDon = conn.prepareStatement(sqlCloseDon)) {
                psDon.setString(1, maDonHienTai);
                psDon.executeUpdate();
            }

            List<String> listBanCheck = new ArrayList<>();
            if (maBanChinh != null) {
                listBanCheck.add(maBanChinh);
                String sqlLink = "SELECT maBan FROM DonDatMon WHERE ghiChu = ? AND trangThai != N'Đã thanh toán' AND trangThai != N'Đã hủy'";
                try (java.sql.PreparedStatement psLink = conn.prepareStatement(sqlLink)) {
                    psLink.setString(1, "LINKED:" + maBanChinh);
                    java.sql.ResultSet rsLink = psLink.executeQuery();
                    while(rsLink.next()) listBanCheck.add(rsLink.getString("maBan"));
                }
            }

            if (listBanCheck.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for(String s : listBanCheck) sb.append("'").append(s).append("',");
                String inClause = sb.substring(0, sb.length()-1);

                String sqlDummy = "UPDATE DonDatMon SET trangThai = N'Đã thanh toán' WHERE ghiChu = ? AND maBan IN (" + inClause + ")";
                try (java.sql.PreparedStatement psDum = conn.prepareStatement(sqlDummy)) {
                    psDum.setString(1, "LINKED:" + maBanChinh);
                    psDum.executeUpdate();
                }
            }

            String sqlUpdateBan =
                    "UPDATE Ban SET " +
                            "gioMoBan = NULL, " +
                            "trangThai = ?, " +
                            "tenBan = CASE " +
                            // Trường hợp 1: Bàn 7 (Ghép Bàn 6) -> Lấy Bàn 7
                            "WHEN CHARINDEX(N' (Ghép', tenBan) > 0 THEN RTRIM(LEFT(tenBan, CHARINDEX(N' (Ghép', tenBan) - 1)) " +
                            // Trường hợp 2: Bàn 6 + 7 + 8 -> Lấy Bàn 6
                            "WHEN CHARINDEX(N' +', tenBan) > 0 THEN RTRIM(LEFT(tenBan, CHARINDEX(N' +', tenBan) - 1)) " +
                            // Trường hợp thường: Giữ nguyên
                            "ELSE tenBan " +
                            "END " +
                            "WHERE maBan = ?";

            String sqlCount = "SELECT COUNT(*) FROM DonDatMon " +
                    "WHERE maBan = ? " +
                    "AND maDon != ? " +
                    "AND (ghiChu IS NULL OR ghiChu NOT LIKE 'LINKED:%') " +
                    "AND trangThai NOT IN (N'Đã thanh toán', N'Đã hủy')";

            try (java.sql.PreparedStatement psUpBan = conn.prepareStatement(sqlUpdateBan);
                 java.sql.PreparedStatement psCount = conn.prepareStatement(sqlCount)) {

                for (String mb : listBanCheck) {

                    psCount.setString(1, mb);
                    psCount.setString(2, maDonHienTai);

                    int soDonCho = 0;
                    java.sql.ResultSet rsCount = psCount.executeQuery();
                    if (rsCount.next()) soDonCho = rsCount.getInt(1);

                    String trangThaiMoi = (soDonCho > 0) ? "Đã đặt trước" : "Trống";

                    // Update Bàn (bao gồm cả trả tên gốc)
                    psUpBan.setNString(1, trangThaiMoi);
                    psUpBan.setString(2, mb);
                    psUpBan.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ex) {}
        }
    }
    public boolean capNhatTongTien(String maHD, float tongTienMoi) {
        String sql = "UPDATE HoaDon SET tongTien = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, tongTienMoi);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");

        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");

        String maDon = rs.getString("maDon");
        String maNV = rs.getString("maNV");
        String maKM = rs.getString("maKM");

        float tongTienLuuTrongDB = rs.getFloat("tongTien");
        float giamGia = rs.getFloat("giamGia");

        float tienKhachDua = rs.getFloat("tienKhachDua");
        float tongTienGoc = tongTienLuuTrongDB + giamGia ;


        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThucThanhToan, maDon, maNV, maKM);

        try {
            int tenBanIndex = rs.findColumn("tenBan");
            hd.setTenBan(rs.getString(tenBanIndex));
        } catch (SQLException e) {
            hd.setTenBan(null);
        }


        hd.setTienKhachDua(tienKhachDua);
        hd.setTongTienTuDB(tongTienGoc); // Gán tổng tiền gốc
        hd.setGiamGia(rs.getFloat("giamGia"));
        hd.capNhatTongThanhToanTuCacThanhPhan();

        return hd;
    }

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


            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        if (maNV == null || maNV.trim().isEmpty() || thoiGianBatDauCa == null || hinhThuc == null) {
            return 0;
        }

        double total = 0;

        // Sửa: Tính chính xác (tongTien - giamGia)
        String sql = "SELECT ISNULL(SUM(tongTien - ISNULL(giamGia, 0)), 0) AS DoanhThu " +
                "FROM HoaDon " +
                "WHERE maNV = ? " +
                "AND ngayLap >= ? " +
                "AND trangThai = N'Đã thanh toán' " +
                "AND hinhThucThanhToan = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(thoiGianBatDauCa));
            ps.setString(3, hinhThuc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("DoanhThu");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();

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
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn doanh thu hàng ngày", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi lấy doanh thu", e);
        }
        return dailyRevenue;
    }

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
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn số lượng hóa đơn", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi đếm hóa đơn", e);
        }
        return count;
    }

    public List<HoaDon> getAllHoaDonFiltered(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        List<HoaDon> dsHoaDon = new ArrayList<>();


        String sql = "SELECT hd.*, b.tenBan " +
                "FROM HoaDon hd " +
                "LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "LEFT JOIN Ban b ON ddm.maBan = b.maBan " +
                "WHERE 1=1";

        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }
        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        sql += " ORDER BY hd.ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsHoaDon.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHoaDon;
    }
}