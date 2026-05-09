package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.phancong.PhanCongRequestDTO;
import iuh.fit.core.net.protocol.EventType;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;
import iuh.fit.core.service.PhanCongService;

public class PhanCongRemoveHandler extends BaseCommandHandler implements CommandHandler {

    private final PhanCongService phanCongService = new PhanCongService();
    private final SessionRegistry sessionRegistry;

    public PhanCongRemoveHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            PhanCongRequestDTO payload = parsePayload(request, PhanCongRequestDTO.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaNV(), "Mã nhân viên không được để trống.");
            requireNotBlank(payload.getMaCa(), "Mã ca làm không được để trống.");
            requireNotNull(payload.getNgayLam(), "Ngày làm không được để trống.");

            boolean success = phanCongService.xoaPhanCong(
                    payload.getMaNV(),
                    payload.getMaCa(),
                    payload.getNgayLam()
            );

            System.out.println("[SocketServer] PHANCONG_REMOVE thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maNV=" + payload.getMaNV()
                    + ", maCa=" + payload.getMaCa()
                    + ", ngayLam=" + payload.getNgayLam());

            if (success) {
                sessionRegistry.broadcastBusinessEvent(
                        EventType.SHIFT_UPDATED,
                        request.getName(),
                        "PHANCONG",
                        payload.getMaNV() + "_" + payload.getMaCa() + "_" + payload.getNgayLam(),
                        "DELETED",
                        session.getTenTK(),
                        java.util.Map.of(
                                "maNV", payload.getMaNV(),
                                "maCa", payload.getMaCa(),
                                "ngayLam", payload.getNgayLam().toString(),
                                "action", "REMOVE"
                        )
                );
            }

            return ok(request, success);
        }, "Lỗi server khi hủy phân công ca.");
    }
}
