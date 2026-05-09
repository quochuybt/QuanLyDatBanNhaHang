package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.net.dto.khachhang.KhachHangSearchRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.ArrayList;
import java.util.List;

public class KhachHangRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public KhachHangRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public boolean addKhachHang(KhachHangDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHACHHANG_ADD.name(),
                dto,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể thêm khách hàng.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /*
     * Alias để code GUI/service cũ gọi add(...) vẫn chạy được.
     */
    public boolean add(KhachHangDTO dto) {
        return addKhachHang(dto);
    }

    public boolean save(KhachHangDTO dto) {
        return addKhachHang(dto);
    }

    public boolean update(KhachHangDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHACHHANG_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể cập nhật khách hàng.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /*
     * Alias để code cũ gọi updateKhachHang(...) vẫn chạy.
     */
    public boolean updateKhachHang(KhachHangDTO dto) {
        return update(dto);
    }

    public List<KhachHangDTO> search(String keyword) {
        KhachHangSearchRequest request = new KhachHangSearchRequest(keyword);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHACHHANG_SEARCH.name(),
                request,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể tìm kiếm khách hàng.");

        List<KhachHangDTO> result = JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<KhachHangDTO>>() {}
        );

        return result != null ? result : new ArrayList<>();
    }

    public List<KhachHangDTO> findAll() {
        return search("");
    }

    public List<KhachHangDTO> getAll() {
        return findAll();
    }

    public KhachHangDTO findByIdDTO(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        String id = maKH.trim();

        /*
         * Hiện CommandAction chưa có KHACHHANG_GET_BY_ID,
         * nên dùng findAll() rồi lọc theo mã để đảm bảo vẫn đi qua socket.
         */
        List<KhachHangDTO> dsKhachHang = findAll();

        for (KhachHangDTO kh : dsKhachHang) {
            if (kh != null && id.equalsIgnoreCase(safe(kh.getMaKH()))) {
                return kh;
            }
        }

        return null;
    }

    public KhachHangDTO findById(String maKH) {
        return findByIdDTO(maKH);
    }

    public KhachHangDTO findByMaKH(String maKH) {
        return findByIdDTO(maKH);
    }

    public KhachHangDTO findBySdtDTO(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        String phone = sdt.trim();

        /*
         * KHACHHANG_SEARCH hiện search được theo SĐT,
         * nên tìm theo SĐT bằng search(phone), sau đó lọc chính xác.
         */
        List<KhachHangDTO> dsKhachHang = search(phone);

        for (KhachHangDTO kh : dsKhachHang) {
            if (kh != null && phone.equals(safe(kh.getSdt()))) {
                return kh;
            }
        }

        return null;
    }

    public KhachHangDTO findBySdt(String sdt) {
        return findBySdtDTO(sdt);
    }

    public boolean addChiTieu(String maKH, float soTien) {
        if (maKH == null || maKH.trim().isEmpty() || soTien <= 0) {
            return false;
        }

        KhachHangDTO khachHang = findByIdDTO(maKH);

        if (khachHang == null) {
            return false;
        }

        float tongChiTieuMoi = khachHang.getTongChiTieu() + soTien;

        khachHang.setTongChiTieu(tongChiTieuMoi);
        khachHang.setHangThanhVien(tinhHangThanhVien(tongChiTieuMoi));

        return update(khachHang);
    }

    private HangThanhVien tinhHangThanhVien(float tongChiTieu) {
        if (tongChiTieu >= 20_000_000f) {
            return HangThanhVien.DIAMOND;
        }

        if (tongChiTieu >= 10_000_000f) {
            return HangThanhVien.GOLD;
        }

        if (tongChiTieu >= 5_000_000f) {
            return HangThanhVien.SILVER;
        }

        if (tongChiTieu >= 1_000_000f) {
            return HangThanhVien.BRONZE;
        }

        return HangThanhVien.MEMBER;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}