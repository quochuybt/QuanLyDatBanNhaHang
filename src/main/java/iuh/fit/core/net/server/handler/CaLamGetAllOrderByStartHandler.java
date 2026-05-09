package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.CaLamService;

public class CaLamGetAllOrderByStartHandler extends BaseCommandHandler implements CommandHandler {
    private final CaLamService caLamService = new CaLamService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request,
                () -> ok(request, caLamService.getAllCaLamOrderByGioBatDau()),
                "Lỗi server khi tải danh sách ca làm.");
    }
}
