package iuh.fit.core.repository;

import iuh.fit.core.entity.MonAn;
import java.util.List;

public class MonAnRepository extends GenericRepository<MonAn, String> {

    public MonAnRepository() {
        super(MonAn.class);
    }

    public List<MonAn> findAllDangKinhDoanh() {
        return doInSession(em ->
                em.createQuery("""
                    SELECT m FROM MonAn m
                    WHERE m.trangThai = 'Còn'
                """, MonAn.class)
                        .getResultList()
        );
    }

    public MonAn findByName(String tenMon) {
        return doInSession(em ->
                em.createQuery("""
                    SELECT m FROM MonAn m
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