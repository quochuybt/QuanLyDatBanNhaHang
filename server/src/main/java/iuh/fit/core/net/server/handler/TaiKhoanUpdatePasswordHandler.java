package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.common.UpdatePasswordRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.base.BaseCommandHandler;
import iuh.fit.core.net.server.base.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.TaiKhoanService;

public class TaiKhoanUpdatePasswordHandler extends BaseCommandHandler implements CommandHandler {

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            UpdatePasswordRequest payload = parsePayload(request, UpdatePasswordRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getTenTK(), "Tên tài khoản không được để trống.");
            requireNotBlank(payload.getNewPassword(), "Mật khẩu mới không được để trống.");

            boolean result = taiKhoanService.updatePassword(payload.getTenTK(), payload.getNewPassword());
            return ok(request, result);
        }, "Lỗi server khi cập nhật mật khẩu.");
    }
}
