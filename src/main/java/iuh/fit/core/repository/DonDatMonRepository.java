package iuh.fit.core.repository;

import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.TrangThaiBan;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonDatMonRepository extends GenericRepository<DonDatMon, String> {

    public DonDatMonRepository() {
        super(DonDatMon.class);
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
            // 1. Lock: Cập nhật bàn thành 'Đã đặt trước' (Sử dụng Native SQL)
            String sqlLock = "UPDATE Ban b SET b.trangThai = 'Đã đặt trước' " +
                    "WHERE b.trangThai = 'Trống' AND b.maBan IN (" +
                    "   SELECT d.maBan FROM DonDatMon d " +
                    "   WHERE d.trangThai = 'Chưa thanh toán' " +
                    "   AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = d.maDon) " +
                    "   AND TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, d.thoiGianDen) BETWEEN 0 AND 120" +
                    ")";
            em.createNativeQuery(sqlLock).executeUpdate();

            // 2. Unlock: Trả về 'Trống' nếu không còn đơn đặt trong vòng 120p
            String sqlUnlock = "UPDATE Ban b SET b.trangThai = 'Trống' " +
                    "WHERE b.trangThai = 'Đã đặt trước' AND b.maBan NOT IN (" +
                    "   SELECT d.maBan FROM DonDatMon d " +
                    "   WHERE d.trangThai = 'Chưa thanh toán' " +
                    "   AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon = d.maDon) " +
                    "   AND TIMESTAMPDIFF(MINUTE, CURRENT_TIMESTAMP, d.thoiGianDen) BETWEEN 0 AND 120" +
                    ")";
            em.createNativeQuery(sqlUnlock).executeUpdate();
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