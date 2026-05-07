package iuh.fit.core.repository;

import iuh.fit.core.entity.ChiTietHoaDon;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.MonAn;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDonRepository extends GenericRepository<ChiTietHoaDon, Long> {

    public record ChiTietHoaDonItem(String maMonAn, int soLuong, float donGia) {}

    private static final String DA_THANH_TOAN = "Đã thanh toán";

    public ChiTietHoaDonRepository() {
        super(ChiTietHoaDon.class);
    }

    public List<ChiTietHoaDon> getChiTietTheoMaDon(String maDon) {
        return doInSession(em ->
                em.createQuery("""
                        SELECT c
                        FROM ChiTietHoaDon c
                        JOIN FETCH c.donDatMon d
                        JOIN FETCH c.monAn m
                        WHERE d.maDon = :maDon
                        """, ChiTietHoaDon.class)
                        .setParameter("maDon", maDon)
                        .getResultList()
        );
    }

    public ChiTietHoaDon findByMaDonAndMaMonAn(String maDon, String maMonAn) {
        return doInSession(em -> findByMaDonAndMaMonAn(em, maDon, maMonAn));
    }

    public void replaceByMaDon(String maDon, List<ChiTietHoaDonItem> items) {
        doInTransaction(em -> {
            DonDatMon don = em.find(DonDatMon.class, maDon);
            if (don == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn đặt món: " + maDon);
            }

            em.createQuery("""
                    DELETE FROM ChiTietHoaDon c
                    WHERE c.donDatMon.maDon = :maDon
                    """)
                    .setParameter("maDon", maDon)
                    .executeUpdate();

            for (ChiTietHoaDonItem item : items) {
                MonAn mon = em.find(MonAn.class, item.maMonAn());
                if (mon == null) {
                    throw new IllegalArgumentException("Không tìm thấy món ăn: " + item.maMonAn());
                }

                ChiTietHoaDon chiTiet = ChiTietHoaDon.builder()
                        .donDatMon(don)
                        .monAn(mon)
                        .tenMon(mon.getTenMon())
                        .soluong(item.soLuong())
                        .dongia(item.donGia())
                        .thanhtien(item.soLuong() * item.donGia())
                        .build();

                em.persist(chiTiet);
            }
        });
    }

    public boolean themChiTiet(String maDon, String maMonAn, int soLuong, float donGia) {
        return executeTransaction(em -> {
            DonDatMon don = em.find(DonDatMon.class, maDon);
            if (don == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn đặt món: " + maDon);
            }

            MonAn mon = em.find(MonAn.class, maMonAn);
            if (mon == null) {
                throw new IllegalArgumentException("Không tìm thấy món ăn: " + maMonAn);
            }

            ChiTietHoaDon chiTiet = ChiTietHoaDon.builder()
                    .donDatMon(don)
                    .monAn(mon)
                    .tenMon(mon.getTenMon())
                    .soluong(soLuong)
                    .dongia(donGia)
                    .thanhtien(soLuong * donGia)
                    .build();

            em.persist(chiTiet);
            return true;
        });
    }

    public boolean suaSoLuongChiTiet(String maDon, String maMonAn, int soLuong) {
        return executeTransaction(em -> {
            ChiTietHoaDon chiTiet = findByMaDonAndMaMonAn(em, maDon, maMonAn);

            if (chiTiet == null) {
                return false;
            }

            chiTiet.setSoluong(soLuong);
            chiTiet.setThanhtien(soLuong * chiTiet.getDongia());

            em.merge(chiTiet);
            return true;
        });
    }

    public boolean suaChiTiet(String maDon, String maMonAn, int soLuong) {
        return suaSoLuongChiTiet(maDon, maMonAn, soLuong);
    }

    public boolean xoaChiTiet(String maDon, String maMonAn) {
        return executeTransaction(em -> {
            int deleted = em.createQuery("""
                    DELETE FROM ChiTietHoaDon c
                    WHERE c.donDatMon.maDon = :maDon
                      AND c.monAn.maMonAn = :maMonAn
                    """)
                    .setParameter("maDon", maDon)
                    .setParameter("maMonAn", maMonAn)
                    .executeUpdate();

            return deleted > 0;
        });
    }

    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        return doInSession(em -> {
            Map<String, Integer> topItems = new LinkedHashMap<>();

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

            List<Object[]> rows = em.createQuery("""
                    SELECT c.monAn.tenMon, SUM(c.soluong)
                    FROM ChiTietHoaDon c, HoaDon h
                    WHERE h.donDatMon.maDon = c.donDatMon.maDon
                      AND h.trangThai = :trangThai
                      AND h.ngayLap >= :start
                      AND h.ngayLap < :endExclusive
                    GROUP BY c.monAn.tenMon
                    ORDER BY SUM(c.soluong) DESC
                    """, Object[].class)
                    .setParameter("trangThai", DA_THANH_TOAN)
                    .setParameter("start", start)
                    .setParameter("endExclusive", endExclusive)
                    .setMaxResults(limit)
                    .getResultList();

            for (Object[] row : rows) {
                String tenMon = (String) row[0];
                int tongSoLuong = ((Number) row[1]).intValue();

                topItems.put(tenMon, tongSoLuong);
            }

            return topItems;
        });
    }

    public List<String> getTopMonBanChayTrongNgay() {
        return doInSession(em -> {
            List<String> list = new ArrayList<>();

            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();

            List<Object[]> rows = em.createQuery("""
                    SELECT c.monAn.tenMon, SUM(c.soluong)
                    FROM ChiTietHoaDon c, HoaDon h
                    WHERE h.donDatMon.maDon = c.donDatMon.maDon
                      AND h.ngayLap >= :start
                      AND h.ngayLap < :endExclusive
                    GROUP BY c.monAn.tenMon
                    ORDER BY SUM(c.soluong) DESC
                    """, Object[].class)
                    .setParameter("start", start)
                    .setParameter("endExclusive", endExclusive)
                    .setMaxResults(3)
                    .getResultList();

            int rank = 1;

            for (Object[] row : rows) {
                String tenMon = (String) row[0];
                int soLuong = ((Number) row[1]).intValue();

                list.add("#" + rank + " " + tenMon + " (" + soLuong + " suất)");
                rank++;
            }

            if (list.isEmpty()) {
                list.add("Chưa có dữ liệu hôm nay");
            }

            return list;
        });
    }

    public Map<String, Integer> getLeastSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        return doInSession(em -> {
            Map<String, Integer> result = new LinkedHashMap<>();

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

            List<Object[]> rows = em.createQuery("""
                    SELECT c.monAn.tenMon, SUM(c.soluong)
                    FROM ChiTietHoaDon c, HoaDon h
                    WHERE h.donDatMon.maDon = c.donDatMon.maDon
                      AND h.trangThai = :trangThai
                      AND h.ngayLap >= :start
                      AND h.ngayLap < :endExclusive
                    GROUP BY c.monAn.tenMon
                    ORDER BY SUM(c.soluong) ASC
                    """, Object[].class)
                    .setParameter("trangThai", DA_THANH_TOAN)
                    .setParameter("start", start)
                    .setParameter("endExclusive", endExclusive)
                    .setMaxResults(limit)
                    .getResultList();

            for (Object[] row : rows) {
                String tenMon = (String) row[0];
                int soLuong = ((Number) row[1]).intValue();

                result.put(tenMon, soLuong);
            }

            return result;
        });
    }

    private ChiTietHoaDon findByMaDonAndMaMonAn(
            EntityManager em,
            String maDon,
            String maMonAn
    ) {
        List<ChiTietHoaDon> rows = em.createQuery("""
                SELECT c
                FROM ChiTietHoaDon c
                JOIN FETCH c.donDatMon d
                JOIN FETCH c.monAn m
                WHERE d.maDon = :maDon
                  AND m.maMonAn = :maMonAn
                """, ChiTietHoaDon.class)
                .setParameter("maDon", maDon)
                .setParameter("maMonAn", maMonAn)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) {
            return null;
        }

        return rows.get(0);
    }
}