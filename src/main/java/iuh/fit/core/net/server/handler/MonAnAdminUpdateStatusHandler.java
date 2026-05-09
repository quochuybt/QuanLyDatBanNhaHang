package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.entity.MonAn;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.MonAnService;

public class MonAnAdminUpdateStatusHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonAnAdminUpdateStatusHandler.class);
    private final MonAnService monAnService = new MonAnService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            MonAnDTO payload = parsePayload(request, MonAnDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaMonAn(), "Mã món không được để trống.");
            requireNotBlank(payload.getTrangThai(), "Trạng thái món không được để trống.");

            MonAn monAn = monAnService.findById(payload.getMaMonAn());
            if (monAn == null) {
                throw new IllegalArgumentException("Không tìm thấy món ăn để cập nhật trạng thái.");
            }

            monAn.setTrangThai(payload.getTrangThai());
            monAnService.update(monAn);
            return ok(request, true);
        }, "Lỗi server khi cập nhật trạng thái món ăn.");
    }
}
