package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiAddHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhuyenMaiAddHandler.class);
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
    private final SessionRegistry sessionRegistry;

    public KhuyenMaiAddHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            KhuyenMaiDTO payload = parsePayload(request, KhuyenMaiDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaKM(), "Mã khuyến mãi không được để trống.");
            requireNotBlank(payload.getTenChuongTrinh(), "Tên chương trình không được để trống.");
            khuyenMaiService.addFromDTO(payload);
            sessionRegistry.broadcastBusinessEvent(
                    EventType.KHUYENMAI_UPDATED, request.getName(),
                    "KHUYENMAI", payload.getMaKM(), "CREATED",
                    session.getTenTK(), java.util.Map.of("action", "ADD")
            );
            return ok(request, true);
        }, "Lỗi server khi thêm khuyến mãi.");
    }
}
