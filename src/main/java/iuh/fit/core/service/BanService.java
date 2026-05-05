package iuh.fit.core.service;

import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.repository.BanRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BanService {

    private final BanRepository banRepository = new BanRepository();

    public List<Ban> findAll() {
        return banRepository.getAllBan();
    }

    public List<Ban> findByTrangThai(TrangThaiBan trangThai) {
        return banRepository.getAllBan().stream()
                .filter(b -> b.getTrangThai() == trangThai)
                .collect(Collectors.toList());
    }

    public Ban findById(String maBan) {
        return banRepository.getBanByMa(maBan);
    }
}
