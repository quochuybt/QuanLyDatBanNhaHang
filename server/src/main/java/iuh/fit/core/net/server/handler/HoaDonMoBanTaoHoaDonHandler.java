package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.dto.ban.MoBanRequest;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.HoaDonService;

public class HoaDonMoBanTaoHoaDonHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoaDonMoBanTaoHoaDonHandler.class);

    private final HoaDonService hoaDonService = new HoaDonService();
    private final SessionRegistry sessionRegistry;

    public HoaDonMoBanTaoHoaDonHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            MoBanRequest payload = parsePayload(request, MoBanRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaBan(), "Mã bàn không được để trống.");
            requireNotBlank(payload.getMaNV(), "Mã nhân viên không được để trống.");

            HoaDonDTO result = hoaDonService.moBanVaTaoHoaDon(
                    payload.getMaBan().trim(),
                    payload.getMaNV().trim(),
                    payload.getMaKH(),
                    payload.getThoiGianDen(),
                    payload.getGhiChu()
            );

            LOGGER.info("[SocketServer] HOADON_MO_BAN_TAO_HOA_DON thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maBan=" + payload.getMaBan()
                    + ", maNV=" + payload.getMaNV());

            if (result != null && result.getMaHD() != null) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.INVOICE_UPDATED,
                        request.getName(),
                        "HOADON",
                        result.getMaHD(),
                        "CREATED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "maHD", result.getMaHD(),
                                "maBan", payload.getMaBan()
                        )
                );
                sessionRegistry.broadcastBusinessEvent(
                        EventType.TABLE_STATUS_CHANGED,
                        request.getName(),
                        "BAN",
                        payload.getMaBan(),
                        "STATUS_CHANGED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "maBan", payload.getMaBan()
                        )
                );
            }

            return ok(request, result);
        }, "Lỗi server khi mở bàn và tạo hóa đơn.");
    }
}
