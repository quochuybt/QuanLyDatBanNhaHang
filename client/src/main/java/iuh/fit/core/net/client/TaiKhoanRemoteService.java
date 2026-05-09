package iuh.fit.core.net.client;

import iuh.fit.core.net.dto.common.UpdatePasswordRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

/**
 * Skeleton remote service cho tác vụ tài khoản.
 */
public class TaiKhoanRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public TaiKhoanRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public boolean updatePassword(String tenTK, String newPassword) {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .tenTK(tenTK)
                .newPassword(newPassword)
                .build();

        MessageEnvelope response = connection.sendCommand(
                CommandAction.TAIKHOAN_UPDATE_PASSWORD.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể cập nhật mật khẩu.");
        return Boolean.TRUE.equals(JsonCodec.fromJsonNode(response.getPayload(), Boolean.class));
    }
}
