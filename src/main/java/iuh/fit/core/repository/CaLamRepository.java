package iuh.fit.core.repository;

import iuh.fit.core.entity.CaLam;

import java.util.List;

public class CaLamRepository extends GenericRepository<CaLam, String> {

    public CaLamRepository() {
        super(CaLam.class);
    }

    public List<CaLam> getAllCaLamOrderByGioBatDau() {
        return doInSession(em ->
                em.createQuery("""
                        SELECT c
                        FROM CaLam c
                        WHERE c.deletedAt IS NULL
                        ORDER BY c.gioBatDau
                        """, CaLam.class)
                        .getResultList()
        );
    }
}