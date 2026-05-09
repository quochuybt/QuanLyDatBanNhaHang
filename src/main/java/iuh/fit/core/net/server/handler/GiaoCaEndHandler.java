package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.dto.giaoca.GiaoCaEndRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaEndHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiaoCaEndHandler.class);

    private final GiaoCaService giaoCaService = new GiaoCaService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            GiaoCaEndRequest payload = parsePayload(request, GiaoCaEndRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotBlank(payload.getMaGiaoCa(), "Mã giao ca không được để trống.");
            requireTrue(payload.getTienCuoiCa() >= 0, "Tiền cuối ca không được âm.");

            int maGiaoCa;
            try {
                maGiaoCa = Integer.parseInt(payload.getMaGiaoCa().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Mã giao ca không hợp lệ.");
            }

            boolean success = giaoCaService.ketThucCa(
                    maGiaoCa,
                    payload.getTienCuoiCa(),
                    payload.getGhiChu()
            );

            LOGGER.info("[SocketServer] GIAOCA_END thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", maGiaoCa=" + payload.getMaGiaoCa());

            return ok(request, success);
        }, "Lỗi server khi kết thúc ca.");
    }
}