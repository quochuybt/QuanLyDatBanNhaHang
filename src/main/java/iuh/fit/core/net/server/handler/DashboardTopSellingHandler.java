package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.dashboard.TopItemsRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.ChiTietHoaDonService;

public class DashboardTopSellingHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardTopSellingHandler.class);
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            TopItemsRequestDTO dto = parsePayload(request, TopItemsRequestDTO.class);
            requireNotNull(dto, "Thiếu dữ liệu top món bán chạy");
            requireNotNull(dto.getStartDate(), "Ngày bắt đầu không được để trống");
            requireNotNull(dto.getEndDate(), "Ngày kết thúc không được để trống");
            requireTrue(!dto.getStartDate().isAfter(dto.getEndDate()), "Ngày bắt đầu không được sau ngày kết thúc");
            requirePositive(dto.getLimit(), "Giới hạn top món phải lớn hơn 0");

            var result = chiTietHoaDonService.getTopSellingItems(dto.getStartDate(), dto.getEndDate(), dto.getLimit());
            return ok(request, result);
        }, "Lỗi hệ thống khi tải top món bán chạy");
    }
}
