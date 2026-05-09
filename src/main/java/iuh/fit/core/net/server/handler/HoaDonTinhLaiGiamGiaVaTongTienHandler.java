package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.HoaDonService;

public class HoaDonTinhLaiGiamGiaVaTongTienHandler extends BaseCommandHandler implements CommandHandler {
    private final HoaDonService hoaDonService = new HoaDonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            HoaDonDTO dto = parsePayload(request, HoaDonDTO.class);
            HoaDonDTO updatedDto = hoaDonService.tinhLaiGiamGiaVaTongTien(dto);
            return ok(request, updatedDto);
        }, "Lỗi tính lại hóa đơn");
    }
}
