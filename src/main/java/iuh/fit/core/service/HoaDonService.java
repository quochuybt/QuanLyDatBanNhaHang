package iuh.fit.core.service;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.entity.HoaDon;
import iuh.fit.core.repository.HoaDonRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoaDonService {

    private final HoaDonRepository repository = new HoaDonRepository();

    // --- Các phương thức CRUD cơ bản ---

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

    // --- Các phương thức nghiệp vụ nâng cao ---

    /**
     * Lấy hóa đơn chưa thanh toán tại một bàn cụ thể
     */
    public HoaDonDTO getHoaDonChuaThanhToan(String maBan) {
        HoaDon entity = repository.getHoaDonChuaThanhToan(maBan);
        return HoaDonDTO.fromEntity(entity);
    }

    /**
     * Lấy danh sách hóa đơn theo trang và bộ lọc (Dùng cho bảng quản lý hóa đơn)
     */
    public List<HoaDonDTO> getHoaDonByPage(int page, int itemsPerPage, String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return repository.getHoaDonByPage(page, itemsPerPage, trangThai, keyword, tuNgay, denNgay)
                .stream()
                .map(HoaDonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tổng số lượng hóa đơn theo bộ lọc để tính toán phân trang
     */
    public long getTotalHoaDonCount(String trangThai, String keyword, LocalDateTime tuNgay, LocalDateTime denNgay) {
        return repository.getTotalHoaDonCount(trangThai, keyword, tuNgay, denNgay);
    }

    /**
     * Thống kê doanh thu hàng ngày trong một khoảng thời gian (Dùng cho biểu đồ doanh thu)
     */
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        return repository.getDailyRevenue(startDate, endDate);
    }

    /**
     * Lấy doanh thu theo hình thức thanh toán (Tiền mặt/Chuyển khoản)
     * Thường dùng để đối soát khi nhân viên giao ca
     */
    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        return repository.getDoanhThuTheoHinhThuc(maNV, thoiGianBatDauCa, hinhThuc);
    }

    /**
     * Cập nhật mã khuyến mãi cho hóa đơn và tính lại tổng tiền thanh toán
     */
    public boolean capNhatMaKM(String maHD, String maKM) {
        if (maHD == null || maHD.isEmpty()) return false;
        return repository.capNhatMaKM(maHD, maKM);
    }

    /**
     * Xử lý nghiệp vụ thanh toán hóa đơn
     * Chú ý: Logic thanh toán phức tạp (đổi trạng thái bàn, trạng thái đơn) nên được
     * thực hiện trong một Transaction tại Repository hoặc Service này.
     */
    public boolean thanhToanHoaDon(HoaDonDTO dto) {
        if (dto == null || dto.getMaHD() == null) return false;

        // Cập nhật trạng thái và thông tin thanh toán
        dto.setTrangThai("Đã thanh toán");
        dto.setNgayLap(LocalDateTime.now());

        try {
            repository.update(dto.toEntity());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Transaction mở bàn + tạo đơn + tạo hóa đơn chưa thanh toán.
     */
    public HoaDonDTO moBanVaTaoHoaDon(String maBan, String maNV, String maKH, LocalDateTime thoiGianDen, String ghiChu) {
        HoaDon hd = repository.moBanVaTaoHoaDon(maBan, maNV, maKH, thoiGianDen, ghiChu);
        return HoaDonDTO.fromEntity(hd);
    }
}
