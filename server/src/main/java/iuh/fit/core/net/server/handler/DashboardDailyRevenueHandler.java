package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.dashboard.DashboardRangeRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.HoaDonService;

public class DashboardDailyRevenueHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardDailyRevenueHandler.class);
    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            DashboardRangeRequestDTO dto = parsePayload(request, DashboardRangeRequestDTO.class);
            requireNotNull(dto, "Thiếu dữ liệu khoảng ngày thống kê");
            requireNotNull(dto.getStartDate(), "Ngày bắt đầu không được để trống");
            requireNotNull(dto.getEndDate(), "Ngày kết thúc không được để trống");
            requireTrue(!dto.getStartDate().isAfter(dto.getEndDate()), "Ngày bắt đầu không được sau ngày kết thúc");

            var result = hoaDonService.getDailyRevenue(dto.getStartDate(), dto.getEndDate());
            return ok(request, result);
        }, "Lỗi hệ thống khi tải doanh thu theo ngày");
    }
}
