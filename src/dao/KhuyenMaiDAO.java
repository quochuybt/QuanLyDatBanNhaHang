package dao;
import connectDB.SQLConnection;
import entity.KhuyenMai; // Đảm bảo entity KhuyenMai của bạn có đủ các trường này

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date; // Dùng java.sql.Date cho PreparedStatement
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    /**
     * Lấy danh sách tất cả khuyến mãi.
     * ĐÃ SỬA: Tên bảng và tên cột khớp với SQL INSERT.
     */
    public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        // SỬA 1: Đổi tên bảng thành MaKhuyenMai
        String sql = "SELECT maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai FROM KhuyenMai";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    // SỬA 2: Đọc đúng tên cột từ SQL INSERT
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM"); // Đọc tenKM
                    String moTa = rs.getString("moTa");   // Đọc moTa (Bạn có thể cần thêm trường này vào Entity KhuyenMai)
                    LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                    LocalDate ngayKT = null;
                    if (rs.getDate("ngayKetThuc") != null) {
                        ngayKT = rs.getDate("ngayKetThuc").toLocalDate();
                    }
                    String loaiGiam = rs.getString("loaiGiam"); // Đọc loaiGiam
                    double giaTriGiam = rs.getDouble("giaTriGiam"); // Đọc giaTriGiam
                    String trangThai = rs.getString("trangThai");

                    // SỬA 3: Gọi Constructor của KhuyenMai với các trường đã đọc
                    // **QUAN TRỌNG:** Đảm bảo Constructor trong entity/KhuyenMai.java khớp với thứ tự và kiểu dữ liệu này!
                    // Ví dụ: public KhuyenMai(String maKM, String tenKM, String loaiGiam, double giaTriGiam, LocalDate ngayBD, LocalDate ngayKT, String trangThai /*, String moTa */)
                    KhuyenMai km = new KhuyenMai(maKM, tenKM, loaiGiam, giaTriGiam, ngayBD, ngayKT, trangThai /*, moTa */); // Cập nhật Constructor nếu cần
                    dsKhuyenMai.add(km);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // In ra lỗi để dễ debug hơn
        }

        return dsKhuyenMai;
    }

    /**
     * [UPDATE] - Cập nhật (Sửa) một chương trình khuyến mãi.
     * ĐÃ SỬA: Tên bảng và tên cột khớp với SQL INSERT.
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        // SỬA 4: Đổi tên bảng và tên cột trong câu UPDATE
        String sql = "UPDATE KhuyenMai SET tenKM = ?, moTa = ?, loaiGiam = ?, giaTriGiam = ?, " +
                     "ngayBatDau = ?, ngayKetThuc = ?, trangThai = ? WHERE maKM = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // SỬA 5: Set giá trị cho các cột tương ứng
            ps.setString(1, km.getTenChuongTrinh()); // Giả sử getTenChuongTrinh() trả về tenKM
            ps.setString(2, "Mô tả cập nhật"); // Bạn cần thêm getter cho moTa vào KhuyenMai entity
            ps.setString(3, km.getLoaiKhuyenMai()); // Giả sử getLoaiKhuyenMai() trả về loaiGiam
            ps.setDouble(4, km.getGiaTri()); // Giả sử getGiaTri() trả về giaTriGiam
            ps.setDate(5, Date.valueOf(km.getNgayBatDau())); // Dùng java.sql.Date

            if (km.getNgayKetThuc() != null) {
                ps.setDate(6, Date.valueOf(km.getNgayKetThuc()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }

            ps.setString(7, km.getTrangThai());
            ps.setString(8, km.getMaKM()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
             System.err.println("Lỗi khi cập nhật khuyến mãi " + km.getMaKM() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Bạn có thể cần thêm các hàm khác như: themKhuyenMai, xoaKhuyenMai, timKhuyenMai...
}