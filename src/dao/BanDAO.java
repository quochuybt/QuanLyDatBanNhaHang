package dao;

import connectDB.SQLConnection;
import entity.Ban;
import entity.TrangThaiBan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap; // Thêm import này
import java.util.List;
import java.util.Map; // Thêm import này

public class BanDAO {

    /**
     * Chuyển đổi chuỗi trạng thái từ CSDL (ví dụ: "Đang có khách")
     * sang kiểu Enum (TrangThaiBan.DANG_PHUC_VU).
     */
    private TrangThaiBan convertStringToTrangThai(String trangThaiDB) {
        if (trangThaiDB == null) {
            return TrangThaiBan.TRONG;
        }
        switch (trangThaiDB) {
            case "Đang có khách": // Giống trong SQL script
            case "Đang phục vụ": // Tên dùng trong logic GUI
                return TrangThaiBan.DANG_PHUC_VU;
            case "Đã đặt trước":
                return TrangThaiBan.DA_DAT_TRUOC;
            case "Trống":
            default:
                return TrangThaiBan.TRONG;
        }
    }
    public boolean updateBan(Ban ban) {
        String sql = "UPDATE Ban SET tenBan = ?, soGhe = ?, trangThai = ?, gioMoBan = ?, khuVuc = ? WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ban.getTenBan());
            ps.setInt(2, ban.getSoGhe());

            // Chuyển Enum trạng thái thành String lưu vào DB
            String trangThaiDB;
            switch (ban.getTrangThai()) {
                case DANG_PHUC_VU:
                    trangThaiDB = "Đang có khách"; // Hoặc "Đang phục vụ" tùy CSDL
                    break;
                case DA_DAT_TRUOC:
                    trangThaiDB = "Đã đặt trước";
                    break;
                case TRONG:
                default:
                    trangThaiDB = "Trống";
                    break;
            }
            ps.setString(3, trangThaiDB);

            // Xử lý gioMoBan (có thể null)
            if (ban.getGioMoBan() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(ban.getGioMoBan()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }

            ps.setString(5, ban.getKhuVuc());
            ps.setString(6, ban.getMaBan()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean chuyenBan(entity.Ban banCu, entity.Ban banMoi) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement psUpdateDon = null;
        java.sql.PreparedStatement psUpdateBanMoi = null;
        java.sql.PreparedStatement psUpdateBanCu = null;

        String sqlUpdateDon = "UPDATE DonDatMon SET maBan = ? WHERE maBan = ? AND trangThai != N'Đã thanh toán' AND trangThai != N'Đã hủy'";
        String sqlUpdateBanMoi = "UPDATE Ban SET trangThai = ?, gioMoBan = ? WHERE maBan = ?";
        String sqlUpdateBanCu = "UPDATE Ban SET trangThai = N'Trống', gioMoBan = NULL WHERE maBan = ?";

        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // --- BƯỚC 1: Chuyển Đơn Đặt Món ---
            psUpdateDon = conn.prepareStatement(sqlUpdateDon);
            psUpdateDon.setString(1, banMoi.getMaBan());
            psUpdateDon.setString(2, banCu.getMaBan());
            int donAffected = psUpdateDon.executeUpdate();
            System.out.println("Đã chuyển đơn: " + donAffected + " dòng.");

            // --- BƯỚC 2: Cập nhật trạng thái Bàn Mới ---
            // SỬA LỖI Ở ĐÂY: Chuyển Enum sang Chuỗi Tiếng Việt chuẩn của CSDL
            String trangThaiTiengViet = "Đang có khách"; // Mặc định
            if (banCu.getTrangThai() == entity.TrangThaiBan.DA_DAT_TRUOC) {
                trangThaiTiengViet = "Đã đặt trước";
            } else if (banCu.getTrangThai() == entity.TrangThaiBan.DANG_PHUC_VU) {
                // Kiểm tra lại trong CSDL của bạn dùng "Đang phục vụ" hay "Đang có khách"
                // Theo script insert trước đó của bạn là "Đang có khách", nhưng chuẩn thường là "Đang phục vụ"
                // Hãy dùng chuỗi mà các hàm load dữ liệu khác đang dùng. Tôi dùng "Đang phục vụ" (hoặc "Đang có khách")
                trangThaiTiengViet = "Đang có khách";
            }

            psUpdateBanMoi = conn.prepareStatement(sqlUpdateBanMoi);
            psUpdateBanMoi.setNString(1, trangThaiTiengViet); // Dùng setNString cho tiếng Việt

            // Chuyển giờ
            if (banCu.getGioMoBan() != null) {
                psUpdateBanMoi.setTimestamp(2, java.sql.Timestamp.valueOf(banCu.getGioMoBan()));
            } else {
                psUpdateBanMoi.setNull(2, java.sql.Types.TIMESTAMP);
            }

            psUpdateBanMoi.setString(3, banMoi.getMaBan());
            psUpdateBanMoi.executeUpdate();

            // --- BƯỚC 3: Cập nhật trạng thái Bàn Cũ ---
            psUpdateBanCu = conn.prepareStatement(sqlUpdateBanCu);
            psUpdateBanCu.setString(1, banCu.getMaBan());
            psUpdateBanCu.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try {
                if (psUpdateDon != null) psUpdateDon.close();
                if (psUpdateBanMoi != null) psUpdateBanMoi.close();
                if (psUpdateBanCu != null) psUpdateBanCu.close();
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (Exception ex) {}
        }
    }

    public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich) {
        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false);

            // --- 1. XÁC ĐỊNH TRẠNG THÁI CHUNG ---
            // Mặc định là trạng thái của bàn đích
            boolean isDangPhucVu = (banDich.getTrangThai() == entity.TrangThaiBan.DANG_PHUC_VU);

            // Quét qua các bàn nguồn, nếu có bàn nào đang phục vụ -> Set cờ thành True
            for (Ban b : listBanNguon) {
                if (b.getTrangThai() == entity.TrangThaiBan.DANG_PHUC_VU) {
                    isDangPhucVu = true;
                    break;
                }
            }

            // Xác định chuỗi trạng thái để lưu vào CSDL
            // Nếu có ít nhất 1 bàn đang phục vụ -> "Đang có khách"
            // Nếu tất cả đều là đặt trước -> "Đã đặt trước"
            String trangThaiChung = isDangPhucVu ? "Đang có khách" : "Đã đặt trước";

            // --- 2. TÌM MÃ ĐƠN CHÍNH CỦA BÀN ĐÍCH ---
            String sqlGetTargetOrder = "SELECT TOP 1 maDon FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND trangThai != N'Đã hủy'";
            String maDonDich = null;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlGetTargetOrder)) {
                ps.setString(1, banDich.getMaBan());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) maDonDich = rs.getString("maDon");
                }
            }

            // Nếu bàn đích chưa có đơn (VD: Bàn đặt trước chưa đến), ta cần giữ nguyên hoặc xử lý riêng.
            // Nhưng để gộp được món thì cần có đơn. Ở đây tạm thời return false nếu ko tìm thấy đơn đích.
            if (maDonDich == null && isDangPhucVu) return false;

            // --- CÁC CÂU SQL ---
            // A. Gộp món
            String sqlMergeDuplicate = "UPDATE Dich SET Dich.soLuong = Dich.soLuong + Nguon.soLuong FROM ChiTietHoaDon Dich INNER JOIN ChiTietHoaDon Nguon ON Dich.maMonAn = Nguon.maMonAn WHERE Dich.maDon = ? AND Nguon.maDon = ?";
            String sqlDeleteDuplicateSource = "DELETE Nguon FROM ChiTietHoaDon Nguon WHERE Nguon.maDon = ? AND Nguon.maMonAn IN (SELECT maMonAn FROM ChiTietHoaDon WHERE maDon = ?)";
            String sqlMoveUnique = "UPDATE ChiTietHoaDon SET maDon = ? WHERE maDon = ?";

            // B. Xử lý bàn nguồn
            String sqlCancelOldOrder = "UPDATE DonDatMon SET trangThai = N'Đã hủy', ghiChu = N'Đã gộp vào ' + ? WHERE maDon = ?";
            String sqlCreateDummy = "INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai, ghiChu) VALUES(?, GETDATE(), GETDATE(), 'NV01102', ?, N'Chưa thanh toán', ?)";

            // C. Cập nhật Bàn Nguồn (Update cả Tên và Trạng thái)
            String sqlUpdateSource = "UPDATE Ban SET tenBan = ?, trangThai = ? WHERE maBan = ?";

            // D. Cập nhật Bàn Đích (Update Trạng thái theo logic chung)
            String sqlUpdateTarget = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";

            java.sql.PreparedStatement psMerge = conn.prepareStatement(sqlMergeDuplicate);
            java.sql.PreparedStatement psDeleteDup = conn.prepareStatement(sqlDeleteDuplicateSource);
            java.sql.PreparedStatement psMove = conn.prepareStatement(sqlMoveUnique);
            java.sql.PreparedStatement psCancel = conn.prepareStatement(sqlCancelOldOrder);
            java.sql.PreparedStatement psDummy = conn.prepareStatement(sqlCreateDummy);
            java.sql.PreparedStatement psUpdateSource = conn.prepareStatement(sqlUpdateSource);
            java.sql.PreparedStatement psUpdateTarget = conn.prepareStatement(sqlUpdateTarget);

            String linkKey = "LINKED:" + banDich.getMaBan();

            // --- VÒNG LẶP XỬ LÝ BÀN NGUỒN ---
            for (Ban bNguon : listBanNguon) {
                if (bNguon.getMaBan().equals(banDich.getMaBan())) continue; // Bỏ qua nếu trùng

                // Lấy đơn nguồn
                String maDonNguon = null;
                String sqlGetSourceOrder = "SELECT TOP 1 maDon FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán'";
                try(java.sql.PreparedStatement psSrc = conn.prepareStatement(sqlGetSourceOrder)) {
                    psSrc.setString(1, bNguon.getMaBan());
                    try(java.sql.ResultSet rsSrc = psSrc.executeQuery()) {
                        if(rsSrc.next()) maDonNguon = rsSrc.getString("maDon");
                    }
                }

                if (maDonNguon != null && maDonDich != null) {
                    // Gộp món
                    psMerge.setString(1, maDonDich);
                    psMerge.setString(2, maDonNguon);
                    psMerge.executeUpdate();

                    psDeleteDup.setString(1, maDonNguon);
                    psDeleteDup.setString(2, maDonDich);
                    psDeleteDup.executeUpdate();

                    psMove.setString(1, maDonDich);
                    psMove.setString(2, maDonNguon);
                    psMove.executeUpdate();

                    // Hủy đơn cũ
                    psCancel.setString(1, banDich.getTenBan());
                    psCancel.setString(2, maDonNguon);
                    psCancel.executeUpdate();
                }

                // Tạo đơn ảo
                String timeStr = String.valueOf(System.currentTimeMillis());
                String shortTime = timeStr.substring(timeStr.length() - 9);
                String dummyID = "L" + shortTime + bNguon.getMaBan();

                psDummy.setString(1, dummyID);
                psDummy.setString(2, bNguon.getMaBan());
                psDummy.setString(3, linkKey);
                psDummy.executeUpdate();

                // Cập nhật Bàn Nguồn (Tên + Trạng thái chung)
                String tenGoc = bNguon.getTenBan().replaceAll("\\s*\\(Ghép.*\\)", "").trim();
                String tenMoi = tenGoc + " (Ghép " + banDich.getTenBan() + ")";

                psUpdateSource.setString(1, tenMoi);
                psUpdateSource.setNString(2, trangThaiChung); // <-- Set trạng thái chung (đỏ/vàng)
                psUpdateSource.setString(3, bNguon.getMaBan());
                psUpdateSource.executeUpdate();
            }

            // --- CẬP NHẬT BÀN ĐÍCH ---
            // Bắt buộc update bàn đích để nó đồng bộ màu sắc với các bàn con
            psUpdateTarget.setNString(1, trangThaiChung);
            psUpdateTarget.setString(2, banDich.getMaBan());
            psUpdateTarget.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
            return false;
        } finally {
            try { if(conn!=null) { conn.setAutoCommit(true); conn.close(); } } catch(Exception ex){}
        }
    }

    public String getTenBanByMa(String maBan) {
        String tenBan = maBan; // Giá trị mặc định
        String sql = "SELECT tenBan FROM Ban WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenBan = rs.getString("tenBan");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tenBan;
    }
    public Ban getBanByMa(String maBan) {
        Ban ban = null;
        String sql = "SELECT * FROM Ban WHERE maBan = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenBan = rs.getString("tenBan");
                    int soGhe = rs.getInt("soGhe");
                    String trangThaiStr = rs.getString("trangThai");
                    Timestamp gioMoBanTS = rs.getTimestamp("gioMoBan");
                    String khuVuc = rs.getString("khuVuc");

                    TrangThaiBan trangThai = convertStringToTrangThai(trangThaiStr);
                    LocalDateTime gioMoBan = (gioMoBanTS != null) ? gioMoBanTS.toLocalDateTime() : null;

                    ban = new Ban(maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ban;
    }
    /**
     * Lấy toàn bộ danh sách Bàn từ CSDL
     */
    public List<Ban> getAllBan() {
        List<Ban> dsBan = new ArrayList<>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = SQLConnection.getConnection();
            String sql = "SELECT maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc FROM Ban";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String maBan = rs.getString("maBan");
                String tenBan = rs.getString("tenBan");
                int soGhe = rs.getInt("soGhe");
                String trangThaiStr = rs.getString("trangThai");
                Timestamp gioMoBanTS = rs.getTimestamp("gioMoBan");
                String khuVuc = rs.getString("khuVuc");

                // Chuyển đổi kiểu dữ liệu
                TrangThaiBan trangThai = convertStringToTrangThai(trangThaiStr);
                LocalDateTime gioMoBan = null;
                if (gioMoBanTS != null) {
                    gioMoBan = gioMoBanTS.toLocalDateTime();
                }

                Ban ban = new Ban(maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc);
                dsBan.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Đóng tài nguyên
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Không đóng 'con' nếu nó được quản lý bởi ConnectDB singleton
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dsBan;
    }

    /**
     * Lấy số thứ tự (phần số) lớn nhất của mã bàn (ví dụ: BAN10 -> 10)
     * Dùng để cập nhật lại bộ đếm static trong class Ban
     */
    public int getSoThuTuBanLonNhat() {
        int maxSoThuTu = 0;
        Connection con = SQLConnection.getConnection();
        String sql = "SELECT MAX(CAST(SUBSTRING(maBan, 4, LEN(maBan) - 3) AS INT)) FROM Ban WHERE maBan LIKE 'BAN[0-9]%'";
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                maxSoThuTu = rs.getInt(1); // Lấy kết quả từ cột đầu tiên
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return maxSoThuTu;
    }

    // --- HÀM MỚI CHO DASHBOARD ---
    /**
     * (MỚI) Đếm số lượng bàn theo từng trạng thái (real-time).
     * @return Map<String, Integer> (Key: Tên trạng thái, Value: Số lượng)
     */
    public Map<String, Integer> getTableStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        // Khởi tạo các giá trị mặc định dựa trên CSDL của bạn
        counts.put("Trống", 0);
        counts.put("Đang có khách", 0);
        counts.put("Đã đặt trước", 0);

        String sql = "SELECT trangThai, COUNT(maBan) AS SoLuong FROM Ban GROUP BY trangThai";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String trangThai = rs.getString("trangThai");
                int soLuong = rs.getInt("SoLuong");
                if (trangThai != null) {
                    // Ghi đè giá trị 0 nếu tìm thấy
                    // Cần đảm bảo key khớp chính xác với CSDL (ví dụ: "Đang có khách")
                    counts.put(trangThai, soLuong);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while counting table status: " + e.getMessage());
            e.printStackTrace();
            // Ném lỗi để báo cho SwingWorker biết
            throw new RuntimeException("Lỗi truy vấn trạng thái bàn", e);
        }
        return counts;
    }
    public List<List<Ban>> goiYGhepBan(int soKhach, String khuVucUuTien) {
        List<List<Ban>> ketQua = new ArrayList<>();
        List<Ban> dsBanTrong = new ArrayList<>();

        // 1. Lấy tất cả bàn TRỐNG
        // (Bạn có thể lọc thêm theo ngày giờ nếu muốn chính xác tuyệt đối, ở đây ta lấy bàn hiện tại đang Trống)
        for (Ban b : getAllBan()) {
            if (b.getTrangThai() == TrangThaiBan.TRONG) {
                // Nếu có lọc khu vực
                if (khuVucUuTien.equals("Tất cả") || b.getKhuVuc().equals(khuVucUuTien)) {
                    dsBanTrong.add(b);
                }
            }
        }

        // 2. Tìm BÀN ĐƠN đủ chỗ (Ưu tiên cao nhất)
        for (Ban b : dsBanTrong) {
            if (b.getSoGhe() >= soKhach) {
                List<Ban> goiy = new ArrayList<>();
                goiy.add(b);
                ketQua.add(goiy);
            }
        }

        // Nếu đã có bàn đơn đủ chỗ thì trả về luôn, không cần tìm ghép
        if (!ketQua.isEmpty()) return ketQua;

        // 3. Tìm CẶP BÀN ghép lại đủ chỗ (Ưu tiên cùng khu vực)
        // Thuật toán: Duyệt 2 vòng lặp để tìm cặp
        for (int i = 0; i < dsBanTrong.size() - 1; i++) {
            for (int j = i + 1; j < dsBanTrong.size(); j++) {
                Ban b1 = dsBanTrong.get(i);
                Ban b2 = dsBanTrong.get(j);

                // Điều kiện ghép: Cùng khu vực (để tiện phục vụ) & Tổng ghế đủ
                if (b1.getKhuVuc().equals(b2.getKhuVuc()) && (b1.getSoGhe() + b2.getSoGhe() >= soKhach)) {
                    List<Ban> capGhep = new ArrayList<>();
                    capGhep.add(b1);
                    capGhep.add(b2);
                    ketQua.add(capGhep);
                }
            }
        }

        return ketQua;
    }

} // Kết thúc class BanDAO