package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.HoaDonService;

public class HoaDonCapNhatTongTienHandler extends BaseCommandHandler implements CommandHandler {

    private final HoaDonService hoaDonService = new HoaDonService();
    private final SessionRegistry sessionRegistry;

    public HoaDonCapNhatTongTienHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

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

            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.INVOICE_UPDATED,
                        request.getName(),
                        "HOADON",
                        payload.getMaHD(),
                        "UPDATED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "maHD", payload.getMaHD(),
                                "tongTien", payload.getTongTien(),
                                "tongThanhToan", payload.getTongThanhToan()
                        )
                );
            }

            return ok(request, success);
        }, "Lỗi server khi cập nhật tổng tiền hóa đơn.");
    }
}
