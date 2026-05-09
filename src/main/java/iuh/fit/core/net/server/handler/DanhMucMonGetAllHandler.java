package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonGetAllHandler extends BaseCommandHandler implements CommandHandler {
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> ok(request, danhMucMonService.getAllDanhMuc()),
                "Lỗi server khi tải danh sách danh mục món.");
    }
}
