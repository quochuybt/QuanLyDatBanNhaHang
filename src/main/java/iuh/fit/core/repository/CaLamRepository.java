package iuh.fit.core.repository;

import iuh.fit.core.entity.CaLam;

import java.util.List;
public class CaLamRepository extends GenericRepository<CaLam, String> {

    public CaLamRepository() {
        super(CaLam.class);
    }


    public List<Object[]> getAllCaLamNativeExecuteTransaction() {
        String query = "SELECT maCa, tenCa, gioBatDau, gioKetThuc FROM CaLam ORDER BY gioBatDau";
        return executeTransaction(em ->
                em.createNativeQuery(query)
                        .getResultList()
        );
    }
}
