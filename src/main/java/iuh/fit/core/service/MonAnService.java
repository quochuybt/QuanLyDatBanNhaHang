package iuh.fit.core.service;

import iuh.fit.core.entity.MonAn;
import iuh.fit.core.repository.MonAnRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonAnService {

    private final MonAnRepository repo = new MonAnRepository();

    public void add(MonAn m) {
        if (repo.findById(m.getMaMonAn()) != null)
            throw new IllegalArgumentException("Món ăn đã tồn tại");
        repo.save(m);
    }

    public void save(MonAn m) {
        add(m);
    }

    public List<MonAn> findAll() {
        return repo.findAll();
    }

    public List<MonAn> findDangKinhDoanh() {
        return repo.findAllDangKinhDoanh();
    }

    public MonAn findById(String maMon) {
        return repo.findById(maMon);
    }

    public MonAn findByName(String tenMon) {
        return repo.findByName(tenMon);
    }

    public void update(MonAn m) {
        if (repo.findById(m.getMaMonAn()) == null)
            throw new IllegalArgumentException("Món ăn không tồn tại");
        repo.update(m);
    }

    public String getNextMaMonAn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MA" + datePart + randomPart;
    }

    public void delete(String maMon) {
        if (repo.findById(maMon) == null)
            throw new IllegalArgumentException("Món ăn không tồn tại");
        repo.delete(maMon);
    }
}
