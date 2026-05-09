package iuh.fit.core.net.client;

import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.MessageEnvelope;

/**
 * Base cho các RemoteService phía client để gom xử lý lỗi chung,
 * tránh lặp ensureSuccess ở từng service.
 */
public abstract class BaseRemoteService {

    protected void ensureSuccess(MessageEnvelope response, String defaultMessage) {
        if (response.isSuccess()) {
            return;
        }

        String code = response.getErrorCode();
        String msg = response.getMessage() != null ? response.getMessage() : defaultMessage;

        if (ErrorCode.BAD_REQUEST.name().equals(code)) {
            throw new IllegalArgumentException(msg);
        }
        if (ErrorCode.AUTH_LOCKED.name().equals(code)) {
            throw new IllegalArgumentException("Tài khoản đã bị khóa.");
        }
        if (ErrorCode.AUTH_INVALID.name().equals(code)) {
            throw new IllegalArgumentException("Sai tên tài khoản hoặc mật khẩu!");
        }
        if (ErrorCode.SERVER_UNREACHABLE.name().equals(code)) {
            throw new RuntimeException("Không thể kết nối server.");
        }

        throw new RuntimeException(msg);
    }
}
