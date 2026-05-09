package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.PhanCongDTO;
import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.net.dto.phancong.PhanCongRequestDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.PhanCongService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhanCongListByDateHandler extends BaseCommandHandler implements CommandHandler {

    private final PhanCongService phanCongService = new PhanCongService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            PhanCongRequestDTO payload = parsePayload(request, PhanCongRequestDTO.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getNgayLam(), "Ngày làm không được để trống.");

            List<PhanCong> dsPhanCong = phanCongService.findByNgayLam(payload.getNgayLam());

            List<PhanCongDTO> result = dsPhanCong.stream()
                    .map(PhanCongDTO::fromEntity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            System.out.println("[SocketServer] PHANCONG_LIST_BY_DATE thành công"
                    + " session=" + session.getSessionId()
                    + ", ngayLam=" + payload.getNgayLam()
                    + ", total=" + result.size());

            return ok(request, result);
        }, "Lỗi server khi tải danh sách phân công theo ngày.");
    }
}