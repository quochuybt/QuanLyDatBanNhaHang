package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaGetActiveStaffHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiaoCaGetActiveStaffHandler.class);
    private final GiaoCaService giaoCaService = new GiaoCaService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request,
                () -> ok(request, giaoCaService.getNhanVienDangLamViecChiTiet()),
                "Lỗi server khi tải nhân viên đang làm việc.");
    }
}
