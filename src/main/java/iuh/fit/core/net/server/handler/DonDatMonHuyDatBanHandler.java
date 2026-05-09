package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.dondatmon.DonDatMonCancelRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonHuyDatBanHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            DonDatMonCancelRequest payload = parsePayload(request, DonDatMonCancelRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaDon(), "Mã đơn không được để trống.");

            boolean success = donDatMonService.huyDatBanVaGiaiPhongBanGhep(payload.getMaDon());

            System.out.println("[SocketServer] DONDATMON_HUY_DAT_BAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maDon=" + payload.getMaDon());

            return ok(request, success);
        }, "Lỗi server khi hủy đặt bàn.");
    }
}