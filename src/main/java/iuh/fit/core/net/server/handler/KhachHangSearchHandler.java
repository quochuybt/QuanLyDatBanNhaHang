package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.net.dto.khachhang.KhachHangSearchRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.KhachHangService;

import java.util.List;

public class KhachHangSearchHandler extends BaseCommandHandler implements CommandHandler {

    private final KhachHangService khachHangService = new KhachHangService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            String keyword = "";

            if (request.getPayload() != null && !request.getPayload().isNull()) {
                KhachHangSearchRequest payload = parsePayload(request, KhachHangSearchRequest.class);
                if (payload != null && payload.getKeyword() != null) {
                    keyword = payload.getKeyword().trim();
                }
            }

            List<KhachHangDTO> result = khachHangService.searchDTO(keyword);

            System.out.println("[SocketServer] KHACHHANG_SEARCH thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", keyword=" + keyword
                    + ", total=" + result.size());

            return ok(request, result);
        }, "Lỗi server khi tìm kiếm khách hàng.");
    }
}