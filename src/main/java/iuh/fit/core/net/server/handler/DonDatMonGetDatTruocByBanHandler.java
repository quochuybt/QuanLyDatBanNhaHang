package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.dto.ban.MaBanRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonGetDatTruocByBanHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            MaBanRequest payload = parsePayload(request, MaBanRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaBan(), "Mã bàn không được để trống.");

            DonDatMonDTO result = donDatMonService.getDonDatMonDatTruoc(payload.getMaBan().trim());

            System.out.println("[SocketServer] DONDATMON_GET_DAT_TRUOC_BY_BAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maBan=" + payload.getMaBan());

            return ok(request, result);
        }, "Lỗi server khi tải đơn đặt trước theo bàn.");
    }
}