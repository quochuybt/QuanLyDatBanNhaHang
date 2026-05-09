package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.giaoca.GiaoCaEndRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaEndHandler extends BaseCommandHandler implements CommandHandler {

    private final GiaoCaService giaoCaService = new GiaoCaService();
    private final SessionRegistry sessionRegistry;

    public GiaoCaEndHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");
            GiaoCaEndRequest payload = parsePayload(request, GiaoCaEndRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaGiaoCa(), "Mã giao ca không được để trống.");
            requireTrue(payload.getTienCuoiCa() >= 0, "Tiền cuối ca không được âm.");
            int maGiaoCa;
            try {
                maGiaoCa = Integer.parseInt(payload.getMaGiaoCa().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Mã giao ca không hợp lệ.");
            }
            boolean success = giaoCaService.ketThucCa(maGiaoCa, payload.getTienCuoiCa(), payload.getGhiChu());
            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.GIAOCA_UPDATED, request.getName(),
                        "GIAOCA", payload.getMaGiaoCa(), "ENDED",
                        session.getTenTK(), java.util.Map.of("action", "END")
                );
            }
            return ok(request, success);
        }, "Lỗi server khi kết thúc ca.");
    }
}
