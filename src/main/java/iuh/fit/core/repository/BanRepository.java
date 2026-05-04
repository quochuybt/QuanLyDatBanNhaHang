package iuh.fit.core.repository;

import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import jakarta.persistence.*;

import java.util.*;
import java.util.function.Function;

public class BanRepository {

    private final EntityManagerFactory emf;

    public BanRepository() {
        this.emf = Persistence.createEntityManagerFactory("QuanLyNhaHang");
    }

    private <T> T doInTransaction(Function<EntityManager, T> action) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            T result = action.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean updateBan(Ban ban) {
        return doInTransaction(em -> {
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
        return doInTransaction(em -> {
            String oldTag = "LINKED:" + banCu.getMaBan();
            String newTag = "LINKED:" + banMoi.getMaBan();

            em.createNativeQuery("""
                    UPDATE DonDatMon
                    SET ghiChu = REPLACE(ghiChu, :oldTag, :newTag)
                    WHERE maBan <> :maBanCu
                      AND trangThai = 'Chưa thanh toán'
                      AND ghiChu LIKE :likeOldTag
                    """)
                    .setParameter("oldTag", oldTag)
                    .setParameter("newTag", newTag)
                    .setParameter("maBanCu", banCu.getMaBan())
                    .setParameter("likeOldTag", "%" + oldTag + "%")
                    .executeUpdate();

            em.createNativeQuery("""
                    UPDATE DonDatMon
                    SET maBan = :maBanMoi
                    WHERE maBan = :maBanCu
                      AND trangThai <> 'Đã thanh toán'
                      AND trangThai <> 'Đã hủy'
                    """)
                    .setParameter("maBanMoi", banMoi.getMaBan())
                    .setParameter("maBanCu", banCu.getMaBan())
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
        return doInTransaction(em -> {
            String maBanMaster = maBanCheck;
            List<String> tenBanPhu = new ArrayList<>();

            List<?> ghiChuRows = em.createNativeQuery("""
                SELECT ghiChu
                FROM DonDatMon
                WHERE maBan = :maBan
                  AND trangThai = 'Chưa thanh toán'
                  AND ghiChu LIKE '%LINKED:%'
                """)
                    .setParameter("maBan", maBanCheck)
                    .setMaxResults(1)
                    .getResultList();

            if (!ghiChuRows.isEmpty() && ghiChuRows.get(0) != null) {
                String ghiChu = ghiChuRows.get(0).toString();
                int index = ghiChu.indexOf("LINKED:");
                if (index != -1) {
                    maBanMaster = ghiChu.substring(index + 7).trim().split(" ")[0];
                }
            }

            String tenGoc = getTenBanByMaInternal(em, maBanMaster);

            List<?> slaveRows = em.createNativeQuery("""
                    SELECT b.tenBan
                    FROM DonDatMon d
                    JOIN Ban b ON d.maBan = b.maBan
                    WHERE d.ghiChu LIKE :ghiChu
                      AND d.trangThai = 'Chưa thanh toán'
                    """)
                    .setParameter("ghiChu", "%LINKED:" + maBanMaster + "%")
                    .getResultList();

            for (Object row : slaveRows) {
                if (row != null) {
                    String tenBan = row.toString().replace("Bàn ", "");
                    if (!tenGoc.contains(tenBan)) {
                        tenBanPhu.add(tenBan);
                    }
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
        return doInTransaction(em -> {
            List<?> rows = em.createNativeQuery("""
                SELECT ghiChu
                FROM DonDatMon
                WHERE maBan = :maBan
                  AND trangThai = 'Chưa thanh toán'
                  AND ghiChu LIKE 'LINKED:%'
                """)
                    .setParameter("maBan", maBanCheck)
                    .setMaxResults(1)
                    .getResultList();

            if (!rows.isEmpty() && rows.get(0) != null) {
                return rows.get(0).toString()
                        .replace("LINKED:", "")
                        .trim();
            }

            return maBanCheck;
        });
    }

    public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich) {
        return doInTransaction(em -> {
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

                em.createNativeQuery("""
                        INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai)
                        VALUES(:maDon, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'NV01102', :maBan, 'Chưa thanh toán')
                        """)
                        .setParameter("maDon", maDonDich)
                        .setParameter("maBan", banDich.getMaBan())
                        .executeUpdate();
            }

            for (Ban banNguon : listBanNguon) {
                if (banNguon.getMaBan().equals(banDich.getMaBan())) {
                    continue;
                }

                String maDonNguon = findDonChuaThanhToanByMaBan(em, banNguon.getMaBan());

                if (maDonNguon != null && !maDonNguon.equals(maDonDich)) {
                    List<MonAnTam> listItems = getChiTietMonAnTam(em, maDonNguon);

                    em.createNativeQuery("""
                            DELETE FROM ChiTietHoaDon
                            WHERE maDon = :maDon
                            """)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();

                    for (MonAnTam item : listItems) {
                        int count = ((Number) em.createNativeQuery("""
                                SELECT COUNT(*)
                                FROM ChiTietHoaDon
                                WHERE maDon = :maDon
                                  AND maMonAn = :maMonAn
                                """)
                                .setParameter("maDon", maDonDich)
                                .setParameter("maMonAn", item.maMon)
                                .getSingleResult()).intValue();

                        if (count > 0) {
                            em.createNativeQuery("""
                                    UPDATE ChiTietHoaDon
                                    SET soLuong = soLuong + :soLuong
                                    WHERE maDon = :maDon
                                      AND maMonAn = :maMonAn
                                    """)
                                    .setParameter("soLuong", item.soLuong)
                                    .setParameter("maDon", maDonDich)
                                    .setParameter("maMonAn", item.maMon)
                                    .executeUpdate();
                        } else {
                            em.createNativeQuery("""
                                    INSERT INTO ChiTietHoaDon(maDon, maMonAn, soLuong, donGia)
                                    VALUES(:maDon, :maMonAn, :soLuong, :donGia)
                                    """)
                                    .setParameter("maDon", maDonDich)
                                    .setParameter("maMonAn", item.maMon)
                                    .setParameter("soLuong", item.soLuong)
                                    .setParameter("donGia", item.donGia)
                                    .executeUpdate();
                        }
                    }

                    em.createNativeQuery("""
                            UPDATE DonDatMon
                            SET trangThai = 'Đã hủy'
                            WHERE maDon = :maDon
                            """)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();

                    em.createNativeQuery("""
                            UPDATE HoaDon
                            SET trangThai = 'Đã hủy'
                            WHERE maDon = :maDon
                            """)
                            .setParameter("maDon", maDonNguon)
                            .executeUpdate();
                }

                String dummyID = "L" + (System.currentTimeMillis() % 100000000) + banNguon.getMaBan();

                em.createNativeQuery("""
                        INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai, ghiChu)
                        VALUES(:maDon, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'NV01102', :maBan, 'Chưa thanh toán', :ghiChu)
                        """)
                        .setParameter("maDon", dummyID)
                        .setParameter("maBan", banNguon.getMaBan())
                        .setParameter("ghiChu", "LINKED:" + banDich.getMaBan())
                        .executeUpdate();

                updateTrangThaiBan(em, banNguon.getMaBan(), trangThaiSauGop, banNguon.getGioMoBan());
            }

            updateTrangThaiBan(em, banDich.getMaBan(), trangThaiSauGop, banDich.getGioMoBan());

            return true;
        });
    }

    public String getTenBanByMa(String maBan) {
        return doInTransaction(em -> getTenBanByMaInternal(em, maBan));
    }

    public Ban getBanByMa(String maBan) {
        return doInTransaction(em -> em.find(Ban.class, maBan));
    }

    public List<Ban> getAllBan() {
        return doInTransaction(em ->
                em.createQuery("""
                        SELECT b
                        FROM Ban b
                        ORDER BY b.maBan
                        """, Ban.class)
                        .getResultList()
        );
    }

    public int getSoThuTuBanLonNhat() {
        return doInTransaction(em -> {
            Object result = em.createNativeQuery("""
                SELECT COALESCE(MAX(CAST(SUBSTRING(maBan, 4) AS UNSIGNED)), 0)
                FROM Ban
                WHERE maBan LIKE 'BAN%'
                """)
                    .getSingleResult();

            return ((Number) result).intValue();
        });
    }

    public Map<String, Integer> getTableStatusCounts() {
        return doInTransaction(em -> {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("Trống", 0);
            counts.put("Đang có khách", 0);
            counts.put("Đã đặt trước", 0);

            List<Object[]> rows = em.createNativeQuery("""
                    SELECT trangThai, COUNT(maBan) AS SoLuong
                    FROM Ban
                    GROUP BY trangThai
                    """)
                    .getResultList();

            for (Object[] row : rows) {
                String trangThai = row[0] != null ? row[0].toString() : "Trống";
                int soLuong = ((Number) row[1]).intValue();
                counts.put(trangThai, soLuong);
            }

            return counts;
        });
    }

    private void updateTrangThaiBan(EntityManager em, String maBan, TrangThaiBan trangThai, Object gioMoBan) {
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
        List<?> rows = em.createNativeQuery("""
                SELECT maDon
                FROM DonDatMon
                WHERE maBan = :maBan
                  AND trangThai = 'Chưa thanh toán'
                  AND trangThai <> 'Đã hủy'
                """)
                .setParameter("maBan", maBan)
                .setMaxResults(1)
                .getResultList();

        if (rows.isEmpty() || rows.get(0) == null) {
            return null;
        }

        return rows.get(0).toString();
    }

    private List<MonAnTam> getChiTietMonAnTam(EntityManager em, String maDon) {
        List<Object[]> rows = em.createNativeQuery("""
                SELECT maMonAn, soLuong, donGia
                FROM ChiTietHoaDon
                WHERE maDon = :maDon
                """)
                .setParameter("maDon", maDon)
                .getResultList();

        List<MonAnTam> result = new ArrayList<>();

        for (Object[] row : rows) {
            String maMon = row[0].toString();
            int soLuong = ((Number) row[1]).intValue();
            double donGia = ((Number) row[2]).doubleValue();

            result.add(new MonAnTam(maMon, soLuong, donGia));
        }

        return result;
    }

    private static class MonAnTam {
        String maMon;
        int soLuong;
        double donGia;

        public MonAnTam(String maMon, int soLuong, double donGia) {
            this.maMon = maMon;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }
    }
}