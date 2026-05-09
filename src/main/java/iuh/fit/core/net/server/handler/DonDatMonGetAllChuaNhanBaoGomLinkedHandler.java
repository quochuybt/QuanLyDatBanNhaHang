package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

import java.util.List;

public class DonDatMonGetAllChuaNhanBaoGomLinkedHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonDatMonGetAllChuaNhanBaoGomLinkedHandler.class);

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<DonDatMonDTO> result = donDatMonService.getAllDonDatMonChuaNhanBaoGomLinked();

            LOGGER.info("[SocketServer] DONDATMON_GET_ALL_CHUA_NHAN_BAO_GOM_LINKED thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi tải danh sách đơn chưa nhận bao gồm linked.");
    }
}