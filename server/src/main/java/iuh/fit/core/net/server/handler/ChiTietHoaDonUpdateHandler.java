package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.ChiTietHoaDonService;

public class ChiTietHoaDonUpdateHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChiTietHoaDonUpdateHandler.class);
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final SessionRegistry sessionRegistry;

    public ChiTietHoaDonUpdateHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            ChiTietHoaDonDTO dto = parsePayload(request, ChiTietHoaDonDTO.class);
            boolean result = chiTietHoaDonService.suaChiTiet(dto);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Cập nhật số lượng món đơn " + dto.getMaDon());
            }
            return ok(request, result);
        }, "Lỗi cập nhật chi tiết hóa đơn");
    }
}
