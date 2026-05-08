package iuh.fit.core.repository;

import iuh.fit.core.entity.GiaoCa;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.PhanCong;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GiaoCaRepository extends GenericRepository<GiaoCa, String> {

    public GiaoCaRepository() {
        super(GiaoCa.class);
    }

    /**
     * Lấy thông tin ca đang làm việc (chưa kết thúc) của nhân viên
     */
    public GiaoCa getThongTinCaDangLam(String maNV) {
        return doInSession(em -> {
            String jpql = "SELECT g FROM GiaoCa g WHERE g.maNV = :maNV " +
                    "AND g.thoiGianKetThuc IS NULL ORDER BY g.thoiGianBatDau DESC";
            try {
                return em.createQuery(jpql, GiaoCa.class)
                        .setParameter("maNV", maNV)
                        .setMaxResults(1)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Bắt đầu ca làm việc mới
     */
    public boolean batDauCa(GiaoCa giaoCa) {
        return executeTransaction(em -> {
            String maNV = (giaoCa.getNhanVien() != null) ? giaoCa.getNhanVien().getManv() : giaoCa.getMaNV();

            String jpql = "SELECT g FROM GiaoCa g WHERE g.maNV = :maNV AND g.thoiGianKetThuc IS NULL";
            List<?> active = em.createQuery(jpql)
                    .setParameter("maNV", maNV)
                    .getResultList();

            if (!active.isEmpty()) {
                return false;
            }

            if (giaoCa.getNhanVien() == null && maNV != null) {
                giaoCa.setNhanVien(em.getReference(NhanVien.class, maNV));
            }

            em.persist(giaoCa);
            return true;
        });
    }

    /**
     * Kết thúc ca làm việc (Xử lý logic tính tiền hệ thống và chênh lệch)
     */
    public boolean ketThucCa(int maGiaoCa, double tienCuoiCa, String ghiChu) {
        return executeTransaction(em -> {
            GiaoCa gc = em.find(GiaoCa.class, maGiaoCa);
            if (gc == null || gc.getThoiGianKetThuc() != null) return false;

            // Tính tiền mặt từ hệ thống (HoaDon) trong khoảng thời gian ca làm
            String jpqlTien = "SELECT COALESCE(SUM(h.tongTien - COALESCE(h.giamGia, 0)), 0.0) " +
                    "FROM HoaDon h WHERE h.nhanVien.manv = :maNV " +
                    "AND h.trangThai = 'Đã thanh toán' " +
                    "AND h.hinhThucThanhToan = 'Tiền mặt' " +
                    "AND h.ngayLap >= :batDau AND h.ngayLap <= :hienTai";

            Double tienHeThong = em.createQuery(jpqlTien, Double.class)
                    .setParameter("maNV", gc.getNhanVien().getManv())
                    .setParameter("batDau", gc.getThoiGianBatDau())
                    .setParameter("hienTai", LocalDateTime.now())
                    .getSingleResult();

            gc.setThoiGianKetThuc(LocalDateTime.now());
            gc.setTienCuoiCa(tienCuoiCa);
            gc.setTienHeThongTinh(tienHeThong);
            gc.setChenhLech(tienCuoiCa - (gc.getTienDauCa() + tienHeThong));
            gc.setGhiChu(ghiChu);

            em.merge(gc);
            return true;
        });
    }

    /**
     * Thống kê tổng giờ làm theo tháng/tuần sử dụng TIMESTAMPDIFF (MariaDB)
     */
    public double getTongGioLam(String maNV, String type, LocalDate date) {
        return doInSession(em -> {
            String jpql = "SELECT g FROM GiaoCa g WHERE g.maNV = :maNV ";
            if ("MONTH".equals(type)) {
                jpql += "AND FUNCTION('MONTH', g.thoiGianBatDau) = :m AND FUNCTION('YEAR', g.thoiGianBatDau) = :y";
            } else {
                jpql += "AND g.thoiGianBatDau >= :start AND g.thoiGianBatDau < :end";
            }

            TypedQuery<GiaoCa> query = em.createQuery(jpql, GiaoCa.class).setParameter("maNV", maNV);
            if ("MONTH".equals(type)) {
                query.setParameter("m", date.getMonthValue()).setParameter("y", date.getYear());
            } else {
                query.setParameter("start", date.atStartOfDay()).setParameter("end", date.plusDays(7).atStartOfDay());
            }

            List<GiaoCa> list = query.getResultList();
            long totalMinutes = 0;
            for (GiaoCa g : list) {
                LocalDateTime batDau = g.getThoiGianBatDau();
                LocalDateTime ketThuc = (g.getThoiGianKetThuc() != null) ? g.getThoiGianKetThuc() : LocalDateTime.now();
                totalMinutes += java.time.Duration.between(batDau, ketThuc).toMinutes();
            }
            return totalMinutes / 60.0;
        });
    }

    /**
     * Lấy danh sách nhân viên đang làm việc kèm thông tin ca
     */
    public List<Object[]> getNhanVienDangLamViecChiTiet() {
        return doInSession(em -> {
            // Sử dụng JPQL Join thông qua các field đã map trong Entity
            String jpql = "SELECT nv.hoten, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM GiaoCa g " +
                    "JOIN g.nhanVien nv " +
                    "LEFT JOIN PhanCong pc ON nv.manv = pc.nhanVien.manv AND pc.id.ngayLam = CURRENT_DATE " +
                    "LEFT JOIN pc.caLam cl " +
                    "WHERE g.thoiGianKetThuc IS NULL " +
                    "ORDER BY g.thoiGianBatDau DESC";

            return em.createQuery(jpql).getResultList();
        });
    }

    /**
     * Thống kê giờ làm theo ngày để vẽ biểu đồ
     */
    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDateTime limitDate = LocalDateTime.now().minusDays(soNgay).withHour(0).withMinute(0);

        // Khởi tạo Map
        for (int i = soNgay - 1; i >= 0; i--) {
            data.put(LocalDate.now().minusDays(i).format(formatter), 0.0);
        }

        return doInSession(em -> {
            String jpql = "SELECT g FROM GiaoCa g WHERE g.maNV = :maNV AND g.thoiGianBatDau >= :limitDate";
            List<GiaoCa> results = em.createQuery(jpql, GiaoCa.class)
                    .setParameter("maNV", maNV)
                    .setParameter("limitDate", limitDate)
                    .getResultList();

            for (GiaoCa g : results) {
                String dateStr = g.getThoiGianBatDau().format(formatter);
                if (data.containsKey(dateStr)) {
                    LocalDateTime ketThuc = (g.getThoiGianKetThuc() != null) ? g.getThoiGianKetThuc() : LocalDateTime.now();
                    double hours = java.time.Duration.between(g.getThoiGianBatDau(), ketThuc).toMinutes() / 60.0;
                    data.put(dateStr, data.get(dateStr) + hours);
                }
            }
            return data;
        });
    }

    /**
     * Lấy lịch sử giao ca theo khoảng ngày
     */
    public List<GiaoCa> getLichSuGiaoCa(LocalDate tuNgay, LocalDate denNgay) {
        return doInSession(em -> {
            String jpql = "SELECT g FROM GiaoCa g WHERE CAST(g.thoiGianBatDau AS date) BETWEEN :tu AND :den " +
                    "ORDER BY g.thoiGianBatDau DESC";
            return em.createQuery(jpql, GiaoCa.class)
                    .setParameter("tu", tuNgay)
                    .setParameter("den", denNgay)
                    .getResultList();
        });
    }

    public List<String> getCacCaLamSapToi(String maNV) {
        return doInSession(em -> {
            String jpql = "SELECT cl.tenCa, pc.id.ngayLam, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM PhanCong pc " +
                    "JOIN pc.caLam cl " +
                    "WHERE pc.nhanVien.manv = :maNV " +
                    "AND pc.id.ngayLam >= CURRENT_DATE " +
                    "ORDER BY pc.id.ngayLam ASC, cl.gioBatDau ASC";

            List<Object[]> results = em.createQuery(jpql)
                    .setParameter("maNV", maNV)
                    .setMaxResults(3)
                    .getResultList();

            return results.stream().map(row -> {
                String tenCa = (String) row[0];

                Object dateObj = row[1];
                java.time.LocalDate ngay = null;
                if (dateObj instanceof java.sql.Date) {
                    ngay = ((java.sql.Date) dateObj).toLocalDate();
                } else if (dateObj instanceof java.time.LocalDate) {
                    ngay = (java.time.LocalDate) dateObj;
                }

                Object bdObj = row[2];
                java.time.LocalTime bd = null;
                if (bdObj instanceof java.sql.Time) {
                    bd = ((java.sql.Time) bdObj).toLocalTime();
                } else if (bdObj instanceof java.time.LocalTime) {
                    bd = (java.time.LocalTime) bdObj;
                }

                Object ktObj = row[3];
                java.time.LocalTime kt = null;
                if (ktObj instanceof java.sql.Time) {
                    kt = ((java.sql.Time) ktObj).toLocalTime();
                } else if (ktObj instanceof java.time.LocalTime) {
                    kt = (java.time.LocalTime) ktObj;
                }

                String ngayStr = (ngay != null) ? ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) : "--/--";
                String bdStr = (bd != null) ? bd.toString().substring(0, 5) : "--:--";
                String ktStr = (kt != null) ? kt.toString().substring(0, 5) : "--:--";

                return String.format("%s: %s (%s-%s)", ngayStr, tenCa, bdStr, ktStr);
            }).collect(java.util.stream.Collectors.toList());
        });
    }
    public String[] getThongTinCaTruocSau(String maNV, LocalDate ngay) {
        return doInSession(em -> {
            // Lấy tất cả phân công của nhân viên trong ngày này, sắp xếp theo giờ bắt đầu
            String jpql = "SELECT pc FROM PhanCong pc " +
                    "JOIN FETCH pc.caLam cl " +
                    "WHERE pc.nhanVien.manv = :maNV AND pc.id.ngayLam = :ngay " +
                    "ORDER BY cl.gioBatDau ASC";

            List<PhanCong> list = em.createQuery(jpql, PhanCong.class)
                    .setParameter("maNV", maNV)
                    .setParameter("ngay", ngay)
                    .getResultList();

            String caTruoc = "Không có";
            String caSau = "Không có";

            if (list.isEmpty()) return new String[]{caTruoc, caSau};

            // Lấy thời gian hiện tại để xác định vị trí
            java.time.LocalTime bayGio = java.time.LocalTime.now();

            for (int i = 0; i < list.size(); i++) {
                PhanCong hienTai = list.get(i);
                java.time.LocalTime batDau = hienTai.getCaLam().getGioBatDau();
                java.time.LocalTime ketThuc = hienTai.getCaLam().getGioKetThuc();

                // Kiểm tra xem có phải đang trong ca này không hoặc ca này sắp tới/vừa qua
                if (bayGio.isAfter(batDau) && bayGio.isBefore(ketThuc)) {
                    // Nếu đang trong ca này:
                    if (i > 0) caTruoc = formatCaInfo(list.get(i - 1));
                    if (i < list.size() - 1) caSau = formatCaInfo(list.get(i + 1));
                    break;
                } else if (bayGio.isBefore(batDau)) {
                    // Nếu chưa tới ca này (ca này là ca tiếp theo):
                    if (i > 0) caTruoc = formatCaInfo(list.get(i - 1));
                    caSau = formatCaInfo(hienTai);
                    break;
                } else if (i == list.size() - 1) {
                    // Nếu đã qua hết các ca
                    caTruoc = formatCaInfo(hienTai);
                }
            }

            return new String[]{caTruoc, caSau};
        });
    }

    // Hàm helper để định dạng chuỗi hiển thị
    private String formatCaInfo(PhanCong pc) {
        return pc.getCaLam().getTenCa() + " (" +
                pc.getCaLam().getGioBatDau().toString().substring(0, 5) + "-" +
                pc.getCaLam().getGioKetThuc().toString().substring(0, 5) + ")";
    }

    public Map<String, Double> getTopStaffByWorkHours(LocalDate startDate, LocalDate endDate, int limit) {
        return doInSession(em -> {
            // Lấy danh sách ca làm trong khoảng thời gian
            String jpql = "SELECT g FROM GiaoCa g JOIN FETCH g.nhanVien nv " +
                    "WHERE g.thoiGianBatDau >= :start AND g.thoiGianBatDau <= :end " +
                    "AND nv.vaiTro = :role";

            List<GiaoCa> list = em.createQuery(jpql, GiaoCa.class)
                    .setParameter("start", startDate.atStartOfDay())
                    .setParameter("end", endDate.atTime(23, 59, 59))
                    .setParameter("role", iuh.fit.core.entity.VaiTro.NHANVIEN)
                    .getResultList();

            // Gom nhóm và tính tổng giờ bằng Java Stream
            Map<String, Double> unsortedMap = list.stream().collect(java.util.stream.Collectors.groupingBy(
                    g -> g.getNhanVien().getHoten(),
                    java.util.stream.Collectors.summingDouble(g -> {
                        java.time.LocalDateTime start = g.getThoiGianBatDau();
                        java.time.LocalDateTime end = (g.getThoiGianKetThuc() != null) ? g.getThoiGianKetThuc() : java.time.LocalDateTime.now();
                        return java.time.Duration.between(start, end).toMinutes() / 60.0;
                    })
            ));

            // Sắp xếp giảm dần và giới hạn số lượng (limit)
            return unsortedMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
        });
    }
}