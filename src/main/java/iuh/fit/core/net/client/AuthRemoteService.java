package iuh.fit.core.net.client;

import iuh.fit.core.net.dto.auth.LoginRequestDTO;
import iuh.fit.core.net.dto.auth.LoginResponseDTO;
import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

public class AuthRemoteService {
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

        // 3) Chuẩn hóa mapping lỗi server -> exception dễ hiển thị ở GUI
        if (!response.isSuccess()) {
            String code = response.getErrorCode();
            String msg = response.getMessage() != null ? response.getMessage() : "Đăng nhập thất bại";

            if (ErrorCode.AUTH_LOCKED.name().equals(code)) {
                throw new IllegalArgumentException("Tài khoản đã bị khóa.");
            }
            if (ErrorCode.AUTH_INVALID.name().equals(code)) {
                throw new IllegalArgumentException("Sai tên tài khoản hoặc mật khẩu!");
            }
            if (ErrorCode.SERVER_UNREACHABLE.name().equals(code)) {
                throw new RuntimeException("Không thể kết nối server.");
            }
            throw new RuntimeException(msg);
        }

        // 4) Map payload thành DTO kết quả login
        return JsonCodec.fromJsonNode(response.getPayload(), LoginResponseDTO.class);
    }
}
