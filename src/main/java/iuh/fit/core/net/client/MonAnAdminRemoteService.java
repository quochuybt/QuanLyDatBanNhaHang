package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.List;

/**
 * Skeleton remote service cho CRUD món ăn phía màn admin.
 */
public class MonAnAdminRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public MonAnAdminRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<MonAnDTO> findAll() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_GET_ALL.name(),
                java.util.Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải danh sách món ăn.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<MonAnDTO>>() {});
    }

    public boolean add(MonAnDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_ADD.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể thêm món ăn.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean update(MonAnDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật món ăn.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean updateStatus(MonAnDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_UPDATE_STATUS.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật trạng thái món ăn.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public MonAnDTO findById(String maMonAn) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_GET_BY_ID.name(),
                IdRequest.builder().id(maMonAn).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải thông tin món ăn.");
        return JsonCodec.fromJsonNode(response.getPayload(), MonAnDTO.class);
    }

    public boolean delete(String maMonAn) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_ADMIN_DELETE.name(),
                IdRequest.builder().id(maMonAn).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể xóa món ăn.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }
}
