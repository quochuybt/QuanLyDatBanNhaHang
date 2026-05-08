package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.net.dto.dashboard.DashboardRangeRequestDTO;
import iuh.fit.core.net.dto.dashboard.TopItemsRequestDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.time.LocalDate;
import java.util.Map;

public class DashboardRemoteService extends BaseRemoteService {
    private final SocketClientConnection connection;

    public DashboardRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        DashboardRangeRequestDTO req = DashboardRangeRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        MessageEnvelope response = connection.sendCommand(CommandAction.DASHBOARD_DAILY_REVENUE.name(), req, 7000);
        ensureSuccess(response, "Lỗi tải doanh thu theo ngày");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<LocalDate, Double>>() {});
    }

    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        TopItemsRequestDTO req = TopItemsRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();
        MessageEnvelope response = connection.sendCommand(CommandAction.DASHBOARD_TOP_SELLING.name(), req, 7000);
        ensureSuccess(response, "Lỗi tải top món bán chạy");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<String, Integer>>() {});
    }

    public Map<String, Integer> getLeastSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        TopItemsRequestDTO req = TopItemsRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();
        MessageEnvelope response = connection.sendCommand(CommandAction.DASHBOARD_LEAST_SELLING.name(), req, 7000);
        ensureSuccess(response, "Lỗi tải top món bán chậm");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<String, Integer>>() {});
    }

    public Map<String, Integer> getTableStatusCounts() {
        MessageEnvelope response = connection.sendCommand(CommandAction.DASHBOARD_TABLE_STATUS_COUNTS.name(), Map.of(), 7000);
        ensureSuccess(response, "Lỗi tải trạng thái bàn");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<String, Integer>>() {});
    }
}
