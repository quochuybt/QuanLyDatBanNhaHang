package iuh.fit.core.repository;

import iuh.fit.core.entity.HoaDon;

public class HoaDonRepository extends GenericRepository<HoaDon,String> {
    public HoaDonRepository() {
        super(HoaDon.class);
    }
}
