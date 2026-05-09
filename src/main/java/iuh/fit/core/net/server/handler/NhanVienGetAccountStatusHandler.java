package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.common.EmailByUsernameRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.NhanVienService;

public class NhanVienGetAccountStatusHandler extends BaseCommandHandler implements CommandHandler {
    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            EmailByUsernameRequest payload = parsePayload(request, EmailByUsernameRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getTenTK(), "Tên tài khoản không được để trống.");

            int status = nhanVienService.getAccountStatus(payload.getTenTK());
            return ok(request, status);
        }, "Lỗi server khi lấy trạng thái tài khoản nhân viên.");
    }
}
