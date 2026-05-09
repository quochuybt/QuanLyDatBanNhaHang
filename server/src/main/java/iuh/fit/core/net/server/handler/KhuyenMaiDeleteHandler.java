package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiDeleteHandler extends BaseCommandHandler implements CommandHandler {

    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
    private final SessionRegistry sessionRegistry;

    public KhuyenMaiDeleteHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã khuyến mãi không được để trống.");
            khuyenMaiService.delete(payload.getId());
            sessionRegistry.broadcastBusinessEvent(
                    EventType.KHUYENMAI_UPDATED, request.getName(),
                    "KHUYENMAI", payload.getId(), "DELETED",
                    session.getTenTK(), java.util.Map.of("action", "DELETE")
            );
            return ok(request, true);
        }, "Lỗi server khi xóa khuyến mãi.");
    }
}
