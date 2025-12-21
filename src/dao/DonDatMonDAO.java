package dao;

import connectDB.SQLConnection;
import entity.DonDatMon;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DonDatMonDAO {

    public DonDatMon getDonDatMonByMa(String maDon) {
        DonDatMon ddm = null;
        String sql = "SELECT * FROM DonDatMon WHERE maDon = ?";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ddm = createDonDatMonFromResultSet(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ddm;
    }
    private DonDatMon createDonDatMonFromResultSet(ResultSet rs) throws Exception {
        String maDon = rs.getString("maDon");
        LocalDateTime ngayKhoiTao = rs.getTimestamp("ngayKhoiTao").toLocalDateTime();
        LocalDateTime thoiGianDen = null;
        if (rs.getTimestamp("thoiGianDen") != null) {
            thoiGianDen = rs.getTimestamp("thoiGianDen").toLocalDateTime();
        }
        String trangThai = rs.getString("trangThai");
        String maNV = rs.getString("maNV");
        String maKH = rs.getString("maKH");
        String maBan = rs.getString("maBan");
        String ghiChu = rs.getString("ghiChu");
        DonDatMon ddm = new DonDatMon(maDon, ngayKhoiTao, maNV, maKH, maBan, ghiChu);
        ddm.setThoiGianDen(thoiGianDen);
        ddm.setTrangThai(trangThai);
        ddm.setMaKH(maKH);
        return ddm;
    }
    public List<String> getMaBanCungDotDat(String maKH, LocalDateTime thoiGianDen, String maBanHienTai) {
        List<String> dsMaBan = new ArrayList<>();
        if (maKH == null || maKH.isEmpty()) {
            return dsMaBan;
        }

        String sql = "SELECT maBan FROM DonDatMon " +
                "WHERE maKH = ? " +
                "AND maBan != ? " +
                "AND trangThai != N'Đã hủy' " +
                "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = DonDatMon.maDon) " +
                "AND DATEDIFF(MINUTE, thoiGianDen, ?) = 0";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKH);
            ps.setString(2, maBanHienTai);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(thoiGianDen));

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsMaBan.add(rs.getString("maBan"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsMaBan;
    }
    public String getMaBanByMaDon(String maDon) {
        String maBan = null;
        String sql = "SELECT maBan FROM DonDatMon WHERE maDon = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maBan = rs.getString("maBan");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy maBan theo maDon: " + e.getMessage());
            e.printStackTrace();
        }
        return maBan;
    }

    public int tuDongHuyDonQuaGio() {
        int soDonHuy = 0;
        String sqlFind = "SELECT maDon, maBan FROM DonDatMon ddm " +
                "WHERE ddm.thoiGianDen < ? " +
                "AND ddm.trangThai = N'Chưa thanh toán' " +
                "AND ddm.trangThai != N'Đã hủy' " +
                "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon)";

        //cập nhật trạng thái Đơn
        String sqlUpdateDon = "UPDATE DonDatMon SET trangThai = N'Đã hủy', ghiChu = ghiChu + N' (Hủy tự động do quá giờ)' WHERE maDon = ?";

        //cập nhật trạng thái Bàn
        String sqlUpdateBan = "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL WHERE maBan = ?";

        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false);

            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            List<String[]> listQuaHan = new ArrayList<>();

            try (java.sql.PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
                psFind.setTimestamp(1, java.sql.Timestamp.valueOf(oneHourAgo));
                try (java.sql.ResultSet rs = psFind.executeQuery()) {
                    while (rs.next()) {
                        listQuaHan.add(new String[]{rs.getString("maDon"), rs.getString("maBan")});
                    }
                }
            }

            if (!listQuaHan.isEmpty()) {
                try (java.sql.PreparedStatement psUpDon = conn.prepareStatement(sqlUpdateDon);
                     java.sql.PreparedStatement psUpBan = conn.prepareStatement(sqlUpdateBan)) {

                    for (String[] item : listQuaHan) {
                        String maDon = item[0];
                        String maBan = item[1];

                        psUpDon.setString(1, maDon);
                        psUpDon.executeUpdate();

                        String sqlSafeUpdateBan = "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL WHERE maBan = ? AND trangThai = N'Đã đặt trước'";
                        try(java.sql.PreparedStatement psSafe = conn.prepareStatement(sqlSafeUpdateBan)) {
                            psSafe.setString(1, maBan);
                            psSafe.executeUpdate();
                        }

                        soDonHuy++;
                    }
                }
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (java.sql.SQLException ex) {}
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (java.sql.SQLException ex) {}
        }
        return soDonHuy;
    }
    public String getMaBanDichCuaBanGhep(String maBanHienTai) {
        String sql = "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND trangThai != N'Đã hủy'";
        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBanHienTai);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ghiChu = rs.getString("ghiChu");
                    if (ghiChu != null && ghiChu.startsWith("LINKED:")) {
                        return ghiChu.substring(7).trim(); // Cắt bỏ chữ "LINKED:" để lấy mã bàn đích
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void capNhatTrangThaiBanTheoGio() {
        Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlLock =
                    "UPDATE Ban SET trangThai = N'Đã đặt trước' " +
                            "WHERE trangThai = N'Trống' " +
                            "AND maBan IN ( " +
                            "SELECT maBan FROM DonDatMon " +
                            "WHERE trangThai = N'Chưa thanh toán' " +
                            "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = DonDatMon.maDon) " +
                            "AND DATEDIFF(MINUTE, GETDATE(), thoiGianDen) <= 120 " +
                            ")";
            String sqlUnlock =
                    "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL " +
                            "WHERE trangThai = N'Đã đặt trước' " +
                            "AND maBan NOT IN ( " +
                            "SELECT maBan FROM DonDatMon " +
                            "WHERE trangThai = N'Chưa thanh toán' " +
                            "AND DATEDIFF(MINUTE, GETDATE(), thoiGianDen) <= 120 " +
                            ")";

            try (PreparedStatement psLock = conn.prepareStatement(sqlLock);
                 PreparedStatement psUnlock = conn.prepareStatement(sqlUnlock)) {

                int locked = psLock.executeUpdate();
                int unlocked = psUnlock.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
        } finally {
            try { if(conn!=null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception ex){}
        }
    }
    public DonDatMon getDonDatMonDatTruoc(String maBan) {
        DonDatMon ddm = null;
        String sql = "SELECT TOP 1 * FROM DonDatMon " +
                "WHERE maBan = ? " +
                "AND trangThai = N'Chưa thanh toán' " +
                "AND (ghiChu IS NULL OR ghiChu NOT LIKE 'LINKED:%') " +
                "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = DonDatMon.maDon) " +
                "ORDER BY thoiGianDen ASC";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ddm = createDonDatMonFromResultSet(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ddm;
    }
    public boolean themDonDatMon(DonDatMon ddm) {
        String sql = "INSERT INTO DonDatMon (maDon, ngayKhoiTao, thoiGianDen, maNV, maKH, maBan, ghiChu, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ddm.getMaDon());
            ps.setTimestamp(2, Timestamp.valueOf(ddm.getNgayKhoiTao()));
            if (ddm.getThoiGianDen() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(ddm.getThoiGianDen()));
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(4, ddm.getMaNV());
            ps.setString(5, ddm.getMaKH());
            ps.setString(6, ddm.getMaBan());
            ps.setNString(7, ddm.getGhiChu());
            String trangThai = ddm.getTrangThai();
            if (trangThai == null || trangThai.isEmpty()) {
                trangThai = "Chưa thanh toán";
            }
            ps.setNString(8, trangThai);
            return ps.executeUpdate() > 0;


        } catch (Exception e) {
            System.err.println("Lỗi khi thêm DonDatMon: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean huyDonDatMon(String maDon) {
        String sql = "UPDATE DonDatMon SET trangThai = N'Đã hủy' WHERE maDon = ?";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        List<String> dsMaBan = new ArrayList<>();

        String sql = "SELECT DISTINCT maBan FROM DonDatMon " +
                "WHERE thoiGianDen BETWEEN ? AND ? " +
                "AND trangThai != N'Đã hủy' " +
                "AND trangThai != N'Đã thanh toán'";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(tuGio));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(denGio));

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsMaBan.add(rs.getString("maBan"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsMaBan;
    }
    public boolean capNhatMaKH(String maDon, String maKH) {
        String sql = "UPDATE DonDatMon SET maKH = ? WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (maKH != null && !maKH.isEmpty()) {
                ps.setString(1, maKH);
            } else {
                ps.setNull(1, java.sql.Types.NVARCHAR);
            }
            ps.setString(2, maDon);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật maKH cho Đơn Đặt Món " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean xoaDonDatMon(String maDon) {
        String sql = "DELETE FROM DonDatMon WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
public List<DonDatMon> getAllDonDatMonChuaNhan() {
    List<DonDatMon> ds = new ArrayList<>();

    String sql = "SELECT * FROM DonDatMon d " +
            "WHERE d.trangThai = N'Chưa thanh toán' " +
            "AND NOT EXISTS (SELECT 1 FROM HoaDon h WHERE h.maDon = d.maDon) " +
            "AND (d.ghiChu IS NULL OR d.ghiChu NOT LIKE '%LINKED:%') " +
            "AND d.thoiGianDen >= CAST(GETDATE() AS DATE) " +
            "ORDER BY d.thoiGianDen ASC";
    try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
         java.sql.Statement stmt = conn.createStatement();
         java.sql.ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            ds.add(createDonDatMonFromResultSet(rs));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ds;
}
    public List<DonDatMon> timDonDatMonChuaNhan(String query) {
        List<DonDatMon> dsKetQua = new ArrayList<>();
        String sql = "SELECT ddm.* FROM DonDatMon ddm " +
                "LEFT JOIN KhachHang kh ON ddm.maKH = kh.maKH " +
                "WHERE NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon) " +
                "AND ddm.trangThai = N'Chưa thanh toán' " +
                "AND ddm.trangThai != N'Đã hủy' " +
                "AND (ddm.ghiChu IS NULL OR ddm.ghiChu NOT LIKE 'LINKED:%') " +
                "AND (kh.sdt LIKE ? OR kh.tenKH LIKE ?) " +
                "ORDER BY ddm.ngayKhoiTao DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeQuery = "%" + query + "%";
            ps.setString(1, likeQuery);
            ps.setString(2, likeQuery);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsKetQua.add(createDonDatMonFromResultSet(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKetQua;
    }
    public boolean capNhatGhiChu(String maDon, String ghiChu) {
        String sql = "UPDATE DonDatMon SET ghiChu = ? WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, ghiChu);
            ps.setString(2, maDon);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật ghi chú cho đơn " + maDon + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}