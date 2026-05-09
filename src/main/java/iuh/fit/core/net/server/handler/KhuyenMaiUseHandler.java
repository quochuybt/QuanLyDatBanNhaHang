package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiUseHandler extends BaseCommandHandler implements CommandHandler {
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã khuyến mãi không được để trống.");

            var km = khuyenMaiService.useKhuyenMai(payload.getId());
            return ok(request, KhuyenMaiDTO.fromEntity(km));
        }, "Lỗi server khi sử dụng khuyến mãi.");
    }
}
