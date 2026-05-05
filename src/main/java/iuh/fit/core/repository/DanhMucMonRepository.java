package iuh.fit.core.repository;

import iuh.fit.core.entity.DanhMucMon;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DanhMucMonRepository extends GenericRepository<DanhMucMon, String> {

    public DanhMucMonRepository() {
        super(DanhMucMon.class);
    }

    public List<DanhMucMon> findAllByName() {
        return doInSession(em -> em.createQuery(
                        "SELECT d FROM DanhMucMon d ORDER BY d.tendm", DanhMucMon.class)
                .getResultList());
    }

    public List<DanhMucMon> getAllDanhMuc() {
        return doInSession(em ->
                em.createQuery("""
                        SELECT dm
                        FROM DanhMucMonDTO dm
                        ORDER BY dm.madm
                        """, DanhMucMon.class)
                        .getResultList()
        );
    }

    public boolean themDanhMuc(DanhMucMon dm) {
        return executeTransaction(em -> {
            if (dm.getMadm() == null || dm.getMadm().trim().isEmpty()) {
                dm.setMadm(generateNewMaDM(em));
            }

            em.persist(dm);
            return true;
        });
    }

    public boolean capNhatDanhMuc(DanhMucMon dm) {
        return executeTransaction(em -> {
            int updated = em.createQuery("""
                    UPDATE DanhMucMonDTO dm
                    SET dm.tendm = :tenDM,
                        dm.mota = :moTa
                    WHERE dm.madm = :maDM
                    """)
                    .setParameter("tenDM", dm.getTendm())
                    .setParameter("moTa", dm.getMota())
                    .setParameter("maDM", dm.getMadm())
                    .executeUpdate();

            return updated > 0;
        });
    }

    public boolean xoaDanhMuc(String maDM) {
        return executeTransaction(em -> {
            DanhMucMon dm = em.find(DanhMucMon.class, maDM);

            if (dm == null) {
                return false;
            }

            em.remove(dm);
            return true;
        });
    }

    private String generateNewMaDM(EntityManager em) {
        List<String> ids = em.createQuery("""
                SELECT dm.madm
                FROM DanhMucMonDTO dm
                WHERE dm.madm LIKE :prefix
                ORDER BY dm.madm DESC
                """, String.class)
                .setParameter("prefix", "DM%")
                .setMaxResults(1)
                .getResultList();

        if (ids.isEmpty() || ids.get(0) == null) {
            return "DM0001";
        }

        String maxID = ids.get(0);

        try {
            int num = Integer.parseInt(maxID.replace("DM", ""));
            return String.format("DM%04d", num + 1);
        } catch (NumberFormatException e) {
            return "DM0001";
        }
    }
}