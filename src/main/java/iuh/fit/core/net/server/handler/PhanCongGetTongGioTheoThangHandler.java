package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.phancong.PhanCongTongGioTheoThangRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.PhanCongService;

import java.util.Map;

public class PhanCongGetTongGioTheoThangHandler extends BaseCommandHandler implements CommandHandler {
    private final PhanCongService phanCongService = new PhanCongService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            PhanCongTongGioTheoThangRequestDTO payload = parsePayload(request, PhanCongTongGioTheoThangRequestDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireTrue(payload.getThang() >= 1 && payload.getThang() <= 12, "Tháng không hợp lệ.");
            requireTrue(payload.getNam() >= 2000, "Năm không hợp lệ.");

            Map<String, Double> result = phanCongService.getTongGioLamTheoThang(payload.getThang(), payload.getNam());
            return ok(request, result);
        }, "Lỗi server khi tải tổng giờ theo tháng.");
    }
}
