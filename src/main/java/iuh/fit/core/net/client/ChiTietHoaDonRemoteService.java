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
        MaDonRequest request = new MaDonRequest(maDon);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.CHITIETHOADON_GET_BY_MA_DON.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải chi tiết hóa đơn.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<ChiTietHoaDonDTO>>() {}
        );
    }

    public boolean replaceByMaDon(String maDon, List<ChiTietHoaDonDTO> itemDTOList) {
        ChiTietHoaDonReplaceRequest request = new ChiTietHoaDonReplaceRequest(
                maDon,
                itemDTOList
        );

        MessageEnvelope response = connection.sendCommand(
                CommandAction.CHITIETHOADON_REPLACE_BY_MA_DON.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể cập nhật chi tiết hóa đơn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
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
