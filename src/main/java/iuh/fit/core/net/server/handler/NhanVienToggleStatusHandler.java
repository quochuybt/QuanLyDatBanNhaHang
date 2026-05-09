package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.common.ToggleStatusRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.TaiKhoanService;

public class NhanVienToggleStatusHandler extends BaseCommandHandler implements CommandHandler {

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final SessionRegistry sessionRegistry;

    public NhanVienToggleStatusHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            ToggleStatusRequest payload = parsePayload(request, ToggleStatusRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getKey(), "Khóa tài khoản không được để trống.");
            taiKhoanService.toggleStatus(payload.getKey());
            sessionRegistry.broadcastBusinessEvent(
                    EventType.NHANVIEN_UPDATED, request.getName(),
                    "TAIKHOAN", payload.getKey(), "STATUS_CHANGED",
                    session.getTenTK(), java.util.Map.of("action", "TOGGLE_STATUS")
            );
            return ok(request, true);
        }, "Lỗi server khi đổi trạng thái tài khoản nhân viên.");
    }
}
