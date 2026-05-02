package iuh.fit.core.service;

import iuh.fit.core.entity.HoaDon;
import iuh.fit.core.repository.HoaDonRepository;

import java.util.List;

public class HoaDonService {

    private final HoaDonRepository hoaDonRepository = new HoaDonRepository();

    public void save(HoaDon hoaDon) {
        hoaDonRepository.save(hoaDon);
    }

    public HoaDon findById(String id) {
        return hoaDonRepository.findById(id);
    }

    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll();
    }

    public void update(HoaDon hoaDon) {
        hoaDonRepository.update(hoaDon);
    }

    public void delete(String id) {
        hoaDonRepository.delete(id);
    }

    public long count() {
        return hoaDonRepository.count();
    }
}
