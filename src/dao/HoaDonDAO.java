package dao;

import connectDB.SQLConnection;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.sql.SQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.sql.Types;

// Thêm các import cần thiết cho Dashboard
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class HoaDonDAO {
    private ChiTietHoaDonDAO chiTietDAO;

    // ⭐ CONSTANT PHÂN TRANG (Giới hạn 15 dòng) ⭐
    private static final int ITEMS_PER_PAGE = 15;

    public HoaDonDAO() {
        this.chiTietDAO = new ChiTietHoaDonDAO();
    }

    // ===============================================
    // ⭐ PHẦN BỔ SUNG CHO PHÂN TRANG, TÌM KIẾM VÀ LỌC NGÀY TỐI ƯU ⭐
    // ===============================================

    /**
     * [PAGINATION] - Lấy tổng số lượng hóa đơn theo trạng thái, từ khóa tìm kiếm và PHẠM VI NGÀY.
     * @param trangThai Trạng thái lọc ("Tất cả", "Đã thanh toán", "Chưa thanh toán").
     * @param keyword Từ khóa tìm kiếm (theo MaHD), rỗng nếu không tìm kiếm.
     * @param tuNgay Ngày bắt đầu (bao gồm), null để bỏ qua.
     * @param denNgay Ngày kết thúc (bao gồm), null để bỏ qua.
     * @return Tổng số lượng hóa đơn khớp điều kiện.
     */
    public int getTotalHoaDonCount(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        int count = 0;
        String sql = "SELECT COUNT(hd.maHD) FROM HoaDon hd WHERE 1=1";

        // Thêm điều kiện lọc trạng thái
        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        // Thêm điều kiện tìm kiếm theo Mã HD
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }

        // ⭐ THÊM ĐIỀU KIỆN LỌC NGÀY ⭐
        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            // ⭐ Đặt tham số ngày ⭐
            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm hóa đơn: " + e.getMessage());
        }
        return count;
    }

    /**
     * [PAGINATION] - Lấy danh sách HoaDon có giới hạn theo trang, trạng thái, từ khóa và PHẠM VI NGÀY.
     * SỬ DỤNG: Tải dữ liệu từng trang (Lazy Loading)
     * @param page Trang cần lấy (bắt đầu từ 1).
     * @param trangThai Trạng thái lọc.
     * @param keyword Từ khóa tìm kiếm (theo MaHD), rỗng nếu không tìm kiếm.
     * @param tuNgay Ngày bắt đầu (bao gồm), null để bỏ qua.
     * @param denNgay Ngày kết thúc (bao gồm), null để bỏ qua.
     * @return List<HoaDon> danh sách hóa đơn của trang đó (tối đa 15 dòng).
     */
    public List<HoaDon> getHoaDonByPage(int page, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        int offset = (page - 1) * ITEMS_PER_PAGE;

        // Câu lệnh SQL có JOIN và OFFSET/FETCH để phân trang
        String sql = "SELECT hd.*, b.tenBan " +
                "FROM HoaDon hd " +
                "LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "LEFT JOIN Ban b ON ddm.maBan = b.maBan " +
                "WHERE 1=1";

        // Thêm điều kiện lọc trạng thái
        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        // Thêm điều kiện tìm kiếm theo Mã HD
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }

        // ⭐ THÊM ĐIỀU KIỆN LỌC NGÀY ⭐
        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        sql += " ORDER BY hd.ngayLap DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"; // SQL Server Syntax

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            // ⭐ Đặt tham số ngày ⭐
            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            // Tham số OFFSET và LIMIT
            ps.setInt(index++, offset);
            ps.setInt(index++, ITEMS_PER_PAGE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsHoaDon.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi dòng dữ liệu: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy hóa đơn theo trang: " + e.getMessage());
            e.printStackTrace();
        }
        return dsHoaDon;
    }

    // ===============================================
    // ⭐ PHẦN CÒN LẠI GIỮ NGUYÊN HOÀN TOÀN ⭐
    // ===============================================

    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        HoaDon hoaDon = null;

        // --- SỬA CÂU SQL ---
        // Liên kết HoaDon với DonDatMon (để lấy maBan)
        String sql = "SELECT hd.*, ddm.maKH FROM HoaDon hd " +
                "JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "WHERE ddm.maBan = ? AND hd.trangThai = N'Chưa thanh toán'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Lấy thông tin Hóa Đơn
                    hoaDon = createHoaDonFromResultSet(rs);

                    String maKH = rs.getString("maKH");
                    hoaDon.setMaKH(maKH);
                    // 2. Lấy thông tin Chi Tiết Hóa Đơn
                    List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(hoaDon.getMaDon());

                    // 3. Gán chi tiết vào hóa đơn
                    hoaDon.setDsChiTiet(dsChiTiet);
                    hoaDon.tinhLaiTongTienTuChiTiet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hoaDon; // Trả về null nếu không tìm thấy
    }
    // NOTE SỬA: Thêm hàm cập nhật maNV khi thanh toán
    public boolean capNhatNhanVien(String maHD, String maNV) {
        String sql = "UPDATE HoaDon SET maNV = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật NV cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean capNhatMaKM(String maHD, String maKM) {
        // Cập nhật cột maKM trong bảng HoaDon
        String sql = "UPDATE HoaDon SET maKM = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (maKM != null && !maKM.isEmpty()) {
                ps.setString(1, maKM); // Đặt mã KM
            } else {
                ps.setNull(1, java.sql.Types.NVARCHAR); // Đặt là NULL nếu maKM rỗng hoặc null
            }
            ps.setString(2, maHD); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật maKM cho Hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean thanhToanHoaDon(String maHD, double tongTien, double tienKhachDua, String hinhThucTT, double tienGiamGia, String maKM, String tenBanGhiLai) {
        System.out.println("========== BẮT ĐẦU THANH TOÁN & TRẢ TÊN BÀN ==========");

        String sqlUpdateHD = "UPDATE HoaDon SET trangThai = N'Đã thanh toán', ngayLap = GETDATE(), tongTien = ?, tienKhachDua = ?, hinhThucThanhToan = ?, giamGia = ?, maKM = ?, tenBan = ? WHERE maHD = ?";
        java.sql.Connection conn = null;
        try {
            conn = connectDB.SQLConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // --- BƯỚC 0: Lấy thông tin ---
            String maDonHienTai = null;
            String maBanChinh = null;
            String sqlGetInfo = "SELECT d.maDon, d.maBan FROM HoaDon h JOIN DonDatMon d ON h.maDon = d.maDon WHERE h.maHD = ?";
            try (java.sql.PreparedStatement psInfo = conn.prepareStatement(sqlGetInfo)) {
                psInfo.setString(1, maHD);
                java.sql.ResultSet rsInfo = psInfo.executeQuery();
                if (rsInfo.next()) {
                    maDonHienTai = rsInfo.getString("maDon");
                    maBanChinh = rsInfo.getString("maBan");
                }
            }

            // --- BƯỚC 1: UPDATE HÓA ĐƠN (Để hiện đã thanh toán) ---
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlUpdateHD)) {
                ps.setDouble(1, tongTien);
                ps.setDouble(2, tienKhachDua);
                ps.setString(3, hinhThucTT);
                ps.setDouble(4, tienGiamGia);
                ps.setString(5, maKM);

                // Set tên bàn vào tham số số 7
                ps.setNString(6, tenBanGhiLai);

                ps.setString(7, maHD);
                if (ps.executeUpdate() <= 0) { conn.rollback(); return false; }
            }

            // --- BƯỚC 2: ĐÓNG ĐƠN HIỆN TẠI ---
            String sqlCloseDon = "UPDATE DonDatMon SET trangThai = N'Đã thanh toán' WHERE maDon = ?";
            try (java.sql.PreparedStatement psDon = conn.prepareStatement(sqlCloseDon)) {
                psDon.setString(1, maDonHienTai);
                psDon.executeUpdate();
            }

            // --- BƯỚC 3: TÌM CÁC BÀN LIÊN QUAN (Để trả tên) ---
            List<String> listBanCheck = new ArrayList<>();
            if (maBanChinh != null) {
                listBanCheck.add(maBanChinh);
                // Tìm bàn ghép (qua đơn ảo LINKED)
                String sqlLink = "SELECT maBan FROM DonDatMon WHERE ghiChu = ? AND trangThai != N'Đã thanh toán' AND trangThai != N'Đã hủy'";
                try (java.sql.PreparedStatement psLink = conn.prepareStatement(sqlLink)) {
                    psLink.setString(1, "LINKED:" + maBanChinh);
                    java.sql.ResultSet rsLink = psLink.executeQuery();
                    while(rsLink.next()) listBanCheck.add(rsLink.getString("maBan"));
                }
            }

            // --- BƯỚC 4: THANH TOÁN ĐƠN ẢO ---
            if (listBanCheck.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for(String s : listBanCheck) sb.append("'").append(s).append("',");
                String inClause = sb.substring(0, sb.length()-1);

                String sqlDummy = "UPDATE DonDatMon SET trangThai = N'Đã thanh toán' WHERE ghiChu = ? AND maBan IN (" + inClause + ")";
                try (java.sql.PreparedStatement psDum = conn.prepareStatement(sqlDummy)) {
                    psDum.setString(1, "LINKED:" + maBanChinh);
                    psDum.executeUpdate();
                }
            }

            // --- BƯỚC 5: UPDATE BÀN (TRẠNG THÁI & TRẢ TÊN GỐC) ---
            // Logic SQL "thần thánh" để trả tên:
            // 1. Nếu tên có chữ " (Ghép": Cắt bỏ phần sau.
            // 2. Nếu tên có chữ " + ": Cắt bỏ phần sau.
            String sqlUpdateBan =
                    "UPDATE Ban SET " +
                            "gioMoBan = NULL, " +
                            "trangThai = ?, " +
                            "tenBan = CASE " +
                            // Trường hợp 1: "Bàn 7 (Ghép Bàn 6)" -> Lấy "Bàn 7"
                            "WHEN CHARINDEX(N' (Ghép', tenBan) > 0 THEN RTRIM(LEFT(tenBan, CHARINDEX(N' (Ghép', tenBan) - 1)) " +
                            // Trường hợp 2: "Bàn 6 + 7 + 8" -> Lấy "Bàn 6"
                            "WHEN CHARINDEX(N' +', tenBan) > 0 THEN RTRIM(LEFT(tenBan, CHARINDEX(N' +', tenBan) - 1)) " +
                            // Trường hợp thường: Giữ nguyên
                            "ELSE tenBan " +
                            "END " +
                            "WHERE maBan = ?";

            String sqlCount = "SELECT COUNT(*) FROM DonDatMon " +
                    "WHERE maBan = ? " +
                    "AND maDon != ? " +
                    "AND (ghiChu IS NULL OR ghiChu NOT LIKE 'LINKED:%') " +
                    "AND trangThai NOT IN (N'Đã thanh toán', N'Đã hủy')";

            try (java.sql.PreparedStatement psUpBan = conn.prepareStatement(sqlUpdateBan);
                 java.sql.PreparedStatement psCount = conn.prepareStatement(sqlCount)) {

                for (String mb : listBanCheck) {
                    // Đếm đơn chờ
                    psCount.setString(1, mb);
                    psCount.setString(2, maDonHienTai);

                    int soDonCho = 0;
                    java.sql.ResultSet rsCount = psCount.executeQuery();
                    if (rsCount.next()) soDonCho = rsCount.getInt(1);

                    String trangThaiMoi = (soDonCho > 0) ? "Đã đặt trước" : "Trống";

                    // Update Bàn (bao gồm cả trả tên gốc)
                    psUpBan.setNString(1, trangThaiMoi);
                    psUpBan.setString(2, mb);
                    psUpBan.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ex) {}
        }
    }
    public boolean capNhatTongTien(String maHD, float tongTienMoi) {
        // Giả sử cột tổng tiền trong bảng HoaDon tên là tongTien
        String sql = "UPDATE HoaDon SET tongTien = ? WHERE maHD = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, tongTienMoi);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật tổng tiền cho hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Chuyển ResultSet thành đối tượng HoaDon.
     */
    private HoaDon createHoaDonFromResultSet(ResultSet rs) throws Exception {
        String maHD = rs.getString("maHD");

        LocalDateTime ngayLap = rs.getTimestamp("ngayLap").toLocalDateTime();
        String trangThai = rs.getString("trangThai");
        String hinhThucThanhToan = rs.getString("hinhThucThanhToan");

        String maDon = rs.getString("maDon");
        String maNV = rs.getString("maNV");
        String maKM = rs.getString("maKM");

        float tongTienLuuTrongDB = rs.getFloat("tongTien");
        float giamGia = rs.getFloat("giamGia");

        float tienKhachDua = rs.getFloat("tienKhachDua");
        float tongTienGoc = tongTienLuuTrongDB + giamGia ;


        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThucThanhToan, maDon, maNV, maKM);

        // --- SỬA LỖI TẠI ĐÂY ---
        // Kiểm tra xem cột tenBan có tồn tại trong ResultSet hay không
        try {
            // Tìm cột tenBan trong ResultSet metadata
            int tenBanIndex = rs.findColumn("tenBan");
            // Nếu tìm thấy, lấy giá trị
            hd.setTenBan(rs.getString(tenBanIndex));
        } catch (SQLException e) {
            // Nếu không tìm thấy cột tenBan (lỗi "The column name tenBan is not valid"),
            // bỏ qua và để giá trị mặc định (null hoặc chuỗi rỗng tùy logic của bạn)
            // System.err.println("Cảnh báo: Không tìm thấy cột 'tenBan' trong ResultSet. Đặt mặc định là null.");
            hd.setTenBan(null);
        }


        hd.setTienKhachDua(tienKhachDua);
        hd.setTongTienTuDB(tongTienGoc); // Gán tổng tiền gốc
        hd.setGiamGia(rs.getFloat("giamGia"));
        hd.capNhatTongThanhToanTuCacThanhPhan();

        return hd;
    }

    /**
     * [SELECT] - Lấy toàn bộ danh sách hóa đơn từ CSDL.
     */
    /**
     * [ĐÃ SỬA] Lấy toàn bộ hóa đơn kèm theo Tên Bàn
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> dsHoaDon = new ArrayList<>();
        // Sửa câu lệnh SQL để JOIN với bảng DonDatMon và Ban để lấy tenBan
        // Chú ý: Cột tenBan lấy từ bảng Ban (b.tenBan)
        String sql = "SELECT hd.*, b.tenBan " +
                "FROM HoaDon hd " +
                "LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "LEFT JOIN Ban b ON ddm.maBan = b.maBan " +
                "ORDER BY hd.ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    HoaDon hd = createHoaDonFromResultSet(rs);
                    // Nếu HoaDon có thuộc tính tenBan, hãy set nó ở đây
                    // Ví dụ: hd.setTenBan(rs.getString("tenBan"));
                    dsHoaDon.add(hd);
                } catch (Exception e) {
                    // In lỗi chi tiết 1 lần để debug nếu cần, thay vì spam console
                    System.err.println("Lỗi dòng dữ liệu: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsHoaDon;
    }

    /**
     * [INSERT] - Thêm một hóa đơn mới vào CSDL.
     */
    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua,maNV, maKM, maDon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            ps.setFloat(3, hd.getTongTien()); // Tổng tiền gốc
            ps.setString(4, hd.getTrangThai());
            ps.setString(5, hd.getHinhThucThanhToan());
            ps.setFloat(6, hd.getTienKhachDua());
            ps.setString(7, hd.getMaNV());
            if (hd.getMaKM() != null) {
                ps.setString(8, hd.getMaKM());
            } else {
                ps.setNull(8, java.sql.Types.NVARCHAR);
            }
            ps.setString(9, hd.getMaDon());

            // Nếu CSDL của bạn có cột tongThanhToan, bạn cần thêm 1 tham số ? vào SQL
            // và thêm dòng này:
            // ps.setFloat(10, hd.getTongThanhToan());

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("Lỗi ràng buộc: Mã HD đã tồn tại: " + hd.getMaHD());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Thêm vào HoaDonDAO.java
    public double getDoanhThuTienMatCaHienTai(String maNV, LocalDateTime thoiGianBatDauCa) {
        double total = 0;
        // Chỉ tính hóa đơn ĐÃ THANH TOÁN và hình thức là TIỀN MẶT
        String sql = "SELECT SUM(tienKhachDua - tienThoi) FROM HoaDon " +
                "WHERE maNV = ? AND ngayLap >= ? " +
                "AND trangThai = N'Đã thanh toán' " +
                "AND hinhThucThanhToan = N'Tiền mặt'";

        try (java.sql.Connection conn = connectDB.SQLConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(thoiGianBatDauCa));
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }
    /**
     * [SEARCH] - Tìm kiếm hóa đơn theo Mã HD.
     */
    public List<HoaDon> timHoaDon(String tuKhoa) {
        List<HoaDon> dsKetQua = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maHD LIKE ? ORDER BY ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tuKhoa + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsKetQua.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo HoaDon từ ResultSet (tìm kiếm): " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsKetQua;
    }
    public HoaDon getHoaDonTheoMaDon(String maDon) {
        HoaDon hoaDon = null;
        String sql = "SELECT * FROM HoaDon WHERE maDon = ?";
        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        hoaDon = createHoaDonFromResultSet(rs);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tạo HoaDon từ ResultSet (theo mã đơn): " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm hóa đơn theo mã đơn " + maDon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return hoaDon;
    }
    // Trong HoaDonDAO.java

    // [ĐÃ SỬA LỖI] Dùng tongTien thay vì (tienKhachDua - tienThoi)
    // Thêm vào class HoaDonDAO

    /**
     * [ĐÃ SỬA] Lấy doanh thu theo hình thức thanh toán
     * Tính chính xác: tongTien - giamGia
     */
    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        if (maNV == null || maNV.trim().isEmpty() || thoiGianBatDauCa == null || hinhThuc == null) {
            return 0;
        }

        double total = 0;

        // Sửa: Tính chính xác (tongTien - giamGia)
        String sql = "SELECT ISNULL(SUM(tongTien - ISNULL(giamGia, 0)), 0) AS DoanhThu " +
                "FROM HoaDon " +
                "WHERE maNV = ? " +
                "AND ngayLap >= ? " +
                "AND trangThai = N'Đã thanh toán' " +
                "AND hinhThucThanhToan = ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(thoiGianBatDauCa));
            ps.setString(3, hinhThuc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("DoanhThu");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDoanhThuTheoHinhThuc: " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    /**
     * [MỚI] Lấy tổng doanh thu của nhân viên trong khoảng thời gian (tất cả hình thức)
     */
    public double getTongDoanhThuNhanVien(String maNV, LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (maNV == null || maNV.trim().isEmpty()) {
            return 0;
        }

        double total = 0;

        String sql = "SELECT ISNULL(SUM(tongTien - ISNULL(giamGia, 0)), 0) AS TongDoanhThu " +
                "FROM HoaDon " +
                "WHERE maNV = ? " +
                "AND ngayLap >= ? " +
                "AND ngayLap <= ? " +
                "AND trangThai = N'Đã thanh toán'";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setTimestamp(2, tuNgay != null ? Timestamp.valueOf(tuNgay) : Timestamp.valueOf(LocalDateTime.now().minusYears(10)));
            ps.setTimestamp(3, denNgay != null ? Timestamp.valueOf(denNgay) : Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("TongDoanhThu");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTongDoanhThuNhanVien: " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Lấy tổng doanh thu theo từng ngày trong khoảng thời gian.
     * Chỉ tính các hóa đơn đã thanh toán.
     * @param startDate Ngày bắt đầu (bao gồm)
     * @param endDate Ngày kết thúc (bao gồm)
     * @return Map với Key là LocalDate, Value là tổng doanh thu ngày đó.
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();
        // Giả sử cột 'tongTien' trong DB LÀ tổng tiền cuối cùng khách trả (đã bao gồm giảm giá,...)
        // Nếu 'tongTien' trong DB là tổng tiền gốc (trước giảm giá), bạn cần tính toán lại
        // hoặc (tốt nhất) là lưu một cột 'tongThanhToan' trong bảng HoaDon và SUM cột đó.
        // Ví dụ này giả định 'tongTien' là tổng cuối cùng.
        String sql = "SELECT CAST(ngayLap AS DATE) AS Ngay, SUM(tongTien) AS DoanhThuNgay " +
                "FROM HoaDon " +
                "WHERE trangThai = N'Đã thanh toán' " +
                "AND ngayLap >= ? AND ngayLap < ? " +
                "GROUP BY CAST(ngayLap AS DATE) " +
                "ORDER BY Ngay";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate ngay = rs.getDate("Ngay").toLocalDate();
                    double doanhThu = rs.getDouble("DoanhThuNgay");
                    dailyRevenue.put(ngay, doanhThu);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching daily revenue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn doanh thu hàng ngày", e);
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching daily revenue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi lấy doanh thu", e);
        }
        return dailyRevenue;
    }

    /**
     * Đếm số lượng hóa đơn đã thanh toán trong khoảng thời gian.
     * @param startDate Ngày bắt đầu (bao gồm)
     * @param endDate Ngày kết thúc (bao gồm)
     * @return Số lượng hóa đơn.
     */
    public int getOrderCount(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        String sql = "SELECT COUNT(maHD) FROM HoaDon " +
                "WHERE trangThai = N'Đã thanh toán' " +
                "AND ngayLap >= ? AND ngayLap < ?";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while counting orders: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn số lượng hóa đơn", e);
        } catch (Exception e) {
            System.err.println("Unexpected error while counting orders: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi không xác định khi đếm hóa đơn", e);
        }
        return count;
    }

    /**
     * (MỚI) Lấy top nhân viên theo doanh thu trong khoảng thời gian.
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param limit Số lượng nhân viên top (ví dụ: 5)
     * @return Map<String, Double> (Key: Tên Nhân viên, Value: Tổng doanh thu)
     */
    public Map<String, Double> getTopStaffByRevenue(LocalDate startDate, LocalDate endDate, int limit) {
        Map<String, Double> topStaff = new LinkedHashMap<>(); // Giữ thứ tự

        // Dùng cột maNV từ bảng HoaDon để tính doanh thu
        // Giả định 'tongTien' là tổng tiền cuối cùng
        String sql = "SELECT TOP (?) nv.hoTen, SUM(hd.tongTien) AS TongDoanhThu " +
                "FROM HoaDon hd " +
                "JOIN NhanVien nv ON hd.maNV = nv.maNV " + // Join với NhanVien để lấy hoTen
                "WHERE hd.trangThai = N'Đã thanh toán' " +
                "AND hd.ngayLap >= ? AND hd.ngayLap < ? " +
                "GROUP BY nv.hoTen " + // Nhóm theo tên (hoặc mã NV nếu muốn)
                "ORDER BY TongDoanhThu DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit); // Đặt tham số TOP
            ps.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenNV = rs.getString("hoTen");
                    double tongDoanhThu = rs.getDouble("TongDoanhThu");
                    topStaff.put(tenNV, tongDoanhThu);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching top staff: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi truy vấn top nhân viên", e);
        }
        return topStaff;
    }
    public List<HoaDon> getAllHoaDonFiltered(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        List<HoaDon> dsHoaDon = new ArrayList<>();

        // Câu lệnh SQL có JOIN
        String sql = "SELECT hd.*, b.tenBan " +
                "FROM HoaDon hd " +
                "LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                "LEFT JOIN Ban b ON ddm.maBan = b.maBan " +
                "WHERE 1=1";

        // Thêm điều kiện lọc trạng thái
        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            sql += " AND hd.trangThai = ?";
        }

        // Thêm điều kiện tìm kiếm theo Mã HD
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND hd.maHD LIKE ?";
        }

        // THÊM ĐIỀU KIỆN LỌC NGÀY
        if (tuNgay != null) {
            sql += " AND hd.ngayLap >= ?";
        }
        if (denNgay != null) {
            sql += " AND hd.ngayLap <= ?";
        }

        sql += " ORDER BY hd.ngayLap DESC";

        try (Connection conn = SQLConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;
            if (!"Tất cả".equalsIgnoreCase(trangThai)) {
                ps.setString(index++, trangThai);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
            }

            // Đặt tham số ngày
            if (tuNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(tuNgay));
            }
            if (denNgay != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(denNgay));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        dsHoaDon.add(createHoaDonFromResultSet(rs));
                    } catch (Exception e) {
                        System.err.println("Lỗi dòng dữ liệu (Export): " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy hóa đơn để Export: " + e.getMessage());
            e.printStackTrace();
        }
        return dsHoaDon;
    }
}