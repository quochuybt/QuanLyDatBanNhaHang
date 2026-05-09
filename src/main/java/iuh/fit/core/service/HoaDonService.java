package iuh.fit.core.service;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.entity.HoaDon;
import iuh.fit.core.repository.HoaDonRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoaDonService {

    private final HoaDonRepository repository = new HoaDonRepository();
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
    private final KhachHangService khachHangService = new KhachHangService();

    public void save(HoaDonDTO dto) {
        if (dto != null) {
            repository.save(dto.toEntity());
        }
    }

    public HoaDonDTO findById(String id) {
        HoaDon entity = repository.findById(id);
        return HoaDonDTO.fromEntity(entity);
    }

    public List<HoaDonDTO> findAll() {
        return repository.findAll().stream()
                .map(HoaDonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void update(HoaDonDTO dto) {
        if (dto != null) {
            repository.update(dto.toEntity());
        }
    }

    public void delete(String id) {
        repository.delete(id);
    }

    public long count() {
        return repository.count();
    }

    public HoaDonDTO getHoaDonChuaThanhToan(String maBan) {
        HoaDon entity = repository.getHoaDonChuaThanhToan(maBan);
        return HoaDonDTO.fromEntity(entity);
    }

    public List<HoaDonDTO> getHoaDonByPage(
            int page,
            int itemsPerPage,
            String trangThai,
            String keyword,
            LocalDateTime tuNgay,
            LocalDateTime denNgay
    ) {
        return repository.getHoaDonByPage(page, itemsPerPage, trangThai, keyword, tuNgay, denNgay)
                .stream()
                .map(HoaDonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public long getTotalHoaDonCount(
            String trangThai,
            String keyword,
            LocalDateTime tuNgay,
            LocalDateTime denNgay
    ) {
        return repository.getTotalHoaDonCount(trangThai, keyword, tuNgay, denNgay);
    }

    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return repository.getDailyRevenue(startDate, endDate);
    }

    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        return repository.getDoanhThuTheoHinhThuc(maNV, thoiGianBatDauCa, hinhThuc);
    }

    public boolean capNhatMaKM(String maHD, String maKM) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }

        boolean ok = repository.capNhatMaKM(maHD.trim(), normalizeBlankToNull(maKM));

        if (ok) {
            HoaDonDTO hd = findById(maHD.trim());
            if (hd != null) {
                capNhatTongTien(hd);
            }
        }

        return ok;
    }

    public boolean capNhatMaKH(String maHD, String maKH) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }

        boolean ok = repository.capNhatMaKH(maHD.trim(), normalizeBlankToNull(maKH));

        if (ok) {
            HoaDonDTO hd = findById(maHD.trim());
            if (hd != null) {
                capNhatTongTien(hd);
            }
        }

        return ok;
    }

    public boolean thanhToanHoaDon(HoaDonDTO dto) {
        if (dto == null || dto.getMaHD() == null || dto.getMaHD().trim().isEmpty()) {
            return false;
        }

        try {
            HoaDonDTO latest = tinhLaiGiamGiaVaTongTien(dto);

            if (latest != null) {
                dto.setTongTien(latest.getTongTien());
                dto.setGiamGia(latest.getGiamGia());
                dto.setTongThanhToan(latest.getTongThanhToan());
                dto.setMaKM(latest.getMaKM());
                dto.setMaKH(latest.getMaKH());
            }

            return repository.thanhToanHoaDon(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public HoaDonDTO moBanVaTaoHoaDon(
            String maBan,
            String maNV,
            String maKH,
            LocalDateTime thoiGianDen,
            String ghiChu
    ) {
        HoaDon hd = repository.moBanVaTaoHoaDon(maBan, maNV, maKH, thoiGianDen, ghiChu);
        return HoaDonDTO.fromEntity(hd);
    }

    public HoaDonDTO tinhLaiGiamGiaVaTongTien(HoaDonDTO activeHoaDon) {
        if (activeHoaDon == null || activeHoaDon.getMaHD() == null || activeHoaDon.getMaHD().trim().isEmpty()) {
            return activeHoaDon;
        }

        HoaDonDTO current = findById(activeHoaDon.getMaHD().trim());

        if (current == null) {
            return activeHoaDon;
        }

        float tongTien = activeHoaDon.getTongTien() > 0
                ? activeHoaDon.getTongTien()
                : current.getTongTien();

        float giamGia = tinhTongGiamGia(
                tongTien,
                current.getMaKM(),
                current.getMaKH()
        );

        float tongThanhToan = Math.max(0f, tongTien - giamGia);

        repository.capNhatTongTien(
                current.getMaHD(),
                tongTien,
                giamGia,
                tongThanhToan
        );

        HoaDonDTO result = findById(current.getMaHD());

        if (result == null) {
            current.setTongTien(tongTien);
            current.setGiamGia(giamGia);
            current.setTongThanhToan(tongThanhToan);
            return current;
        }

        return result;
    }

    public boolean capNhatTongTien(HoaDonDTO dto) {
        if (dto == null || dto.getMaHD() == null || dto.getMaHD().trim().isEmpty()) {
            return false;
        }

        String maHD = dto.getMaHD().trim();

        HoaDonDTO current = findById(maHD);

        if (current == null) {
            return false;
        }

        float tongTien = dto.getTongTien();

        if (tongTien < 0) {
            tongTien = 0f;
        }

        String maKM = current.getMaKM();
        String maKH = current.getMaKH();

        if (dto.getMaKM() != null && !dto.getMaKM().trim().isEmpty()) {
            maKM = dto.getMaKM().trim();
        }

        if (dto.getMaKH() != null && !dto.getMaKH().trim().isEmpty()) {
            maKH = dto.getMaKH().trim();
        }

        float giamGia = tinhTongGiamGia(tongTien, maKM, maKH);
        float tongThanhToan = Math.max(0f, tongTien - giamGia);

        return repository.capNhatTongTien(
                maHD,
                tongTien,
                giamGia,
                tongThanhToan
        );
    }

    private float tinhTongGiamGia(float tongTien, String maKM, String maKH) {
        if (tongTien <= 0) {
            return 0f;
        }

        float giamKhuyenMai = tinhGiamKhuyenMai(tongTien, maKM);
        float giamThanhVien = tinhGiamThanhVien(tongTien, maKH);

        float tongGiam = giamKhuyenMai + giamThanhVien;

        if (tongGiam < 0) {
            tongGiam = 0f;
        }

        return Math.min(tongTien, tongGiam);
    }

    private float tinhGiamKhuyenMai(float tongTien, String maKM) {
        if (tongTien <= 0 || maKM == null || maKM.trim().isEmpty()) {
            return 0f;
        }

        try {
            KhuyenMaiDTO km = khuyenMaiService.findByIdDTO(maKM.trim());

            if (km == null || !isKhuyenMaiActive(km)) {
                return 0f;
            }

            if (tongTien < km.getDieuKienApDung()) {
                return 0f;
            }

            float giaTri = (float) km.getGiaTri();

            if (giaTri <= 0) {
                return 0f;
            }

            String loai = km.getLoaiKhuyenMai() == null
                    ? ""
                    : km.getLoaiKhuyenMai().trim().toLowerCase();

            float tienGiam;

            if (loai.contains("%")
                    || loai.contains("phần")
                    || loai.contains("phan")
                    || loai.contains("percent")) {
                tienGiam = tongTien * giaTri / 100f;
            } else if (loai.contains("tiền")
                    || loai.contains("tien")
                    || loai.contains("vnd")
                    || loai.contains("vnđ")) {
                tienGiam = giaTri;
            } else {
                tienGiam = giaTri <= 100f
                        ? tongTien * giaTri / 100f
                        : giaTri;
            }

            return Math.min(tongTien, Math.max(0f, tienGiam));

        } catch (Exception e) {
            e.printStackTrace();
            return 0f;
        }
    }

    private boolean isKhuyenMaiActive(KhuyenMaiDTO km) {
        if (km == null) {
            return false;
        }

        LocalDate now = LocalDate.now();

        String trangThai = km.getTrangThai() == null
                ? ""
                : km.getTrangThai().trim();

        boolean dungTrangThai = "Đang áp dụng".equalsIgnoreCase(trangThai);
        boolean daBatDau = km.getNgayBatDau() == null || !km.getNgayBatDau().isAfter(now);
        boolean chuaKetThuc = km.getNgayKetThuc() == null || !km.getNgayKetThuc().isBefore(now);

        int gioiHan = km.getSoLuongGioiHan();
        int daDung = km.getSoLuotDaDung();

        boolean conLuot = gioiHan <= 0 || daDung < gioiHan;

        return dungTrangThai && daBatDau && chuaKetThuc && conLuot;
    }

    private float tinhGiamThanhVien(float tongTien, String maKH) {
        if (tongTien <= 0 || maKH == null || maKH.trim().isEmpty()) {
            return 0f;
        }

        try {
            KhachHangDTO kh = khachHangService.findByIdDTO(maKH.trim());

            if (kh == null || kh.getHangThanhVien() == null) {
                return 0f;
            }

            float phanTram = getPhanTramGiamTheoHang(kh.getHangThanhVien());

            if (phanTram <= 0) {
                return 0f;
            }

            return tongTien * phanTram / 100f;

        } catch (Exception e) {
            e.printStackTrace();
            return 0f;
        }
    }

    private float getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) {
            return 0f;
        }

        return switch (hang) {
            case BRONZE -> 3f;
            case SILVER -> 5f;
            case GOLD -> 10f;
            case DIAMOND -> 15f;
            default -> 0f;
        };
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}