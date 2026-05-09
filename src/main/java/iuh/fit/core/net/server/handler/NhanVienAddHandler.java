package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.net.dto.nhanvien.NhanVienAddRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.NhanVienService;

public class NhanVienAddHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NhanVienAddHandler.class);
    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            NhanVienAddRequest payload = parsePayload(request, NhanVienAddRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getNhanVien(), "Thông tin nhân viên không được để trống.");
            requireNotBlank(payload.getTenTK(), "Tên tài khoản không được để trống.");
            requireNotBlank(payload.getMatKhau(), "Mật khẩu không được để trống.");

            NhanVien nv = payload.getNhanVien().toEntity();
            nhanVienService.addNhanVien(nv, payload.getTenTK(), payload.getMatKhau());
            return ok(request, true);
        }, "Lỗi server khi thêm nhân viên.");
    }
}
