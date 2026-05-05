package iuh.fit.core.service;

import iuh.fit.core.entity.DanhMucMon;
import iuh.fit.core.repository.DanhMucMonRepository;

import java.util.List;

public class DanhMucMonService {

    private final DanhMucMonRepository repo = new DanhMucMonRepository();

    public List<DanhMucMon> findAll() {
        return repo.findAllByName();
    }

    public DanhMucMon findById(String maDM) {
        return repo.findById(maDM);
    }
}
