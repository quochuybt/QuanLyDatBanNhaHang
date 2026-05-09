package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonAddHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DanhMucMonAddHandler.class);
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();
    private final SessionRegistry sessionRegistry;

    public DanhMucMonAddHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            DanhMucMonDTO payload = parsePayload(request, DanhMucMonDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getTendm(), "Tên danh mục không được để trống.");
            boolean result = danhMucMonService.themDanhMuc(payload);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.MENU_UPDATED, request.getName(),
                        "DANHMUCMON", payload.getMadm(), "CREATED",
                        session.getTenTK(), java.util.Map.of("action", "ADD", "tenDM", payload.getTendm())
                );
            }
            return ok(request, result);
        }, "Lỗi server khi thêm danh mục món.");
    }
}
