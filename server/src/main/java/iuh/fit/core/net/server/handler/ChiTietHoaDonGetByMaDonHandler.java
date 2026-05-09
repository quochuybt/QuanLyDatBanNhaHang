package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.dto.dondatmon.MaDonRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.ChiTietHoaDonService;

import java.util.List;

public class ChiTietHoaDonGetByMaDonHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChiTietHoaDonGetByMaDonHandler.class);

    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            MaDonRequest payload = parsePayload(request, MaDonRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaDon(), "Mã đơn không được để trống.");

            ChiTietHoaDonDTO filter = ChiTietHoaDonDTO.builder()
                    .maDon(payload.getMaDon().trim())
                    .build();

            List<ChiTietHoaDonDTO> result = chiTietHoaDonService.getChiTietTheoMaDon(filter);

            LOGGER.info("[SocketServer] CHITIETHOADON_GET_BY_MA_DON thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maDon=" + payload.getMaDon()
                    + ", total=" + (result != null ? result.size() : 0));

            return ok(request, result);
        }, "Lỗi server khi tải chi tiết hóa đơn.");
    }
}