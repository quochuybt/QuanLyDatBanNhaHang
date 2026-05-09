package iuh.fit.core.net.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.HoaDonService;

public class HoaDonCapNhatMaKHHandler extends BaseCommandHandler implements CommandHandler {
    private final HoaDonService hoaDonService = new HoaDonService();
    private final SessionRegistry sessionRegistry;

    public HoaDonCapNhatMaKHHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            JsonNode payload = request.getPayload();
            String maHD = payload.get("maHD").asText();
            String maKH = payload.get("maKH").isNull() ? null : payload.get("maKH").asText();

            boolean result = hoaDonService.capNhatMaKH(maHD, maKH);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Cập nhật khách hàng cho HD " + maHD);
            }
            return ok(request, result);
        }, "Lỗi cập nhật mã khách hàng");
    }
}
