package iuh.fit.core.service;

import iuh.fit.core.dto.CaLamDTO;
import iuh.fit.core.entity.CaLam;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.repository.CaLamRepository;

import java.util.List;

public class CaLamService {

    private final CaLamRepository caLamRepository;

    public CaLamService() {
        this.caLamRepository = new CaLamRepository();
    }

    public List<CaLamDTO> getAllCaLamOrderByGioBatDau() {
        return caLamRepository.getAllCaLamOrderByGioBatDau()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public CaLamDTO getCaLamByMa(CaLamDTO caLamDTO) {
        validateCaLamDTOForId(caLamDTO);

        CaLam caLam = caLamRepository.findById(caLamDTO.getMaCa());

        if (caLam == null) {
            return null;
        }

        return toDTO(caLam);
    }

    public boolean saveCaLam(CaLamDTO caLamDTO) {
        validateCaLamDTOForSave(caLamDTO);

        CaLam caLamTonTai = caLamRepository.findById(caLamDTO.getMaCa());
        if (caLamTonTai != null) {
            throw new IllegalArgumentException("Mã ca làm đã tồn tại: " + caLamDTO.getMaCa());
        }

        CaLam caLam = toEntity(caLamDTO);
        caLamRepository.save(caLam);
        return true;
    }

    public boolean updateCaLam(CaLamDTO caLamDTO) {
        validateCaLamDTOForSave(caLamDTO);

        CaLam caLamTonTai = caLamRepository.findById(caLamDTO.getMaCa());
        if (caLamTonTai == null) {
            throw new IllegalArgumentException("Không tìm thấy ca làm có mã: " + caLamDTO.getMaCa());
        }

        CaLam caLam = toEntity(caLamDTO);
        caLamRepository.update(caLam);
        return true;
    }

    public boolean deleteCaLam(CaLamDTO caLamDTO) {
        validateCaLamDTOForId(caLamDTO);

        CaLam caLamTonTai = caLamRepository.findById(caLamDTO.getMaCa());
        if (caLamTonTai == null) {
            throw new IllegalArgumentException("Không tìm thấy ca làm có mã: " + caLamDTO.getMaCa());
        }
        caLamTonTai.softDelete();
        caLamRepository.update(caLamTonTai);
        return true;
    }

    private CaLam toEntity(CaLamDTO caLamDTO) {
        return JsonMapper.convert(caLamDTO, CaLam.class);
    }

    private CaLamDTO toDTO(CaLam caLam) {
        return JsonMapper.convert(caLam, CaLamDTO.class);
    }

    private void validateCaLamDTOForId(CaLamDTO caLamDTO) {
        if (caLamDTO == null) {
            throw new IllegalArgumentException("Ca làm không được null");
        }

        if (isBlank(caLamDTO.getMaCa())) {
            throw new IllegalArgumentException("Mã ca làm không được rỗng");
        }
    }

    private void validateCaLamDTOForSave(CaLamDTO caLamDTO) {
        validateCaLamDTOForId(caLamDTO);

        if (isBlank(caLamDTO.getTenCa())) {
            throw new IllegalArgumentException("Tên ca làm không được rỗng");
        }

        if (caLamDTO.getGioBatDau() == null) {
            throw new IllegalArgumentException("Giờ bắt đầu không được null");
        }

        if (caLamDTO.getGioKetThuc() == null) {
            throw new IllegalArgumentException("Giờ kết thúc không được null");
        }

        if (!caLamDTO.getGioBatDau().isBefore(caLamDTO.getGioKetThuc())) {
            throw new IllegalArgumentException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}