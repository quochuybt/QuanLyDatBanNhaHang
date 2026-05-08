package iuh.fit.core.net.server.dispatch;

import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.handler.AuthLoginHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandDispatcher(SessionRegistry sessionRegistry) {
        handlers.put(CommandAction.AUTH_LOGIN.name(), new AuthLoginHandler(sessionRegistry));
    }

    public MessageEnvelope dispatch(ClientSession session, MessageEnvelope request) {
        // request.name đóng vai trò route key đến handler
        if (request.getName() == null) {
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                    "Thiếu tên command");
        }

        CommandHandler handler = handlers.get(request.getName());
        if (handler == null) {
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                    "Command không được hỗ trợ: " + request.getName());
        }

        // Giao xử lý nghiệp vụ cho handler tương ứng
        return handler.handle(session, request);
    }
}
