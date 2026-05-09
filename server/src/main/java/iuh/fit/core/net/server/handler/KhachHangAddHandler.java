package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.KhachHangService;

public class KhachHangAddHandler extends BaseCommandHandler implements CommandHandler {

    private final KhachHangService khachHangService = new KhachHangService();
    private final SessionRegistry sessionRegistry;

    public KhachHangAddHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");
            KhachHangDTO payload = parsePayload(request, KhachHangDTO.class);
            requireNotNull(payload, "Dữ liệu khách hàng không hợp lệ.");
            requireNotBlank(payload.getTenKH(), "Tên khách hàng không được để trống.");
            requireNotBlank(payload.getSdt(), "Số điện thoại không được để trống.");
            requireNotNull(payload.getNgaySinh(), "Ngày sinh không được để trống.");
            khachHangService.addFromDTO(payload);
            sessionRegistry.broadcastBusinessEvent(
                    EventType.KHACHHANG_UPDATED, request.getName(),
                    "KHACHHANG", payload.getMaKH(), "CREATED",
                    session.getTenTK(), java.util.Map.of("action", "ADD")
            );
            return ok(request, true);
        }, "Lỗi server khi thêm khách hàng.");
    }
}
