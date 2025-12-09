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
        // 1. Nếu null -> Trống
        if (trangThaiDB == null) {
            return TrangThaiBan.TRONG;
        }

        // 2. Cắt khoảng trắng thừa
        String original = trangThaiDB; // Lưu lại để debug
        trangThaiDB = trangThaiDB.trim();

        // 3. DEBUG: In ra xem Java thực sự đọc được cái gì
        // (Sau khi chạy ngon thì bạn có thể comment dòng này lại)
        // System.out.println("DEBUG DAO: Đọc từ DB: '" + original + "' -> Trim: '" + trangThaiDB + "'");

        // 4. So sánh mềm dẻo (equalsIgnoreCase) thay vì switch-case
        if (trangThaiDB.equalsIgnoreCase("Đang có khách") ||
                trangThaiDB.equalsIgnoreCase("Đang phục vụ") ||
                trangThaiDB.equalsIgnoreCase("Có người")) {
            return TrangThaiBan.DANG_PHUC_VU;
        }

        else if (trangThaiDB.equalsIgnoreCase("Đã đặt trước") ||
                trangThaiDB.equalsIgnoreCase("Đã đặt") ||
                trangThaiDB.equalsIgnoreCase("Đặt trước")) {
            return TrangThaiBan.DA_DAT_TRUOC;
        }

        // 5. Kiểm tra trường hợp đặc biệt (nếu CSDL lưu không dấu hoặc lỗi font)
        else if (trangThaiDB.contains("t tr") || trangThaiDB.contains("at truoc")) {
            // Mẹo: Nếu chuỗi chứa cụm từ đặc trưng cũng cho là Đặt trước
            return TrangThaiBan.DA_DAT_TRUOC;
        }

        return TrangThaiBan.TRONG;
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

            // Nếu bàn cũ là Master, phải báo cho các bàn Slave biết địa chỉ Master mới
            String sqlUpdateLink = "UPDATE DonDatMon SET ghiChu = REPLACE(ghiChu, ?, ?) " +
                    "WHERE maBan != ? AND trangThai = N'Chưa thanh toán' AND ghiChu LIKE ?";

            // Dùng try-with-resources nhỏ ở đây để tự đóng PreparedStatement này cho gọn
            try (java.sql.PreparedStatement psLink = conn.prepareStatement(sqlUpdateLink)) {
                String oldTag = "LINKED:" + banCu.getMaBan(); // VD: LINKED:BAN09
                String newTag = "LINKED:" + banMoi.getMaBan(); // VD: LINKED:BAN01

                psLink.setString(1, oldTag);
                psLink.setString(2, newTag);
                psLink.setString(3, banCu.getMaBan()); // Trừ chính bàn cũ ra
                psLink.setString(4, "%" + oldTag + "%"); // Chỉ tìm những thằng có chứa LINKED

                psLink.executeUpdate();
            }

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
    private static class MonAnTam {
        String maMon;
        int soLuong;
        double donGia;
        public MonAnTam(String maMon, int soLuong, double donGia) {
            this.maMon = maMon;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }
    }
    public String getTenHienThiGhep(String maBanCheck) {
        String tenGoc = "";
        List<String> tenBanPhu = new ArrayList<>();

        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();

            // 1. Kiểm tra xem bàn này là Bàn Phụ (Slave) hay Bàn Chính (Master)?
            // Tìm xem bàn này có đang trỏ tới ai không?
            String sqlCheckSlave = "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND ghiChu LIKE '%LINKED:%'";
            String maBanMaster = maBanCheck; // Mặc định mình là master

            try(java.sql.PreparedStatement ps = conn.prepareStatement(sqlCheckSlave)) {
                ps.setString(1, maBanCheck);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String ghiChu = rs.getString("ghiChu");
                    int index = ghiChu.indexOf("LINKED:");
                    if (index != -1) {
                        // Cắt bỏ mọi thứ phía trước chữ LINKED:, chỉ lấy mã bàn phía sau
                        maBanMaster = ghiChu.substring(index + 7).trim().split(" ")[0];
                    }
                }
            }

            // 2. Lấy tên của Bàn Master
            String sqlGetMasterName = "SELECT tenBan FROM Ban WHERE maBan = ?";
            try(java.sql.PreparedStatement ps = conn.prepareStatement(sqlGetMasterName)) {
                ps.setString(1, maBanMaster);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) tenGoc = rs.getString("tenBan");
            }

            // 3. Lấy tên các Bàn Phụ đang trỏ tới Master
            String sqlGetSlaves =
                    "SELECT b.tenBan FROM DonDatMon d " +
                            "JOIN Ban b ON d.maBan = b.maBan " +
                            "WHERE d.ghiChu LIKE ? AND d.trangThai = N'Chưa thanh toán'";

            try(java.sql.PreparedStatement ps = conn.prepareStatement(sqlGetSlaves)) {
                ps.setString(1, "%LINKED:" + maBanMaster+"%");
                java.sql.ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    String t = rs.getString("tenBan").replace("Bàn ", ""); // Bỏ chữ "Bàn" cho gọn
                    // Nếu chính là bàn đang check thì bỏ qua (để tránh lặp nếu logic sai)
                    if (!tenGoc.contains(t)) {
                        tenBanPhu.add(t);
                    }
                }
            }

        } catch (Exception e) { e.printStackTrace(); }

        // 4. Ghép chuỗi: "Bàn 9 + 7 + 8"
        StringBuilder sb = new StringBuilder(tenGoc);
        for (String t : tenBanPhu) {
            sb.append(" + ").append(t);
        }

        return sb.toString();
    }

    // Hàm hỗ trợ: Lấy Mã Bàn Chính (nếu đang là bàn phụ)
    public String getMaBanChinh(String maBanCheck) {
        String sql = "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND ghiChu LIKE 'LINKED:%'";
        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBanCheck);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ghiChu").replace("LINKED:", "").trim();
            }
        } catch (Exception e) {}
        return maBanCheck; // Nếu không ghép thì chính là nó
    }
    public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich) {
        System.out.println("=== BẮT ĐẦU GỘP BÀN (PHIÊN BẢN FINAL) ===");
        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            boolean coKhachDangAn = (banDich.getTrangThai() == entity.TrangThaiBan.DANG_PHUC_VU);
            for (Ban b : listBanNguon) {
                if (b.getTrangThai() == entity.TrangThaiBan.DANG_PHUC_VU) {
                    coKhachDangAn = true;
                    break;
                }
            }
            // Nếu có ai đang ăn -> Đỏ ("Đang có khách"). Nếu toàn bộ là đặt trước -> Vàng ("Đã đặt trước")
            String trangThaiSauGop = coKhachDangAn ? "Đang có khách" : "Đã đặt trước";

            // 1. TÌM ĐƠN ĐÍCH (Bàn 10)
            String maDonDich = null;
            String sqlFindDest = "SELECT TOP 1 maDon FROM DonDatMon WHERE maBan = ? AND trangThai = N'Chưa thanh toán' AND trangThai != N'Đã hủy'";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlFindDest)) {
                ps.setString(1, banDich.getMaBan());
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) maDonDich = rs.getString("maDon");
            }

            // Nếu đích chưa có đơn, tạo mới
            if (maDonDich == null) {
                maDonDich = "DON" + System.currentTimeMillis();
                String sqlNew = "INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai) VALUES(?, GETDATE(), GETDATE(), 'NV01102', ?, N'Chưa thanh toán')";
                try (java.sql.PreparedStatement psNew = conn.prepareStatement(sqlNew)) {
                    psNew.setString(1, maDonDich); psNew.setString(2, banDich.getMaBan()); psNew.executeUpdate();
                }
            }

            // 2. DUYỆT CÁC BÀN NGUỒN (Bàn 2)
            for (Ban bNguon : listBanNguon) {
                if (bNguon.getMaBan().equals(banDich.getMaBan())) continue;

                // Lấy đơn nguồn
                String maDonNguon = null;
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlFindDest)) {
                    ps.setString(1, bNguon.getMaBan());
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) maDonNguon = rs.getString("maDon");
                }

                if (maDonNguon != null && !maDonNguon.equals(maDonDich)) {

                    // A. COPY MÓN TỪ NGUỒN VÀO RAM
                    List<MonAnTam> listItems = new ArrayList<>();
                    String sqlGetItems = "SELECT maMonAn, soLuong, donGia FROM ChiTietHoaDon WHERE maDon = ?";
                    try (java.sql.PreparedStatement psItems = conn.prepareStatement(sqlGetItems)) {
                        psItems.setString(1, maDonNguon);
                        java.sql.ResultSet rsItems = psItems.executeQuery();
                        while(rsItems.next()) {
                            listItems.add(new MonAnTam(rsItems.getString("maMonAn"), rsItems.getInt("soLuong"), rsItems.getDouble("donGia")));
                        }
                    }

                    // B. XÓA SẠCH CHI TIẾT BÊN NGUỒN
                    String sqlDel = "DELETE FROM ChiTietHoaDon WHERE maDon = ?";
                    try (java.sql.PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
                        psDel.setString(1, maDonNguon); psDel.executeUpdate();
                    }

                    // C. GHI VÀO ĐÍCH (CỘNG DỒN HOẶC THÊM MỚI)
                    String sqlCheck = "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maDon = ? AND maMonAn = ?";
                    String sqlUpdate = "UPDATE ChiTietHoaDon SET soLuong = soLuong + ? WHERE maDon = ? AND maMonAn = ?";
                    String sqlInsert = "INSERT INTO ChiTietHoaDon(maDon, maMonAn, soLuong, donGia) VALUES(?, ?, ?, ?)";
                    try (java.sql.PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
                         java.sql.PreparedStatement psUp = conn.prepareStatement(sqlUpdate);
                         java.sql.PreparedStatement psIn = conn.prepareStatement(sqlInsert)) {
                        for (MonAnTam item : listItems) {
                            psCheck.setString(1, maDonDich); psCheck.setString(2, item.maMon);
                            java.sql.ResultSet rsCheck = psCheck.executeQuery(); rsCheck.next();
                            if (rsCheck.getInt(1) > 0) {
                                psUp.setInt(1, item.soLuong); psUp.setString(2, maDonDich); psUp.setString(3, item.maMon); psUp.executeUpdate();
                            } else {
                                psIn.setString(1, maDonDich); psIn.setString(2, item.maMon); psIn.setInt(3, item.soLuong); psIn.setDouble(4, item.donGia); psIn.executeUpdate();
                            }
                        }
                    }

                    // D. HỦY ĐƠN & BILL NGUỒN
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE DonDatMon SET trangThai = N'Đã hủy' WHERE maDon = ?")) {
                        ps.setString(1, maDonNguon); ps.executeUpdate();
                    }
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE HoaDon SET trangThai = N'Đã hủy' WHERE maDon = ?")) {
                        ps.setString(1, maDonNguon); ps.executeUpdate();
                    }
                }

                // 3. TẠO ĐƠN DUMMY TRÊN BÀN NGUỒN
                String dummyID = "L" + (System.currentTimeMillis() % 100000000) + bNguon.getMaBan();
                // Ghi chú dạng: "LINKED:BAN09" (Bàn 9 là bàn đích)
                String sqlDummy = "INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai, ghiChu) VALUES(?, GETDATE(), GETDATE(), 'NV01102', ?, N'Chưa thanh toán', ?)";
                try (java.sql.PreparedStatement psDum = conn.prepareStatement(sqlDummy)) {
                    psDum.setString(1, dummyID);
                    psDum.setString(2, bNguon.getMaBan());
                    psDum.setNString(3, "LINKED:" + banDich.getMaBan());
                    psDum.executeUpdate();
                }

                // 4. CẬP NHẬT TÊN BÀN NGUỒN
                String sqlUpSrc = "UPDATE Ban SET  trangThai = ? WHERE maBan = ?";
                try(java.sql.PreparedStatement psUp = conn.prepareStatement(sqlUpSrc)){
                    psUp.setNString(1, trangThaiSauGop);
                    psUp.setString(2, bNguon.getMaBan());
                    psUp.executeUpdate();
                }
            }

            // 5. CẬP NHẬT TÊN BÀN ĐÍCH
            String sqlUpDest = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
            try(java.sql.PreparedStatement psUp = conn.prepareStatement(sqlUpDest)){
                psUp.setNString(1, trangThaiSauGop);
                psUp.setString(2, banDich.getMaBan());
                psUp.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ ĐÃ GỘP XONG!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ex) {}
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


} // Kết thúc class BanDAO