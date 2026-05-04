package iuh.fit.core.repository;

import iuh.fit.core.entity.KhuyenMai;

import java.util.List;

public class KhuyenMaiRepository extends GenericRepository<KhuyenMai, String> {

    public KhuyenMaiRepository() {
        super(KhuyenMai.class);
    }

    public KhuyenMai findActiveById(String maKM) {
        return doInSession(em ->
                em.createQuery("""
                    SELECT k FROM KhuyenMai k
                    WHERE k.maKM = :ma AND k.trangThai = 'Đang áp dụng'
                """, KhuyenMai.class)
                        .setParameter("ma", maKM)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    public List<KhuyenMai> findAllActive() {
        return doInSession(em ->
                em.createQuery("""
                    SELECT k FROM KhuyenMai k
                    WHERE k.trangThai = 'Đang áp dụng'
                """, KhuyenMai.class)
                        .getResultList()
        );
    }

    public List<KhuyenMai> findByKeyword(String keyword) {
        return doInSession(em ->
                em.createQuery("""
                    SELECT k FROM KhuyenMai k
                    WHERE LOWER(k.tenChuongTrinh) LIKE LOWER(:kw)
                    OR LOWER(k.maKM) LIKE LOWER(:kw)
                """, KhuyenMai.class)
                        .setParameter("kw", "%" + keyword + "%")
                        .getResultList()
        );
    }
}