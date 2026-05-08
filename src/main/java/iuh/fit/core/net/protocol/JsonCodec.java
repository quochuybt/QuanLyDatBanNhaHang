package iuh.fit.core.net.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JsonCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private JsonCodec() {
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể serialize JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Không thể parse JSON", e);
        }
    }

    public static <T> T fromJsonNode(JsonNode node, Class<T> clazz) {
        try {
            return MAPPER.treeToValue(node, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Không thể map JsonNode", e);
        }
    }

    public static JsonNode toJsonNode(Object value) {
        return MAPPER.valueToTree(value);
    }

    public static <T> T convertValue(Object value, TypeReference<T> typeReference) {
        return MAPPER.convertValue(value, typeReference);
    }
}
