package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.net.dto.common.IdRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonDeleteHandler extends BaseCommandHandler implements CommandHandler {
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            IdRequest payload = parsePayload(request, IdRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getId(), "Mã danh mục không được để trống.");
            DanhMucMonDTO dto = DanhMucMonDTO.builder().madm(payload.getId()).build();
            return ok(request, danhMucMonService.xoaDanhMuc(dto));
        }, "Lỗi server khi xóa danh mục món.");
    }
}
