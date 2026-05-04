package iuh.fit.core.repository;

import iuh.fit.core.entity.GiaoCa;
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
        if (getThongTinCaDangLam(giaoCa.getNhanVien().getManv()) != null) {
            return false; // Nhân viên đang có ca chưa kết thúc
        }
        try {
            save(giaoCa);
            return true;
        } catch (Exception e) {
            return false;
        }
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
                    "FROM HoaDon h WHERE h.maNV = :maNV " +
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
            StringBuilder sql = new StringBuilder("SELECT SUM(TIMESTAMPDIFF(MINUTE, thoiGianBatDau, COALESCE(thoiGianKetThuc, NOW()))) ");
            sql.append("FROM GiaoCa WHERE maNV = :maNV ");

            if ("MONTH".equals(type)) {
                sql.append("AND MONTH(thoiGianBatDau) = :m AND YEAR(thoiGianBatDau) = :y ");
            } else {
                sql.append("AND thoiGianBatDau >= :start AND thoiGianBatDau < :end ");
            }

            var query = em.createNativeQuery(sql.toString());
            query.setParameter("maNV", maNV);

            if ("MONTH".equals(type)) {
                query.setParameter("m", date.getMonthValue());
                query.setParameter("y", date.getYear());
            } else {
                query.setParameter("start", date.atStartOfDay());
                query.setParameter("end", date.plusDays(7).atStartOfDay());
            }

            Object result = query.getSingleResult();
            return result != null ? ((Number) result).doubleValue() / 60.0 : 0.0;
        });
    }

    /**
     * Lấy danh sách nhân viên đang làm việc kèm thông tin ca
     */
    public List<Object[]> getNhanVienDangLamViecChiTiet() {
        return doInSession(em -> {
            String sql = "SELECT nv.hoTen, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM GiaoCa g " +
                    "JOIN NhanVien nv ON g.maNV = nv.maNV " +
                    "LEFT JOIN PhanCongCa pc ON g.maNV = pc.maNV AND pc.ngayLam = CURRENT_DATE " +
                    "LEFT JOIN CaLam cl ON pc.maCa = cl.maCa " +
                    "WHERE g.thoiGianKetThuc IS NULL " +
                    "ORDER BY g.thoiGianBatDau DESC";
            return em.createNativeQuery(sql).getResultList();
        });
    }

    /**
     * Thống kê giờ làm theo ngày để vẽ biểu đồ
     */
    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();

        // Khởi tạo các ngày với giá trị 0
        for (int i = soNgay - 1; i >= 0; i--) {
            data.put(today.minusDays(i).format(formatter), 0.0);
        }

        return doInSession(em -> {
            String sql = "SELECT DATE(thoiGianBatDau) as Ngay, " +
                    "SUM(TIMESTAMPDIFF(MINUTE, thoiGianBatDau, COALESCE(thoiGianKetThuc, NOW()))) / 60.0 as GioLam " +
                    "FROM GiaoCa WHERE maNV = :maNV " +
                    "AND thoiGianBatDau >= DATE_SUB(CURRENT_DATE, INTERVAL :days DAY) " +
                    "GROUP BY DATE(thoiGianBatDau)";

            List<Object[]> results = em.createNativeQuery(sql)
                    .setParameter("maNV", maNV)
                    .setParameter("days", soNgay)
                    .getResultList();

            for (Object[] row : results) {
                String dateStr = ((java.sql.Date) row[0]).toLocalDate().format(formatter);
                if (data.containsKey(dateStr)) {
                    data.put(dateStr, ((Number) row[1]).doubleValue());
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
}