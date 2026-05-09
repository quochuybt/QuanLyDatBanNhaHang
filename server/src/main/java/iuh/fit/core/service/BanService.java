package iuh.fit.core.service;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.repository.BanRepository;

import java.util.List;
import java.util.Map;

public class BanService {

    private final BanRepository banRepository;

    public BanService() {
        this.banRepository = new BanRepository();
    }

    public boolean updateBan(BanDTO banDTO) {
        validateBanDTOForUpdate(banDTO);

        Ban banTrongDB = banRepository.getBanByMa(banDTO.getMaBan());
        if (banTrongDB == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn có mã: " + banDTO.getMaBan());
        }

        Ban ban = toEntity(banDTO);
        return banRepository.updateBan(ban);
    }

    public boolean chuyenBan(BanDTO banCuDTO, BanDTO banMoiDTO) {
        validateBanDTOForId(banCuDTO, "Bàn cũ");
        validateBanDTOForId(banMoiDTO, "Bàn mới");

        if (banCuDTO.getMaBan().equals(banMoiDTO.getMaBan())) {
            throw new IllegalArgumentException("Bàn cũ và bàn mới không được trùng nhau");
        }

        Ban banCu = banRepository.getBanByMa(banCuDTO.getMaBan());
        Ban banMoi = banRepository.getBanByMa(banMoiDTO.getMaBan());

        if (banCu == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn cũ: " + banCuDTO.getMaBan());
        }

        if (banMoi == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn mới: " + banMoiDTO.getMaBan());
        }

        if (banCu.getTrangThai() == TrangThaiBan.TRONG) {
            throw new IllegalArgumentException("Không thể chuyển bàn đang trống");
        }

        return banRepository.chuyenBan(banCu, banMoi);
    }

    public boolean ghepBanLienKet(List<BanDTO> listBanNguonDTO, BanDTO banDichDTO) {
        if (listBanNguonDTO == null || listBanNguonDTO.isEmpty()) {
            throw new IllegalArgumentException("Danh sách bàn nguồn không được rỗng");
        }

        validateBanDTOForId(banDichDTO, "Bàn đích");

        Ban banDich = banRepository.getBanByMa(banDichDTO.getMaBan());
        if (banDich == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn đích: " + banDichDTO.getMaBan());
        }

        List<Ban> listBanNguon = listBanNguonDTO.stream()
                // 1. Âm thầm lọc bỏ bàn đích ra khỏi danh sách nguồn để không bị lỗi
                .filter(dto -> !dto.getMaBan().equals(banDichDTO.getMaBan()))
                .map(dto -> {
                    validateBanDTOForId(dto, "Bàn nguồn");

                    Ban ban = banRepository.getBanByMa(dto.getMaBan());
                    if (ban == null) {
                        throw new IllegalArgumentException("Không tìm thấy bàn nguồn: " + dto.getMaBan());
                    }

                    return ban;
                })
                .toList();

        // 2. Nếu sau khi lọc xong mà danh sách rỗng (tức là giao diện chỉ truyền đúng 1 bàn)
        if (listBanNguon.isEmpty()) {
            // Tạm thời trả về false để giao diện báo lỗi "Có lỗi xảy ra" thay vì sập app
            return false;
        }

        // 3. Đẩy xuống Repository xử lý bình thường
        return banRepository.ghepBanLienKet(listBanNguon, banDich);
    }

    public String getTenHienThiGhep(String maBan) {
//        validateBanDTOForId(banDTO, "Bàn");

        Ban ban = banRepository.getBanByMa(maBan);
        if (ban == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn: " + maBan);
        }

        return banRepository.getTenHienThiGhep(maBan);
    }

    public String getChuoiTenBanGhep(String maBanChinh, String maHD) {
        return banRepository.getChuoiTenBanGhep(maBanChinh, maHD);
    }

    public String getMaBanChinh(BanDTO banDTO) {
        validateBanDTOForId(banDTO, "Bàn");

        Ban ban = banRepository.getBanByMa(banDTO.getMaBan());
        if (ban == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn: " + banDTO.getMaBan());
        }

        return banRepository.getMaBanChinh(banDTO.getMaBan());
    }

    public String getTenBanByMa(BanDTO banDTO) {
        validateBanDTOForId(banDTO, "Bàn");

        Ban ban = banRepository.getBanByMa(banDTO.getMaBan());
        if (ban == null) {
            throw new IllegalArgumentException("Không tìm thấy bàn: " + banDTO.getMaBan());
        }

        return banRepository.getTenBanByMa(banDTO.getMaBan());
    }

    public BanDTO getBanByMa(String maBan) {
        if(maBan.equalsIgnoreCase("")){
            return null;
        }
        Ban ban = banRepository.getBanByMa(maBan);
        if (ban == null) {
            return null;
        }

        return toDTO(ban);
    }

    public List<BanDTO> getAllBan() {
        return banRepository.getAllBan()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public int getSoThuTuBanLonNhat() {
        return banRepository.getSoThuTuBanLonNhat();
    }

    public Map<String, Integer> getTableStatusCounts() {
        return banRepository.getTableStatusCounts();
    }

    private Ban toEntity(BanDTO banDTO) {
        return JsonMapper.convert(banDTO, Ban.class);
    }

    private BanDTO toDTO(Ban ban) {
        return JsonMapper.convert(ban, BanDTO.class);
    }

    private void validateBanDTOForId(BanDTO banDTO, String fieldName) {
        if (banDTO == null) {
            throw new IllegalArgumentException(fieldName + " không được null");
        }

        if (isBlank(banDTO.getMaBan())) {
            throw new IllegalArgumentException("Mã bàn không được rỗng");
        }
    }

    private void validateBanDTOForUpdate(BanDTO banDTO) {
        validateBanDTOForId(banDTO, "Bàn");

        if (isBlank(banDTO.getTenBan())) {
            throw new IllegalArgumentException("Tên bàn không được rỗng");
        }

        if (banDTO.getSoGhe() <= 0) {
            throw new IllegalArgumentException("Số ghế phải lớn hơn 0");
        }

        if (banDTO.getTrangThai() == null) {
            throw new IllegalArgumentException("Trạng thái bàn không được null");
        }

        if (isBlank(banDTO.getKhuVuc())) {
            throw new IllegalArgumentException("Khu vực không được rỗng");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}