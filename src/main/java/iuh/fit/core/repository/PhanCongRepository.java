package iuh.fit.core.repository;

import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.PhanCongId;
import java.time.LocalDate;
import java.util.List;

public class PhanCongRepository extends GenericRepository<PhanCong, PhanCongId> {

    public PhanCongRepository() {
        super(PhanCong.class);
    }

    public List<PhanCong> findByNhanVien(String maNV) {
        return doInSession(em ->
                em.createQuery("SELECT pc FROM PhanCong pc WHERE pc.nhanVien.manv = :maNV", PhanCong.class)
                        .setParameter("maNV", maNV)
                        .getResultList());
    }

    public List<PhanCong> findByNgayLam(LocalDate ngayLam) {
        return doInSession(em ->
                em.createQuery("SELECT pc FROM PhanCong pc WHERE pc.id.ngayLam = :ngayLam", PhanCong.class)
                        .setParameter("ngayLam", ngayLam)
                        .getResultList());
    }
}
