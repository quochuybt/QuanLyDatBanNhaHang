package iuh.fit.core.service;

import iuh.fit.core.dto.GiaoCaDTO;
import iuh.fit.core.entity.GiaoCa;
import iuh.fit.core.repository.GiaoCaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GiaoCaService {

    private final GiaoCaRepository repository = new GiaoCaRepository();

    // --- Các phương thức CRUD cơ bản sử dụng DTO ---

    public void save(GiaoCaDTO dto) {
        if (dto != null) {
            repository.save(dto.toEntity());
        }
    }

    public GiaoCaDTO findById(String id) {
        GiaoCa entity = repository.findById(id);
        return GiaoCaDTO.fromEntity(entity);
    }

    public List<GiaoCaDTO> findAll() {
        return repository.findAll().stream()
                .map(GiaoCaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void update(GiaoCaDTO dto) {
        if (dto != null) {
            repository.update(dto.toEntity());
        }
    }

    public void delete(String id) {
        repository.delete(id);
    }

    // --- Các phương thức nghiệp vụ đặc thù cho Giao Ca ---

    /**
     * Lấy thông tin ca đang làm việc của một nhân viên cụ thể
     */
    public GiaoCaDTO getThongTinCaDangLam(String maNV) {
        GiaoCa entity = repository.getThongTinCaDangLam(maNV);
        return GiaoCaDTO.fromEntity(entity);
    }

    /**
     * Thực hiện bắt đầu ca mới.
     * Kiểm tra xem nhân viên đó có đang trong ca nào chưa kết thúc không.
     */
    public boolean batDauCa(GiaoCaDTO dto) {
        if (dto == null) return false;
        return repository.batDauCa(dto.toEntity());
    }

    /**
     * Kết thúc ca làm việc: Tính toán tiền mặt thực tế và chênh lệch
     */
    public boolean ketThucCa(int maGiaoCa, double tienCuoiCa, String ghiChu) {
        if (maGiaoCa <= 0 || tienCuoiCa < 0) return false;
        return repository.ketThucCa(maGiaoCa, tienCuoiCa, ghiChu);
    }

    /**
     * Thống kê tổng giờ làm theo tháng (Dùng cho tính lương)
     */
    public double getTongGioLamTheoThang(String maNV, LocalDate date) {
        return repository.getTongGioLam(maNV, "MONTH", date);
    }

    /**
     * Thống kê tổng giờ làm theo tuần
     */
    public double getTongGioLamTheoTuan(String maNV, LocalDate startOfWeek) {
        return repository.getTongGioLam(maNV, "WEEK", startOfWeek);
    }

    /**
     * Lấy dữ liệu biểu đồ giờ làm việc trong N ngày gần nhất
     * Trả về Map với Key là "dd/MM" và Value là số giờ làm
     */
    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        return repository.getGioLamTheoNgay(maNV, soNgay);
    }

    /**
     * Lấy danh sách nhân viên đang online kèm thông tin ca làm
     */
    public List<String> getNhanVienDangLamViecChiTiet() {
        List<Object[]> data = repository.getNhanVienDangLamViecChiTiet();
        return data.stream().map(row -> {
            String tenNV = (String) row[0];
            String tenCa = (String) row[1];
            if (tenCa != null) {
                return String.format("%s (%s)", tenNV, tenCa);
            }
            return tenNV + " (Ca bổ sung)";
        }).collect(Collectors.toList());
    }

    /**
     * Truy xuất lịch sử giao ca trong một khoảng thời gian
     */
    public List<GiaoCaDTO> getLichSuGiaoCa(LocalDate tuNgay, LocalDate denNgay) {
        return repository.getLichSuGiaoCa(tuNgay, denNgay).stream()
                .map(GiaoCaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public long count() {
        return repository.count();
    }

    public List<String> getCacCaLamSapToi(String maNV) {
        // Gọi repository để lấy danh sách ca sắp tới
        return repository.getCacCaLamSapToi(maNV);
    }

    public String[] getThongTinCaTruocSau(String maNV, LocalDate ngay) {
        if (maNV == null || ngay == null) return new String[]{"Không có", "Không có"};
        return repository.getThongTinCaTruocSau(maNV, ngay);
    }
}