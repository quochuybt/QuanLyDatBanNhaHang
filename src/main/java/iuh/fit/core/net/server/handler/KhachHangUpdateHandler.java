package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhachHangService;

public class KhachHangUpdateHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KhachHangUpdateHandler.class);

    private final KhachHangService khachHangService = new KhachHangService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            KhachHangDTO payload = parsePayload(request, KhachHangDTO.class);

            requireNotNull(payload, "Dữ liệu khách hàng không hợp lệ.");
            requireNotBlank(payload.getMaKH(), "Mã khách hàng không được để trống.");
            requireNotBlank(payload.getTenKH(), "Tên khách hàng không được để trống.");
            requireNotBlank(payload.getSdt(), "Số điện thoại không được để trống.");
            requireNotNull(payload.getNgaySinh(), "Ngày sinh không được để trống.");

            khachHangService.updateFromDTO(payload);

            LOGGER.info("[SocketServer] KHACHHANG_UPDATE thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maKH=" + payload.getMaKH());

            return ok(request, true);
        }, "Lỗi server khi cập nhật khách hàng.");
    }
}