package iuh.fit.core.repository;

import iuh.fit.core.entity.KhachHang;

import java.util.List;

public class KhachHangRepository extends GenericRepository<KhachHang, String> {

    public KhachHangRepository() {
        super(KhachHang.class);
    }

    public KhachHang findByMaKH(String maKH) {
        return findById(maKH);
    }


    public KhachHang findBySdt(String sdt) {
        return doInSession(em ->
                em.createQuery("SELECT k FROM KhachHang k WHERE k.sdt = :sdt", KhachHang.class)
                        .setParameter("sdt", sdt)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    public List<KhachHang> search(String keyword) {
        return doInSession(em ->
                em.createQuery(
                                "SELECT k FROM KhachHang k " +
                                        "WHERE LOWER(k.tenKH) LIKE :kw OR k.sdt LIKE :kw",
                                KhachHang.class
                        )
                        .setParameter("kw", "%" + keyword.toLowerCase().trim() + "%")
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