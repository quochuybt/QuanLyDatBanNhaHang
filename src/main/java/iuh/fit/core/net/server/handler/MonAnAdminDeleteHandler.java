package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.MonAnService;

public class MonAnAdminDeleteHandler extends BaseCommandHandler implements CommandHandler {

    private final MonAnService monAnService = new MonAnService();
    private final SessionRegistry sessionRegistry;

    public MonAnAdminDeleteHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã món không được để trống.");

            monAnService.delete(payload.getId());
            sessionRegistry.broadcastBusinessEvent(
                    EventType.MENU_UPDATED, request.getName(),
                    "MONAN", payload.getId(), "DELETED",
                    session.getTenTK(), java.util.Map.of("action", "DELETE")
            );
            return ok(request, true);
        }, "Lỗi server khi xóa món ăn.");
    }
}
