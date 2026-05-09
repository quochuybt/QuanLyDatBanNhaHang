package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.net.dto.auth.LoginRequestDTO;
import iuh.fit.core.net.dto.auth.LoginResponseDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.repository.NhanVienRepository;
import iuh.fit.core.service.TaiKhoanService;

public class AuthLoginHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthLoginHandler.class);
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final NhanVienRepository nhanVienRepository = new NhanVienRepository();
    private final SessionRegistry sessionRegistry;

    public AuthLoginHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return executeAuth(request, () -> {
            // 1) Parse payload login từ message envelope
            LoginRequestDTO dto = parsePayload(request, LoginRequestDTO.class);

            // 2) Validate dữ liệu đầu vào
            if (dto == null || isBlank(dto.getTenTK()) || isBlank(dto.getMatKhau())) {
                return badRequest(request, "Tên đăng nhập và mật khẩu không được để trống");
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
            return ok(request, response);

        }, msg -> {
            if (msg.toLowerCase().contains("khóa")) {
                return authLocked(request, msg);
            }
            return authInvalid(request, msg);
        }, "Lỗi hệ thống khi xử lý đăng nhập");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
