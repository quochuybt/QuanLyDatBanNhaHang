package iuh.fit.core.net.server.handler;

import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.net.dto.nhanvien.NhanVienAddRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.NhanVienService;

public class NhanVienAddHandler extends BaseCommandHandler implements CommandHandler {

    private final NhanVienService nhanVienService = new NhanVienService();
    private final SessionRegistry sessionRegistry;

    public NhanVienAddHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            NhanVienAddRequest payload = parsePayload(request, NhanVienAddRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getNhanVien(), "Thông tin nhân viên không được để trống.");
            requireNotBlank(payload.getTenTK(), "Tên tài khoản không được để trống.");
            requireNotBlank(payload.getMatKhau(), "Mật khẩu không được để trống.");
            NhanVien nv = payload.getNhanVien().toEntity();
            nhanVienService.addNhanVien(nv, payload.getTenTK(), payload.getMatKhau());
            sessionRegistry.broadcastBusinessEvent(
                    EventType.NHANVIEN_UPDATED, request.getName(),
                    "NHANVIEN", nv.getManv(), "CREATED",
                    session.getTenTK(), java.util.Map.of("action", "ADD")
            );
            return ok(request, true);
        }, "Lỗi server khi thêm nhân viên.");
    }
}
