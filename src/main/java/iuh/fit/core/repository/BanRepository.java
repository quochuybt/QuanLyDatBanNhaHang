package iuh.fit.core.repository;

import iuh.fit.core.db.JPAUtil;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.ChiTietHoaDon;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.entity.MonAn;
import iuh.fit.core.entity.TrangThaiBan;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.*;

public class BanRepository extends GenericRepository<Ban, String> {

    private static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    private static final String DA_THANH_TOAN = "Đã thanh toán";
    private static final String DA_HUY = "Đã hủy";

    public BanRepository() {
        super(Ban.class);
    }

    public boolean updateBan(Ban ban) {
        return executeTransaction(em -> {
            int updated = em.createQuery("""
                    UPDATE Ban b
                    SET b.tenBan = :tenBan,
                        b.soGhe = :soGhe,
                        b.trangThai = :trangThai,
                        b.gioMoBan = :gioMoBan,
                        b.khuVuc = :khuVuc
                    WHERE b.maBan = :maBan
                    """)
                    .setParameter("tenBan", ban.getTenBan())
                    .setParameter("soGhe", ban.getSoGhe())
                    .setParameter("trangThai", ban.getTrangThai())
                    .setParameter("gioMoBan", ban.getGioMoBan())
                    .setParameter("khuVuc", ban.getKhuVuc())
                    .setParameter("maBan", ban.getMaBan())
                    .executeUpdate();

            return updated > 0;
        });
    }

    public boolean chuyenBan(Ban banCu, Ban banMoi) {
        return executeTransaction(em -> {
            String oldTag = "LINKED:" + banCu.getMaBan();
            String newTag = "LINKED:" + banMoi.getMaBan();

            List<DonDatMon> donLienKet = em.createQuery("""
                    SELECT d
                    FROM DonDatMon d
                    WHERE d.maBan <> :maBanCu
                      AND d.trangThai = :chuaThanhToan
                      AND d.ghiChu LIKE :likeOldTag
                    """, DonDatMon.class)
                    .setParameter("maBanCu", banCu.getMaBan())
                    .setParameter("chuaThanhToan", CHUA_THANH_TOAN)
                    .setParameter("likeOldTag", "%" + oldTag + "%")
                    .getResultList();

            for (DonDatMon don : donLienKet) {
                if (don.getGhiChu() != null) {
                    don.setGhiChu(don.getGhiChu().replace(oldTag, newTag));
                }
            }

            em.createQuery("""
                    UPDATE DonDatMon d
                    SET d.maBan = :maBanMoi
                    WHERE d.maBan = :maBanCu
                      AND d.trangThai <> :daThanhToan
                      AND d.trangThai <> :daHuy
                    """)
                    .setParameter("maBanMoi", banMoi.getMaBan())
                    .setParameter("maBanCu", banCu.getMaBan())
                    .setParameter("daThanhToan", DA_THANH_TOAN)
                    .setParameter("daHuy", DA_HUY)
                    .executeUpdate();

            TrangThaiBan trangThaiMoi = banCu.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC
                    ? TrangThaiBan.DA_DAT_TRUOC
                    : TrangThaiBan.DANG_PHUC_VU;

            updateTrangThaiBan(em, banMoi.getMaBan(), trangThaiMoi, banCu.getGioMoBan());
            updateTrangThaiBan(em, banCu.getMaBan(), TrangThaiBan.TRONG, null);

            return true;
        });
    }

    public String getTenHienThiGhep(String maBanCheck) {
        return doInSession(em -> {
            String maBanMaster = maBanCheck;
            List<String> tenBanPhu = new ArrayList<>();

            List<String> ghiChuRows = em.createQuery("""
                    SELECT d.ghiChu
                    FROM DonDatMon d
                    WHERE d.maBan = :maBan
                      AND d.trangThai = :chuaThanhToan
                      AND d.ghiChu LIKE :linkedPattern
                    """, String.class)
                    .setParameter("maBan", maBanCheck)
                    .setParameter("chuaThanhToan", CHUA_THANH_TOAN)
                    .setParameter("linkedPattern", "%LINKED:%")
                    .setMaxResults(1)
                    .getResultList();

            if (!ghiChuRows.isEmpty() && ghiChuRows.get(0) != null) {
                String ghiChu = ghiChuRows.get(0);
                int index = ghiChu.indexOf("LINKED:");

                if (index != -1) {
                    maBanMaster = ghiChu.substring(index + 7).trim().split(" ")[0];
                }
            }

            String tenGoc = getTenBanByMaInternal(em, maBanMaster);

            List<String> slaveRows = em.createQuery("""
                    SELECT b.tenBan
                    FROM DonDatMon d, Ban b
                    WHERE d.maBan = b.maBan
                      AND d.ghiChu LIKE :ghiChu
                      AND d.trangThai = :chuaThanhToan
                    """, String.class)
                    .setParameter("ghiChu", "%LINKED:" + maBanMaster + "%")
                    .setParameter("chuaThanhToan", CHUA_THANH_TOAN)
                    .getResultList();

            for (String tenBan : slaveRows) {
                if (tenBan == null) {
                    continue;
                }

                String tenRutGon = tenBan.replace("Bàn ", "");

                if (!tenGoc.contains(tenRutGon)) {
                    tenBanPhu.add(tenRutGon);
                }
            }

            StringBuilder sb = new StringBuilder(tenGoc);

            for (String ten : tenBanPhu) {
                sb.append(" + ").append(ten);
            }

            return sb.toString();
        });
    }

    public String getMaBanChinh(String maBanCheck) {
        return doInSession(em -> {
            List<String> rows = em.createQuery("""
                    SELECT d.ghiChu
                    FROM DonDatMon d
                    WHERE d.maBan = :maBan
                      AND d.trangThai = :chuaThanhToan
                      AND d.ghiChu LIKE :linkedPattern
                    """, String.class)
                    .setParameter("maBan", maBanCheck)
                    .setParameter("chuaThanhToan", CHUA_THANH_TOAN)
                    .setParameter("linkedPattern", "LINKED:%")
                    .setMaxResults(1)
                    .getResultList();

            if (!rows.isEmpty() && rows.get(0) != null) {
                return rows.get(0)
                        .replace("LINKED:", "")
                        .trim();
            }

            return maBanCheck;
        });
    }

    public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich) {
        return executeTransaction(em -> {
            boolean coKhachDangAn = banDich.getTrangThai() == TrangThaiBan.DANG_PHUC_VU;

            for (Ban b : listBanNguon) {
                if (b.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                    coKhachDangAn = true;
                    break;
                }
            }

            TrangThaiBan trangThaiSauGop = coKhachDangAn
                    ? TrangThaiBan.DANG_PHUC_VU
                    : TrangThaiBan.DA_DAT_TRUOC;

            String maDonDich = findDonChuaThanhToanByMaBan(em, banDich.getMaBan());

            if (maDonDich == null) {
                maDonDich = "DON" + System.currentTimeMillis();

                DonDatMon donDich = new DonDatMon();
                donDich.setMaDon(maDonDich);
                donDich.setNgayKhoiTao(LocalDateTime.now());
                donDich.setThoiGianDen(LocalDateTime.now());
                donDich.setMaNV("NV01102");
                donDich.setMaBan(banDich.getMaBan());
                donDich.setTrangThai(CHUA_THANH_TOAN);
                donDich.setGhiChu("");

                em.persist(donDich);
            }

            for (Ban banNguon : listBanNguon) {
                if (banNguon.getMaBan().equals(banDich.getMaBan())) {
                    continue;
                }

                String maDonNguon = findDonChuaThanhToanByMaBan(em, banNguon.getMaBan());

                if (maDonNguon != null && !maDonNguon.equals(maDonDich)) {
                    List<MonAnTam> listItems = getChiTietMonAnTam(em, maDonNguon);

                    em.createQuery("""
                            DELETE FROM ChiTietHoaDon c
                            WHERE c.donDatMon.maDon = :maDon
                            """)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();

                    for (MonAnTam item : listItems) {
                        ChiTietHoaDon chiTietDich = findChiTietHoaDon(
                                em,
                                maDonDich,
                                item.monAn
                        );

                        if (chiTietDich != null) {
                            chiTietDich.setSoluong(chiTietDich.getSoluong() + item.soLuong);
                            chiTietDich.setThanhtien(chiTietDich.getSoluong() * chiTietDich.getDongia());
                            em.merge(chiTietDich);
                        } else {
                            ChiTietHoaDon chiTietMoi = new ChiTietHoaDon();
                            chiTietMoi.setDonDatMon(em.getReference(DonDatMon.class, maDonDich));
                            chiTietMoi.setMonAn(item.monAn);
                            chiTietMoi.setSoluong(item.soLuong);
                            chiTietMoi.setDongia(item.donGia);
                            chiTietMoi.setThanhtien(item.soLuong * item.donGia);
                            chiTietMoi.setTenMon(item.tenMon);

                            em.persist(chiTietMoi);
                        }
                    }

                    em.createQuery("""
                            UPDATE DonDatMon d
                            SET d.trangThai = :daHuy
                            WHERE d.maDon = :maDon
                            """)
                            .setParameter("daHuy", DA_HUY)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();

                    em.createQuery("""
                            UPDATE HoaDon h
                            SET h.trangThai = :daHuy
                            WHERE h.donDatMon.maDon = :maDon
                            """)
                            .setParameter("daHuy", DA_HUY)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();
                }

                String dummyID = "L" + (System.currentTimeMillis() % 100000000) + banNguon.getMaBan();

                DonDatMon donLienKet = new DonDatMon();
                donLienKet.setMaDon(dummyID);
                donLienKet.setNgayKhoiTao(LocalDateTime.now());
                donLienKet.setThoiGianDen(LocalDateTime.now());
                donLienKet.setMaNV("NV01102");
                donLienKet.setMaBan(banNguon.getMaBan());
                donLienKet.setTrangThai(CHUA_THANH_TOAN);
                donLienKet.setGhiChu("LINKED:" + banDich.getMaBan());

                em.persist(donLienKet);

                updateTrangThaiBan(em, banNguon.getMaBan(), trangThaiSauGop, banNguon.getGioMoBan());
            }

            updateTrangThaiBan(em, banDich.getMaBan(), trangThaiSauGop, banDich.getGioMoBan());

            return true;
        });
    }

    public String getTenBanByMa(String maBan) {
        return doInSession(em -> getTenBanByMaInternal(em, maBan));
    }

    public Ban getBanByMa(String maBan) {
        return findById(maBan);
    }

    public List<Ban> getAllBan() {
        return doInSession(em ->
                em.createQuery("""
                        SELECT b
                        FROM Ban b
                        ORDER BY b.maBan
                        """, Ban.class)
                        .getResultList()
        );
    }

    public int getSoThuTuBanLonNhat() {
        return doInSession(em -> {
            List<String> maBanList = em.createQuery("""
                    SELECT b.maBan
                    FROM Ban b
                    WHERE b.maBan LIKE :prefix
                    """, String.class)
                    .setParameter("prefix", "BAN%")
                    .getResultList();

            int max = 0;

            for (String maBan : maBanList) {
                if (maBan == null || !maBan.startsWith("BAN")) {
                    continue;
                }

                try {
                    int so = Integer.parseInt(maBan.substring(3));

                    if (so > max) {
                        max = so;
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            return max;
        });
    }

    public Map<String, Integer> getTableStatusCounts() {
        return doInSession(em -> {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("Trống", 0);
            counts.put("Đang có khách", 0);
            counts.put("Đã đặt trước", 0);

            List<Object[]> rows = em.createQuery("""
                    SELECT b.trangThai, COUNT(b)
                    FROM Ban b
                    GROUP BY b.trangThai
                    """, Object[].class)
                    .getResultList();

            for (Object[] row : rows) {
                TrangThaiBan trangThai = (TrangThaiBan) row[0];
                int soLuong = ((Long) row[1]).intValue();

                counts.put(convertTrangThaiToString(trangThai), soLuong);
            }

            return counts;
        });
    }

    private void updateTrangThaiBan(
            EntityManager em,
            String maBan,
            TrangThaiBan trangThai,
            LocalDateTime gioMoBan
    ) {
        em.createQuery("""
                UPDATE Ban b
                SET b.trangThai = :trangThai,
                    b.gioMoBan = :gioMoBan
                WHERE b.maBan = :maBan
                """)
                .setParameter("trangThai", trangThai)
                .setParameter("gioMoBan", gioMoBan)
                .setParameter("maBan", maBan)
                .executeUpdate();
    }

    private String getTenBanByMaInternal(EntityManager em, String maBan) {
        Ban ban = em.find(Ban.class, maBan);
        return ban != null ? ban.getTenBan() : maBan;
    }

    private String findDonChuaThanhToanByMaBan(EntityManager em, String maBan) {
        List<String> rows = em.createQuery("""
                SELECT d.maDon
                FROM DonDatMon d
                WHERE d.maBan = :maBan
                  AND d.trangThai = :chuaThanhToan
                """, String.class)
                .setParameter("maBan", maBan)
                .setParameter("chuaThanhToan", CHUA_THANH_TOAN)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) {
            return null;
        }

        return rows.get(0);
    }

    private List<MonAnTam> getChiTietMonAnTam(EntityManager em, String maDon) {
        List<ChiTietHoaDon> rows = em.createQuery("""
                SELECT c
                FROM ChiTietHoaDon c
                JOIN FETCH c.monAn
                WHERE c.donDatMon.maDon = :maDon
                """, ChiTietHoaDon.class)
                .setParameter("maDon", maDon)
                .getResultList();

        List<MonAnTam> result = new ArrayList<>();

        for (ChiTietHoaDon c : rows) {
            result.add(new MonAnTam(
                    c.getMonAn(),
                    c.getSoluong(),
                    c.getDongia(),
                    c.getTenMon()
            ));
        }

        return result;
    }

    private ChiTietHoaDon findChiTietHoaDon(EntityManager em, String maDon, MonAn monAn) {
        List<ChiTietHoaDon> rows = em.createQuery("""
                SELECT c
                FROM ChiTietHoaDon c
                WHERE c.donDatMon.maDon = :maDon
                  AND c.monAn = :monAn
                """, ChiTietHoaDon.class)
                .setParameter("maDon", maDon)
                .setParameter("monAn", monAn)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty()) {
            return null;
        }

        return rows.get(0);
    }

    private String convertTrangThaiToString(TrangThaiBan trangThai) {
        if (trangThai == null) {
            return "Trống";
        }

        return switch (trangThai) {
            case TRONG -> "Trống";
            case DANG_PHUC_VU -> "Đang có khách";
            case DA_DAT_TRUOC -> "Đã đặt trước";
        };
    }

    private static class MonAnTam {
        private final MonAn monAn;
        private final int soLuong;
        private final float donGia;
        private final String tenMon;

        public MonAnTam(MonAn monAn, int soLuong, float donGia, String tenMon) {
            this.monAn = monAn;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.tenMon = tenMon;
        }
    }
}
