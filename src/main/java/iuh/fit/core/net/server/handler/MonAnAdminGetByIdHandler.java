package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.MonAnService;

public class MonAnAdminGetByIdHandler extends BaseCommandHandler implements CommandHandler {
    private final MonAnService monAnService = new MonAnService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã món không được để trống.");

            MonAnDTO dto = monAnService.findByIdDTO(payload.getId());
            if (dto == null) {
                throw new IllegalArgumentException("Không tìm thấy món ăn theo mã.");
            }
            return ok(request, dto);
        }, "Lỗi server khi tải món ăn theo mã.");
    }
}
