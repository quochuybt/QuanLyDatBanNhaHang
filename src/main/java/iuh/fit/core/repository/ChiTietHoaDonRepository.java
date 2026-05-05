package iuh.fit.core.repository;

import iuh.fit.core.entity.ChiTietHoaDon;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.MonAn;

import java.util.List;

public class ChiTietHoaDonRepository extends GenericRepository<ChiTietHoaDon, Long> {

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

    public record ChiTietHoaDonItem(String maMonAn, int soLuong, float donGia) {}
}
