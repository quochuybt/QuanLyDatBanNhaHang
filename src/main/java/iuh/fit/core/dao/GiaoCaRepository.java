package iuh.fit.core.dao;

import iuh.fit.core.entity.GiaoCa;

public class GiaoCaRepository extends GenericRepository<GiaoCa, String>{
    public GiaoCaRepository() {
        super(GiaoCa.class);
    }
}
