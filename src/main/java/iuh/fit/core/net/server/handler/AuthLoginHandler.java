package iuh.fit.core.net.server.handler;

import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.net.dto.auth.LoginRequestDTO;
import iuh.fit.core.net.dto.auth.LoginResponseDTO;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.repository.NhanVienRepository;
import iuh.fit.core.service.TaiKhoanService;

public class AuthLoginHandler implements CommandHandler {
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final NhanVienRepository nhanVienRepository = new NhanVienRepository();
    private final SessionRegistry sessionRegistry;

    public AuthLoginHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        try {
            // 1) Parse payload login từ message envelope
            LoginRequestDTO dto = JsonCodec.fromJsonNode(request.getPayload(), LoginRequestDTO.class);

            // 2) Validate dữ liệu đầu vào
            if (dto == null || isBlank(dto.getTenTK()) || isBlank(dto.getMatKhau())) {
                return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                        "Tên đăng nhập và mật khẩu không được để trống");
            }

            // 3) Xác thực tài khoản qua service nghiệp vụ hiện có
            taiKhoanService.login(dto.getTenTK(), dto.getMatKhau());
            NhanVien nv = nhanVienRepository.findByTenTK(dto.getTenTK());

            // 4) Map dữ liệu trả về cho client
            LoginResponseDTO response = LoginResponseDTO.builder()
                    .tenTK(dto.getTenTK())
                    .maNV(nv != null ? nv.getManv() : dto.getTenTK())
                    .hoTen(nv != null ? nv.getHoten() : dto.getTenTK())
                    .vaiTro(nv != null && nv.getVaiTro() != null ? nv.getVaiTro().name() : "NHANVIEN")
                    .build();

            // 5) Bind session theo policy 1 user/1 phiên
            sessionRegistry.bindUser(dto.getTenTK(), session);
            return MessageEnvelope.responseOk(request.getMessageId(), JsonCodec.toJsonNode(response));

        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Đăng nhập thất bại";
            if (msg.toLowerCase().contains("khóa")) {
                return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.AUTH_LOCKED, msg);
            }
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.AUTH_INVALID, msg);
        } catch (Exception ex) {
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.SERVER_ERROR,
                    "Lỗi hệ thống khi xử lý đăng nhập");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
