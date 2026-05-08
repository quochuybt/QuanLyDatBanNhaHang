package iuh.fit.core.net.server.dispatch;

import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.handler.AuthLoginHandler;
import iuh.fit.core.net.server.handler.DashboardDailyRevenueHandler;
import iuh.fit.core.net.server.handler.DashboardLeastSellingHandler;
import iuh.fit.core.net.server.handler.DashboardTableStatusHandler;
import iuh.fit.core.net.server.handler.DashboardTopSellingHandler;
import iuh.fit.core.net.server.handler.BanChuyenBanHandler;
import iuh.fit.core.net.server.handler.BanGhepBanHandler;
import iuh.fit.core.net.server.handler.BanUpdateStatusHandler;
import iuh.fit.core.net.server.handler.HoaDonGetByPageHandler;
import iuh.fit.core.net.server.handler.HoaDonGetDetailHandler;
import iuh.fit.core.net.server.handler.HoaDonGetTotalHandler;
import iuh.fit.core.net.server.handler.KhachHangAddHandler;
import iuh.fit.core.net.server.handler.KhachHangSearchHandler;
import iuh.fit.core.net.server.handler.KhachHangUpdateHandler;
import iuh.fit.core.net.server.handler.PhanCongAddHandler;
import iuh.fit.core.net.server.handler.PhanCongListByDateHandler;
import iuh.fit.core.net.server.handler.PhanCongRemoveHandler;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandDispatcher(SessionRegistry sessionRegistry) {
        handlers.put(CommandAction.AUTH_LOGIN.name(), new AuthLoginHandler(sessionRegistry));

        // ===== Hóa đơn (read-only) =====
        handlers.put(CommandAction.HOADON_GET_BY_PAGE.name(), new HoaDonGetByPageHandler());
        handlers.put(CommandAction.HOADON_GET_TOTAL.name(), new HoaDonGetTotalHandler());
        handlers.put(CommandAction.HOADON_GET_DETAIL.name(), new HoaDonGetDetailHandler());

        // ===== Dashboard (read) =====
        handlers.put(CommandAction.DASHBOARD_DAILY_REVENUE.name(), new DashboardDailyRevenueHandler());
        handlers.put(CommandAction.DASHBOARD_TOP_SELLING.name(), new DashboardTopSellingHandler());
        handlers.put(CommandAction.DASHBOARD_LEAST_SELLING.name(), new DashboardLeastSellingHandler());
        handlers.put(CommandAction.DASHBOARD_TABLE_STATUS_COUNTS.name(), new DashboardTableStatusHandler());

        // ===== Phase 3 (write) - skeleton =====
        handlers.put(CommandAction.PHANCONG_ADD.name(), new PhanCongAddHandler());
        handlers.put(CommandAction.PHANCONG_REMOVE.name(), new PhanCongRemoveHandler());
        handlers.put(CommandAction.PHANCONG_LIST_BY_DATE.name(), new PhanCongListByDateHandler());

        handlers.put(CommandAction.BAN_UPDATE_STATUS.name(), new BanUpdateStatusHandler());
        handlers.put(CommandAction.BAN_CHUYEN_BAN.name(), new BanChuyenBanHandler());
        handlers.put(CommandAction.BAN_GHEP_BAN.name(), new BanGhepBanHandler());

        handlers.put(CommandAction.KHACHHANG_ADD.name(), new KhachHangAddHandler());
        handlers.put(CommandAction.KHACHHANG_UPDATE.name(), new KhachHangUpdateHandler());
        handlers.put(CommandAction.KHACHHANG_SEARCH.name(), new KhachHangSearchHandler());
    }

    public MessageEnvelope dispatch(ClientSession session, MessageEnvelope request) {
        // request.name đóng vai trò route key đến handler
        if (request.getName() == null) {
            System.out.println("[SocketServer] Bad request: thiếu tên command (messageId=" + request.getMessageId() + ")");
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                    "Thiếu tên command");
        }

        CommandHandler handler = handlers.get(request.getName());
        if (handler == null) {
            System.out.println("[SocketServer] Bad request: command không hỗ trợ: " + request.getName());
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                    "Command không được hỗ trợ: " + request.getName());
        }

        // Giao xử lý nghiệp vụ cho handler tương ứng
        return handler.handle(session, request);
    }
}
