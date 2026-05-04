package iuh.fit.core.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapper {

    private static final ObjectMapper INSTANCE = createMapper();

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static ObjectMapper get() {
        return INSTANCE;
    }

    public static String toJson(Object obj) {
        try {
            return INSTANCE.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi serialize JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return INSTANCE.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi deserialize JSON: " + e.getMessage(), e);
        }
    }
}
