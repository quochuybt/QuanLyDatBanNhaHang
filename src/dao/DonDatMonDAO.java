package dao;

import connectDB.SQLConnection;
import entity.DonDatMon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

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

        // Dùng constructor mới (từ Bước 1)
        return new DonDatMon(maDon, ngayKhoiTao, maNV, maKH, maBan);
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
}