package iuh.fit.core.net.server.handler;

import iuh.fit.core.net.dto.giaoca.GiaoCaTopStaffRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.GiaoCaService;

public class GiaoCaGetTopStaffHoursHandler extends BaseCommandHandler implements CommandHandler {
    private final GiaoCaService giaoCaService = new GiaoCaService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            GiaoCaTopStaffRequest payload = parsePayload(request, GiaoCaTopStaffRequest.class);
            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getStartDate(), "Ngày bắt đầu không được để trống.");
            requireNotNull(payload.getEndDate(), "Ngày kết thúc không được để trống.");
            requireTrue(!payload.getStartDate().isAfter(payload.getEndDate()), "Ngày bắt đầu không được sau ngày kết thúc.");
            requirePositive(payload.getLimit(), "Giới hạn top nhân viên phải lớn hơn 0.");

            return ok(request, giaoCaService.getTopStaffByWorkHours(
                    payload.getStartDate(),
                    payload.getEndDate(),
                    payload.getLimit()
            ));
        }, "Lỗi server khi tải top nhân viên theo giờ làm.");
    }
}
