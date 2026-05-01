package iuh.fit.core.service;

import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.repository.DonDatMonRepository;

import java.util.List;

public class DonDatMonService {
    
    private final DonDatMonRepository donDatMonRepository = new DonDatMonRepository();

    public void save(DonDatMon donDatMon) {
        donDatMonRepository.save(donDatMon);
    }

    public DonDatMon findById(String id) {
        return donDatMonRepository.findById(id);
    }

    public List<DonDatMon> findAll() {
        return donDatMonRepository.findAll();
    }

    public void update(DonDatMon donDatMon) {
        donDatMonRepository.update(donDatMon);
    }

    public void delete(String id) {
        donDatMonRepository.delete(id);
    }

    public long count() {
        return donDatMonRepository.count();
    }
}
