package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonSaveHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DonDatMonSaveHandler.class);

    private final DonDatMonService donDatMonService = new DonDatMonService();

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

            LOGGER.info("[SocketServer] DONDATMON_SAVE thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maBan=" + payload.getMaBan()
                    + ", maKH=" + payload.getMaKH());

            return ok(request, true);
        }, "Lỗi server khi lưu đơn đặt bàn.");
    }
}