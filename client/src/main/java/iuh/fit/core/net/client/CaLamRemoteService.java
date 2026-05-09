package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.CaLamDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.Collections;
import java.util.List;

public class CaLamRemoteService extends BaseRemoteService {
    private static final long DEFAULT_TIMEOUT_MS = 8000;
    private final SocketClientConnection connection;

    public CaLamRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<CaLamDTO> getAllCaLamOrderByGioBatDau() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.CALAM_GET_ALL_ORDER_BY_START.name(),
                Collections.emptyMap(),
                DEFAULT_TIMEOUT_MS
        );
        ensureSuccess(response, "Không thể tải danh sách ca làm.");
        return JsonCodec.convertValue(response.getPayload(), new TypeReference<List<CaLamDTO>>() {});
    }
}
