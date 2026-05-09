package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.NhanVienDTO;
import iuh.fit.core.net.dto.common.EmailByUsernameRequest;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.dto.common.ToggleStatusRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.Collections;
import java.util.List;

/**
 * Skeleton remote service cho nhóm màn hình nhân viên.
 */
public class NhanVienRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public NhanVienRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<NhanVienDTO> findAll() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_GET_ALL.name(),
                Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải danh sách nhân viên.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<NhanVienDTO>>() {});
    }

    public NhanVienDTO findById(String maNV) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_GET_BY_ID.name(),
                IdRequest.builder().id(maNV).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải thông tin nhân viên.");
        return JsonCodec.fromJsonNode(response.getPayload(), NhanVienDTO.class);
    }

    public boolean add(NhanVienDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_ADD.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể thêm nhân viên.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean update(NhanVienDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_UPDATE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật nhân viên.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public boolean toggleStatus(String maNV) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_TOGGLE_STATUS.name(),
                ToggleStatusRequest.builder().key(maNV).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể đổi trạng thái nhân viên.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }

    public String getEmailByTenTK(String tenTK) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.NHANVIEN_GET_EMAIL_BY_TENTK.name(),
                EmailByUsernameRequest.builder().tenTK(tenTK).build(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể lấy email theo tài khoản.");
        return JsonCodec.fromJsonNode(response.getPayload(), String.class);
    }
}
