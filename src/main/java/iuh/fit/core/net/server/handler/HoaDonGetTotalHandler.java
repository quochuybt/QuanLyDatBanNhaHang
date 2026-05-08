package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.hoadon.HoaDonTotalRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.HoaDonService;

import java.util.Map;

public class HoaDonGetTotalHandler extends BaseCommandHandler implements CommandHandler {
    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            HoaDonTotalRequestDTO dto = parsePayload(request, HoaDonTotalRequestDTO.class);
            requireNotNull(dto, "Thiếu dữ liệu bộ lọc hóa đơn");

            long total = hoaDonService.getTotalHoaDonCount(
                    dto.getTrangThai(),
                    dto.getKeyword(),
                    dto.getTuNgay(),
                    dto.getDenNgay()
            );

            return ok(request, Map.of("total", total));
        }, "Lỗi hệ thống khi đếm tổng hóa đơn");
    }
}
