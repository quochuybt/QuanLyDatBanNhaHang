package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.MonAnService;

import java.util.List;

public class MonAnAdminGetAllHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonAnAdminGetAllHandler.class);
    private final MonAnService monAnService = new MonAnService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<MonAnDTO> result = monAnService.findAllDTO();
            return ok(request, result);
        }, "Lỗi server khi tải danh sách món ăn.");
    }
}
