package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.dto.dondatmon.DonDatMonSearchRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

import java.util.List;

public class DonDatMonSearchChuaNhanHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            String keyword = "";

            if (request.getPayload() != null && !request.getPayload().isNull()) {
                DonDatMonSearchRequest payload = parsePayload(request, DonDatMonSearchRequest.class);
                if (payload != null && payload.getKeyword() != null) {
                    keyword = payload.getKeyword().trim();
                }
            }

            List<DonDatMonDTO> result = donDatMonService.timDonDatMonChuaNhan(keyword);

            System.out.println("[SocketServer] DONDATMON_SEARCH_CHUA_NHAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", keyword=" + keyword
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi tìm kiếm phiếu đặt.");
    }
}