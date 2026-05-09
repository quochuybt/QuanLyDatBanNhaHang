package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.ChiTietHoaDonService;

public class ChiTietHoaDonDeleteHandler extends BaseCommandHandler implements CommandHandler {
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final SessionRegistry sessionRegistry;

    public ChiTietHoaDonDeleteHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            ChiTietHoaDonDTO dto = parsePayload(request, ChiTietHoaDonDTO.class);
            boolean result = chiTietHoaDonService.xoaChiTiet(dto);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Xóa món khỏi đơn " + dto.getMaDon());
            }
            return ok(request, result);
        }, "Lỗi xóa chi tiết hóa đơn");
    }
}
