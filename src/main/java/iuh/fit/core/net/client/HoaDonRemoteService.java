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

import java.util.List;
import java.util.Map;

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
}
