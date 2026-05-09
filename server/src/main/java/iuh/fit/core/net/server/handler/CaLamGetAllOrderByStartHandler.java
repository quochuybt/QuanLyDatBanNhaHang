package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.CaLamService;

public class CaLamGetAllOrderByStartHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaLamGetAllOrderByStartHandler.class);
    private final CaLamService caLamService = new CaLamService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request,
                () -> ok(request, caLamService.getAllCaLamOrderByGioBatDau()),
                "Lỗi server khi tải danh sách ca làm.");
    }
}
