package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.BanService;

import java.util.List;

public class BanGetAllHandler extends BaseCommandHandler implements CommandHandler {

    private final BanService banService = new BanService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<BanDTO> result = banService.getAllBan();

            System.out.println("[SocketServer] BAN_GET_ALL thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi tải danh sách bàn.");
    }
}