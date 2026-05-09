package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonUpdateGhiChuHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        try {
            DonDatMonDTO dto = JsonCodec.fromJsonNode(request.getPayload(), DonDatMonDTO.class);

            if (dto == null || dto.getMaDon() == null || dto.getMaDon().trim().isEmpty()) {
                return MessageEnvelope.responseFail(
                        request.getMessageId(),
                        ErrorCode.BAD_REQUEST,
                        "Mã đơn đặt món không hợp lệ."
                );
            }

            boolean ok = donDatMonService.updateGhiChu(dto.getMaDon(), dto.getGhiChu());

            return MessageEnvelope.responseOk(
                    request.getMessageId(),
                    JsonCodec.toJsonNode(ok)
            );

        } catch (Exception e) {
            return MessageEnvelope.responseFail(
                    request.getMessageId(),
                    ErrorCode.BAD_REQUEST,
                    "Lỗi cập nhật ghi chú: " + e.getMessage()
            );
        }
    }
}