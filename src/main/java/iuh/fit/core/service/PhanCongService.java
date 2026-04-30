package iuh.fit.core.service;

import iuh.fit.core.entity.CaLam;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.PhanCongId;
import iuh.fit.core.repository.PhanCongRepository;
import iuh.fit.infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;

public class PhanCongService {

    private final PhanCongRepository phanCongRepo = new PhanCongRepository();

    public void phanCong(String maNV, String maCa, LocalDate ngayLam) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            NhanVien nv = em.find(NhanVien.class, maNV);
            if (nv == null)
                throw new IllegalArgumentException("Nhân viên '" + maNV + "' không tồn tại.");

            CaLam ca = em.find(CaLam.class, maCa);
            if (ca == null)
                throw new IllegalArgumentException("Ca làm '" + maCa + "' không tồn tại.");

            PhanCongId id = new PhanCongId(maNV, maCa, ngayLam);
            if (em.find(PhanCong.class, id) != null)
                throw new IllegalArgumentException("Nhân viên đã được phân công ca này trong ngày.");

            em.persist(new PhanCong(nv, ca, ngayLam));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void huyPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PhanCongId id = new PhanCongId(maNV, maCa, ngayLam);
            PhanCong pc = em.find(PhanCong.class, id);
            if (pc == null)
                throw new IllegalArgumentException("Không tìm thấy phân công để hủy.");
            em.remove(pc);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<PhanCong> findByNhanVien(String maNV) {
        return phanCongRepo.findByNhanVien(maNV);
    }

    public List<PhanCong> findByNgayLam(LocalDate ngayLam) {
        return phanCongRepo.findByNgayLam(ngayLam);
    }

    public List<PhanCong> findAll() {
        return phanCongRepo.findAll();
    }
}
