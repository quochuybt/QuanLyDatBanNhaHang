package iuh.fit.core.net.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.JsonCodec;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.DonDatMonService;

public class DonDatMonGetByIdHandler implements CommandHandler {

    private final DonDatMonService donDatMonService = new DonDatMonService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        try {
            String maDon = JsonCodec.fromJsonNode(request.getPayload(), String.class);

            if (maDon == null || maDon.trim().isEmpty()) {
                return MessageEnvelope.responseFail(
                        request.getMessageId(),
                        ErrorCode.BAD_REQUEST,
                        "Mã đơn đặt món không hợp lệ."
                );
            }

            DonDatMonDTO dto = donDatMonService.findById(maDon);

            JsonNode payload = dto == null
                    ? NullNode.getInstance()
                    : JsonCodec.toJsonNode(dto);

            return MessageEnvelope.responseOk(
                    request.getMessageId(),
                    payload
            );

        } catch (Exception e) {
            return MessageEnvelope.responseFail(
                    request.getMessageId(),
                    ErrorCode.BAD_REQUEST,
                    "Lỗi tìm đơn đặt món theo mã: " + e.getMessage()
            );
        }
    }
}