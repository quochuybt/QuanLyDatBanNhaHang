package iuh.fit.core.net.client;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonDetailRequestDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonPageRequestDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonTotalRequestDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class HoaDonRemoteService extends BaseRemoteService {
    private final SocketClientConnection connection;

    public HoaDonRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<HoaDonDTO> getHoaDonByPage(HoaDonPageRequestDTO req) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_GET_BY_PAGE.name(), req, 7000);
        ensureSuccess(response, "Lỗi tải danh sách hóa đơn");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<HoaDonDTO>>() {});
    }

    public long getTotalHoaDonCount(HoaDonTotalRequestDTO req) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_GET_TOTAL.name(), req, 7000);
        ensureSuccess(response, "Lỗi đếm tổng hóa đơn");
        Map<String, Object> map = JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<String, Object>>() {});
        Object total = map.get("total");
        if (total instanceof Number) {
            return ((Number) total).longValue();
        }
        return 0L;
    }

    public List<ChiTietHoaDonDTO> getChiTietHoaDon(HoaDonDetailRequestDTO req) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_GET_DETAIL.name(), req, 7000);
        ensureSuccess(response, "Lỗi tải chi tiết hóa đơn");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<ChiTietHoaDonDTO>>() {});
    }

    public HoaDonDTO getHoaDonChuaThanhToan(String maBan) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_GET_CHUA_THANH_TOAN_BY_BAN.name(), maBan, 7000);
        ensureSuccess(response, "Lỗi lấy hóa đơn chưa thanh toán");
        if (response.getPayload() == null || response.getPayload().isNull()) return null;
        return JsonCodec.fromJsonNode(response.getPayload(), HoaDonDTO.class);
    }

    public HoaDonDTO moBanVaTaoHoaDon(String maBan, String maNV, String maKH, LocalDateTime ngayLap, String ghiChu) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("maBan", maBan);
        payload.put("maNV", maNV);
        payload.put("maKH", maKH);
        payload.put("ngayLap", ngayLap);
        payload.put("ghiChu", ghiChu);

        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_MO_BAN_TAO_HOA_DON.name(), payload, 7000);
        ensureSuccess(response, "Lỗi mở bàn và tạo hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), HoaDonDTO.class);
    }

    public boolean thanhToanHoaDon(HoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_THANH_TOAN.name(), dto, 7000);
        ensureSuccess(response, "Lỗi thanh toán hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean capNhatMaKM(String maHD, String maKM) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("maHD", maHD);
        payload.put("maKM", maKM);

        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_CAP_NHAT_MA_KM.name(), payload, 7000);
        ensureSuccess(response, "Lỗi cập nhật mã KM");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean capNhatMaKH(String maHD, String maKH) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("maHD", maHD);
        payload.put("maKH", maKH);

        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_CAP_NHAT_MA_KH.name(), payload, 7000);
        ensureSuccess(response, "Lỗi cập nhật mã KH");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean capNhatTongTien(HoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_CAP_NHAT_TONG_TIEN.name(), dto, 7000);
        ensureSuccess(response, "Lỗi cập nhật tổng tiền");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public HoaDonDTO tinhLaiGiamGiaVaTongTien(HoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.HOADON_TINH_LAI_GIAM_GIA_VA_TONG_TIEN.name(), dto, 7000);
        ensureSuccess(response, "Lỗi tính lại hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), HoaDonDTO.class);
    }
}
