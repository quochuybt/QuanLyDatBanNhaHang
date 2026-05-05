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

    public List<ChiTietHoaDon> findByMaDon(String maDon) {
        return doInSession(em -> em.createQuery(
                        "SELECT c FROM ChiTietHoaDon c WHERE c.donDatMon.maDon = :maDon", ChiTietHoaDon.class)
                .setParameter("maDon", maDon)
                .getResultList());
    }

    public void replaceByMaDon(String maDon, List<ChiTietHoaDonItem> items) {
        doInTransaction(em -> {
            em.createQuery("DELETE FROM ChiTietHoaDon c WHERE c.donDatMon.maDon = :maDon")
                    .setParameter("maDon", maDon)
                    .executeUpdate();

            DonDatMon don = em.find(DonDatMon.class, maDon);
            if (don == null) {
                throw new IllegalArgumentException("Không tìm thấy đơn đặt món: " + maDon);
            }

            for (ChiTietHoaDonItem item : items) {
                MonAn mon = em.find(MonAn.class, item.maMonAn());
                if (mon == null) continue;

                ChiTietHoaDon ct = ChiTietHoaDon.builder()
                        .donDatMon(don)
                        .monAn(mon)
                        .tenMon(mon.getTenMon())
                        .soluong(item.soLuong())
                        .dongia(item.donGia())
                        .thanhtien(item.soLuong() * item.donGia())
                        .build();
                em.persist(ct);
            }
        });
    }


    public List<ChiTietHoaDon> getChiTietTheoMaDon(String maDon) {
        return doInSession(em -> {
            List<ChiTietHoaDon> result = em.createQuery("""
                    SELECT c
                    FROM ChiTietHoaDonDTO c
                    JOIN FETCH c.donDatMon d
                    JOIN FETCH c.monAn m
                    WHERE d.maDon = :maDon
                    """, ChiTietHoaDon.class)
                    .setParameter("maDon", maDon)
                    .getResultList();

            for (ChiTietHoaDon ct : result) {
                if (ct.getMonAn() != null) {
                    ct.setTenMon(ct.getMonAn().getTenMon());
                }
            }

            return result;
        });
    }

    public boolean themChiTiet(ChiTietHoaDon ct) {
        return executeTransaction(em -> {
            if (ct.getDonDatMon() == null || ct.getDonDatMon().getMaDon() == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn chưa có mã đơn.");
            }

            if (ct.getMonAn() == null || ct.getMonAn().getMaMonAn() == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn chưa có mã món.");
            }

            DonDatMon donRef = em.getReference(
                    DonDatMon.class,
                    ct.getDonDatMon().getMaDon()
            );

            MonAn monRef = em.getReference(
                    MonAn.class,
                    ct.getMonAn().getMaMonAn()
            );

            ct.setDonDatMon(donRef);
            ct.setMonAn(monRef);

            if (ct.getTenMon() == null || ct.getTenMon().isBlank()) {
                ct.setTenMon(monRef.getTenMon());
            }

            ct.setThanhtien(ct.getSoluong() * ct.getDongia());

            em.persist(ct);
            return true;
        });
    }

    public boolean themChiTiet(String maDon, String maMonAn, int soLuong, float donGia) {
        return executeTransaction(em -> {
            DonDatMon donRef = em.getReference(DonDatMon.class, maDon);
            MonAn monRef = em.getReference(MonAn.class, maMonAn);

            ChiTietHoaDon ct = new ChiTietHoaDon();
            ct.setDonDatMon(donRef);
            ct.setMonAn(monRef);
            ct.setSoluong(soLuong);
            ct.setDongia(donGia);
            ct.setThanhtien(soLuong * donGia);
            ct.setTenMon(monRef.getTenMon());

            em.persist(ct);
            return true;
        });
    }

    public boolean xoaChiTiet(String maDon, String maMonAn) {
        return executeTransaction(em -> {
            int deleted = em.createQuery("""
                    DELETE FROM ChiTietHoaDonDTO c
                    WHERE c.donDatMon.maDon = :maDon
                      AND c.monAn.maMonAn = :maMonAn
                    """)
                    .setParameter("maDon", maDon)
                    .setParameter("maMonAn", maMonAn)
                    .executeUpdate();

            return deleted > 0;
        });
    }

    public boolean suaChiTiet(ChiTietHoaDon ct) {
        return executeTransaction(em -> {
            if (ct.getDonDatMon() == null || ct.getDonDatMon().getMaDon() == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn chưa có mã đơn.");
            }

            if (ct.getMonAn() == null || ct.getMonAn().getMaMonAn() == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn chưa có mã món.");
            }

            String maDon = ct.getDonDatMon().getMaDon();
            String maMonAn = ct.getMonAn().getMaMonAn();

            int updated = em.createQuery("""
                    UPDATE ChiTietHoaDonDTO c
                    SET c.soluong = :soLuong,
                        c.thanhtien = :thanhTien
                    WHERE c.donDatMon.maDon = :maDon
                      AND c.monAn.maMonAn = :maMonAn
                    """)
                    .setParameter("soLuong", ct.getSoluong())
                    .setParameter("thanhTien", ct.getSoluong() * ct.getDongia())
                    .setParameter("maDon", maDon)
                    .setParameter("maMonAn", maMonAn)
                    .executeUpdate();

            return updated > 0;
        });
    }

    public boolean suaChiTiet(String maDon, String maMonAn, int soLuong) {
        return executeTransaction(em -> {
            ChiTietHoaDon ct = findByMaDonAndMaMonAn(em, maDon, maMonAn);

            if (ct == null) {
                return false;
            }

            ct.setSoluong(soLuong);
            ct.setThanhtien(soLuong * ct.getDongia());

            em.merge(ct);
            return true;
        });
    }

    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        return doInSession(em -> {
            Map<String, Integer> topItems = new LinkedHashMap<>();

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

            List<Object[]> rows = em.createQuery("""
                    SELECT c.monAn.tenMon, SUM(c.soluong)
                    FROM ChiTietHoaDonDTO c, HoaDon h
                    WHERE h.maDon = c.donDatMon.maDon
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
                    FROM ChiTietHoaDonDTO c, HoaDon h
                    WHERE h.maDon = c.donDatMon.maDon
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
                    FROM ChiTietHoaDonDTO c, HoaDon h
                    WHERE h.maDon = c.donDatMon.maDon
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
                FROM ChiTietHoaDonDTO c
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
