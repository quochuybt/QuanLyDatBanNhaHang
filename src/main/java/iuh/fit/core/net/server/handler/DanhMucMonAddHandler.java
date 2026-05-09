package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DanhMucMonService;

public class DanhMucMonAddHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DanhMucMonAddHandler.class);
    private final DanhMucMonService danhMucMonService = new DanhMucMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            DanhMucMonDTO payload = parsePayload(request, DanhMucMonDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getTendm(), "Tên danh mục không được để trống.");
            return ok(request, danhMucMonService.themDanhMuc(payload));
        }, "Lỗi server khi thêm danh mục món.");
    }
}
