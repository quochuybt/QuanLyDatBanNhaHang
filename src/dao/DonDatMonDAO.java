package dao;

import connectDB.SQLConnection;
import entity.DonDatMon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.sql.Timestamp; //
import java.util.ArrayList; // Thêm import
import java.util.List;


public class DonDatMonDAO {

    /**
     * Hàm helper: Tạo đối tượng DonDatMon từ ResultSet
     */
    private DonDatMon createDonDatMonFromResultSet(ResultSet rs) throws Exception {
        String maDon = rs.getString("maDon");
        LocalDateTime ngayKhoiTao = rs.getTimestamp("ngayKhoiTao").toLocalDateTime();
        String maNV = rs.getString("maNV");
        String maKH = rs.getString("maKH");
        String maBan = rs.getString("maBan");
        String ghiChu = rs.getString("ghiChu");
        return new DonDatMon(maDon, ngayKhoiTao, maNV, maKH, maBan,ghiChu);
    }

    /**
     * [SEARCH] Tìm một Đơn Đặt Món (chưa có Hóa Đơn) dựa vào Mã Bàn
     * Đây là logic để xác định một bàn "Đã đặt trước"
     */
    public DonDatMon getDonDatMonDatTruoc(String maBan) {
        DonDatMon ddm = null;

        // Tìm DonDatMon theo maBan MÀ maDon đó CHƯA TỒN TẠI trong bảng HoaDon
        String sql = "SELECT * FROM DonDatMon " +
                "WHERE maBan = ? " +
                "AND maDon NOT IN (SELECT maDon FROM HoaDon WHERE maDon IS NOT NULL)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ddm = createDonDatMonFromResultSet(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ddm; // Trả về null nếu không tìm thấy
    }
    public boolean themDonDatMon(DonDatMon ddm) {
        String sql = "INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ddm.getMaDon());
            ps.setTimestamp(2, Timestamp.valueOf(ddm.getNgayKhoiTao()));
            ps.setString(3, ddm.getMaNV());
            ps.setString(4, ddm.getMaKH()); // Có thể null
            ps.setString(5, ddm.getMaBan()); // Có thể null (nếu đặt mang về)
            ps.setString(6, ddm.getGhiChu());

            return ps.executeUpdate() > 0;


        } catch (Exception e) {
            System.err.println("Lỗi khi thêm DonDatMon: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
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
        List<DonDatMon> dsDonDat = new ArrayList<>();
        // Lấy các DonDatMon mà maDon không tồn tại trong HoaDon
        String sql = "SELECT * FROM DonDatMon ddm " +
                "WHERE NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = ddm.maDon) " +
                "ORDER BY ddm.ngayKhoiTao DESC"; // Sắp xếp mới nhất lên đầu

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsDonDat.add(createDonDatMonFromResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsDonDat;
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