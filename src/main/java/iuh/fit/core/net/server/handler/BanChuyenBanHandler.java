package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.net.dto.ban.BanActionRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.BanService;

public class BanChuyenBanHandler extends BaseCommandHandler implements CommandHandler {

    private final BanService banService = new BanService();
    private final SessionRegistry sessionRegistry;

    public BanChuyenBanHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            BanActionRequest payload = parsePayload(request, BanActionRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getBanCu(), "Bàn cũ không được để trống.");
            requireNotNull(payload.getBanMoi(), "Bàn mới không được để trống.");

            BanDTO banCu = payload.getBanCu();
            BanDTO banMoi = payload.getBanMoi();

            requireNotBlank(banCu.getMaBan(), "Mã bàn cũ không được để trống.");
            requireNotBlank(banMoi.getMaBan(), "Mã bàn mới không được để trống.");
            requireTrue(!banCu.getMaBan().equals(banMoi.getMaBan()), "Bàn cũ và bàn mới không được trùng nhau.");

            boolean success = banService.chuyenBan(banCu, banMoi);

            System.out.println("[SocketServer] BAN_CHUYEN_BAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", banCu=" + banCu.getMaBan()
                    + ", banMoi=" + banMoi.getMaBan());

            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.TABLE_STATUS_CHANGED,
                        request.getName(),
                        "BAN",
                        banMoi.getMaBan(),
                        "UPDATED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "banCu", banCu.getMaBan(),
                                "banMoi", banMoi.getMaBan()
                        )
                );
            }

            return ok(request, success);
        }, "Lỗi server khi chuyển bàn.");
    }
}
