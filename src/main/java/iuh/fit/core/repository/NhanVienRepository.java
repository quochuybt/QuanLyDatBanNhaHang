package iuh.fit.core.repository;

import iuh.fit.core.entity.NhanVien;

public class NhanVienRepository extends GenericRepository<NhanVien, String> {

    public NhanVienRepository() {
        super(NhanVien.class);
    }

    public NhanVien findByTenTK(String tenTK) {
        return doInSession(em ->
                em.createQuery("SELECT n FROM NhanVien n WHERE n.taiKhoan.tentk = :tenTK", NhanVien.class)
                        .setParameter("tenTK", tenTK)
                        .getResultStream()
                        .findFirst()
                        .orElse(null));
    }
}
