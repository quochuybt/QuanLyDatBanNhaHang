package iuh.fit.core.repository;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.entity.HoaDon;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.TrangThaiBan;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HoaDonRepository extends GenericRepository<HoaDon, String> {

    public HoaDonRepository() {
        super(HoaDon.class);
    }

    /**
     * Lấy hóa đơn chưa thanh toán theo mã bàn
     */
    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        return doInSession(em -> {
            String jpql = "SELECT hd FROM HoaDon hd JOIN hd.donDatMon ddm " +
                    "WHERE ddm.ban.maBan = :maBan AND hd.trangThai = :trangThai";
            try {
                return em.createQuery(jpql, HoaDon.class)
                        .setParameter("maBan", maBan)
                        .setParameter("trangThai", "Chưa thanh toán")
                        .getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                return null;
            }
        });
    }

    /**
     * Lọc danh sách hóa đơn có phân trang
     */
    public List<HoaDon> getHoaDonByPage(int page, int itemsPerPage, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return doInSession(em -> {
            StringBuilder jpql = new StringBuilder("SELECT hd FROM HoaDon hd WHERE 1=1");
            buildFilterQuery(jpql, trangThai, keyword, tuNgay, denNgay);
            jpql.append(" ORDER BY hd.ngayLap DESC");

            TypedQuery<HoaDon> query = em.createQuery(jpql.toString(), HoaDon.class);
            setFilterParameters(query, trangThai, keyword, tuNgay, denNgay);

            query.setFirstResult((page - 1) * itemsPerPage);
            query.setMaxResults(itemsPerPage);

            return query.getResultList();
        });
    }

    /**
     * Đếm tổng số hóa đơn theo bộ lọc
     */
    public long getTotalHoaDonCount(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return doInSession(em -> {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(hd) FROM HoaDon hd WHERE 1=1");
            buildFilterQuery(jpql, trangThai, keyword, tuNgay, denNgay);

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            setFilterParameters(query, trangThai, keyword, tuNgay, denNgay);

            return query.getSingleResult();
        });
    }

    /**
     * Thống kê doanh thu hàng ngày
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return doInSession(em -> {
            String jpql = "SELECT CAST(hd.ngayLap AS date), SUM(hd.tongTien) " +
                    "FROM HoaDon hd " +
                    "WHERE hd.trangThai = 'Đã thanh toán' " +
                    "AND hd.ngayLap BETWEEN :start AND :end " +
                    "GROUP BY CAST(hd.ngayLap AS date) ORDER BY CAST(hd.ngayLap AS date)";

            List<Object[]> results = em.createQuery(jpql)
                    .setParameter("start", startDate.atStartOfDay())
                    .setParameter("end", endDate.plusDays(1).atStartOfDay())
                    .getResultList();

            Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();
            for (Object[] result : results) {
                revenueMap.put((LocalDate) result[0], (Double) result[1]);
            }
            return revenueMap;
        });
    }

    /**
     * Lấy doanh thu theo hình thức thanh toán của một nhân viên trong ca
     */
    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        return doInSession(em -> {
            String jpql = "SELECT COALESCE(SUM(hd.tongTien - COALESCE(hd.giamGia, 0)), 0.0) " +
                    "FROM HoaDon hd " +
                    "WHERE hd.maNV = :maNV " +
                    "AND hd.ngayLap >= :startTime " +
                    "AND hd.trangThai = 'Đã thanh toán' " +
                    "AND hd.hinhThucThanhToan = :hinhThuc";

            return em.createQuery(jpql, Double.class)
                    .setParameter("maNV", maNV)
                    .setParameter("startTime", thoiGianBatDauCa)
                    .setParameter("hinhThuc", hinhThuc)
                    .getSingleResult();
        });
    }

    /**
     * Helper: Xây dựng câu truy vấn động
     */
    private void buildFilterQuery(StringBuilder jpql, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            jpql.append(" AND hd.trangThai = :trangThai");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND hd.maHD LIKE :keyword");
        }
        if (tuNgay != null) {
            jpql.append(" AND hd.ngayLap >= :tuNgay");
        }
        if (denNgay != null) {
            jpql.append(" AND hd.ngayLap <= :denNgay");
        }
    }

    /**
     * Helper: Set parameters cho câu truy vấn động
     */
    private void setFilterParameters(TypedQuery<?> query, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!"Tất cả".equalsIgnoreCase(trangThai)) {
            query.setParameter("trangThai", trangThai);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (tuNgay != null) {
            query.setParameter("tuNgay", tuNgay);
        }
        if (denNgay != null) {
            query.setParameter("denNgay", denNgay);
        }
    }

    /**
     * Cập nhật mã khuyến mãi
     */
    public boolean capNhatMaKM(String maHD, String maKM) {
        return executeTransaction(em -> {
            HoaDon hd = em.find(HoaDon.class, maHD);
            if (hd != null) {
                // Giả sử HoaDon có field KhuyenMai hoặc maKM String
                hd.setMaKM(maKM);
                return true;
            }
            return false;
        });
    }

    public HoaDon moBanVaTaoHoaDon(String maBan, String maNV, String maKH, LocalDateTime thoiGianDen, String ghiChu) {
        return executeTransaction(em -> {
            Ban ban = em.find(Ban.class, maBan);
            if (ban == null) throw new IllegalArgumentException("Không tìm thấy bàn: " + maBan);

            DonDatMon donDangMo = em.createQuery(
                            "SELECT d FROM DonDatMon d WHERE d.maBan = :maBan AND d.trangThai = 'Chưa thanh toán'", DonDatMon.class)
                    .setParameter("maBan", maBan)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (donDangMo == null) {
                DonDatMonDTO dto = DonDatMonDTO.builder()
                        .maDon(new DonDatMon(true).getMaDon())
                        .ngayKhoiTao(LocalDateTime.now())
                        .maNV(maNV)
                        .maKH(maKH)
                        .thoiGianDen(thoiGianDen != null ? thoiGianDen : LocalDateTime.now())
                        .trangThai("Chưa thanh toán")
                        .maBan(maBan)
                        .ghiChu(ghiChu)
                        .build();

                donDangMo = dto.toEntity();
                donDangMo.setBan(ban);
                em.persist(donDangMo);
            }

            HoaDon hd = em.createQuery(
                            "SELECT h FROM HoaDon h WHERE h.donDatMon.maDon = :maDon AND h.trangThai = 'Chưa thanh toán'", HoaDon.class)
                    .setParameter("maDon", donDangMo.getMaDon())
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (hd == null) {
                hd = new HoaDon();
                hd.setMaHD("HD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                hd.setNgayLap(LocalDateTime.now());
                hd.setTrangThai("Chưa thanh toán");
                hd.setHinhThucThanhToan("Tiền mặt");
                hd.setMaDon(donDangMo.getMaDon());
                hd.setMaNV(maNV);
                hd.setMaKH(maKH);
                hd.setTongTien(0f);
                hd.setGiamGia(0f);
                hd.setTongThanhToan(0f);
                hd.setDonDatMon(donDangMo);
                em.persist(hd);
            }

            ban.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
            ban.setGioMoBan(LocalDateTime.now());
            em.merge(ban);

            return hd;
        });
    }
}
