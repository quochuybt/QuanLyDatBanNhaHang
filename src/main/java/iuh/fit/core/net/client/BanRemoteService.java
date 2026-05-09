package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.net.dto.ban.BanActionRequest;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.List;

public class BanRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public BanRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<BanDTO> getAllBan() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.BAN_GET_ALL.name(),
                null,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể tải danh sách bàn.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<BanDTO>>() {
                });
    }

    public boolean updateBan(BanDTO ban) {
        BanActionRequest request = new BanActionRequest();
        request.setBan(ban);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.BAN_UPDATE_STATUS.name(),
                request,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể cập nhật trạng thái bàn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean chuyenBan(BanDTO banCu, BanDTO banMoi) {
        BanActionRequest request = new BanActionRequest();
        request.setBanCu(banCu);
        request.setBanMoi(banMoi);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.BAN_CHUYEN_BAN.name(),
                request,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể chuyển bàn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    public boolean ghepBanLienKet(List<BanDTO> dsBanNguon, BanDTO banDich) {
        BanActionRequest request = new BanActionRequest();
        request.setDsBanNguon(dsBanNguon);
        request.setBanDich(banDich);

        MessageEnvelope response = connection.sendCommand(
                CommandAction.BAN_GHEP_BAN.name(),
                request,
                DEFAULT_TIMEOUT_MS);

        ensureSuccess(response, "Không thể ghép bàn.");

        Boolean result = JsonCodec.fromJsonNode(response.getPayload(), Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}