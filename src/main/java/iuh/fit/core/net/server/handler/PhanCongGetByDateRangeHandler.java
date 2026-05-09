package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.PhanCongDTO;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.net.dto.phancong.PhanCongDateRangeRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.PhanCongService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhanCongGetByDateRangeHandler extends BaseCommandHandler implements CommandHandler {
    private final PhanCongService phanCongService = new PhanCongService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            PhanCongDateRangeRequestDTO payload = parsePayload(request, PhanCongDateRangeRequestDTO.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getTuNgay(), "Từ ngày không được để trống.");
            requireNotNull(payload.getDenNgay(), "Đến ngày không được để trống.");
            requireTrue(!payload.getTuNgay().isAfter(payload.getDenNgay()), "Từ ngày không được sau đến ngày.");

            List<PhanCong> ds = phanCongService.getPhanCongChiTiet(payload.getTuNgay(), payload.getDenNgay());
            List<PhanCongDTO> result = ds.stream()
                    .map(PhanCongDTO::fromEntity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ok(request, result);
        }, "Lỗi server khi tải phân công theo khoảng ngày.");
    }
}
