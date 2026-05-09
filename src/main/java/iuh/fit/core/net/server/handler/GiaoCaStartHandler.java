package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.GiaoCaDTO;
import iuh.fit.core.net.dto.giaoca.GiaoCaStartRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaStartHandler extends BaseCommandHandler implements CommandHandler {

    private final GiaoCaService giaoCaService = new GiaoCaService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            GiaoCaStartRequest payload = parsePayload(request, GiaoCaStartRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaNV(), "Mã nhân viên không được để trống.");
            requireNotNull(payload.getThoiGianBatDau(), "Thời gian bắt đầu ca không được để trống.");
            requireTrue(payload.getTienDauCa() >= 0, "Tiền đầu ca không được âm.");

            GiaoCaDTO dto = GiaoCaDTO.builder()
                    .maNV(payload.getMaNV().trim())
                    .thoiGianBatDau(payload.getThoiGianBatDau())
                    .tienDauCa(payload.getTienDauCa())
                    .build();

            boolean success = giaoCaService.batDauCa(dto);

            System.out.println("[SocketServer] GIAOCA_START thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maNV=" + payload.getMaNV());

            return ok(request, success);
        }, "Lỗi server khi bắt đầu ca.");
    }
}