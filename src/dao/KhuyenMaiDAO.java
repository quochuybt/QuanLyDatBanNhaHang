package dao;

import connectDB.SQLConnection;
import entity.KhuyenMai;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class KhuyenMaiDAO {

    /**
     * - Tự động cập nhật trạng thái các KM đã hết hạn
     * Chuyển "Đang áp dụng" -> "Ngưng áp dụng" nếu ngayKetThuc < hôm nay
     */
    public void autoUpdateExpiredStatuses() {
        String sql = "UPDATE KhuyenMai SET trangThai = N'Ngưng áp dụng' " +
                "WHERE ngayKetThuc < ? AND trangThai = N'Đang áp dụng'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now())); // Ngày hôm nay
            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println("Lỗi khi tự động cập nhật trạng thái khuyến mãi: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Lấy danh sách tất cả khuyến mãi.
     */
    public List<KhuyenMai> getAllKhuyenMai() {
        // [CẬP NHẬT] Gọi hàm tự động cập nhật trước khi lấy danh sách
        autoUpdateExpiredStatuses();

        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM");
                    String moTa = rs.getString("moTa");
                    LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                    LocalDate ngayKT = null;
                    if (rs.getDate("ngayKetThuc") != null) {
                        ngayKT = rs.getDate("ngayKetThuc").toLocalDate();
                    }
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String trangThai = rs.getString("trangThai");

                    KhuyenMai km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam,dieuKien, ngayBD, ngayKT, trangThai);
                    dsKhuyenMai.add(km);
                } catch (Exception e) {
                    System.err.println("Lỗi khi đọc dữ liệu khuyến mãi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKhuyenMai;
    }

    /**
     * Cập nhật  một chương trình khuyến mãi.
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM = ?, moTa = ?, loaiGiam = ?, giaTriGiam = ?, " +
                "ngayBatDau = ?, ngayKetThuc = ?, trangThai = ?, dieuKienApDung = ? WHERE maKM = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getTenChuongTrinh());
            ps.setString(2, km.getMoTa());
            ps.setString(3, km.getLoaiKhuyenMai());
            ps.setDouble(4, km.getGiaTri());
            ps.setDate(5, Date.valueOf(km.getNgayBatDau()));

            if (km.getNgayKetThuc() != null) {
                ps.setDate(6, Date.valueOf(km.getNgayKetThuc()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setString(7, km.getTrangThai());
            ps.setDouble(8, km.getDieuKienApDung()); // Thêm tham số 8
            ps.setString(9, km.getMaKM());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật khuyến mãi " + km.getMaKM() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * - Thêm một khuyến mãi mới.
     */
    public boolean themKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKM, tenKM, moTa, loaiGiam, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai, dieuKienApDung) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getMaKM());
            ps.setString(2, km.getTenChuongTrinh());
            ps.setString(3, km.getMoTa());
            ps.setString(4, km.getLoaiKhuyenMai());
            ps.setDouble(5, km.getGiaTri());
            ps.setDate(6, Date.valueOf(km.getNgayBatDau()));

            if (km.getNgayKetThuc() != null) {
                ps.setDate(7, Date.valueOf(km.getNgayKetThuc()));
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }
            ps.setString(8, km.getTrangThai());
            ps.setDouble(9, km.getDieuKienApDung());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm khuyến mãi " + km.getMaKM() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * [DELETE] - Xóa một khuyến mãi dựa trên mã.
     */
    public boolean xoaKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa khuyến mãi " + maKM + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Hàm này sẽ cập nhật trạng thái thành "Ngưng áp dụng" thay vì XÓA vĩnh viễn
     */

    /**
     * [SEARCH & FILTER] - Tìm kiếm và lọc khuyến mãi.
     */
    public List<KhuyenMai> timKiemVaLoc(String tuKhoa, String trangThai) {
        // Gọi hàm tự động cập nhật trước khi tìm
        autoUpdateExpiredStatuses();

        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai, dieuKienApDung FROM KhuyenMai WHERE 1=1";
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty() && !tuKhoa.equals("Tìm kiếm khuyến mãi");
        boolean coTrangThai = trangThai != null && !trangThai.equals("Lọc khuyến mãi");

        if (coTuKhoa) {
            sql += " AND (maKM LIKE ? OR tenKM LIKE ?)";
        }
        if (coTrangThai) {
            sql += " AND trangThai = ?";
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (coTuKhoa) {
                String searchTerm = "%" + tuKhoa + "%";
                ps.setString(paramIndex++, searchTerm);
                ps.setString(paramIndex++, searchTerm);
            }
            if (coTrangThai) {
                ps.setString(paramIndex++, trangThai);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM");
                    String moTa = rs.getString("moTa");
                    LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                    LocalDate ngayKT = null;
                    if (rs.getDate("ngayKetThuc") != null) {
                        ngayKT = rs.getDate("ngayKetThuc").toLocalDate();
                    }
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String trangThaiDB = rs.getString("trangThai");

                    KhuyenMai km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam,dieuKien, ngayBD, ngayKT, trangThaiDB);
                    dsKhuyenMai.add(km);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKhuyenMai;
    }
    public KhuyenMai getKhuyenMaiHopLeByMa(String maKM) {
        KhuyenMai km = null;
        // Câu lệnh SQL tìm mã KM VÀ kiểm tra điều kiện hợp lệ
        String sql = "SELECT maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai, dieuKienApDung " +
                "FROM KhuyenMai " +
                "WHERE maKM = ? " +
                "AND trangThai = N'Đang áp dụng' " +
                "AND ngayBatDau <= ? " + // <-- So sánh DATETIME
                "AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?)"; // Ngày kết thúc phải NULL hoặc >= hôm nay

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            ps.setString(1, maKM);           // Tham số 1: mã KM cần tìm
            ps.setTimestamp(2, Timestamp.valueOf(now)); // Tham số 2: ngày hiện tại (cho ngayBatDau)
            ps.setTimestamp(3, Timestamp.valueOf(now)); // Tham số 3: ngày hiện tại (cho ngayKetThuc)

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Nếu tìm thấy MỘT dòng thỏa mãn
                    // Tạo đối tượng KhuyenMai từ ResultSet (giống hàm getAll)
                    String tenKM = rs.getString("tenKM");
                    String moTa = rs.getString("moTa");
                    LocalDate ngayBD = rs.getTimestamp("ngayBatDau").toLocalDateTime().toLocalDate();
                    LocalDate ngayKT = null;
                    Timestamp ngayKTTimestamp = rs.getTimestamp("ngayKetThuc");
                    if (ngayKTTimestamp != null) {
                        ngayKT = ngayKTTimestamp.toLocalDateTime().toLocalDate();
                    }
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String trangThai = rs.getString("trangThai"); // Sẽ luôn là "Đang áp dụng"

                    km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam, dieuKien,ngayBD, ngayKT, trangThai);
                }else {
                    System.out.println("DEBUG: Mã KM '" + maKM + "' không hợp lệ hoặc không tìm thấy (kiểm tra lại ngày giờ CSDL vs Java hoặc trạng thái).");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm và kiểm tra khuyến mãi hợp lệ cho mã " + maKM + ": " + e.getMessage());
            e.printStackTrace();
        }
        return km; // Trả về object nếu hợp lệ, null nếu không
    }
}