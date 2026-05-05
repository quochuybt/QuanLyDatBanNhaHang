package iuh.fit.core.service;

import iuh.fit.core.entity.CaLam;
import iuh.fit.core.repository.CaLamRepository;

import java.util.List;

public class CaLamService {
    private final CaLamRepository repo = new CaLamRepository();

    public List<CaLam> findAll() {
        return repo.findAll();
    }
}
