package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonGetAllHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DanhMucMonGetAllHandler.class);
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> ok(request, danhMucMonService.getAllDanhMuc()),
                "Lỗi server khi tải danh sách danh mục món.");
    }
}
