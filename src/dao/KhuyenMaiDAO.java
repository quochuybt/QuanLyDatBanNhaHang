package dao;

import connectDB.SQLConnection;
import entity.KhuyenMai;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    // [GIỮ NGUYÊN] Hàm tự động cập nhật
    public void autoUpdateExpiredStatuses() {
        String sql = "UPDATE KhuyenMai SET trangThai = N'Ngưng áp dụng' WHERE ngayKetThuc < ? AND trangThai = N'Đang áp dụng'";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * [GIỮ NGUYÊN] Lấy danh sách
     */
    public List<KhuyenMai> getAllKhuyenMai() {
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
                    LocalDate ngayKT = (rs.getDate("ngayKetThuc") != null) ? rs.getDate("ngayKetThuc").toLocalDate() : null;
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String trangThai = rs.getString("trangThai");

                    KhuyenMai km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam, dieuKien, ngayBD, ngayKT, trangThai);
                    km.setSoLuongGioiHan(rs.getInt("soLuongGioiHan"));
                    km.setSoLuotDaDung(rs.getInt("soLuotDaDung"));

                    dsKhuyenMai.add(km);
                } catch (Exception e) { e.printStackTrace(); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dsKhuyenMai;
    }

    /**
     * [GIỮ NGUYÊN] Thêm khuyến mãi
     */
    public boolean themKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKM, tenKM, moTa, loaiGiam, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai, dieuKienApDung, soLuongGioiHan, soLuotDaDung) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getMaKM());
            ps.setString(2, km.getTenChuongTrinh());
            ps.setString(3, km.getMoTa());
            ps.setString(4, km.getLoaiKhuyenMai());
            ps.setDouble(5, km.getGiaTri());
            ps.setDate(6, Date.valueOf(km.getNgayBatDau()));
            if (km.getNgayKetThuc() != null) ps.setDate(7, Date.valueOf(km.getNgayKetThuc())); else ps.setNull(7, Types.DATE);
            ps.setString(8, km.getTrangThai());
            ps.setDouble(9, km.getDieuKienApDung());
            ps.setInt(10, km.getSoLuongGioiHan());
            ps.setInt(11, 0);

            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /**
     * [GIỮ NGUYÊN] Cập nhật khuyến mãi
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM=?, moTa=?, loaiGiam=?, giaTriGiam=?, ngayBatDau=?, ngayKetThuc=?, trangThai=?, dieuKienApDung=?, soLuongGioiHan=? WHERE maKM=?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, km.getTenChuongTrinh());
            ps.setString(2, km.getMoTa());
            ps.setString(3, km.getLoaiKhuyenMai());
            ps.setDouble(4, km.getGiaTri());
            ps.setDate(5, Date.valueOf(km.getNgayBatDau()));
            if (km.getNgayKetThuc() != null) ps.setDate(6, Date.valueOf(km.getNgayKetThuc())); else ps.setNull(6, Types.DATE);
            ps.setString(7, km.getTrangThai());
            ps.setDouble(8, km.getDieuKienApDung());
            ps.setInt(9, km.getSoLuongGioiHan());
            ps.setString(10, km.getMaKM());

            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // [GIỮ NGUYÊN] Xóa
    public boolean xoaKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // [GIỮ NGUYÊN] Tìm kiếm
    public List<KhuyenMai> timKiemVaLoc(String tuKhoa, String trangThai) {
        autoUpdateExpiredStatuses();
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE 1=1";

        if (tuKhoa != null && !tuKhoa.trim().isEmpty() && !tuKhoa.equals("Tìm kiếm khuyến mãi")) {
            sql += " AND (maKM LIKE ? OR tenKM LIKE ?)";
        }
        if (trangThai != null && !trangThai.equals("Lọc khuyến mãi")) {
            sql += " AND trangThai = ?";
        }

        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            if (tuKhoa != null && !tuKhoa.trim().isEmpty() && !tuKhoa.equals("Tìm kiếm khuyến mãi")) {
                ps.setString(i++, "%" + tuKhoa + "%");
                ps.setString(i++, "%" + tuKhoa + "%");
            }
            if (trangThai != null && !trangThai.equals("Lọc khuyến mãi")) {
                ps.setString(i++, trangThai);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maKM = rs.getString("maKM");
                    String tenKM = rs.getString("tenKM");
                    String moTa = rs.getString("moTa");
                    LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                    LocalDate ngayKT = (rs.getDate("ngayKetThuc") != null) ? rs.getDate("ngayKetThuc").toLocalDate() : null;
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String tt = rs.getString("trangThai");

                    KhuyenMai km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam, dieuKien, ngayBD, ngayKT, tt);
                    km.setSoLuongGioiHan(rs.getInt("soLuongGioiHan"));
                    km.setSoLuotDaDung(rs.getInt("soLuotDaDung"));

                    dsKhuyenMai.add(km);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dsKhuyenMai;
    }

    /**
     * [ĐÃ SỬA] Lấy thông tin Khuyến Mãi hợp lệ dựa trên Mã KM.
     * Hàm này dùng để hiển thị thông tin khi áp dụng mã.
     * Chỉ kiểm tra cơ bản (tồn tại, ngày, trạng thái).
     * Việc kiểm tra chi tiết (số lượng, lịch sử khách) sẽ dùng hàm kiemTraDieuKienSuDung.
     */
    public KhuyenMai getKhuyenMaiHopLeByMa(String maKM) {
        autoUpdateExpiredStatuses(); // Cập nhật trạng thái trước
        String sql = "SELECT * FROM KhuyenMai WHERE maKM = ? AND trangThai = N'Đang áp dụng'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKM);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Kiểm tra ngày hiệu lực
                    LocalDate now = LocalDate.now();
                    LocalDate ngayBD = rs.getDate("ngayBatDau").toLocalDate();
                    Date dateKT = rs.getDate("ngayKetThuc");
                    LocalDate ngayKT = (dateKT != null) ? dateKT.toLocalDate() : null;

                    if (now.isBefore(ngayBD)) return null; // Chưa đến ngày
                    if (ngayKT != null && now.isAfter(ngayKT)) return null; // Hết hạn

                    // Tạo đối tượng
                    String tenKM = rs.getString("tenKM");
                    String moTa = rs.getString("moTa");
                    String loaiGiam = rs.getString("loaiGiam");
                    double giaTriGiam = rs.getDouble("giaTriGiam");
                    double dieuKien = rs.getDouble("dieuKienApDung");
                    String trangThai = rs.getString("trangThai");
                    int slGioiHan = rs.getInt("soLuongGioiHan");
                    int slDaDung = rs.getInt("soLuotDaDung");

                    KhuyenMai km = new KhuyenMai(maKM, tenKM, moTa, loaiGiam, giaTriGiam, dieuKien, ngayBD, ngayKT, trangThai);
                    km.setSoLuongGioiHan(slGioiHan);
                    km.setSoLuotDaDung(slDaDung);

                    return km;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Không tìm thấy hoặc không hợp lệ
    }



    public String kiemTraDieuKienSuDung(String maKM, String maKH, double tongTien) {
        String sql = "SELECT soLuongGioiHan, soLuotDaDung, dieuKienApDung, trangThai, ngayBatDau, ngayKetThuc FROM KhuyenMai WHERE maKM = ?";
        try (Connection conn = SQLConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (!rs.getString("trangThai").equals("Đang áp dụng")) return "Mã đã ngưng áp dụng.";

                    LocalDate now = LocalDate.now();
                    if (now.isBefore(rs.getDate("ngayBatDau").toLocalDate())) return "Chưa đến ngày áp dụng.";
                    if (rs.getDate("ngayKetThuc") != null && now.isAfter(rs.getDate("ngayKetThuc").toLocalDate())) return "Mã đã hết hạn.";

                    if (tongTien < rs.getDouble("dieuKienApDung")) return "Chưa đủ giá trị tối thiểu.";

                    int limit = rs.getInt("soLuongGioiHan");
                    int used = rs.getInt("soLuotDaDung");
                    if (limit > 0 && used >= limit) return "Mã đã hết lượt sử dụng.";

                    if (checkKhachHangDaDung(conn, maKM, maKH)) return "Khách hàng đã dùng mã này rồi.";

                    return "OK";
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Lỗi kiểm tra.";
    }

    private boolean checkKhachHangDaDung(Connection conn, String maKM, String maKH) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichSuSuDungKM WHERE maKM = ? AND maKH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            ps.setString(2, maKH);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }


    public void ghiNhanSuDung(String maKM, String maKH) {
        try (Connection conn = SQLConnection.getConnection()) {
            conn.setAutoCommit(false);

            String sqlUpdateCount = "UPDATE KhuyenMai SET soLuotDaDung = ISNULL(soLuotDaDung, 0) + 1 WHERE maKM = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateCount)) {
                ps.setString(1, maKM);
                ps.executeUpdate();
            }


            if (maKH != null && !maKH.trim().isEmpty() && !maKH.equals("KH_VANGLAI")) {
                String sqlHist = "INSERT INTO LichSuSuDungKM (maKH, maKM) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlHist)) {
                    ps.setString(1, maKH);
                    ps.setString(2, maKM);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("Cảnh báo: Không thể lưu lịch sử dùng KM cho khách " + maKH + ": " + ex.getMessage());
                }
            }

            // 3. Tự động chuyển trạng thái nếu đạt giới hạn
            String sqlAutoStop = "UPDATE KhuyenMai SET trangThai = N'Ngưng áp dụng' " +
                    "WHERE maKM = ? AND soLuongGioiHan > 0 AND soLuotDaDung >= soLuongGioiHan";
            try (PreparedStatement ps = conn.prepareStatement(sqlAutoStop)) {
                ps.setString(1, maKM);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Đã ghi nhận lượt dùng cho mã: " + maKM);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }}