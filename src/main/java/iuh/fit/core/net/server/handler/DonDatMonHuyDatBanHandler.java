package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.dondatmon.DonDatMonCancelRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonHuyDatBanHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();
    private final SessionRegistry sessionRegistry;

    public DonDatMonHuyDatBanHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");
            DonDatMonCancelRequest payload = parsePayload(request, DonDatMonCancelRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaDon(), "Mã đơn không được để trống.");
            boolean success = donDatMonService.huyDatBanVaGiaiPhongBanGhep(payload.getMaDon());
            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.DONDATMON_UPDATED, request.getName(),
                        "DONDATMON", payload.getMaDon(), "CANCELLED",
                        session.getTenTK(), java.util.Map.of("action", "HUY_DAT_BAN")
                );
                sessionRegistry.broadcastBusinessEvent(
                        EventType.TABLE_STATUS_CHANGED, request.getName(),
                        "BAN", payload.getMaDon(), "TRONG",
                        session.getTenTK(), java.util.Map.of("action", "HUY_DAT_BAN")
                );
            }
            return ok(request, success);
        }, "Lỗi server khi hủy đặt bàn.");
    }
}
