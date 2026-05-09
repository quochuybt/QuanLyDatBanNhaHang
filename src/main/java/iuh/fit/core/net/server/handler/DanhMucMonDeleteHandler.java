package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonDeleteHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DanhMucMonDeleteHandler.class);
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();
    private final SessionRegistry sessionRegistry;

    public DanhMucMonDeleteHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã danh mục không được để trống.");
            DanhMucMonDTO dto = DanhMucMonDTO.builder().madm(payload.getId()).build();
            boolean result = danhMucMonService.xoaDanhMuc(dto);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.MENU_UPDATED, request.getName(),
                        "DANHMUCMON", payload.getId(), "DELETED",
                        session.getTenTK(), java.util.Map.of("action", "DELETE")
                );
            }
            return ok(request, result);
        }, "Lỗi server khi xóa danh mục món.");
    }
}
