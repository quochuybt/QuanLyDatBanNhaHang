package iuh.fit.core.repository;

import iuh.fit.core.entity.NhanVien;
import java.util.List;

public class NhanVienRepository extends GenericRepository<NhanVien, String> {

    public NhanVienRepository() {
        super(NhanVien.class);
    }

    public NhanVien findByTenTK(String tenTK) {
        return doInSession(em ->
                em.createQuery("SELECT n FROM NhanVien n WHERE n.taiKhoan.tentk = :tenTK AND n.deletedAt IS NULL", NhanVien.class)
                        .setParameter("tenTK", tenTK)
                        .getResultStream()
                        .findFirst()
                        .orElse(null));
    }
}
