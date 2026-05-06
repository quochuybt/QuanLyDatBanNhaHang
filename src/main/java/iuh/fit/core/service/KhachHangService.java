package iuh.fit.core.service;

import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.entity.KhachHang;
import iuh.fit.core.repository.KhachHangRepository;

import java.util.List;
import java.util.stream.Collectors;

public class KhachHangService {

    private final KhachHangRepository khachHangRepo = new KhachHangRepository();

    public void addKhachHang(KhachHang kh) {
        if (khachHangRepo.findById(kh.getMaKH()) != null)
            throw new IllegalArgumentException("Mã khách hàng '" + kh.getMaKH() + "' đã tồn tại.");

        khachHangRepo.save(kh);
    }

    public void addFromDTO(KhachHangDTO dto) {
        addKhachHang(dto.toEntity());
    }

    public KhachHang findById(String maKH) {
        return khachHangRepo.findById(maKH);
    }

    public KhachHangDTO findByIdDTO(String maKH) {
        KhachHang kh = khachHangRepo.findById(maKH);
        return kh != null ? KhachHangDTO.fromEntity(kh) : null;
    }

    public List<KhachHang> findAll() {
        return khachHangRepo.findAll();
    }

    public List<KhachHangDTO> findAllDTO() {
        return khachHangRepo.findAll().stream()
                .map(KhachHangDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void update(KhachHang kh) {
        if (khachHangRepo.findById(kh.getMaKH()) == null)
            throw new IllegalArgumentException("Khách hàng '" + kh.getMaKH() + "' không tồn tại.");

        khachHangRepo.update(kh);
    }

    public void updateFromDTO(KhachHangDTO dto) {
        update(dto.toEntity());
    }

    public void delete(String maKH) {
        KhachHang kh = khachHangRepo.findById(maKH);

        if (kh == null)
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");

        khachHangRepo.delete(maKH);
    }

    public List<KhachHang> search(String keyword) {
        return khachHangRepo.search(keyword);
    }

    public List<KhachHangDTO> searchDTO(String keyword) {
        return khachHangRepo.search(keyword).stream()
                .map(KhachHangDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void addChiTieu(String maKH, float soTien) {
        KhachHang kh = khachHangRepo.findById(maKH);

        if (kh == null)
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");

        kh.capNhatTongChiTieu(soTien);
        khachHangRepo.update(kh);
    }

    public KhachHang findBySdt(String sdt) {
        return khachHangRepo.findBySdt(sdt);
    }

    public KhachHangDTO findBySdtDTO(String sdt) {
        KhachHang kh = khachHangRepo.findBySdt(sdt);
        return kh != null ? KhachHangDTO.fromEntity(kh) : null;
    }
}