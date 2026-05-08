package iuh.fit.core.repository;

import iuh.fit.core.entity.KhachHang;

import java.util.List;

public class KhachHangRepository extends GenericRepository<KhachHang, String> {

    public KhachHangRepository() {
        super(KhachHang.class);
    }

    public KhachHang findByMaKH(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        return findById(maKH.trim());
    }

    public KhachHang findBySdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        return doInSession(em ->
                em.createQuery(
                                "SELECT k FROM KhachHang k WHERE k.sdt = :sdt",
                                KhachHang.class
                        )
                        .setParameter("sdt", sdt.trim())
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    public List<KhachHang> search(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();

        return doInSession(em ->
                em.createQuery(
                                "SELECT k FROM KhachHang k " +
                                        "WHERE LOWER(k.tenKH) LIKE :kw OR k.sdt LIKE :kw",
                                KhachHang.class
                        )
                        .setParameter("kw", "%" + kw + "%")
                        .getResultList()
        );
    }

    public List<KhachHang> getAll() {
        return findAll();
    }

    public void add(KhachHang kh) {
        save(kh);
    }

    public void updateKhachHang(KhachHang kh) {
        update(kh);
    }
}