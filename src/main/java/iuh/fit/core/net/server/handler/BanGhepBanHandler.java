package iuh.fit.core.net.server.handler;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.net.dto.ban.BanActionRequest;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.dispatch.CommandHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.service.BanService;

import java.util.List;

public class BanGhepBanHandler extends BaseCommandHandler implements CommandHandler {

    private final BanService banService = new BanService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        return execute(request, () -> {
            requireNotNull(request.getPayload(), "Payload không được để trống.");

            BanActionRequest payload = parsePayload(request, BanActionRequest.class);

            requireNotNull(payload, "Payload không hợp lệ.");
            requireNotNull(payload.getDsBanNguon(), "Danh sách bàn nguồn không được để trống.");
            requireTrue(!payload.getDsBanNguon().isEmpty(), "Danh sách bàn nguồn không được rỗng.");
            requireNotNull(payload.getBanDich(), "Bàn đích không được để trống.");
            requireNotBlank(payload.getBanDich().getMaBan(), "Mã bàn đích không được để trống.");

            List<BanDTO> dsBanNguon = payload.getDsBanNguon();
            BanDTO banDich = payload.getBanDich();

            for (BanDTO banNguon : dsBanNguon) {
                requireNotNull(banNguon, "Bàn nguồn không được null.");
                requireNotBlank(banNguon.getMaBan(), "Mã bàn nguồn không được để trống.");
            }

            boolean success = banService.ghepBanLienKet(dsBanNguon, banDich);

            System.out.println("[SocketServer] BAN_GHEP_BAN thành công"
                    + " command=" + request.getName()
                    + ", messageId=" + request.getMessageId()
                    + ", banDich=" + banDich.getMaBan()
                    + ", totalNguon=" + dsBanNguon.size());

            return ok(request, success);
        }, "Lỗi server khi ghép bàn.");
    }
}