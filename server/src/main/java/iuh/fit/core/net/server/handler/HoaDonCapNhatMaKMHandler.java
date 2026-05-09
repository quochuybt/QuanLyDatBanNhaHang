package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.databind.JsonNode;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.HoaDonService;

public class HoaDonCapNhatMaKMHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoaDonCapNhatMaKMHandler.class);
    private final HoaDonService hoaDonService = new HoaDonService();
    private final SessionRegistry sessionRegistry;

    public HoaDonCapNhatMaKMHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            JsonNode payload = request.getPayload();
            String maHD = payload.get("maHD").asText();
            String maKM = payload.get("maKM").isNull() ? null : payload.get("maKM").asText();

            boolean result = hoaDonService.capNhatMaKM(maHD, maKM);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Cập nhật mã khuyến mãi cho HD " + maHD);
            }
            return ok(request, result);
        }, "Lỗi cập nhật mã khuyến mãi");
    }
}
