package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.nhanvien.NhanVienUpdateRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.NhanVienService;

public class NhanVienUpdateHandler extends BaseCommandHandler implements CommandHandler {

    private final NhanVienService nhanVienService = new NhanVienService();
    private final SessionRegistry sessionRegistry;

    public NhanVienUpdateHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            NhanVienUpdateRequest payload = parsePayload(request, NhanVienUpdateRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getNhanVien(), "Thông tin nhân viên không được để trống.");
            requireNotBlank(payload.getOldTenTK(), "Tên tài khoản cũ không được để trống.");
            requireNotBlank(payload.getNewTenTK(), "Tên tài khoản mới không được để trống.");
            nhanVienService.updateNhanVienAndAccount(
                    payload.getNhanVien(),
                    payload.getOldTenTK(),
                    payload.getNewTenTK(),
                    payload.getNewMatKhau() == null ? "" : payload.getNewMatKhau()
            );
            sessionRegistry.broadcastBusinessEvent(
                    EventType.NHANVIEN_UPDATED, request.getName(),
                    "NHANVIEN", payload.getNhanVien().getMaNV(), "UPDATED",
                    session.getTenTK(), java.util.Map.of("action", "UPDATE")
            );
            return ok(request, true);
        }, "Lỗi server khi cập nhật nhân viên.");
    }
}
