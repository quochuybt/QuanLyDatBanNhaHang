package iuh.fit.core.net.client;

import iuh.fit.core.dto.GiaoCaDTO;
import iuh.fit.core.net.dto.giaoca.GiaoCaDashboardRequest;
import iuh.fit.core.net.dto.giaoca.GiaoCaDashboardResponse;
import iuh.fit.core.net.dto.giaoca.GiaoCaEndRequest;
import iuh.fit.core.net.dto.giaoca.GiaoCaHistoryRequest;
import iuh.fit.core.net.dto.giaoca.GiaoCaStartRequest;
import iuh.fit.core.net.dto.giaoca.GiaoCaTopStaffRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GiaoCaRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public GiaoCaRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public GiaoCaDashboardResponse loadDashboard(String maNV) {
        GiaoCaDashboardRequest request = new GiaoCaDashboardRequest(maNV);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_DASHBOARD_LOAD.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải dashboard nhân viên.");

        return JsonCodec.fromJsonNode(response.getPayload(), GiaoCaDashboardResponse.class);
    }

    public boolean batDauCa(GiaoCaDTO dto) {
        GiaoCaStartRequest request = new GiaoCaStartRequest(
                dto.getMaNV(),
                dto.getThoiGianBatDau(),
                dto.getTienDauCa()
        );

        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_START.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể bắt đầu ca.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean ketThucCa(String maGiaoCa, double tienCuoiCa, String ghiChu) {
        GiaoCaEndRequest request = new GiaoCaEndRequest(maGiaoCa, tienCuoiCa, ghiChu);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_END.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể kết thúc ca.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public List<GiaoCaDTO> getLichSuGiaoCa(LocalDate fromDate, LocalDate toDate) {
        GiaoCaHistoryRequest request = GiaoCaHistoryRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_GET_LICH_SU.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải lịch sử giao ca.");

        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<GiaoCaDTO>>() {});
    }

    public List<String> getNhanVienDangLamViecChiTiet() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_GET_ACTIVE_STAFF.name(),
                Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải nhân viên đang làm việc.");

        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<String>>() {});
    }

    public Map<String, Double> getTopStaffByWorkHours(LocalDate startDate, LocalDate endDate, int limit) {
        GiaoCaTopStaffRequest request = GiaoCaTopStaffRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();

        MessageEnvelope response = connection.sendCommand(
                CommandAction.GIAOCA_GET_TOP_STAFF_HOURS.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải top nhân viên theo giờ làm.");

        return JsonCodec.convertValue(response.getPayload(), new TypeReference<Map<String, Double>>() {});
    }
}
