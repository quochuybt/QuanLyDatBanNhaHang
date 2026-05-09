package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonSaveHandler extends BaseCommandHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();
    private final SessionRegistry sessionRegistry;

    public DonDatMonSaveHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");
            DonDatMonDTO payload = parsePayload(request, DonDatMonDTO.class);
            requireNotNull(payload, "Dữ liệu đơn đặt món không hợp lệ.");
            requireNotNull(payload.getNgayKhoiTao(), "Ngày khởi tạo không được để trống.");
            requireNotNull(payload.getThoiGianDen(), "Thời gian đến không được để trống.");
            requireNotBlank(payload.getTrangThai(), "Trạng thái không được để trống.");
            requireNotBlank(payload.getMaNV(), "Mã nhân viên không được để trống.");
            requireNotBlank(payload.getMaBan(), "Mã bàn không được để trống.");
            donDatMonService.save(payload);
            sessionRegistry.broadcastBusinessEvent(
                    EventType.DONDATMON_UPDATED, request.getName(),
                    "DONDATMON", payload.getMaDon(), "CREATED",
                    session.getTenTK(), java.util.Map.of("action", "SAVE", "maBan", payload.getMaBan())
            );
            return ok(request, true);
        }, "Lỗi server khi lưu đơn đặt bàn.");
    }
}
