package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.net.dto.ban.BanActionRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.BanService;

public class BanUpdateStatusHandler extends BaseCommandHandler implements CommandHandler {

    private final BanService banService = new BanService();
    private final SessionRegistry sessionRegistry;

    public BanUpdateStatusHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            BanActionRequest payload = parsePayload(request, BanActionRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getBan(), "Thông tin bàn không được để trống.");

            BanDTO ban = payload.getBan();

            requireNotBlank(ban.getMaBan(), "Mã bàn không được để trống.");
            requireNotBlank(ban.getTenBan(), "Tên bàn không được để trống.");
            requireTrue(ban.getSoGhe() > 0, "Số ghế phải lớn hơn 0.");
            requireNotNull(ban.getTrangThai(), "Trạng thái bàn không được để trống.");
            requireNotBlank(ban.getKhuVuc(), "Khu vực không được để trống.");

            boolean success = banService.updateBan(ban);

            System.out.println("[SocketServer] BAN_UPDATE_STATUS thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maBan=" + ban.getMaBan()
                    + ", trangThai=" + ban.getTrangThai());

            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.TABLE_STATUS_CHANGED,
                        request.getName(),
                        "BAN",
                        ban.getMaBan(),
                        "STATUS_CHANGED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "maBan", ban.getMaBan(),
                                "trangThaiMoi", String.valueOf(ban.getTrangThai())
                        )
                );
            }

            return ok(request, success);
        }, "Lỗi server khi cập nhật trạng thái bàn.");
    }
}
