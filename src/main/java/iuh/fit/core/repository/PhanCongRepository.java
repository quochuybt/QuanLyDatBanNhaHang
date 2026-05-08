package iuh.fit.core.repository;

import iuh.fit.core.entity.CaLam;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.PhanCongId;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhanCongRepository extends GenericRepository<PhanCong, PhanCongId> {

    public PhanCongRepository() {
        super(PhanCong.class);
    }

    public List<PhanCong> findByNhanVien(String maNV) {
        return doInSession(em ->
                em.createQuery("""
                                SELECT pc
                                FROM PhanCong pc
                                JOIN FETCH pc.nhanVien nv
                                JOIN FETCH pc.caLam ca
                                WHERE nv.manv = :maNV
                                ORDER BY pc.id.ngayLam, ca.gioBatDau
                                """, PhanCong.class)
                        .setParameter("maNV", maNV)
                        .getResultList()
        );
    }

    public List<PhanCong> findByNgayLam(LocalDate ngayLam) {
        return doInSession(em ->
                em.createQuery("""
                                SELECT pc
                                FROM PhanCong pc
                                JOIN FETCH pc.nhanVien nv
                                JOIN FETCH pc.caLam ca
                                WHERE pc.id.ngayLam = :ngayLam
                                ORDER BY ca.gioBatDau, nv.hoten
                                """, PhanCong.class)
                        .setParameter("ngayLam", ngayLam)
                        .getResultList()
        );
    }

    public List<PhanCong> findByNhanVienAndNgayLam(String maNV, LocalDate ngayLam) {
        return doInSession(em ->
                em.createQuery("""
                                SELECT pc
                                FROM PhanCong pc
                                JOIN FETCH pc.nhanVien nv
                                JOIN FETCH pc.caLam ca
                                WHERE nv.manv = :maNV
                                  AND pc.id.ngayLam = :ngayLam
                                ORDER BY ca.gioBatDau
                                """, PhanCong.class)
                        .setParameter("maNV", maNV)
                        .setParameter("ngayLam", ngayLam)
                        .getResultList()
        );
    }

    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        return doInSession(em ->
                em.createQuery("""
                                SELECT pc
                                FROM PhanCong pc
                                JOIN FETCH pc.nhanVien nv
                                JOIN FETCH pc.caLam ca
                                WHERE pc.id.ngayLam BETWEEN :tuNgay AND :denNgay
                                ORDER BY pc.id.ngayLam, ca.gioBatDau, nv.hoten
                                """, PhanCong.class)
                        .setParameter("tuNgay", tuNgay)
                        .setParameter("denNgay", denNgay)
                        .getResultList()
        );
    }

    public boolean existsById(PhanCongId id) {
        return doInSession(em ->
                em.find(PhanCong.class, id) != null
        );
    }

    public CaLam getCaLamViecCuaNhanVien(String maNV, LocalDate ngayLam) {
        return doInSession(em -> {
            List<CaLam> result = em.createQuery("""
                            SELECT pc.caLam
                            FROM PhanCong pc
                            JOIN pc.caLam ca
                            JOIN pc.nhanVien nv
                            WHERE nv.manv = :maNV
                              AND pc.id.ngayLam = :ngayLam
                            ORDER BY ca.gioBatDau
                            """, CaLam.class)
                    .setParameter("maNV", maNV)
                    .setParameter("ngayLam", ngayLam)
                    .setMaxResults(1)
                    .getResultList();

            return result.isEmpty() ? null : result.get(0);
        });
    }

    public Map<String, Double> getTongGioLamTheoThang(int thang, int nam) {
        return doInSession(em -> {
            YearMonth yearMonth = YearMonth.of(nam, thang);
            LocalDate tuNgay = yearMonth.atDay(1);
            LocalDate denNgay = yearMonth.atEndOfMonth();

            List<PhanCong> dsPhanCong = em.createQuery("""
                        SELECT pc
                        FROM PhanCong pc
                        JOIN FETCH pc.nhanVien nv
                        JOIN FETCH pc.caLam ca
                        WHERE pc.id.ngayLam BETWEEN :tuNgay AND :denNgay
                        ORDER BY nv.manv, pc.id.ngayLam, ca.gioBatDau
                        """, PhanCong.class)
                    .setParameter("tuNgay", tuNgay)
                    .setParameter("denNgay", denNgay)
                    .getResultList();

            Map<String, Double> map = new HashMap<>();

            for (PhanCong pc : dsPhanCong) {
                if (pc.getNhanVien() == null || pc.getCaLam() == null) {
                    continue;
                }

                String maNV = pc.getNhanVien().getManv();

                LocalTime gioBatDau = pc.getCaLam().getGioBatDau();
                LocalTime gioKetThuc = pc.getCaLam().getGioKetThuc();

                if (maNV == null || gioBatDau == null || gioKetThuc == null) {
                    continue;
                }

                double soGio = tinhSoGioLam(gioBatDau, gioKetThuc);

                map.merge(maNV, soGio, Double::sum);
            }

            return map;
        });
    }

    private double tinhSoGioLam(LocalTime gioBatDau, LocalTime gioKetThuc) {
        Duration duration;

        if (gioKetThuc.isAfter(gioBatDau)) {
            duration = Duration.between(gioBatDau, gioKetThuc);
        } else {
            duration = Duration.between(gioBatDau, gioKetThuc.plusHours(24));
        }

        return duration.toMinutes() / 60.0;
    }
}