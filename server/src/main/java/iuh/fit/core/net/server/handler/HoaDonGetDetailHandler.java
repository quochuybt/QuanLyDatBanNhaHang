package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonDetailRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.ChiTietHoaDonService;

public class HoaDonGetDetailHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoaDonGetDetailHandler.class);
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            HoaDonDetailRequestDTO dto = parsePayload(request, HoaDonDetailRequestDTO.class);
            requireNotNull(dto, "Thiếu dữ liệu chi tiết hóa đơn");
            requireNotBlank(dto.getMaDon(), "Mã đơn không được để trống");

            ChiTietHoaDonDTO filter = ChiTietHoaDonDTO.builder().maDon(dto.getMaDon()).build();
            var result = chiTietHoaDonService.getChiTietTheoMaDon(filter);
            return ok(request, result);
        }, "Lỗi hệ thống khi tải chi tiết hóa đơn");
    }
}
