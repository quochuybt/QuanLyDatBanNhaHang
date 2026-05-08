package iuh.fit.core.service;

import iuh.fit.core.db.JPAUtil;
import iuh.fit.core.entity.CaLam;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.PhanCongId;
import iuh.fit.core.repository.PhanCongRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class PhanCongService {

    private final PhanCongRepository phanCongRepo = new PhanCongRepository();

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
            validateTrungPhanCong(em, maNV, maCa, ngayLam);
            validateTrungThoiGianCaLam(maNV, ca, ngayLam);

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

    public boolean themPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        try {
            phanCong(maNV, maCa, ngayLam);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean xoaPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        try {
            huyPhanCong(maNV, maCa, ngayLam);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Từ ngày và đến ngày không được để trống.");
        }

        if (tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }

        return phanCongRepo.getPhanCongChiTiet(tuNgay, denNgay);
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

    public List<PhanCong> findByNhanVienAndNgayLam(String maNV, LocalDate ngayLam) {
        validateMaNV(maNV);

        if (ngayLam == null) {
            throw new IllegalArgumentException("Ngày làm không được để trống.");
        }

        return phanCongRepo.findByNhanVienAndNgayLam(maNV.trim(), ngayLam);
    }

    public List<PhanCong> findAll() {
        return phanCongRepo.findAll();
    }

    public CaLam getCaLamViecCuaNhanVien(String maNV, LocalDate ngayLam) {
        validateMaNV(maNV);

        if (ngayLam == null) {
            throw new IllegalArgumentException("Ngày làm không được để trống.");
        }

        return phanCongRepo.getCaLamViecCuaNhanVien(maNV.trim(), ngayLam);
    }

    public Map<String, Double> getTongGioLamTheoThang(int thang, int nam) {
        if (thang < 1 || thang > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ.");
        }

        if (nam < 2000) {
            throw new IllegalArgumentException("Năm không hợp lệ.");
        }

        return phanCongRepo.getTongGioLamTheoThang(thang, nam);
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

    private void validateTrungPhanCong(EntityManager em, String maNV, String maCa, LocalDate ngayLam) {
        PhanCongId id = new PhanCongId(maNV, maCa, ngayLam);

        if (em.find(PhanCong.class, id) != null) {
            throw new IllegalArgumentException("Nhân viên đã được phân công ca này trong ngày.");
        }
    }

    private void validateTrungThoiGianCaLam(String maNV, CaLam caMoi, LocalDate ngayLam) {
        List<PhanCong> dsPhanCongTrongNgay = phanCongRepo.findByNhanVienAndNgayLam(maNV, ngayLam);

        for (PhanCong pc : dsPhanCongTrongNgay) {
            CaLam caCu = pc.getCaLam();

            if (caCu == null) {
                continue;
            }

            if (isTimeOverlap(
                    caCu.getGioBatDau(),
                    caCu.getGioKetThuc(),
                    caMoi.getGioBatDau(),
                    caMoi.getGioKetThuc()
            )) {
                throw new IllegalArgumentException(
                        "Nhân viên đã có ca làm trùng thời gian trong ngày "
                                + ngayLam
                                + "."
                );
            }
        }
    }

    private boolean isTimeOverlap(
            LocalTime start1,
            LocalTime end1,
            LocalTime start2,
            LocalTime end2
    ) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}