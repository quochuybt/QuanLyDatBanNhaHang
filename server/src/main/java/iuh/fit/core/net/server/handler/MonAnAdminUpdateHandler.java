package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.MonAnService;

public class MonAnAdminUpdateHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonAnAdminUpdateHandler.class);
    private final MonAnService monAnService = new MonAnService();
    private final SessionRegistry sessionRegistry;

    public MonAnAdminUpdateHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            MonAnDTO payload = parsePayload(request, MonAnDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaMonAn(), "Mã món không được để trống.");
            monAnService.updateFromDTO(payload);
            sessionRegistry.broadcastBusinessEvent(
                    EventType.MENU_UPDATED, request.getName(),
                    "MONAN", payload.getMaMonAn(), "UPDATED",
                    session.getTenTK(), java.util.Map.of("action", "UPDATE")
            );
            return ok(request, true);
        }, "Lỗi server khi cập nhật món ăn.");
    }
}
