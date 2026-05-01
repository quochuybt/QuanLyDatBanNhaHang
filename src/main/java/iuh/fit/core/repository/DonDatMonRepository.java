package iuh.fit.core.repository;

import iuh.fit.core.entity.DonDatMon;

public class DonDatMonRepository extends GenericRepository<DonDatMon, String> {
    public DonDatMonRepository() {
       super(DonDatMon.class);
    }
}
