package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.ChiTietHoaDonService;

public class ChiTietHoaDonAddHandler extends BaseCommandHandler implements CommandHandler {
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final SessionRegistry sessionRegistry;

    public ChiTietHoaDonAddHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            ChiTietHoaDonDTO dto = parsePayload(request, ChiTietHoaDonDTO.class);
            boolean result = chiTietHoaDonService.themChiTiet(dto);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Thêm món vào đơn " + dto.getMaDon());
            }
            return ok(request, result);
        }, "Lỗi thêm chi tiết hóa đơn");
    }
}
