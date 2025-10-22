package dao;
import connectDB.SQLConnection;
import entity.KhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {
	public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai"; // Tên bảng CSDL của bạn

        try (Connection conn = SQLConnection.getConnection(); // Lấy kết nối
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Đọc dữ liệu từ ResultSet và tạo đối tượng KhuyenMai
                String maKM = rs.getString("maKM");
                String tenCT = rs.getString("tenChuongTrinh");
                String loaiKM = rs.getString("loaiKhuyenMai");
                double giaTri = rs.getDouble("giaTri");
                LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                
                // Ngày kết thúc có thể NULL
                LocalDate ngayKT = null;
                if (rs.getDate("ngayKetThuc") != null) {
                    ngayKT = rs.getDate("ngayKetThuc").toLocalDate();
                }
                
                String trangThai = rs.getString("trangThai");

                KhuyenMai km = new KhuyenMai(maKM, tenCT, loaiKM, giaTri, ngayBD, ngayKT, trangThai);
                dsKhuyenMai.add(km);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKhuyenMai;
    }

    /**
     * [UPDATE] - Cập nhật (Sửa) một chương trình khuyến mãi
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenChuongTrinh = ?, loaiKhuyenMai = ?, giaTri = ?, " +
                     "ngayBatDau = ?, ngayKetThuc = ?, trangThai = ? WHERE maKM = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, km.getTenChuongTrinh());
            ps.setString(2, km.getLoaiKhuyenMai());
            ps.setDouble(3, km.getGiaTri());
            ps.setDate(4, java.sql.Date.valueOf(km.getNgayBatDau()));
            
            if (km.getNgayKetThuc() != null) {
                ps.setDate(5, java.sql.Date.valueOf(km.getNgayKetThuc()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            
            ps.setString(6, km.getTrangThai());
            ps.setString(7, km.getMaKM()); // Điều kiện WHERE

            return ps.executeUpdate() > 0; // Trả về true nếu cập nhật thành công (1 hàng bị ảnh hưởng)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
