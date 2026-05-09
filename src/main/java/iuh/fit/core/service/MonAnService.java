package iuh.fit.core.service;

import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.entity.MonAn;
import iuh.fit.core.repository.MonAnRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    public List<MonAnDTO> findAllDTO() {
        return repo.findAll().stream()
                .map(MonAnDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MonAn> findDangKinhDoanh() {
        return repo.findAllDangKinhDoanh();
    }

    public List<MonAnDTO> findDangKinhDoanhDTO() {
        return repo.findAllDangKinhDoanh().stream()
                .map(MonAnDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public MonAn findById(String maMon) {
        return repo.findById(maMon);
    }

    public MonAnDTO findByIdDTO(String maMon) {
        return MonAnDTO.fromEntity(repo.findById(maMon));
    }

    public MonAn findByName(String tenMon) {
        return repo.findByName(tenMon);
    }

    public void update(MonAn m) {
        if (repo.findById(m.getMaMonAn()) == null)
            throw new IllegalArgumentException("Món ăn không tồn tại");
        repo.update(m);
    }

    public void addFromDTO(MonAnDTO dto) {
        add(dto.toEntity());
    }

    public void updateFromDTO(MonAnDTO dto) {
        update(dto.toEntity());
    }

    public String getNextMaMonAn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "MA" + datePart + randomPart;
    }

    public void delete(String maMon) {
        MonAn m = repo.findById(maMon);
        if (m == null)
            throw new IllegalArgumentException("Món ăn không tồn tại");
        m.softDelete();
        repo.update(m);
    }
}
