package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;

import java.util.function.Function;

/**
 * Base helper để giảm lặp code khi viết các command handler.
 * Giữ chuẩn trả response thống nhất cho toàn bộ server.
 */
public abstract class BaseCommandHandler {

    @FunctionalInterface
    protected interface HandlerWork {
        MessageEnvelope run() throws Exception;
    }

    protected <T> T parsePayload(MessageEnvelope request, Class<T> clazz) {
        return JsonCodec.fromJsonNode(request.getPayload(), clazz);
    }

    protected void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    protected void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    protected void requirePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    protected void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    protected MessageEnvelope ok(MessageEnvelope request, Object data) {
        return MessageEnvelope.responseOk(request.getMessageId(), JsonCodec.toJsonNode(data));
    }

    protected MessageEnvelope badRequest(MessageEnvelope request, String message) {
        System.out.println("[SocketServer] Bad request: " + message
                + " (messageId=" + request.getMessageId() + ")");
        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST, message);
    }

    protected MessageEnvelope serverError(MessageEnvelope request, String message) {
        System.out.println("[SocketServer] Server error: " + message
                + " (messageId=" + request.getMessageId() + ")");
        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.SERVER_ERROR, message);
    }

    protected MessageEnvelope authInvalid(MessageEnvelope request, String message) {
        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.AUTH_INVALID, message);
    }

    protected MessageEnvelope authLocked(MessageEnvelope request, String message) {
        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.AUTH_LOCKED, message);
    }

    /**
     * Wrapper dùng chung để giảm lặp try/catch ở từng handler.
     */
    protected MessageEnvelope execute(MessageEnvelope request, HandlerWork work, String serverErrorMessage) {
        try {
            return work.run();
        } catch (IllegalArgumentException ex) {
            return badRequest(request, ex.getMessage());
        } catch (Exception ex) {
            return serverError(request, serverErrorMessage);
        }
    }

    /**
     * Biến thể cho luồng auth: cho phép map IllegalArgumentException
     * sang AUTH_INVALID / AUTH_LOCKED tùy message.
     */
    protected MessageEnvelope executeAuth(
            MessageEnvelope request,
            HandlerWork work,
            Function<String, MessageEnvelope> authErrorMapper,
            String serverErrorMessage
    ) {
        try {
            return work.run();
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Đăng nhập thất bại";
            return authErrorMapper.apply(msg);
        } catch (Exception ex) {
            return serverError(request, serverErrorMessage);
        }
    }
}
