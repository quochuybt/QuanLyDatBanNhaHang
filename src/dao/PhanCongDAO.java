package dao;

import connectDB.SQLConnection;
import entity.CaLam;
import entity.NhanVien;
import entity.PhanCong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PhanCongDAO {

    /**
     * [ĐÃ SỬA]
     * Lấy danh sách phân công, nhưng tạo NhanVien
     * theo đúng tên setter của file NhanVien.java mới
     */
    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        List<PhanCong> dsPhanCong = new ArrayList<>();

        String sql = "SELECT " +
                "  pc.ngayLam, " +
                "  c.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, " +
                "  nv.maNV, nv.hoTen " + // Tên cột trong CSDL
                "FROM PhanCongCa pc " +
                "JOIN CaLam c ON pc.maCa = c.maCa " +
                "JOIN NhanVien nv ON pc.maNV = nv.maNV " +
                "WHERE pc.ngayLam BETWEEN ? AND ? " +
                "ORDER BY pc.ngayLam, c.gioBatDau";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 1. Lấy thông tin CaLam (Giữ nguyên)
                    String maCa = rs.getString("maCa");
                    String tenCa = rs.getString("tenCa");
                    LocalTime gioBatDau = rs.getTime("gioBatDau").toLocalTime();
                    LocalTime gioKetThuc = rs.getTime("gioKetThuc").toLocalTime();
                    CaLam ca = new CaLam(maCa, tenCa, gioBatDau, gioKetThuc);

                    // 2. [SỬA] Lấy thông tin NhanVien (dùng đúng setter)
                    NhanVien nv = new NhanVien(); // Dùng constructor rỗng
                    nv.setManv(rs.getString("maNV")); // Dùng setManv
                    nv.setHoten(rs.getString("hoTen")); // Dùng setHoten

                    // 3. Lấy thông tin PhanCong (Giữ nguyên)
                    LocalDate ngayLam = rs.getDate("ngayLam").toLocalDate();

                    // 4. Tạo đối tượng PhanCong (Giữ nguyên)
                    PhanCong pc = new PhanCong(ca, nv, ngayLam);
                    dsPhanCong.add(pc);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách phân công: " + e.getMessage());
            e.printStackTrace();
        }
        return dsPhanCong;
    }

    // --- (Các hàm themPhanCong và xoaPhanCong giữ nguyên) ---

    public boolean themPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        String sql = "INSERT INTO PhanCongCa (maNV, maCa, ngayLam) VALUES (?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maCa);
            ps.setDate(3, Date.valueOf(ngayLam));
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Lỗi khi thêm phân công: " + e.getMessage());
            return false;
        }
    }

    public boolean xoaPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        String sql = "DELETE FROM PhanCongCa WHERE maNV = ? AND maCa = ? AND ngayLam = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maCa);
            ps.setDate(3, Date.valueOf(ngayLam));
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Lỗi khi xóa phân công: " + e.getMessage());
            return false;
        }
    }
}