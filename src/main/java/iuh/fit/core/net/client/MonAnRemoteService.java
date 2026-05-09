package iuh.fit.core.net.client;

import com.fasterxml.jackson.core.type.TypeReference;
import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.List;

public class MonAnRemoteService extends BaseRemoteService {

    private static final long DEFAULT_TIMEOUT_MS = 8000;

    private final SocketClientConnection connection;

    public MonAnRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public List<MonAnDTO> findAll() {
        MessageEnvelope response = connection.sendCommand(
                CommandAction.MONAN_GET_ALL.name(),
                null,
                DEFAULT_TIMEOUT_MS
        );

        ensureSuccess(response, "Không thể tải danh sách món ăn.");

        return JsonCodec.convertValue(
                response.getPayload(),
                new TypeReference<List<MonAnDTO>>() {}
        );
    }

    public List<MonAnDTO> getAllMonAn() {
        return findAll();
    }
}