package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.common.ToggleStatusRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.TaiKhoanService;

public class NhanVienToggleStatusHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NhanVienToggleStatusHandler.class);
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            ToggleStatusRequest payload = parsePayload(request, ToggleStatusRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getKey(), "Khóa tài khoản không được để trống.");

            taiKhoanService.toggleStatus(payload.getKey());
            return ok(request, true);
        }, "Lỗi server khi đổi trạng thái tài khoản nhân viên.");
    }
}
