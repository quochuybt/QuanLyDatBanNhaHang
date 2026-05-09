package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.dto.ban.MaBanRequest;
import iuh.fit.core.net.dto.dondatmon.DonDatMonCancelRequest;
import iuh.fit.core.net.dto.dondatmon.DonDatMonSearchRequest;
import iuh.fit.core.net.dto.dondatmon.DonDatMonTimeRangeRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.time.LocalDateTime;
import java.util.List;

public class DonDatMonRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public DonDatMonRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<DonDatMonDTO> getAllDonDatMonChuaNhan() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_GET_ALL_CHUA_NHAN.name(),
                null,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải danh sách đặt trước.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<DonDatMonDTO>>() {}
        );
    }

    public List<DonDatMonDTO> timDonDatMonChuaNhan(String keyword) {
        DonDatMonSearchRequest request = new DonDatMonSearchRequest(keyword);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_SEARCH_CHUA_NHAN.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tìm kiếm phiếu đặt.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<DonDatMonDTO>>() {}
        );
    }

    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        DonDatMonTimeRangeRequest request = new DonDatMonTimeRangeRequest(tuGio, denGio);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_GET_BAN_DA_DAT_TRONG_KHOANG.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể kiểm tra bàn đã đặt trong khoảng giờ.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<String>>() {}
        );
    }

    public boolean save(DonDatMonDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_SAVE.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể lưu đơn đặt bàn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean huyDatBanVaGiaiPhongBanGhep(String maDon) {
        DonDatMonCancelRequest request = new DonDatMonCancelRequest(maDon);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_HUY_DAT_BAN.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể hủy đặt bàn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /*
     * ===== Các hàm bổ sung cho ManHinhGoiMonGUI =====
     */

    public DonDatMonDTO getDonDatMonDatTruoc(String maBan) {
        MaBanRequest request = new MaBanRequest(maBan);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_GET_DAT_TRUOC_BY_BAN.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải đơn đặt trước theo bàn.");

        if (response.getPayload() == null || response.getPayload().isNull()) {
            return null;
        }

        return JsonCodec.fromJsonNode(response.getPayload(), DonDatMonDTO.class);
    }

    public DonDatMonDTO getDonDatMonChuaNhanTheoMaBanBaoGomLinked(String maBan) {
        MaBanRequest request = new MaBanRequest(maBan);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_GET_CHUA_NHAN_THEO_BAN_BAO_GOM_LINKED.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải đơn chưa nhận theo bàn.");

        if (response.getPayload() == null || response.getPayload().isNull()) {
            return null;
        }

        return JsonCodec.fromJsonNode(response.getPayload(), DonDatMonDTO.class);
    }

    public List<DonDatMonDTO> getAllDonDatMonChuaNhanBaoGomLinked() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.DONDATMON_GET_ALL_CHUA_NHAN_BAO_GOM_LINKED.name(),
                null,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải danh sách đơn chưa nhận bao gồm linked.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<DonDatMonDTO>>() {}
        );
    }
}