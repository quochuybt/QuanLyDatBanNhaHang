package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.net.dto.khachhang.KhachHangSearchRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

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
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể thêm khách hàng.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean update(KhachHangDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHACHHANG_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể cập nhật khách hàng.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public List<KhachHangDTO> search(String keyword) {
        KhachHangSearchRequest request = new KhachHangSearchRequest(keyword);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHACHHANG_SEARCH.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tìm kiếm khách hàng.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<KhachHangDTO>>() {}
        );
    }

    public List<KhachHangDTO> findAll() {
        return search("");
    }
}