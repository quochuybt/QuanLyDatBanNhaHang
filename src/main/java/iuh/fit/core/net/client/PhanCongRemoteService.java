package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.PhanCongDTO;
import iuh.fit.core.net.dto.phancong.PhanCongDateRangeRequestDTO;
import iuh.fit.core.net.dto.phancong.PhanCongRequestDTO;
import iuh.fit.core.net.dto.phancong.PhanCongTongGioTheoThangRequestDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PhanCongRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public PhanCongRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public boolean themPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        PhanCongRequestDTO request = new PhanCongRequestDTO(maNV, maCa, ngayLam);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_ADD.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể thêm phân công ca.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean xoaPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        PhanCongRequestDTO request = new PhanCongRequestDTO(maNV, maCa, ngayLam);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_REMOVE.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể hủy phân công ca.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public List<PhanCongDTO> findByNgayLam(LocalDate ngayLam) {
        PhanCongRequestDTO request = new PhanCongRequestDTO(null, null, ngayLam);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_LIST_BY_DATE.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải danh sách phân công theo ngày.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<PhanCongDTO>>() {}
        );
    }

    public List<PhanCongDTO> findByDateRange(LocalDate tuNgay, LocalDate denNgay) {
        PhanCongDateRangeRequestDTO request = new PhanCongDateRangeRequestDTO(tuNgay, denNgay);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_GET_BY_DATE_RANGE.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải danh sách phân công theo khoảng ngày.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<PhanCongDTO>>() {}
        );
    }

    public Map<String, Double> getTongGioLamTheoThang(int thang, int nam) {
        PhanCongTongGioTheoThangRequestDTO request = new PhanCongTongGioTheoThangRequestDTO(thang, nam);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.PHANCONG_GET_TONG_GIO_THEO_THANG.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải tổng giờ làm theo tháng.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<Map<String, Double>>() {}
        );
    }
}
