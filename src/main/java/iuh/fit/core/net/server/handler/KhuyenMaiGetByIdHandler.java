package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhuyenMaiService;

public class KhuyenMaiGetByIdHandler extends BaseCommandHandler implements CommandHandler {
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã khuyến mãi không được để trống.");

            KhuyenMaiDTO dto = khuyenMaiService.findByIdDTO(payload.getId());
            if (dto == null) {
                throw new IllegalArgumentException("Không tìm thấy khuyến mãi theo mã.");
            }
            return ok(request, dto);
        }, "Lỗi server khi tải thông tin khuyến mãi.");
    }
}
