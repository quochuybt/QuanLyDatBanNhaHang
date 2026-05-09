package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.PhanCongDTO;
import iuh.fit.core.net.dto.common.DateRangeRequest;
import iuh.fit.core.net.dto.phancong.PhanCongRequestDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.time.LocalDate;
import java.util.List;

/**
 * Skeleton remote service cho màn lịch làm việc.
 */
public class LichLamViecRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public LichLamViecRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<PhanCongDTO> getPhanCongTheoNgay(LocalDate ngay) {
        PhanCongRequestDTO request = new PhanCongRequestDTO(null, null, ngay);
        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_LIST_BY_DATE.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải phân công theo ngày.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<PhanCongDTO>>() {});
    }

    public List<PhanCongDTO> getPhanCongTheoKhoangNgay(LocalDate fromDate, LocalDate toDate) {
        DateRangeRequest request = DateRangeRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_GET_BY_DATE_RANGE.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải phân công theo khoảng ngày.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<PhanCongDTO>>() {});
    }
}
