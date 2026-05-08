package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.hoadon.HoaDonPageRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.HoaDonService;

public class HoaDonGetByPageHandler extends BaseCommandHandler implements CommandHandler {
    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            HoaDonPageRequestDTO dto = parsePayload(request, HoaDonPageRequestDTO.class);
            requireNotNull(dto, "Thiếu dữ liệu phân trang hóa đơn");
            requireTrue(dto.getPage() >= 1, "Trang phải lớn hơn hoặc bằng 1");
            requirePositive(dto.getItemsPerPage(), "Số lượng item mỗi trang phải lớn hơn 0");

            var result = hoaDonService.getHoaDonByPage(
                    dto.getPage(),
                    dto.getItemsPerPage(),
                    dto.getTrangThai(),
                    dto.getKeyword(),
                    dto.getTuNgay(),
                    dto.getDenNgay()
            );

            return ok(request, result);
        }, "Lỗi hệ thống khi tải danh sách hóa đơn");
    }
}
