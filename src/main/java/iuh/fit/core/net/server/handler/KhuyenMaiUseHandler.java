package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiUseHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhuyenMaiUseHandler.class);
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
    private final SessionRegistry sessionRegistry;

    public KhuyenMaiUseHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã khuyến mãi không được để trống.");

            var km = khuyenMaiService.useKhuyenMai(payload.getId());
            sessionRegistry.broadcastBusinessEvent(
                    EventType.KHUYENMAI_UPDATED, request.getName(),
                    "KHUYENMAI", payload.getId(), "USED",
                    session.getTenTK(), java.util.Map.of("action", "USE")
            );
            return ok(request, KhuyenMaiDTO.fromEntity(km));
        }, "Lỗi server khi sử dụng khuyến mãi.");
    }
}
