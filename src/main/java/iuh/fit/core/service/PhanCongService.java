package iuh.fit.core.service;

import iuh.fit.core.entity.CaLam;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.PhanCongId;
import iuh.fit.core.repository.PhanCongRepository;
import iuh.fit.core.db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;

public class PhanCongService {

    private final PhanCongRepository phanCongRepo = new PhanCongRepository();

    // Có thể chỉnh theo nghiệp vụ: 30, 60 hoặc 90 ngày
    private static final int SO_NGAY_TOI_DA_DUOC_PHAN_CONG_TRUOC = 60;

    public void phanCong(String maNV, String maCa, LocalDate ngayLam) {
        validateMaNV(maNV);
        validateMaCa(maCa);
        validateNgayLamCoBan(ngayLam);

        maNV = maNV.trim();
        maCa = maCa.trim();

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            NhanVien nv = em.find(NhanVien.class, maNV);
            if (nv == null) {
                throw new IllegalArgumentException("Nhân viên '" + maNV + "' không tồn tại.");
            }

            CaLam ca = em.find(CaLam.class, maCa);
            if (ca == null) {
                throw new IllegalArgumentException("Ca làm '" + maCa + "' không tồn tại.");
            }

            validateNgayLamTheoNhanVien(nv, ngayLam);

            PhanCongId id = new PhanCongId(maNV, maCa, ngayLam);
            if (em.find(PhanCong.class, id) != null) {
                throw new IllegalArgumentException("Nhân viên đã được phân công ca này trong ngày.");
            }

            PhanCong phanCong = new PhanCong(nv, ca, ngayLam);
            em.persist(phanCong);

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void huyPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        validateMaNV(maNV);
        validateMaCa(maCa);
        validateNgayLamHuyPhanCong(ngayLam);

        maNV = maNV.trim();
        maCa = maCa.trim();

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            PhanCongId id = new PhanCongId(maNV, maCa, ngayLam);
            PhanCong pc = em.find(PhanCong.class, id);

            if (pc == null) {
                throw new IllegalArgumentException("Không tìm thấy phân công để hủy.");
            }

            em.remove(pc);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<PhanCong> findByNhanVien(String maNV) {
        validateMaNV(maNV);
        return phanCongRepo.findByNhanVien(maNV.trim());
    }

    public List<PhanCong> findByNgayLam(LocalDate ngayLam) {
        if (ngayLam == null) {
            throw new IllegalArgumentException("Ngày làm không được để trống.");
        }

        return phanCongRepo.findByNgayLam(ngayLam);
    }

    public List<PhanCong> findAll() {
        return phanCongRepo.findAll();
    }

    private void validateMaNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống.");
        }
    }

    private void validateMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ca làm không được để trống.");
        }
    }

    private void validateNgayLamCoBan(LocalDate ngayLam) {
        if (ngayLam == null) {
            throw new IllegalArgumentException("Ngày làm không được để trống.");
        }

        LocalDate homNay = LocalDate.now();

        if (ngayLam.isBefore(homNay)) {
            throw new IllegalArgumentException("Không thể phân công cho ngày đã qua.");
        }

        LocalDate ngayToiDa = homNay.plusDays(SO_NGAY_TOI_DA_DUOC_PHAN_CONG_TRUOC);
        if (ngayLam.isAfter(ngayToiDa)) {
            throw new IllegalArgumentException(
                    "Chỉ được phân công tối đa trước "
                            + SO_NGAY_TOI_DA_DUOC_PHAN_CONG_TRUOC
                            + " ngày."
            );
        }
    }

    private void validateNgayLamTheoNhanVien(NhanVien nv, LocalDate ngayLam) {
        if (nv.getNgayvaolam() == null) {
            throw new IllegalArgumentException("Nhân viên chưa có ngày vào làm, không thể phân công.");
        }

        if (ngayLam.isBefore(nv.getNgayvaolam())) {
            throw new IllegalArgumentException(
                    "Không thể phân công trước ngày vào làm của nhân viên. Ngày vào làm: "
                            + nv.getNgayvaolam()
            );
        }
    }

    private void validateNgayLamHuyPhanCong(LocalDate ngayLam) {
        if (ngayLam == null) {
            throw new IllegalArgumentException("Ngày làm không được để trống.");
        }

        if (ngayLam.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể hủy phân công của ngày đã qua.");
        }
    }
}