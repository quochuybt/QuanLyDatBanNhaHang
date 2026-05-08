package iuh.fit.core.service;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.entity.DonDatMon;
import iuh.fit.core.repository.DonDatMonRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DonDatMonService {

    private final DonDatMonRepository repository = new DonDatMonRepository();

    /**
     * Lưu đơn đặt mới từ DTO
     */
    public void save(DonDatMonDTO dto) {
        if (dto == null) return;
        DonDatMon entity = dto.toEntity();
        repository.save(entity);
    }

    /**
     * Tìm đơn theo mã và trả về DTO
     */
    public DonDatMonDTO findById(String id) {
        DonDatMon entity = repository.findById(id);
        return DonDatMonDTO.fromEntity(entity);
    }

    public DonDatMonDTO getDonDatMonChuaNhanTheoMaBanBaoGomLinked(String maBan) {
        DonDatMon entity = repository.getDonDatMonChuaNhanTheoMaBanBaoGomLinked(maBan);
        return DonDatMonDTO.fromEntity(entity);
    }

    public List<DonDatMonDTO> getAllDonDatMonChuaNhanBaoGomLinked() {
        return repository.getAllDonDatMonChuaNhanBaoGomLinked()
                .stream()
                .map(DonDatMonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy toàn bộ danh sách dưới dạng DTO
     */
    public List<DonDatMonDTO> findAll() {
        return repository.findAll().stream()
                .map(DonDatMonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin từ DTO
     */
    public void update(DonDatMonDTO dto) {
        if (dto == null || dto.getMaDon() == null) return;
        repository.update(dto.toEntity());
    }

    /**
     * Xóa đơn theo mã
     */
    public void delete(String id) {
        repository.delete(id);
    }

    public boolean huyDatBanVaGiaiPhongBanGhep(String maDon) {
        return repository.huyDatBanVaGiaiPhongBanGhep(maDon);
    }

    // --- Các phương thức nghiệp vụ đặc thù đã chuyển sang DTO ---

    /**
     * Tìm kiếm đơn chưa nhận theo tên hoặc SĐT khách hàng
     */
    public List<DonDatMonDTO> timDonDatMonChuaNhan(String query) {
        return repository.timDonDatMonChuaNhan(query).stream()
                .map(DonDatMonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các đơn đặt trước của ngày hôm nay trở đi
     */
    public List<DonDatMonDTO> getAllDonDatMonChuaNhan() {
        return repository.getAllDonDatMonChuaNhan().stream()
                .map(DonDatMonDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn đặt trước sớm nhất của một bàn cụ thể
     */
    public DonDatMonDTO getDonDatMonDatTruoc(String maBan) {
        DonDatMon entity = repository.getDonDatMonDatTruoc(maBan);
        return DonDatMonDTO.fromEntity(entity);
    }

    /**
     * Kiểm tra danh sách bàn đã có lịch đặt trong khoảng thời gian xác định
     */
    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        return repository.getMaBanDaDatTrongKhoang(tuGio, denGio);
    }

    /**
     * Lấy danh sách các bàn đi cùng đoàn (Ghép bàn)
     */
    public List<String> getMaBanCungDotDat(String maKH, LocalDateTime thoiGianDen, String maBanHienTai) {
        return repository.getMaBanCungDotDat(maKH, thoiGianDen, maBanHienTai);
    }

    /**
     * Tác vụ tự động: Hủy các đơn quá giờ và trả trạng thái bàn về 'Trống'
     * Trả về số lượng đơn đã xử lý
     */
    public int tuDongHuyDonQuaGio() {
        return repository.tuDongHuyDonQuaGio();
    }

    /**
     * Cập nhật trạng thái 'Đã đặt trước' cho các bàn có lịch trong 120 phút tới
     */
    public void capNhatTrangThaiBanTheoGio() {
        repository.capNhatTrangThaiBanTheoGio();
    }

    /**
     * Đếm tổng số đơn hiện có
     */
    public long count() {
        return repository.count();
    }
}