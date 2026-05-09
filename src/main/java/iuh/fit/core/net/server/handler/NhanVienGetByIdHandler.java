package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.NhanVienDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.NhanVienService;

public class NhanVienGetByIdHandler extends BaseCommandHandler implements CommandHandler {
    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã nhân viên không được để trống.");

            NhanVienDTO dto = nhanVienService.getChiTietNhanVien(payload.getId());
            if (dto == null) {
                throw new IllegalArgumentException("Không tìm thấy nhân viên theo mã.");
            }

            return ok(request, dto);
        }, "Lỗi server khi tải thông tin nhân viên.");
    }
}
