package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;

/**
 * Skeleton Phase 3: thêm khách hàng.
 */
public class KhachHangAddHandler extends BaseCommandHandler implements CommandHandler {
    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return serverError(request, "Chức năng KHACHHANG_ADD đang được triển khai");
    }
}
