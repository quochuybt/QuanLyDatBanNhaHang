package iuh.fit.core.repository;

import iuh.fit.core.entity.MonAn;
import java.util.List;

public class MonAnRepository extends GenericRepository<MonAn, String> {

    public MonAnRepository() {
        super(MonAn.class);
    }

    @Override
    public List<MonAn> findAll() {
        return doInSession(em ->
                em.createQuery("""
                    SELECT m FROM MonAn m
                    LEFT JOIN FETCH m.danhMucMon
                """, MonAn.class)
                        .getResultList()
        );
    }

    public List<MonAn> findAllDangKinhDoanh() {
        return doInSession(em ->
                em.createQuery("""
                    SELECT m FROM MonAn m
                    LEFT JOIN FETCH m.danhMucMon
                    WHERE m.trangThai = 'Còn'
                """, MonAn.class)
                        .getResultList()
        );
    }

    public MonAn findByName(String tenMon) {
        return doInSession(em ->
                em.createQuery("""
                    SELECT m FROM MonAn m
                    LEFT JOIN FETCH m.danhMucMon
                    WHERE m.tenMon = :ten
                """, MonAn.class)
                        .setParameter("ten", tenMon)
                        .getResultStream()
                        .findFirst()
                        .orElse(null)
        );
    }

    public MonAn findByIdSafe(String maMon) {
        return findById(maMon);
    }
}