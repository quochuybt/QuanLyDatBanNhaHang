package iuh.fit.core.service;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.entity.DanhMucMon;
import iuh.fit.core.repository.DanhMucMonRepository;

import java.util.List;

public class DanhMucMonService {

    private final DanhMucMonRepository danhMucMonRepository;

    public DanhMucMonService() {
        this.danhMucMonRepository = new DanhMucMonRepository();
    }

    public List<DanhMucMonDTO> findAllByName() {
        return danhMucMonRepository.findAllByName()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<DanhMucMonDTO> getAllDanhMuc() {
        return danhMucMonRepository.getAllDanhMuc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public boolean themDanhMuc(DanhMucMonDTO dto) {
        validateForCreate(dto);

        DanhMucMon danhMucMon = toEntity(dto);

        return danhMucMonRepository.themDanhMuc(danhMucMon);
    }

    public boolean capNhatDanhMuc(DanhMucMonDTO dto) {
        validateForUpdate(dto);

        DanhMucMon danhMucMon = toEntity(dto);

        return danhMucMonRepository.capNhatDanhMuc(danhMucMon);
    }

    public boolean xoaDanhMuc(DanhMucMonDTO dto) {
        String maDM = getMaDM(dto);

        return danhMucMonRepository.xoaDanhMuc(maDM);
    }

    private DanhMucMon toEntity(DanhMucMonDTO dto) {
        if (dto == null) {
            return null;
        }

        DanhMucMon danhMucMon = new DanhMucMon();

        danhMucMon.setMadm(dto.getMadm());
        danhMucMon.setTendm(dto.getTendm());
        danhMucMon.setMota(dto.getMota());

        return danhMucMon;
    }

    private DanhMucMonDTO toDTO(DanhMucMon entity) {
        if (entity == null) {
            return null;
        }

        DanhMucMonDTO dto = new DanhMucMonDTO();

        dto.setMadm(entity.getMadm());
        dto.setTendm(entity.getTendm());
        dto.setMota(entity.getMota());

        return dto;
    }

    private void validateForCreate(DanhMucMonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Danh mục món không được null");
        }

        if (isBlank(dto.getTendm())) {
            throw new IllegalArgumentException("Tên danh mục không được rỗng");
        }

        /*
         * Không bắt buộc kiểm tra mã danh mục khi thêm mới.
         * Vì trong Repository đã có logic:
         * nếu madm null hoặc rỗng thì tự sinh mã bằng generateNewMaDM().
         */
    }

    private void validateForUpdate(DanhMucMonDTO dto) {
        getMaDM(dto);

        if (isBlank(dto.getTendm())) {
            throw new IllegalArgumentException("Tên danh mục không được rỗng");
        }
    }

    private String getMaDM(DanhMucMonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Danh mục món không được null");
        }

        if (isBlank(dto.getMadm())) {
            throw new IllegalArgumentException("Mã danh mục không được rỗng");
        }

        return dto.getMadm();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}