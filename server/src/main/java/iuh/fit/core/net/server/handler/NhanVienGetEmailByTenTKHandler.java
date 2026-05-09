package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.common.EmailByUsernameRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.NhanVienService;

public class NhanVienGetEmailByTenTKHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NhanVienGetEmailByTenTKHandler.class);
    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            EmailByUsernameRequest payload = parsePayload(request, EmailByUsernameRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getTenTK(), "Tên tài khoản không được để trống.");

            String email = nhanVienService.getEmailByTenTK(payload.getTenTK());
            return ok(request, email);
        }, "Lỗi server khi lấy email theo tài khoản.");
    }
}
