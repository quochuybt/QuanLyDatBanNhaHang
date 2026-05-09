package iuh.fit.core.net.server.handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import iuh.fit.core.net.dto.giaoca.GiaoCaHistoryRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaGetLichSuHandler extends BaseCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GiaoCaGetLichSuHandler.class);
    private final GiaoCaService giaoCaService = new GiaoCaService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            GiaoCaHistoryRequest payload = parsePayload(request, GiaoCaHistoryRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getFromDate(), "Ngày bắt đầu không được để trống.");
            requireNotNull(payload.getToDate(), "Ngày kết thúc không được để trống.");
            requireTrue(!payload.getFromDate().isAfter(payload.getToDate()), "Ngày bắt đầu không được sau ngày kết thúc.");

            return ok(request, giaoCaService.getLichSuGiaoCa(payload.getFromDate(), payload.getToDate()));
        }, "Lỗi server khi tải lịch sử giao ca.");
    }
}
