package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.HoaDonService;

public class HoaDonThanhToanHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoaDonThanhToanHandler.class);
    private final HoaDonService hoaDonService = new HoaDonService();
    private final SessionRegistry sessionRegistry;

    public HoaDonThanhToanHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            HoaDonDTO dto = parsePayload(request, HoaDonDTO.class);
            boolean result = hoaDonService.thanhToanHoaDon(dto);
            if (result) {
                sessionRegistry.broadcastBusinessEvent(EventType.INVOICE_UPDATED, "Hóa đơn " + dto.getMaHD() + " đã thanh toán");
            }
            return ok(request, result);
        }, "Lỗi thanh toán hóa đơn");
    }
}
