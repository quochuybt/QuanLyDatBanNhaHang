package iuh.fit.core.repository;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.entity.*;
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
            String jpqlDon = """
            SELECT d
            FROM DonDatMon d
            LEFT JOIN FETCH d.ban b
            WHERE b.maBan = :maBan
              AND d.trangThai = 'Chưa thanh toán'
            ORDER BY d.ngayKhoiTao DESC
            """;

            List<DonDatMon> listDon = em.createQuery(jpqlDon, DonDatMon.class)
                    .setParameter("maBan", maBan)
                    .setMaxResults(1)
                    .getResultList();

            if (listDon.isEmpty()) {
                return null;
            }

            DonDatMon don = listDon.get(0);
            String maBanDeTimHoaDon = maBan;

            String ghiChu = don.getGhiChu();

            if (ghiChu != null && ghiChu.contains("LINKED:")) {
                int index = ghiChu.indexOf("LINKED:");
                String linkedPart = ghiChu.substring(index + "LINKED:".length()).trim();

                if (!linkedPart.isEmpty()) {
                    maBanDeTimHoaDon = linkedPart.split("\\s+")[0].trim();
                }
            }

            String jpqlHD = """
            SELECT hd
            FROM HoaDon hd
            JOIN FETCH hd.donDatMon ddm
            LEFT JOIN FETCH hd.khachHang kh
            LEFT JOIN FETCH hd.nhanVien nv
            LEFT JOIN FETCH hd.khuyenMai km
            WHERE ddm.ban.maBan = :maBanThucTe
              AND hd.trangThai = 'Chưa thanh toán'
            """;

            return em.createQuery(jpqlHD, HoaDon.class)
                    .setParameter("maBanThucTe", maBanDeTimHoaDon)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        });
    }

    public boolean thanhToanHoaDon(HoaDonDTO dto) {
        return executeTransaction(em -> {
            HoaDon hd = em.find(HoaDon.class, dto.getMaHD());

            if (hd == null) {
                return false;
            }

            // Cập nhật thông tin hóa đơn
            hd.setTrangThai("Đã thanh toán");
            hd.setNgayLap(LocalDateTime.now());
            hd.setTongThanhToan(dto.getTongThanhToan());
            hd.setTienKhachDua(dto.getTienKhachDua());
            hd.setHinhThucThanhToan(dto.getHinhThucThanhToan());
            hd.setGiamGia(dto.getGiamGia());

            if (dto.getTongTien() > 0) {
                hd.setTongTien(dto.getTongTien());
            }

            if (dto.getTenBan() != null && !dto.getTenBan().trim().isEmpty()) {
                hd.setTenBan(dto.getTenBan());
            }

            if (dto.getMaKH() != null && !dto.getMaKH().trim().isEmpty()) {
                KhachHang kh = em.find(KhachHang.class, dto.getMaKH());
                if (kh != null) {
                    hd.setKhachHang(kh);
                    if (hd.getDonDatMon() != null) {
                        hd.getDonDatMon().setKhachHang(kh);
                    }
                }
            }

            if (dto.getMaKM() != null && !dto.getMaKM().trim().isEmpty()) {
                KhuyenMai km = em.find(KhuyenMai.class, dto.getMaKM());
                if (km != null) {
                    hd.setKhuyenMai(km);
                }
            } else {
                hd.setKhuyenMai(null);
            }

            DonDatMon don = hd.getDonDatMon();

            if (don != null) {
                // 1. Thanh toán và dọn Bàn Chính
                don.setTrangThai("Đã thanh toán");
                Ban ban = don.getBan();

                if (ban != null) {
                    ban.setTrangThai(TrangThaiBan.TRONG);
                    ban.setGioMoBan(null);

                    // --- ĐOẠN CODE MỚI THÊM VÀO ĐỂ DỌN BÀN GHÉP ---
                    // 2. Tìm tất cả các Đơn Ảo đang liên kết với Bàn Chính này
                    String linkedTag = "%LINKED:" + ban.getMaBan() + "%";
                    List<DonDatMon> cacDonLienKet = em.createQuery(
                                    "SELECT d FROM DonDatMon d WHERE d.ghiChu LIKE :tag AND d.trangThai = 'Chưa thanh toán'",
                                    DonDatMon.class)
                            .setParameter("tag", linkedTag)
                            .getResultList();

                    // 3. Dọn dẹp từng Bàn Phụ
                    for (DonDatMon donPhu : cacDonLienKet) {
                        donPhu.setTrangThai("Đã thanh toán"); // Đóng luôn đơn ảo
                        donPhu.setGhiChu("PAID_WITH:" + hd.getMaHD());

                        Ban banPhu = donPhu.getBan();
                        if (banPhu != null) {
                            banPhu.setTrangThai(TrangThaiBan.TRONG); // Trả bàn về trạng thái Trống
                            banPhu.setGioMoBan(null);
                            em.merge(banPhu);
                        }
                        em.merge(donPhu);
                    }
                    // ----------------------------------------------
                }
            }

            return true;
        });
    }

    /**
     * Lọc danh sách hóa đơn có phân trang
     */
    public List<HoaDon> getHoaDonByPage(int page, int itemsPerPage, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return doInSession(em -> {
            StringBuilder jpql = new StringBuilder(
                    "SELECT hd FROM HoaDon hd " +
                            "LEFT JOIN FETCH hd.donDatMon ddm " +
                            "LEFT JOIN FETCH hd.nhanVien nv " +
                            "LEFT JOIN FETCH hd.khuyenMai km " +
                            "LEFT JOIN FETCH hd.khachHang kh " +
                            "WHERE 1=1"
            );
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
//    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
//        return doInSession(em -> {
//            String jpql = "SELECT CAST(hd.ngayLap AS date), SUM(hd.tongTien) " +
//                    "FROM HoaDon hd " +
//                    "WHERE hd.trangThai = 'Đã thanh toán' " +
//                    "AND hd.ngayLap BETWEEN :start AND :end " +
//                    "GROUP BY CAST(hd.ngayLap AS date) ORDER BY CAST(hd.ngayLap AS date)";
//
//            List<Object[]> results = em.createQuery(jpql)
//                    .setParameter("start", startDate.atStartOfDay())
//                    .setParameter("end", endDate.plusDays(1).atStartOfDay())
//                    .getResultList();
//
//            Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();
//            for (Object[] result : results) {
//                revenueMap.put((LocalDate) result[0], (Double) result[1]);
//            }
//            return revenueMap;
//        });
//    }

    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return doInSession(em -> {
            String jpql = """
                SELECT CAST(hd.ngayLap AS date), SUM(hd.tongTien)
                FROM HoaDon hd
                WHERE hd.trangThai = 'Đã thanh toán'
                  AND hd.ngayLap BETWEEN :start AND :end
                GROUP BY CAST(hd.ngayLap AS date)
                ORDER BY CAST(hd.ngayLap AS date)
                """;

            List<Object[]> results = em.createQuery(jpql, Object[].class)
                    .setParameter("start", startDate.atStartOfDay())
                    .setParameter("end", endDate.plusDays(1).atStartOfDay())
                    .getResultList();

            Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();

            for (Object[] result : results) {
                LocalDate ngay = convertToLocalDate(result[0]);

                Number doanhThuNumber = (Number) result[1];
                double doanhThu = doanhThuNumber != null ? doanhThuNumber.doubleValue() : 0.0;

                revenueMap.put(ngay, doanhThu);
            }

            return revenueMap;
        });
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }

        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }

        return LocalDate.parse(value.toString());
    }

    /**
     * Lấy doanh thu theo hình thức thanh toán của một nhân viên trong ca
     */
    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        return doInSession(em -> {
            String jpql = "SELECT COALESCE(SUM(hd.tongTien - COALESCE(hd.giamGia, 0)), 0.0) " +
                    "FROM HoaDon hd " +
                    "WHERE hd.nhanVien.manv = :maNV " +
                    "AND hd.ngayLap >= :startTime " +
                    "AND hd.trangThai = 'Đã thanh toán' " +
                    "AND hd.hinhThucThanhToan = :hinhThuc";

            Number result = em.createQuery(jpql, Number.class)
                    .setParameter("maNV", maNV)
                    .setParameter("startTime", thoiGianBatDauCa)
                    .setParameter("hinhThuc", hinhThuc)
                    .getSingleResult();

            return result != null ? result.doubleValue() : 0.0;
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
                if (maKM == null || maKM.trim().isEmpty()) {
                    hd.setKhuyenMai(null);
                } else {
                    KhuyenMai km = em.find(KhuyenMai.class, maKM);
                    if (km != null) {
                        hd.setKhuyenMai(km);
                    } else {
                        return false;
                    }
                }

                return true;
            }
            return false;
        });
    }
    public boolean capNhatMaKH(String maHD, String maKH) {
        return executeTransaction(em -> {
            HoaDon hd = em.find(HoaDon.class, maHD);

            if (hd == null) {
                return false;
            }

            if (maKH == null || maKH.trim().isEmpty()) {
                hd.setKhachHang(null);

                if (hd.getDonDatMon() != null) {
                    hd.getDonDatMon().setKhachHang(null);
                }

                return true;
            }

            KhachHang kh = em.find(KhachHang.class, maKH);

            if (kh == null) {
                return false;
            }

            hd.setKhachHang(kh);

            if (hd.getDonDatMon() != null) {
                hd.getDonDatMon().setKhachHang(kh);
            }

            return true;
        });
    }
    public HoaDon moBanVaTaoHoaDon(
            String maBan,
            String maNV,
            String maKH,
            LocalDateTime thoiGianDen,
            String ghiChu
    ) {
        return executeTransaction(em -> {
            // 1. Tìm bàn
            Ban ban = em.find(Ban.class, maBan);
            if (ban == null) {
                throw new IllegalArgumentException("Không tìm thấy bàn: " + maBan);
            }

            // 2. Tìm đơn đang mở của bàn nếu đã có
            DonDatMon donDangMo = em.createQuery(
                            "SELECT d FROM DonDatMon d " +
                                    "WHERE d.ban.maBan = :maBan " +
                                    "AND d.trangThai = 'Chưa thanh toán'",
                            DonDatMon.class
                    )
                    .setParameter("maBan", maBan)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            // 3. Chuẩn bị nhân viên và khách hàng
            NhanVien nv = null;
            if (maNV != null && !maNV.trim().isEmpty()) {
                nv = em.find(NhanVien.class, maNV);
                if (nv == null) {
                    throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNV);
                }
            }

            KhachHang kh = null;
            if (maKH != null && !maKH.trim().isEmpty()) {
                kh = em.find(KhachHang.class, maKH);
                if (kh == null) {
                    throw new IllegalArgumentException("Không tìm thấy khách hàng: " + maKH);
                }
            }

            // 4. Nếu chưa có đơn thì tạo đơn đặt món mới
            if (donDangMo == null) {
                donDangMo = new DonDatMon();

                donDangMo.setMaDon(new DonDatMon(true).getMaDon());
                donDangMo.setNgayKhoiTao(LocalDateTime.now());
                donDangMo.setThoiGianDen(thoiGianDen != null ? thoiGianDen : LocalDateTime.now());
                donDangMo.setTrangThai("Chưa thanh toán");
                donDangMo.setGhiChu(ghiChu);
                donDangMo.setBan(ban);
                donDangMo.setNhanVien(nv);
                donDangMo.setKhachHang(kh);

                em.persist(donDangMo);
            } else {
                // Nếu đơn đã có sẵn nhưng truyền khách hàng mới thì cập nhật lại
                if (nv != null) {
                    donDangMo.setNhanVien(nv);
                }

                donDangMo.setKhachHang(kh);

                if (thoiGianDen != null) {
                    donDangMo.setThoiGianDen(thoiGianDen);
                }

                if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                    donDangMo.setGhiChu(ghiChu);
                }
            }

            // 5. Tìm hóa đơn chưa thanh toán gắn với đơn này
            HoaDon hd = em.createQuery(
                            "SELECT h FROM HoaDon h " +
                                    "WHERE h.donDatMon.maDon = :maDon " +
                                    "AND h.trangThai = 'Chưa thanh toán'",
                            HoaDon.class
                    )
                    .setParameter("maDon", donDangMo.getMaDon())
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            // 6. Nếu chưa có hóa đơn thì tạo mới
            if (hd == null) {
                hd = new HoaDon();

                hd.setMaHD(hd.phatSinhMaHD());
                hd.setNgayLap(LocalDateTime.now());
                hd.setTrangThai("Chưa thanh toán");
                hd.setHinhThucThanhToan("Tiền mặt");

                hd.setTongTien(0f);
                hd.setGiamGia(0f);
                hd.setTongThanhToan(0f);
                hd.setTienKhachDua(0f);

                hd.setTenBan(ban.getTenBan());

                hd.setDonDatMon(donDangMo);
                hd.setNhanVien(nv);
                hd.setKhachHang(kh);

                em.persist(hd);
            } else {
                // Nếu hóa đơn đã tồn tại thì đồng bộ lại thông tin mới
                if (nv != null) {
                    hd.setNhanVien(nv);
                }

                hd.setKhachHang(kh);

                if (hd.getTongTien() == null) {
                    hd.setTongTien(0f);
                }

                if (hd.getGiamGia() == null) {
                    hd.setGiamGia(0f);
                }

                if (hd.getTongThanhToan() == null) {
                    hd.setTongThanhToan(Math.max(0f, hd.getTongTien() - hd.getGiamGia()));
                }

                if (hd.getTienKhachDua() == null) {
                    hd.setTienKhachDua(0f);
                }

                if (hd.getTenBan() == null || hd.getTenBan().trim().isEmpty()) {
                    hd.setTenBan(ban.getTenBan());
                }
            }

            // 7. Cập nhật trạng thái bàn
            ban.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
            ban.setGioMoBan(LocalDateTime.now());

            // 8. Flush để đảm bảo DB nhận thay đổi trước khi trả về
            em.flush();

            return hd;
        });
    }

    private HoaDon fetchHoaDonDayDu(jakarta.persistence.EntityManager em, String maHD) {
        try {
            String jpql = """
            SELECT hd
            FROM HoaDon hd
            JOIN FETCH hd.donDatMon ddm
            LEFT JOIN FETCH ddm.ban b
            LEFT JOIN FETCH hd.khachHang kh
            LEFT JOIN FETCH hd.nhanVien nv
            LEFT JOIN FETCH hd.khuyenMai km
            WHERE hd.maHD = :maHD
            """;

            return em.createQuery(jpql, HoaDon.class)
                    .setParameter("maHD", maHD)
                    .getSingleResult();

        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    public HoaDon tinhLaiGiamGiaVaTongTien(String maHD) {
        return executeTransaction(em -> {
            HoaDon hd = em.find(HoaDon.class, maHD);

            if (hd != null && hd.getDonDatMon() != null) {
                Double tongTienOpt = em.createQuery(
                                "SELECT SUM(c.thanhtien) " +
                                        "FROM ChiTietHoaDon c " +
                                        "WHERE c.donDatMon.maDon = :maDon",
                                Double.class
                        )
                        .setParameter("maDon", hd.getDonDatMon().getMaDon())
                        .getSingleResult();

                float tongTien = tongTienOpt != null ? tongTienOpt.floatValue() : 0f;
                float giamGia = hd.getGiamGia() != null ? hd.getGiamGia() : 0f;

                hd.setTongTien(tongTien);
                hd.setGiamGia(giamGia);
                hd.setTongThanhToan(Math.max(0f, tongTien - giamGia));

                em.merge(hd);
                em.flush();
            }

            return fetchHoaDonDayDu(em, maHD);
        });
    }
    public boolean capNhatTongTien(String maHD, float tongTien, float giamGia, float tongThanhToan) {
        return executeTransaction(em -> {
            int updated = em.createQuery(
                            "UPDATE HoaDon h " +
                                    "SET h.tongTien = :tongTien, h.giamGia = :giamGia, h.tongThanhToan = :tongThanhToan " +
                                    "WHERE h.maHD = :maHD")
                    .setParameter("tongTien", tongTien)
                    .setParameter("giamGia", giamGia)
                    .setParameter("tongThanhToan", tongThanhToan)
                    .setParameter("maHD", maHD)
                    .executeUpdate();
            return updated > 0;
        });
    }

}
