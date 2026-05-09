package iuh.fit.core.service;

import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.entity.KhuyenMai;
import iuh.fit.core.repository.KhuyenMaiRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class KhuyenMaiService {

    private final KhuyenMaiRepository repo = new KhuyenMaiRepository();

    public void add(KhuyenMai km) {
        if (repo.findById(km.getMaKM()) != null)
            throw new IllegalArgumentException("Khuyến mãi đã tồn tại");
        repo.save(km);
    }

    public void addFromDTO(KhuyenMaiDTO dto) {
        add(dto.toEntity());
    }

    public KhuyenMai findById(String maKM) {
        return repo.findById(maKM);
    }

    public KhuyenMaiDTO findByIdDTO(String maKM) {
        return KhuyenMaiDTO.fromEntity(repo.findById(maKM));
    }

    public KhuyenMai findActiveById(String maKM) {
        return repo.findActiveById(maKM);
    }

    public List<KhuyenMai> findAll() {
        return repo.findAll();
    }

    public List<KhuyenMaiDTO> findAllDTO() {
        return repo.findAll().stream()
                .map(KhuyenMaiDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<KhuyenMai> findAllActive() {
        return repo.findAllActive();
    }

    public List<KhuyenMaiDTO> findAllActiveDTO() {
        return repo.findAllActive().stream()
                .map(KhuyenMaiDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<KhuyenMai> search(String keyword) {
        return repo.findByKeyword(keyword);
    }

    public List<KhuyenMaiDTO> searchDTO(String keyword) {
        return repo.findByKeyword(keyword).stream()
                .map(KhuyenMaiDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void update(KhuyenMai km) {
        if (repo.findById(km.getMaKM()) == null)
            throw new IllegalArgumentException("Không tồn tại khuyến mãi");
        repo.update(km);
    }

    public void updateFromDTO(KhuyenMaiDTO dto) {
        update(dto.toEntity());
    }

    public void delete(String maKM) {
        KhuyenMai km = repo.findById(maKM);
        if (km == null)
            throw new IllegalArgumentException("Không tồn tại khuyến mãi");
        km.softDelete();
        repo.update(km);
    }

    /**
     * Tự động đánh dấu khuyến mãi hết hạn
     */
    public void autoExpire() {
        List<KhuyenMai> list = repo.findAll();
        for (KhuyenMai km : list) {
            if (km.getNgayKetThuc() != null &&
                    km.getNgayKetThuc().isBefore(LocalDate.now()) &&
                    !"Ngưng áp dụng".equals(km.getTrangThai())) {
                km.setTrangThai("Ngưng áp dụng");
                repo.update(km);
            }
        }
    }

    /**
     * Sử dụng khuyến mãi: kiểm tra + tăng lượt dùng
     */
    public KhuyenMai useKhuyenMai(String maKM) {
        KhuyenMai km = repo.findById(maKM);
        if (km == null)
            throw new IllegalArgumentException("Khuyến mãi không tồn tại");
        if (!km.isActive())
            throw new IllegalArgumentException("Khuyến mãi đã hết hạn hoặc ngưng áp dụng");
        if (!km.conLuotSuDung())
            throw new IllegalArgumentException("Khuyến mãi đã hết lượt sử dụng");

        km.tangLuotDaDung();
        repo.update(km);
        return km;
    }
}