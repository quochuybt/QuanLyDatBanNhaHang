package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.dondatmon.DonDatMonTimeRangeRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

import java.util.List;

public class DonDatMonGetBanDaDatTrongKhoangHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            DonDatMonTimeRangeRequest payload = parsePayload(request, DonDatMonTimeRangeRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getTuGio(), "Thời gian bắt đầu không được để trống.");
            requireNotNull(payload.getDenGio(), "Thời gian kết thúc không được để trống.");
            requireTrue(!payload.getTuGio().isAfter(payload.getDenGio()), "Khoảng thời gian không hợp lệ.");

            List<String> result = donDatMonService.getMaBanDaDatTrongKhoang(
                    payload.getTuGio(),
                    payload.getDenGio()
            );

            System.out.println("[SocketServer] DONDATMON_GET_BAN_DA_DAT_TRONG_KHOANG thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi kiểm tra bàn đã đặt trong khoảng giờ.");
    }
}