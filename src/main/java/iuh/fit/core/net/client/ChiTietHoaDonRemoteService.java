package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.dto.chitiethoadon.ChiTietHoaDonReplaceRequest;
import iuh.fit.core.net.dto.dondatmon.MaDonRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.List;

public class ChiTietHoaDonRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public ChiTietHoaDonRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<ChiTietHoaDonDTO> getChiTietTheoMaDon(String maDon) {
        return getChiTietTheoMaDon(ChiTietHoaDonDTO.builder().maDon(maDon).build());
    }

    public List<ChiTietHoaDonDTO> getChiTietTheoMaDon(ChiTietHoaDonDTO filter) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.CHITIETHOADON_GET_BY_MA_DON.name(),
                filter,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể tải chi tiết hóa đơn.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<ChiTietHoaDonDTO>>() {});
    }

    public boolean themChiTiet(ChiTietHoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.CHITIETHOADON_ADD.name(), dto, DEFAULT_TIMEOUT_MS);
        ensureSuccess(response, "Lỗi thêm chi tiết hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean suaChiTiet(ChiTietHoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.CHITIETHOADON_UPDATE.name(), dto, DEFAULT_TIMEOUT_MS);
        ensureSuccess(response, "Lỗi sửa chi tiết hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean xoaChiTiet(ChiTietHoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(CommandAction.CHITIETHOADON_DELETE.name(), dto, DEFAULT_TIMEOUT_MS);
        ensureSuccess(response, "Lỗi xóa chi tiết hóa đơn");
        return JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
    }

    public boolean replaceByMaDon(String maDon, List<ChiTietHoaDonDTO> itemDTOList) {
        ChiTietHoaDonReplaceRequest request = new ChiTietHoaDonReplaceRequest(maDon, itemDTOList);
        MessageEnvelope response = connection.sendCommand(
                CommandAction.CHITIETHOADON_REPLACE_BY_MA_DON.name(),
                request,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể cập nhật chi tiết hóa đơn.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean replaceByMaDon(List<ChiTietHoaDonDTO> itemDTOList) {
        if (itemDTOList == null || itemDTOList.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chi tiết không được rỗng.");
        }

        String maDon = itemDTOList.get(0).getMaDon();

        if (maDon == null || maDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn không hợp lệ.");
        }

        return replaceByMaDon(maDon, itemDTOList);
    }
}
