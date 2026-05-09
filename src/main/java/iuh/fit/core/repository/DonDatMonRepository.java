package iuh.fit.core.repository;

import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.TrangThaiBan;
import java.time.LocalDateTime;
import java.util.List;

public class DonDatMonRepository extends GenericRepository<DonDatMon, String> {

    public DonDatMonRepository() {
        super(DonDatMon.class);
    }

    @Override
    public void save(DonDatMon entity) {
        doInTransaction(em -> {
            if (entity.getMaDon() == null || entity.getMaDon().isEmpty()) {
                entity.setMaDon(new DonDatMon(true).getMaDon());
            }
            if (entity.getNhanVien() != null && entity.getNhanVien().getManv() != null) {
                entity.setNhanVien(em.getReference(iuh.fit.core.entity.NhanVien.class, entity.getNhanVien().getManv()));
            } else {
                entity.setNhanVien(null);
            }
            if (entity.getKhachHang() != null && entity.getKhachHang().getMaKH() != null) {
                entity.setKhachHang(em.getReference(iuh.fit.core.entity.KhachHang.class, entity.getKhachHang().getMaKH()));
            } else {
                entity.setKhachHang(null);
            }
            if (entity.getBan() != null && entity.getBan().getMaBan() != null) {
                entity.setBan(em.getReference(iuh.fit.core.entity.Ban.class, entity.getBan().getMaBan()));
            } else {
                entity.setBan(null);
            }
            em.persist(entity);
        });
    }

    public DonDatMon getDonDatMonChuaNhanTheoMaBanBaoGomLinked(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }

        return doInSession(em -> {
            String jpql = """
            SELECT d
            FROM DonDatMon d
            LEFT JOIN FETCH d.khachHang kh
            LEFT JOIN FETCH d.nhanVien nv
            LEFT JOIN FETCH d.ban b
            WHERE b.maBan = :maBan
              AND d.trangThai = 'Chưa thanh toán'
            ORDER BY d.thoiGianDen ASC
            """;

            return em.createQuery(jpql, DonDatMon.class)
                    .setParameter("maBan", maBan)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        });
    }

    public List<DonDatMon> getAllDonDatMonChuaNhanBaoGomLinked() {
        return doInSession(em -> {
            String jpql = """
            SELECT d
            FROM DonDatMon d
            LEFT JOIN FETCH d.khachHang kh
            LEFT JOIN FETCH d.nhanVien nv
            LEFT JOIN FETCH d.ban b
            WHERE d.trangThai = 'Chưa thanh toán'
            ORDER BY d.thoiGianDen ASC
            """;

            return em.createQuery(jpql, DonDatMon.class)
                    .getResultList();
        });
    }

    /**
     * Lấy các mã bàn cùng đợt đặt món của một khách hàng
     * Sử dụng TIMESTAMPDIFF của MariaDB để thay thế cho DATEDIFF(MINUTE)
     */
    public List<String> getMaBanCungDotDat(String maKH, LocalDateTime thoiGianDen, String maBanHienTai) {
        return doInSession(em -> {
            // So sánh trực tiếp thoiGianDen trong DB với giá trị truyền vào
            String jpql = "SELECT d.ban.maBan FROM DonDatMon d " +
                    "WHERE d.khachHang.maKH = :maKH " +
                    "AND d.ban.maBan != :maBanHienTai " +
                    "AND d.trangThai != 'Đã hủy' " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d) " +
                    "AND d.thoiGianDen = :thoiGianDen"; // So sánh chính xác thời gian

            return em.createQuery(jpql, String.class)
                    .setParameter("maKH", maKH)
                    .setParameter("maBanHienTai", maBanHienTai)
                    .setParameter("thoiGianDen", thoiGianDen)
                    .getResultList();
        });
    }

    /**
     * Tự động hủy các đơn quá giờ (1 tiếng)
     */
    public int tuDongHuyDonQuaGio() {
        return executeTransaction(em -> {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

            // Tìm các đơn quá hạn
            String findJpql = "SELECT d FROM DonDatMon d " +
                    "WHERE d.thoiGianDen < :oneHourAgo " +
                    "AND d.trangThai = 'Chưa thanh toán' " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d)";

            List<DonDatMon> listQuaHan = em.createQuery(findJpql, DonDatMon.class)
                    .setParameter("oneHourAgo", oneHourAgo)
                    .getResultList();

            int count = 0;
            for (DonDatMon d : listQuaHan) {
                // Hủy đơn
                d.setTrangThai("Đã hủy");
                d.setGhiChu((d.getGhiChu() != null ? d.getGhiChu() : "") + " (Hủy tự động do quá giờ)");

                // Cập nhật trạng thái bàn nếu bàn đang ở trạng thái Đã đặt trước
                if (d.getBan() != null && "Đã đặt trước".equals(d.getBan().getTrangThai())) {
                    d.getBan().setTrangThai(TrangThaiBan.TRONG);
                    d.getBan().setGioMoBan(null);
                }
                count++;
            }
            return count;
        });
    }

    /**
     * Cập nhật trạng thái bàn theo thời gian (Booking window 120 phút)
     */
    public void capNhatTrangThaiBanTheoGio() {
        doInTransaction(em -> {
            LocalDateTime bayGio = LocalDateTime.now();
            LocalDateTime haiTiengNua = bayGio.plusMinutes(120);

            // Lock: Chuyển sang JPQL
            String jpqlLock = "UPDATE Ban b SET b.trangThai = 'Đã đặt trước' " +
                    "WHERE b.trangThai = 'Trống' AND b.maBan IN (" +
                    "   SELECT d.ban.maBan FROM DonDatMon d " +
                    "   WHERE d.trangThai = 'Chưa thanh toán' " +
                    "   AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d) " +
                    "   AND d.thoiGianDen BETWEEN :bayGio AND :haiTiengNua" +
                    ")";
            em.createQuery(jpqlLock)
                    .setParameter("bayGio", bayGio)
                    .setParameter("haiTiengNua", haiTiengNua)
                    .executeUpdate();

            // Unlock: Chuyển sang JPQL
            String jpqlUnlock = "UPDATE Ban b SET b.trangThai = 'Trống' " +
                    "WHERE b.trangThai = 'Đã đặt trước' AND b.maBan NOT IN (" +
                    "   SELECT d.ban.maBan FROM DonDatMon d " +
                    "   WHERE d.trangThai = 'Chưa thanh toán' " +
                    "   AND d.thoiGianDen BETWEEN :bayGio AND :haiTiengNua" +
                    ")";
            em.createQuery(jpqlUnlock)
                    .setParameter("bayGio", bayGio)
                    .setParameter("haiTiengNua", haiTiengNua)
                    .executeUpdate();
        });
    }
    public boolean huyDatBanVaGiaiPhongBanGhep(String maDon) {
        return executeTransaction(em -> {
            DonDatMon donChinh = em.find(DonDatMon.class, maDon);
            if (donChinh == null) return false;

            Ban banChinh = donChinh.getBan();
            String maBanChinh = (banChinh != null) ? banChinh.getMaBan() : "";

            // 1. Quét tìm và dọn dẹp các Bàn Phụ (Bàn ghép)
            if (!maBanChinh.isEmpty()) {
                String tagLinked = "%LINKED:" + maBanChinh + "%";
                List<DonDatMon> cacDonPhu = em.createQuery(
                                "SELECT d FROM DonDatMon d WHERE d.ghiChu LIKE :tag", DonDatMon.class)
                        .setParameter("tag", tagLinked)
                        .getResultList();

                for (DonDatMon donPhu : cacDonPhu) {
                    Ban banPhu = donPhu.getBan();
                    if (banPhu != null) {
                        banPhu.setTrangThai(TrangThaiBan.TRONG);
                        banPhu.setGioMoBan(null);
                        // Xóa chữ "(Ghép...)" nếu có
                        String tenGoc = banPhu.getTenBan().replaceAll("\\s*\\(Ghép.*\\)", "").trim();
                        banPhu.setTenBan(tenGoc);
                        em.merge(banPhu);
                    }
                    // Soft-delete đơn ảo
                    donPhu.softDelete();
                    em.merge(donPhu);
                }
            }

            // 2. Dọn Bàn Chính
            if (banChinh != null) {
                banChinh.setTrangThai(TrangThaiBan.TRONG);
                banChinh.setGioMoBan(null);
                String tenGoc = banChinh.getTenBan().replaceAll("\\s*\\(Ghép.*\\)", "").trim();
                banChinh.setTenBan(tenGoc);
                em.merge(banChinh);
            }

            // 3. Soft-delete Đơn Chính
            donChinh.softDelete();
            em.merge(donChinh);

            return true;
        });
    }

    /**
     * Lấy đơn đặt trước của bàn (Đơn sớm nhất)
     */
    public DonDatMon getDonDatMonDatTruoc(String maBan) {
        return doInSession(em -> {
            String jpql = "SELECT d FROM DonDatMon d " +
                    "WHERE d.ban.maBan = :maBan " +
                    "AND d.trangThai = 'Chưa thanh toán' " +
                    "AND (d.ghiChu IS NULL OR d.ghiChu NOT LIKE 'LINKED:%') " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d) " +
                    "ORDER BY d.thoiGianDen ASC";

            try {
                return em.createQuery(jpql, DonDatMon.class)
                        .setParameter("maBan", maBan)
                        .setMaxResults(1)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Tìm kiếm đơn chưa nhận theo SDT hoặc Tên khách hàng
     */
    public List<DonDatMon> timDonDatMonChuaNhan(String query) {
        return doInSession(em -> {
            String jpql = "SELECT d FROM DonDatMon d " +
                    "LEFT JOIN d.khachHang kh " +
                    "WHERE NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d) " +
                    "AND d.trangThai = 'Chưa thanh toán' " +
                    "AND (d.ghiChu IS NULL OR d.ghiChu NOT LIKE 'LINKED:%') " +
                    "AND (kh.sdt LIKE :q OR kh.tenKH LIKE :q) " +
                    "ORDER BY d.ngayKhoiTao DESC";

            return em.createQuery(jpql, DonDatMon.class)
                    .setParameter("q", "%" + query + "%")
                    .getResultList();
        });
    }

    /**
     * Lấy danh sách mã bàn đã được đặt trong khoảng thời gian
     */
    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        return doInSession(em -> {
            String jpql = "SELECT DISTINCT d.ban.maBan FROM DonDatMon d " +
                    "WHERE d.thoiGianDen BETWEEN :tuGio AND :denGio " +
                    "AND d.trangThai NOT IN ('Đã hủy', 'Đã thanh toán')";

            return em.createQuery(jpql, String.class)
                    .setParameter("tuGio", tuGio)
                    .setParameter("denGio", denGio)
                    .getResultList();
        });
    }

    /**
     * Lấy tất cả đơn chưa nhận (trong ngày hôm nay trở đi)
     */
    public List<DonDatMon> getAllDonDatMonChuaNhan() {
        return doInSession(em -> {
            String jpql = "SELECT d FROM DonDatMon d " +
                    "WHERE d.trangThai = 'Chưa thanh toán' " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.donDatMon = d) " +
                    "AND (d.ghiChu IS NULL OR d.ghiChu NOT LIKE '%LINKED:%') " +
                    "AND d.thoiGianDen >= CURRENT_DATE " +
                    "ORDER BY d.thoiGianDen ASC";

            return em.createQuery(jpql, DonDatMon.class).getResultList();
        });
    }


}