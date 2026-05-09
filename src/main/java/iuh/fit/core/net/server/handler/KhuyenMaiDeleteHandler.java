package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiDeleteHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhuyenMaiDeleteHandler.class);
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã khuyến mãi không được để trống.");

            khuyenMaiService.delete(payload.getId());
            return ok(request, true);
        }, "Lỗi server khi xóa khuyến mãi.");
    }
}
