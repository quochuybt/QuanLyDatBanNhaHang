package iuh.fit.core.service;

import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.entity.KhachHang;
import iuh.fit.core.repository.KhachHangRepository;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class KhachHangService {

    private final KhachHangRepository khachHangRepo = new KhachHangRepository();

    public boolean addKhachHang(KhachHang kh) {
        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng không được null.");
        }

        chuanHoaKhachHang(kh, true);

        if (khachHangRepo.findById(kh.getMaKH()) != null) {
            throw new IllegalArgumentException("Mã khách hàng '" + kh.getMaKH() + "' đã tồn tại.");
        }

        if (kh.getSdt() != null && !kh.getSdt().trim().isEmpty()) {
            KhachHang khTheoSDT = khachHangRepo.findBySdt(kh.getSdt().trim());

            if (khTheoSDT != null) {
                throw new IllegalArgumentException("Số điện thoại '" + kh.getSdt() + "' đã tồn tại.");
            }
        }

        khachHangRepo.save(kh);
        return true;
    }

    public void addFromDTO(KhachHangDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được null.");
        }

        KhachHang kh = dto.toEntity();
        addKhachHang(kh);
    }

    public KhachHang findById(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        return khachHangRepo.findById(maKH.trim());
    }

    public KhachHangDTO findByIdDTO(String maKH) {
        KhachHang kh = findById(maKH);
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
        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng không được null.");
        }

        if (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng.");
        }

        KhachHang khCu = khachHangRepo.findById(kh.getMaKH().trim());

        if (khCu == null) {
            throw new IllegalArgumentException("Khách hàng '" + kh.getMaKH() + "' không tồn tại.");
        }

        chuanHoaKhachHang(kh, false);

        // Check trùng SĐT với khách khác
        if (kh.getSdt() != null && !kh.getSdt().trim().isEmpty()) {
            KhachHang khTheoSDT = khachHangRepo.findBySdt(kh.getSdt().trim());

            if (khTheoSDT != null && !khTheoSDT.getMaKH().equals(kh.getMaKH())) {
                throw new IllegalArgumentException("Số điện thoại '" + kh.getSdt() + "' đã thuộc khách hàng khác.");
            }
        }

        khachHangRepo.update(kh);
    }

    public void updateFromDTO(KhachHangDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không được null.");
        }

        update(dto.toEntity());
    }

    public void delete(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng.");
        }

        KhachHang kh = khachHangRepo.findById(maKH.trim());

        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");
        }

        kh.softDelete();
        khachHangRepo.update(kh);
    }

    public List<KhachHang> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        return khachHangRepo.search(keyword);
    }

    public List<KhachHangDTO> searchDTO(String keyword) {
        return search(keyword).stream()
                .map(KhachHangDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void addChiTieu(String maKH, float soTien) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng.");
        }

        KhachHang kh = khachHangRepo.findById(maKH.trim());

        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");
        }

        kh.capNhatTongChiTieu(soTien);
        khachHangRepo.update(kh);
    }

    public KhachHang findBySdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        return khachHangRepo.findBySdt(sdt.trim());
    }

    public KhachHangDTO findBySdtDTO(String sdt) {
        KhachHang kh = findBySdt(sdt);
        return kh != null ? KhachHangDTO.fromEntity(kh) : null;
    }

    private void chuanHoaKhachHang(KhachHang kh, boolean isNew) {
        if (isNew && (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty())) {
            kh.setMaKH(phatSinhMaKH());
        }

        if (kh.getTenKH() == null || kh.getTenKH().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được rỗng.");
        }

        if (kh.getSdt() == null || kh.getSdt().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được rỗng.");
        }

        if (!kh.getSdt().trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ. SĐT phải gồm 10 chữ số.");
        }

        kh.setSdt(kh.getSdt().trim());
        kh.setTenKH(kh.getTenKH().trim());

        if (kh.getGioitinh() == null || kh.getGioitinh().trim().isEmpty()) {
            kh.setGioitinh("Khác");
        }

        if (kh.getHangThanhVien() == null) {
            kh.setHangThanhVien(HangThanhVien.NONE);
        }

        if (kh.getNgayThamGia() == null) {
            kh.setNgayThamGia(LocalDate.now());
        }

        if (kh.getNgaySinh() == null) {
            kh.setNgaySinh(LocalDate.of(2000, 1, 1));
        }

        validateKhachHangDu18Tuoi(kh.getNgaySinh());
    }

    private void validateKhachHangDu18Tuoi(LocalDate ngaySinh) {
        if (ngaySinh == null) {
            throw new IllegalArgumentException("Ngày sinh không được rỗng.");
        }

        LocalDate today = LocalDate.now();

        if (ngaySinh.isAfter(today)) {
            throw new IllegalArgumentException("Ngày sinh không được lớn hơn ngày hiện tại.");
        }

        int tuoi = Period.between(ngaySinh, today).getYears();

        if (tuoi < 18) {
            throw new IllegalArgumentException("Khách hàng phải đủ từ 18 tuổi trở lên.");
        }
    }

    private String phatSinhMaKH() {
        String ngayGio = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

        String maKH;
        do {
            int random = ThreadLocalRandom.current().nextInt(1000, 10000);
            maKH = "KH" + ngayGio + random;
        } while (khachHangRepo.findById(maKH) != null);

        return maKH;
    }
}