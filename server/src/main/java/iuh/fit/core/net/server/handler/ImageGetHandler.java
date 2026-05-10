package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

public class ImageGetHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageGetHandler.class);
    private static final String IMAGE_DIR = System.getProperty("user.dir") + "/resources/img/MonAn";

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            @SuppressWarnings("unchecked")
            Map<String, String> payload = parsePayload(request, Map.class);
            requireNotNull(payload, "Payload không hợp lệ.");

            String fileName = (String) payload.get("fileName");
            requireNotBlank(fileName, "Tên file không được để trống.");

            byte[] bytes = null;

            // 1. Thử đọc từ filesystem (ảnh upload bởi người dùng)
            Path filePath = Paths.get(IMAGE_DIR, fileName);
            if (Files.exists(filePath)) {
                bytes = Files.readAllBytes(filePath);
            } else {
                // 2. Fallback: đọc từ classpath (ảnh mặc định đóng gói trong JAR)
                try (java.io.InputStream is = getClass().getResourceAsStream("/img/MonAn/" + fileName)) {
                    if (is != null) {
                        bytes = is.readAllBytes();
                    }
                }
            }

            if (bytes == null) {
                throw new IllegalArgumentException("Ảnh không tồn tại: " + fileName);
            }

            String base64 = Base64.getEncoder().encodeToString(bytes);
            LOGGER.info("[ImageGet] Gửi ảnh: {}", fileName);
            return ok(request, base64);
        }, "Lỗi server khi lấy ảnh.");
    }
}
