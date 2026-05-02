package iuh.fit.core.service;

import iuh.fit.core.entity.KhuyenMai;
import iuh.fit.core.repository.KhuyenMaiRepository;

import java.time.LocalDate;
import java.util.List;

public class KhuyenMaiService {

    private final KhuyenMaiRepository repo = new KhuyenMaiRepository();

    public void add(KhuyenMai km) {
        if (repo.findById(km.getMaKM()) != null)
            throw new IllegalArgumentException("Khuyến mãi đã tồn tại");
        repo.save(km);
    }

    public KhuyenMai findById(String maKM) {
        return repo.findById(maKM);
    }

    public KhuyenMai findActiveById(String maKM) {
        return repo.findActiveById(maKM);
    }

    public List<KhuyenMai> findAll() {
        return repo.findAll();
    }

    public List<KhuyenMai> findAllActive() {
        return repo.findAllActive();
    }

    public List<KhuyenMai> search(String keyword) {
        return repo.findByKeyword(keyword);
    }

    public void update(KhuyenMai km) {
        if (repo.findById(km.getMaKM()) == null)
            throw new IllegalArgumentException("Không tồn tại khuyến mãi");
        repo.update(km);
    }

    public void delete(String maKM) {
        if (repo.findById(maKM) == null)
            throw new IllegalArgumentException("Không tồn tại khuyến mãi");
        repo.delete(maKM);
    }

    public void autoExpire() {
        List<KhuyenMai> list = repo.findAll();
        for (KhuyenMai km : list) {
            if (km.getNgayKetThuc() != null &&
                    km.getNgayKetThuc().isBefore(LocalDate.now())) {
                km.setTrangThai("Ngưng áp dụng");
                repo.update(km);
            }
        }
    }
}