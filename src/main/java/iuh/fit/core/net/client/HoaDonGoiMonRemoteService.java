package iuh.fit.core.net.client;

import iuh.fit.core.dto.HoaDonDTO;

import iuh.fit.core.net.dto.ban.MaBanRequest;
import iuh.fit.core.net.dto.ban.MoBanRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.time.LocalDateTime;

public class HoaDonGoiMonRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public HoaDonGoiMonRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public HoaDonDTO getHoaDonChuaThanhToan(String maBan) {
        MaBanRequest request = new MaBanRequest(maBan);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.HOADON_GET_CHUA_THANH_TOAN_BY_BAN.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải hóa đơn chưa thanh toán.");

        if (response.getPayload() == null || response.getPayload().isNull()) {
            return null;
        }

        return JsonCodec.fromJsonNode(response.getPayload(), HoaDonDTO.class);
    }

    public HoaDonDTO moBanVaTaoHoaDon(
            String maBan,
            String maNV,
            String maKH,
            LocalDateTime thoiGianDen,
            String ghiChu
    ) {
        MoBanRequest request = new MoBanRequest(
                maBan,
                maNV,
                maKH,
                thoiGianDen,
                ghiChu
        );

        MessageEnvelope response = connection.sendCommand(
                CommandAction.HOADON_MO_BAN_TAO_HOA_DON.name(),
                request,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể mở bàn và tạo hóa đơn.");

        if (response.getPayload() == null || response.getPayload().isNull()) {
            return null;
        }

        return JsonCodec.fromJsonNode(response.getPayload(), HoaDonDTO.class);
    }

    public boolean capNhatTongTien(HoaDonDTO dto) {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.HOADON_CAP_NHAT_TONG_TIEN.name(),
                dto,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể cập nhật tổng tiền hóa đơn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}