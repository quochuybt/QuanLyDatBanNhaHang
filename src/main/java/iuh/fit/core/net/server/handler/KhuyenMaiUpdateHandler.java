package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiUpdateHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhuyenMaiUpdateHandler.class);
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            KhuyenMaiDTO payload = parsePayload(request, KhuyenMaiDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaKM(), "Mã khuyến mãi không được để trống.");

            khuyenMaiService.updateFromDTO(payload);
            return ok(request, true);
        }, "Lỗi server khi cập nhật khuyến mãi.");
    }
}
