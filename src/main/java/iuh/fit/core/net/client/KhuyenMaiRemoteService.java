package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.Collections;
import java.util.List;

/**
 * Skeleton remote service cho màn khuyến mãi.
 */
public class KhuyenMaiRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public KhuyenMaiRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<KhuyenMaiDTO> findAll() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHUYENMAI_GET_ALL.name(),
                Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải danh sách khuyến mãi.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<KhuyenMaiDTO>>() {});
    }

    public KhuyenMaiDTO findById(String maKM) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHUYENMAI_GET_BY_ID.name(),
                IdRequest.builder().id(maKM).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải thông tin khuyến mãi.");
        return JsonCodec.fromJsonNode(response.getPayload(), KhuyenMaiDTO.class);
    }

    public boolean add(KhuyenMaiDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHUYENMAI_ADD.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể thêm khuyến mãi.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean update(KhuyenMaiDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHUYENMAI_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật khuyến mãi.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean delete(String maKM) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.KHUYENMAI_DELETE.name(),
                IdRequest.builder().id(maKM).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể xóa khuyến mãi.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }
}
