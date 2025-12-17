package dao;

import connectDB.SQLConnection;
import entity.HangThanhVien;
import entity.KhachHang;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    /**
     * [SELECT] - Lấy toàn bộ danh sách khách hàng từ CSDL
     */
    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> dsKhachHang = new ArrayList<>();
        // Đảm bảo tên cột khớp với CSDL của bạn (Bảng: KhachHang)
        String sql = "SELECT maKH, tenKH, gioitinh, sdt, email, ngaySinh, diaChi, ngayThamGia, tongChiTieu, hangThanhVien FROM KhachHang";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String maKH = rs.getString("maKH");
                String tenKH = rs.getString("tenKH");
                String gioitinh = rs.getString("gioitinh");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");

                // Xử lý các trường Date
                LocalDate ngaySinh = (rs.getDate("ngaySinh") == null) ? null : rs.getDate("ngaySinh").toLocalDate();
                String diaChi = rs.getString("diaChi");
                LocalDate ngayThamGia = rs.getDate("ngayThamGia").toLocalDate();

                float tongChiTieu = rs.getFloat("tongChiTieu");
                // Chuyển String từ CSDL thành Enum
                HangThanhVien hangTV = HangThanhVien.valueOf(rs.getString("hangThanhVien").toUpperCase());

                KhachHang kh = new KhachHang(maKH, tenKH, gioitinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
                dsKhachHang.add(kh);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKhachHang;
    }

// --------------------------------------------------------------------------------------------------------------------------

    /**
     * [INSERT] - Thêm một khách hàng mới vào CSDL
     */
    public boolean themKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (maKH, tenKH, gioitinh, sdt, email, ngaySinh, diaChi, ngayThamGia, tongChiTieu, hangThanhVien) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getGioitinh());
            ps.setString(4, kh.getSdt());
            ps.setString(5, kh.getEmail());
            ps.setDate(6, Date.valueOf(kh.getNgaySinh()));
            ps.setString(7, kh.getDiaChi());
            ps.setDate(8, Date.valueOf(kh.getNgayThamGia()));
            ps.setFloat(9, kh.getTongChiTieu());
            ps.setString(10, kh.getHangThanhVien().toString());

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi trùng Mã Khách hàng: " + kh.getMaKH());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

// --------------------------------------------------------------------------------------------------------------------------

    /**
     * [UPDATE] - Cập nhật thông tin khách hàng trong CSDL
     */
    public boolean updateKhachHang(KhachHang kh) {
        // Lưu ý: maKH, tongChiTieu, va hangThanhVien KHÔNG được sửa trực tiếp qua form,
        // nhưng ta vẫn gửi chúng lên để đảm bảo tính toàn vẹn.
        String sql = "UPDATE KhachHang SET tenKH = ?, gioitinh = ?, sdt = ?, email = ?, ngaySinh = ?, diaChi = ?, " +
                "ngayThamGia = ?, tongChiTieu = ?, hangThanhVien = ? WHERE maKH = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getGioitinh());
            ps.setString(3, kh.getSdt());
            ps.setString(4, kh.getEmail());
            ps.setDate(5, Date.valueOf(kh.getNgaySinh()));
            ps.setString(6, kh.getDiaChi());
            ps.setDate(7, Date.valueOf(kh.getNgayThamGia()));
            ps.setFloat(8, kh.getTongChiTieu());
            ps.setString(9, kh.getHangThanhVien().toString());
            ps.setString(10, kh.getMaKH()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

// --------------------------------------------------------------------------------------------------------------------------

    /**
     * [SEARCH] - Tìm kiếm khách hàng theo SDT hoặc Tên (ví dụ)
     */
    public List<KhachHang> timKhachHang(String tuKhoa) {
        List<KhachHang> dsKetQua = new ArrayList<>();
        // Tìm kiếm theo tên hoặc số điện thoại
        String sql = "SELECT * FROM KhachHang WHERE tenKH LIKE ? OR sdt LIKE ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");
            ps.setString(2, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maKH = rs.getString("maKH");
                    String tenKH = rs.getString("tenKH");
                    String gioitinh = rs.getString("gioitinh");
                    String sdt = rs.getString("sdt");
                    String email = rs.getString("email");
                    LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                    String diaChi = rs.getString("diaChi");
                    LocalDate ngayThamGia = rs.getDate("ngayThamGia").toLocalDate();
                    float tongChiTieu = rs.getFloat("tongChiTieu");
                    HangThanhVien hangTV = HangThanhVien.valueOf(rs.getString("hangThanhVien").toUpperCase());

                    KhachHang kh = new KhachHang(maKH, tenKH, gioitinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
                    dsKetQua.add(kh);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKetQua;
    }
    public KhachHang timTheoMaKH(String maKH) {
        // Câu lệnh SQL tìm chính xác 1 người
        String sql = "SELECT * FROM KhachHang WHERE maKH = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // (Copy logic từ hàm getAllKhachHang của bạn)
                    String tenKH = rs.getString("tenKH");
                    String gioitinh = rs.getString("gioitinh");
                    String sdt = rs.getString("sdt");
                    String email = rs.getString("email");
                    LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                    String diaChi = rs.getString("diaChi");
                    LocalDate ngayThamGia = rs.getDate("ngayThamGia").toLocalDate();
                    float tongChiTieu = rs.getFloat("tongChiTieu");
                    HangThanhVien hangTV = HangThanhVien.valueOf(rs.getString("hangThanhVien").toUpperCase());

                    return new KhachHang(maKH, tenKH, gioitinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public KhachHang timTheoSDT(String sdt) {
        // Câu lệnh SQL tìm chính xác 1 người
        String sql = "SELECT * FROM KhachHang WHERE sdt = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sdt);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // (Copy logic từ hàm getAllKhachHang của bạn)
                    String maKH = rs.getString("maKH");
                    String tenKH = rs.getString("tenKH");
                    String gioitinh = rs.getString("gioitinh");
                    String email = rs.getString("email");
                    LocalDate ngaySinh = rs.getDate("ngaySinh").toLocalDate();
                    String diaChi = rs.getString("diaChi");
                    LocalDate ngayThamGia = rs.getDate("ngayThamGia").toLocalDate();
                    float tongChiTieu = rs.getFloat("tongChiTieu");
                    HangThanhVien hangTV = HangThanhVien.valueOf(rs.getString("hangThanhVien").toUpperCase());

                    return new KhachHang(maKH, tenKH, gioitinh, sdt, ngaySinh, diaChi, email, ngayThamGia, tongChiTieu, hangTV);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}