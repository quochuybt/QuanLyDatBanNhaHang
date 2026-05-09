package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.Collections;
import java.util.List;

/**
 * Skeleton remote service cho quản lý danh mục món.
 */
public class DanhMucMonRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public DanhMucMonRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<DanhMucMonDTO> findAll() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DANHMUCMON_GET_ALL.name(),
                Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải danh sách danh mục món.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<DanhMucMonDTO>>() {});
    }

    public boolean add(DanhMucMonDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DANHMUCMON_ADD.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể thêm danh mục món.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean update(DanhMucMonDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DANHMUCMON_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật danh mục món.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean delete(String maDM) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DANHMUCMON_DELETE.name(),
                IdRequest.builder().id(maDM).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể xóa danh mục món.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }
}
