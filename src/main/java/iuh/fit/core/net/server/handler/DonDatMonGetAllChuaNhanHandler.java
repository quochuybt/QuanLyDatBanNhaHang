package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

import java.util.List;

public class DonDatMonGetAllChuaNhanHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<DonDatMonDTO> result = donDatMonService.getAllDonDatMonChuaNhan();

            System.out.println("[SocketServer] DONDATMON_GET_ALL_CHUA_NHAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi tải danh sách đặt trước.");
    }
}