package iuh.fit.core.repository;

import iuh.fit.core.entity.DanhMucMon;

import java.util.List;

public class DanhMucMonRepository extends GenericRepository<DanhMucMon, String> {

    public DanhMucMonRepository() {
        super(DanhMucMon.class);
    }

    public List<DanhMucMon> findAllByName() {
        return doInSession(em -> em.createQuery(
                        "SELECT d FROM DanhMucMon d ORDER BY d.tendm", DanhMucMon.class)
                .getResultList());
    }
}
