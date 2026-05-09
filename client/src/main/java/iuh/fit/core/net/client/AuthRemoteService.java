package iuh.fit.core.net.client;

import iuh.fit.core.net.dto.auth.LoginRequestDTO;
import iuh.fit.core.net.dto.auth.LoginResponseDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

public class AuthRemoteService extends BaseRemoteService {
    private final SocketClientConnection connection;

    public AuthRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public LoginResponseDTO login(String tenTK, String matKhau) {
        // 1) Đóng gói command payload
        LoginRequestDTO req = LoginRequestDTO.builder()
                .tenTK(tenTK)
                .matKhau(matKhau)
                .build();

        // 2) Gửi command AUTH_LOGIN qua socket
        MessageEnvelope response = connection.sendCommand(CommandAction.AUTH_LOGIN.name(), req, 5000);
        ensureSuccess(response, "Đăng nhập thất bại");

        // 4) Map payload thành DTO kết quả login
        return JsonCodec.fromJsonNode(response.getPayload(), LoginResponseDTO.class);
    }
}
