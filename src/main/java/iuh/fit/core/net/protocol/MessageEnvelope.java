package iuh.fit.core.net.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEnvelope {
    private String messageId;
    private MessageType type;
    private String name;
    private String correlationId;
    private long timestamp;
    private boolean success;
    private String errorCode;
    private String message;
    private JsonNode payload;

    public static MessageEnvelope command(String name, JsonNode payload) {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.COMMAND)
                .name(name)
                .timestamp(Instant.now().toEpochMilli())
                .success(true)
                .payload(payload)
                .build();
    }

    public static MessageEnvelope responseOk(String correlationId, JsonNode payload) {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.RESPONSE)
                .correlationId(correlationId)
                .timestamp(Instant.now().toEpochMilli())
                .success(true)
                .payload(payload)
                .build();
    }

    public static MessageEnvelope responseFail(String correlationId, ErrorCode code, String message) {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.RESPONSE)
                .correlationId(correlationId)
                .timestamp(Instant.now().toEpochMilli())
                .success(false)
                .errorCode(code != null ? code.name() : null)
                .message(message)
                .build();
    }

    public static MessageEnvelope event(String eventName, JsonNode payload) {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.EVENT)
                .name(eventName)
                .timestamp(Instant.now().toEpochMilli())
                .success(true)
                .payload(payload)
                .build();
    }

    public static MessageEnvelope ping() {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.PING)
                .timestamp(Instant.now().toEpochMilli())
                .success(true)
                .build();
    }

    public static MessageEnvelope pong(String correlationId) {
        return MessageEnvelope.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.PONG)
                .correlationId(correlationId)
                .timestamp(Instant.now().toEpochMilli())
                .success(true)
                .build();
    }
}
