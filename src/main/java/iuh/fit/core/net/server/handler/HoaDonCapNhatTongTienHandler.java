package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.HoaDonService;

public class HoaDonCapNhatTongTienHandler extends BaseCommandHandler implements CommandHandler {

    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            HoaDonDTO payload = parsePayload(request, HoaDonDTO.class);

            requireNotNull(payload, "Dữ liệu hóa đơn không hợp lệ.");
            requireNotBlank(payload.getMaHD(), "Mã hóa đơn không được để trống.");

            boolean success = hoaDonService.capNhatTongTien(payload);

            System.out.println("[SocketServer] HOADON_CAP_NHAT_TONG_TIEN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maHD=" + payload.getMaHD());

            return ok(request, success);
        }, "Lỗi server khi cập nhật tổng tiền hóa đơn.");
    }
}