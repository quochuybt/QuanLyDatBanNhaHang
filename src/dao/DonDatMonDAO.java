package dao;

import connectDB.SQLConnection;
import entity.DonDatMon;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DonDatMonDAO {

    /**
     * Hàm helper: Tạo đối tượng DonDatMon từ ResultSet
     */
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
            System.out.println("DEBUG: Tìm thấy các bàn cùng nhóm với " + maBanHienTai + ": " + dsMaBan);
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

    /**
     * [SEARCH] Tìm một Đơn Đặt Món (chưa có Hóa Đơn) dựa vào Mã Bàn
     * Đây là logic để xác định một bàn "Đã đặt trước"
     */
    public int tuDongHuyDonQuaGio() {
        int soDonHuy = 0;
        // 1. Tìm các đơn quá hạn (Quá 1 tiếng so với thoiGianDen)
        // Điều kiện: Chưa có hóa đơn (nghĩa là khách chưa đến) VÀ Chưa hủy
        String sqlFind = "SELECT maDon, maBan FROM DonDatMon ddm " +
                "WHERE ddm.thoiGianDen < ? " + // Quá giờ hẹn
                "AND ddm.trangThai = N'Chưa thanh toán' " +
                "AND ddm.trangThai != N'Đã hủy' " +
                "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon)"; // Chưa có HĐ

        // SQL cập nhật trạng thái Đơn
        String sqlUpdateDon = "UPDATE DonDatMon SET trangThai = N'Đã hủy', ghiChu = ghiChu + N' (Hủy tự động do quá giờ)' WHERE maDon = ?";

        // SQL cập nhật trạng thái Bàn
        String sqlUpdateBan = "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL WHERE maBan = ?";

        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction để đảm bảo toàn vẹn dữ liệu

            // Tính thời điểm giới hạn (Hiện tại trừ 1 tiếng)
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

                        // Cập nhật Đơn
                        psUpDon.setString(1, maDon);
                        psUpDon.executeUpdate();

                        // Cập nhật Bàn (Chỉ update nếu bàn đó đang ở trạng thái Đặt trước)
                        // Cẩn thận: Nếu nhân viên đã mở bàn cho khách khác ngồi vào rồi thì không được set Trống bừa bãi.
                        // Nên check thêm trạng thái bàn hiện tại, nhưng để đơn giản ta cứ set Trống nếu nó đang Đã đặt.
                        // Tốt nhất là câu lệnh update bàn nên có thêm WHERE trangThai = 'Đã đặt trước'
                        String sqlSafeUpdateBan = "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL WHERE maBan = ? AND trangThai = N'Đã đặt trước'";
                        try(java.sql.PreparedStatement psSafe = conn.prepareStatement(sqlSafeUpdateBan)) {
                            psSafe.setString(1, maBan);
                            psSafe.executeUpdate();
                        }

                        soDonHuy++;
                    }
                }
            }

            conn.commit(); // Xác nhận thay đổi
            if (soDonHuy > 0) {
                System.out.println("Hệ thống: Đã tự động hủy " + soDonHuy + " đơn đặt bàn quá giờ.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (java.sql.SQLException ex) {}
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (java.sql.SQLException ex) {}
        }
        return soDonHuy;
    }
    public String getMaBanDichCuaBanGhep(String maBanHienTai) {
        // Tìm đơn đang treo của bàn hiện tại
        String sql = "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND trangThai != N'Đã hủy'";
        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBanHienTai);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ghiChu = rs.getString("ghiChu");
                    // Kiểm tra xem ghi chu có dạng "LINKED:BAN05" không
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
    public DonDatMon getDonDatMonDatTruoc(String maBan) {
        DonDatMon ddm = null;
        // SỬA CÂU SQL:
        // 1. Thêm điều kiện chưa có hóa đơn (chưa ăn)
        // 2. Thêm điều kiện chưa hủy
        // 3. QUAN TRỌNG NHẤT: ORDER BY thoiGianDen ASC (Tăng dần) -> Để lấy cái giờ sớm nhất (17h trước 19h)
        String sql = "SELECT TOP 1 * FROM DonDatMon " +
                "WHERE maBan = ? " +
                "AND trangThai != N'Đã hủy' " +
                "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = DonDatMon.maDon) " +
                "ORDER BY thoiGianDen ASC"; // <-- DÒNG QUYẾT ĐỊNH

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
        String sql = "INSERT INTO DonDatMon (maDon, ngayKhoiTao,thoiGianDen, maNV, maKH, maBan, ghiChu,trangThai) VALUES (?,?, ?, ?, ?, ?, ?,?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ddm.getMaDon());
            ps.setTimestamp(2, Timestamp.valueOf(ddm.getNgayKhoiTao()));
            if (ddm.getThoiGianDen() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(ddm.getThoiGianDen()));
            } else {
                // Nếu khách vãng lai vào ăn luôn, thoiGianDen = ngayKhoiTao
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(4, ddm.getMaNV());
            ps.setString(5, ddm.getMaKH()); // Có thể null
            ps.setString(6, ddm.getMaBan()); // Có thể null (nếu đặt mang về)
            ps.setString(7, ddm.getGhiChu());
            String trangThai = ddm.getTrangThai();
            if (trangThai == null || trangThai.isEmpty()) {
                trangThai = "Chưa thanh toán";
            }
            ps.setString(8, trangThai);

            return ps.executeUpdate() > 0;


        } catch (Exception e) {
            System.err.println("Lỗi khi thêm DonDatMon: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        List<String> dsMaBan = new ArrayList<>();

        String sql = "SELECT DISTINCT maBan FROM DonDatMon " +
                "WHERE thoiGianDen BETWEEN ? AND ? " +
                "AND trangThai != N'Đã hủy' " +          // <--- QUAN TRỌNG: Không tính đơn đã hủy
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
        // Cập nhật bảng DonDatMon
        String sql = "UPDATE DonDatMon SET maKH = ? WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (maKH != null && !maKH.isEmpty()) {
                ps.setString(1, maKH); // Đặt mã KH
            } else {
                ps.setNull(1, java.sql.Types.NVARCHAR); // Đặt là NULL
            }
            ps.setString(2, maDon); // Điều kiện WHERE là maDon

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật maKH cho Đơn Đặt Món " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean xoaDonDatMon(String maDon) {
        // Cẩn thận: Nếu ChiTietHoaDon có khóa ngoại tới DonDatMon, bạn cần xóa chi tiết trước
        // Hoặc cài đặt ON DELETE CASCADE trong CSDL.
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

    String sql = "SELECT * FROM DonDatMon ddm " +
            "WHERE NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon) " +
            "AND ddm.trangThai != N'Đã hủy' " +
            "AND (ddm.ghiChu IS NULL OR ddm.ghiChu NOT LIKE 'LINKED:%') " + // <-- THÊM DÒNG NÀY
            "ORDER BY ddm.thoiGianDen ASC";

    try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
         java.sql.Statement stmt = conn.createStatement();
         java.sql.ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            // Sử dụng hàm create... mới đã sửa (đọc đủ thoiGianDen, trangThai)
            ds.add(createDonDatMonFromResultSet(rs));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ds;
}
    public List<DonDatMon> timDonDatMonChuaNhan(String query) {
        List<DonDatMon> dsKetQua = new ArrayList<>();
        // JOIN với KhachHang để tìm theo tên hoặc SĐT
        String sql = "SELECT ddm.* FROM DonDatMon ddm " +
                "LEFT JOIN KhachHang kh ON ddm.maKH = kh.maKH " + // LEFT JOIN phòng trường hợp KH null
                "WHERE NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon) " + // Chỉ lấy đơn chưa nhận
                "AND (kh.sdt LIKE ? OR kh.tenKH LIKE ?) " + // Điều kiện tìm kiếm
                "ORDER BY ddm.ngayKhoiTao DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeQuery = "%" + query + "%";
            ps.setString(1, likeQuery); // Tìm sdt
            ps.setString(2, likeQuery); // Tìm tenKH

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
}