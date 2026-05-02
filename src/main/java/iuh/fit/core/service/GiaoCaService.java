package iuh.fit.core.service;

import iuh.fit.core.entity.GiaoCa;
import iuh.fit.core.dao.GiaoCaRepository;

import java.util.List;

public class GiaoCaService {

    private final GiaoCaRepository giaoCaRepository = new GiaoCaRepository();

    public void save(GiaoCa giaoCa) {
        giaoCaRepository.save(giaoCa);
    }

    public GiaoCa findById(String id) {
        return giaoCaRepository.findById(id);
    }

    public List<GiaoCa> findAll() {
        return giaoCaRepository.findAll();
    }

    public void update(GiaoCa giaoCa) {
        giaoCaRepository.update(giaoCa);
    }

    public void delete(String id) {
        giaoCaRepository.delete(id);
    }

    public long count() {
        return giaoCaRepository.count();
    }
}
