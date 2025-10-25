package dao;

import connectDB.SQLConnection;
import entity.HoaDon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    /**
     * Chuyển ResultSet thành đối tượng HoaDon (ĐÃ CẬP NHẬT để lấy maDon).
     */
    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");
        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        float tongTien = rs.getFloat("tongTien");
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");
        // Lấy tiền khách đưa, xử lý NULL trả về 0
        float tienKhachDua = rs.getFloat("tienKhachDua");
        if (rs.wasNull()) {
            tienKhachDua = 0; // Hoặc giá trị mặc định khác nếu muốn
        }

        String maDon = rs.getString("maDon"); // <-- LẤY THÊM maDon

        // Sử dụng Constructor phù hợp hoặc setter
        HoaDon hd = new HoaDon(ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua);
        hd.setMaHD(maHD);
        hd.setMaDon(maDon); // <-- SET maDon

        return hd;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SELECT] - Lấy toàn bộ danh sách hóa đơn từ CSDL (ĐÃ CẬP NHẬT để lấy maDon).
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        // Cập nhật câu lệnh SQL: THÊM maDon
        String sql = "SELECT maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maDon FROM HoaDon ORDER BY ngayLap DESC"; // <-- THÊM maDon

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try { // Thêm try-catch để bắt lỗi từng dòng
                    dsHoaDon.add(createHoaDonFromResultSet(rs));
                } catch (Exception e) {
                    System.err.println("Lỗi khi đọc dòng hóa đơn từ ResultSet: " + e.getMessage());
                    // Có thể lấy maHD để biết dòng nào lỗi: rs.getString("maHD")
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
             System.err.println("Lỗi nghiêm trọng khi lấy danh sách hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("HoaDonDAO: Đã tải " + dsHoaDon.size() + " hóa đơn."); // Thêm log
        return dsHoaDon;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [INSERT] - Thêm một hóa đơn mới vào CSDL (Giữ nguyên nhưng đảm bảo hd có maDon).
     */
    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            ps.setFloat(3, hd.getTongTien());
            ps.setString(4, hd.getTrangThai());
            ps.setString(5, hd.getHinhThucThanhToan());
             // Xử lý tiền khách đưa có thể là 0 nếu chưa thanh toán
             ps.setFloat(6, hd.getTienKhachDua());


             // --- PHẦN KHÓA NGOẠI CẦN LẤY TỪ ĐỐI TƯỢNG HoaDon ---
             // Giả sử HoaDon entity của bạn CÓ các trường/getter này
             // Ví dụ: ps.setString(7, hd.getNhanVien().getMaNV());
             //        ps.setString(8, hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : null);
             //        ps.setString(9, hd.getMaDon());

             // *** Tạm thời dùng giá trị cứng/null để test, BẠN CẦN SỬA LẠI ***
             ps.setString(7, "NV01102"); // Lấy mã NV thực tế từ hd
             ps.setNull(8, java.sql.Types.NVARCHAR); // Lấy mã KM thực tế từ hd (xử lý null)
             ps.setString(9, hd.getMaDon()); // Đã thêm vào entity


            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi ràng buộc khóa chính (Mã HD đã tồn tại) hoặc khóa ngoại không hợp lệ.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SEARCH] - Tìm kiếm hóa đơn theo Mã HD (ĐÃ CẬP NHẬT để lấy maDon).
     */
    public List<HoaDon> timHoaDon(String tuKhoa) {
        List<HoaDon> dsKetQua = new ArrayList<>();
        // Cập nhật câu lệnh SQL: THÊM maDon
        String sql = "SELECT maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maDon " + // <-- THÊM maDon
                     "FROM HoaDon WHERE maHD LIKE ? ORDER BY ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                     try { // Thêm try-catch
                        dsKetQua.add(createHoaDonFromResultSet(rs));
                     } catch (Exception e) {
                         System.err.println("Lỗi khi đọc dòng hóa đơn (tìm kiếm) từ ResultSet: " + e.getMessage());
                         e.printStackTrace();
                     }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsKetQua;
    }
}