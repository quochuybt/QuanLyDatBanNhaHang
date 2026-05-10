package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

public class ImageUploadHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploadHandler.class);

    // Lưu ảnh vào thư mục bên cạnh server.jar
    private static final String IMAGE_DIR = System.getProperty("user.dir") + "/resources/img/MonAn";

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            @SuppressWarnings("unchecked")
            Map<String, String> payload = parsePayload(request, Map.class);
            requireNotNull(payload, "Payload không hợp lệ.");

            String fileName = (String) payload.get("fileName");
            String base64Data = (String) payload.get("data");

            requireNotBlank(fileName, "Tên file không được để trống.");
            requireNotBlank(base64Data, "Dữ liệu ảnh không được để trống.");

            // Tạo thư mục nếu chưa có
            Path dir = Paths.get(IMAGE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // Tạo tên file unique tránh trùng
            String newName = System.currentTimeMillis() + "_" + fileName;
            Path dest = dir.resolve(newName);

            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            Files.write(dest, imageBytes);

            LOGGER.info("[ImageUpload] Lưu ảnh thành công: {}", dest);

            return ok(request, newName);
        }, "Lỗi server khi upload ảnh.");
    }
}
