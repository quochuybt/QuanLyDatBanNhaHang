package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.KhuyenMaiDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhuyenMaiService;

import java.util.List;

public class KhuyenMaiGetAllHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhuyenMaiGetAllHandler.class);
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<KhuyenMaiDTO> result = khuyenMaiService.findAllDTO();
            return ok(request, result);
        }, "Lỗi server khi tải danh sách khuyến mãi.");
    }
}
