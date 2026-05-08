package iuh.fit.core.service;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.entity.ChiTietHoaDon;
import iuh.fit.core.repository.ChiTietHoaDonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDonService {

    private final ChiTietHoaDonRepository chiTietHoaDonRepository;

    public ChiTietHoaDonService() {
        this.chiTietHoaDonRepository = new ChiTietHoaDonRepository();
    }

    public List<ChiTietHoaDonDTO> getChiTietTheoMaDon(ChiTietHoaDonDTO dto) {
        String maDon = getMaDon(dto);

        return chiTietHoaDonRepository.getChiTietTheoMaDon(maDon)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ChiTietHoaDonDTO findByMaDonAndMaMonAn(ChiTietHoaDonDTO dto) {
        String maDon = getMaDon(dto);
        String maMonAn = getMaMonAn(dto);

        ChiTietHoaDon chiTiet = chiTietHoaDonRepository.findByMaDonAndMaMonAn(maDon, maMonAn);

        return toDTO(chiTiet);
    }

    public void replaceByMaDon(ChiTietHoaDonDTO donDTO, List<ChiTietHoaDonDTO> itemDTOList) {
        String maDon = getMaDon(donDTO);

        if (itemDTOList == null) {
            throw new IllegalArgumentException("Danh sách chi tiết hóa đơn không được null");
        }

        List<ChiTietHoaDonRepository.ChiTietHoaDonItem> items = itemDTOList.stream()
                .map(itemDTO -> {
                    validateItemForReplace(itemDTO);

                    return new ChiTietHoaDonRepository.ChiTietHoaDonItem(
                            itemDTO.getMaMonAn(),
                            itemDTO.getSoLuong(),
                            itemDTO.getDonGia()
                    );
                })
                .toList();

        chiTietHoaDonRepository.replaceByMaDon(maDon, items);
    }

    public boolean themChiTiet(ChiTietHoaDonDTO dto) {
        validateForCreate(dto);

        return chiTietHoaDonRepository.themChiTiet(
                dto.getMaDon(),
                dto.getMaMonAn(),
                dto.getSoLuong(),
                dto.getDonGia()
        );
    }

    public boolean suaSoLuongChiTiet(ChiTietHoaDonDTO dto) {
        validateForUpdateSoLuong(dto);

        return chiTietHoaDonRepository.suaSoLuongChiTiet(
                dto.getMaDon(),
                dto.getMaMonAn(),
                dto.getSoLuong()
        );
    }

    public boolean suaChiTiet(ChiTietHoaDonDTO dto) {
        return suaSoLuongChiTiet(dto);
    }

    public boolean xoaChiTiet(ChiTietHoaDonDTO dto) {
        String maDon = getMaDon(dto);
        String maMonAn = getMaMonAn(dto);

        return chiTietHoaDonRepository.xoaChiTiet(maDon, maMonAn);
    }

    public Map<String, Integer> getTopSellingItems(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        validateThongKe(startDate, endDate, limit);

        return chiTietHoaDonRepository.getTopSellingItems(startDate, endDate, limit);
    }

    public List<String> getTopMonBanChayTrongNgay() {
        return chiTietHoaDonRepository.getTopMonBanChayTrongNgay();
    }

    public Map<String, Integer> getLeastSellingItems(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        validateThongKe(startDate, endDate, limit);

        return chiTietHoaDonRepository.getLeastSellingItems(startDate, endDate, limit);
    }

    private ChiTietHoaDonDTO toDTO(ChiTietHoaDon entity) {
        if (entity == null) {
            return null;
        }

        ChiTietHoaDonDTO dto = new ChiTietHoaDonDTO();

        if (entity.getDonDatMon() != null) {
            dto.setMaDon(entity.getDonDatMon().getMaDon());
        }

        if (entity.getMonAn() != null) {
            dto.setMaMonAn(entity.getMonAn().getMaMonAn());
            dto.setTenMon(entity.getMonAn().getTenMon());
        } else {
            dto.setTenMon(entity.getTenMon());
        }

        dto.setSoLuong(entity.getSoluong());
        dto.setDonGia(entity.getDongia());
        dto.setThanhTien(entity.getThanhtien());

        return dto;
    }

    private void validateForCreate(ChiTietHoaDonDTO dto) {
        getMaDon(dto);
        getMaMonAn(dto);

        if (dto.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        if (dto.getDonGia() < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm");
        }
    }

    private void validateForUpdateSoLuong(ChiTietHoaDonDTO dto) {
        getMaDon(dto);
        getMaMonAn(dto);

        if (dto.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }

    private void validateItemForReplace(ChiTietHoaDonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Chi tiết hóa đơn không được null");
        }

        if (isBlank(dto.getMaMonAn())) {
            throw new IllegalArgumentException("Mã món ăn không được rỗng");
        }

        if (dto.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        if (dto.getDonGia() < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm");
        }
    }

    private void validateThongKe(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        if (startDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được null");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("Ngày kết thúc không được null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Số lượng kết quả phải lớn hơn 0");
        }
    }

    private String getMaDon(ChiTietHoaDonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Chi tiết hóa đơn không được null");
        }

        if (isBlank(dto.getMaDon())) {
            throw new IllegalArgumentException("Mã đơn không được rỗng");
        }

        return dto.getMaDon();
    }

    private String getMaMonAn(ChiTietHoaDonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Chi tiết hóa đơn không được null");
        }

        if (isBlank(dto.getMaMonAn())) {
            throw new IllegalArgumentException("Mã món ăn không được rỗng");
        }

        return dto.getMaMonAn();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}