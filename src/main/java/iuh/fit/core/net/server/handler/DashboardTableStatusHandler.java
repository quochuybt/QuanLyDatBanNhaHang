package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.BanService;

public class DashboardTableStatusHandler extends BaseCommandHandler implements CommandHandler {
    private final BanService banService = new BanService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            var result = banService.getTableStatusCounts();
            return ok(request, result);
        }, "Lỗi hệ thống khi tải trạng thái bàn");
    }
}
