package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.dto.NhanVienDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.NhanVienService;

import java.util.List;

public class NhanVienGetAllHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NhanVienGetAllHandler.class);
    private final NhanVienService nhanVienService = new NhanVienService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            List<NhanVienDTO> result = nhanVienService.findAll().stream()
                    .map(NhanVienDTO::fromEntity)
                    .toList();
            return ok(request, result);
        }, "Lỗi server khi tải danh sách nhân viên.");
    }
}
