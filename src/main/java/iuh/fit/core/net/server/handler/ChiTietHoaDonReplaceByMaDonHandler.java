package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.dto.chitiethoadon.ChiTietHoaDonReplaceRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.ChiTietHoaDonService;

import java.util.List;

public class ChiTietHoaDonReplaceByMaDonHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChiTietHoaDonReplaceByMaDonHandler.class);

    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            ChiTietHoaDonReplaceRequest payload = parsePayload(
                    request,
                    ChiTietHoaDonReplaceRequest.class
            );

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaDon(), "Mã đơn không được để trống.");
            requireNotNull(payload.getItems(), "Danh sách chi tiết hóa đơn không được null.");

            List<ChiTietHoaDonDTO> items = payload.getItems();

            for (ChiTietHoaDonDTO item : items) {
                requireNotNull(item, "Chi tiết hóa đơn không được null.");
                requireNotBlank(item.getMaMonAn(), "Mã món ăn không được để trống.");
                requireTrue(item.getSoLuong() > 0, "Số lượng món phải lớn hơn 0.");
                requireTrue(item.getDonGia() >= 0, "Đơn giá không được âm.");
            }

            ChiTietHoaDonDTO donDTO = ChiTietHoaDonDTO.builder()
                    .maDon(payload.getMaDon().trim())
                    .build();

            chiTietHoaDonService.replaceByMaDon(donDTO, items);

            LOGGER.info("[SocketServer] CHITIETHOADON_REPLACE_BY_MA_DON thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maDon=" + payload.getMaDon()
                    + ", totalItems=" + items.size());

            return ok(request, true);
        }, "Lỗi server khi cập nhật chi tiết hóa đơn.");
    }
}