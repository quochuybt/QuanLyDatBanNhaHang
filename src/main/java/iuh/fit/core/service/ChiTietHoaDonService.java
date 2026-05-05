package iuh.fit.core.service;

import iuh.fit.core.entity.ChiTietHoaDon;
import iuh.fit.core.repository.ChiTietHoaDonRepository;
import iuh.fit.core.repository.ChiTietHoaDonRepository.ChiTietHoaDonItem;

import java.util.List;

public class ChiTietHoaDonService {

    private final ChiTietHoaDonRepository repository = new ChiTietHoaDonRepository();

    public List<ChiTietHoaDon> findByMaDon(String maDon) {
        return repository.findByMaDon(maDon);
    }

    public void replaceByMaDon(String maDon, List<ChiTietHoaDonItem> items) {
        repository.replaceByMaDon(maDon, items);
    }
}
