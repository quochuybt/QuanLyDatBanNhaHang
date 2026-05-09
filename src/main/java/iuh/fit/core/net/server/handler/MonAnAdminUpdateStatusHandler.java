package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.MonAnService;

public class MonAnAdminUpdateStatusHandler extends BaseCommandHandler implements CommandHandler {
    private final MonAnService monAnService = new MonAnService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            MonAnDTO payload = parsePayload(request, MonAnDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaMonAn(), "Mã món không được để trống.");
            requireNotBlank(payload.getTrangThai(), "Trạng thái món không được để trống.");

            MonAnDTO old = monAnService.findByIdDTO(payload.getMaMonAn());
            if (old == null) {
                throw new IllegalArgumentException("Không tìm thấy món ăn để cập nhật trạng thái.");
            }
            old.setTrangThai(payload.getTrangThai());
            monAnService.updateFromDTO(old);
            return ok(request, true);
        }, "Lỗi server khi cập nhật trạng thái món ăn.");
    }
}
