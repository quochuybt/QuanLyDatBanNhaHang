package iuh.fit.core.repository;

import iuh.fit.core.entity.CaLam;

public class CaLamRepository extends GenericRepository<CaLam, String> {

    public CaLamRepository() {
        super(CaLam.class);
    }
}
