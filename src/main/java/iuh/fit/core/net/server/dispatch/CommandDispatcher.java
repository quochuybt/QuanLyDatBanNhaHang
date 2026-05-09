package iuh.fit.core.net.server.dispatch;

import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.handler.*;
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

        handlers.put(CommandAction.HOADON_GET_CHUA_THANH_TOAN_BY_BAN.name(), new HoaDonGetChuaThanhToanByBanHandler());
        handlers.put(CommandAction.HOADON_MO_BAN_TAO_HOA_DON.name(), new HoaDonMoBanTaoHoaDonHandler());
        handlers.put(CommandAction.HOADON_CAP_NHAT_TONG_TIEN.name(), new HoaDonCapNhatTongTienHandler());

        // ===== Dashboard (read) =====
        handlers.put(CommandAction.DASHBOARD_DAILY_REVENUE.name(), new DashboardDailyRevenueHandler());
        handlers.put(CommandAction.DASHBOARD_TOP_SELLING.name(), new DashboardTopSellingHandler());
        handlers.put(CommandAction.DASHBOARD_LEAST_SELLING.name(), new DashboardLeastSellingHandler());
        handlers.put(CommandAction.DASHBOARD_TABLE_STATUS_COUNTS.name(), new DashboardTableStatusHandler());

        // ===== Phase 3 (write) - skeleton =====
        handlers.put(CommandAction.PHANCONG_ADD.name(), new PhanCongAddHandler());
        handlers.put(CommandAction.PHANCONG_REMOVE.name(), new PhanCongRemoveHandler());
        handlers.put(CommandAction.PHANCONG_LIST_BY_DATE.name(), new PhanCongListByDateHandler());

        handlers.put(CommandAction.BAN_GET_ALL.name(), new BanGetAllHandler());
        handlers.put(CommandAction.BAN_UPDATE_STATUS.name(), new BanUpdateStatusHandler());
        handlers.put(CommandAction.BAN_CHUYEN_BAN.name(), new BanChuyenBanHandler());
        handlers.put(CommandAction.BAN_GHEP_BAN.name(), new BanGhepBanHandler());

        handlers.put(CommandAction.KHACHHANG_ADD.name(), new KhachHangAddHandler());
        handlers.put(CommandAction.KHACHHANG_UPDATE.name(), new KhachHangUpdateHandler());
        handlers.put(CommandAction.KHACHHANG_SEARCH.name(), new KhachHangSearchHandler());

        // ===== Dashboard nhân viên / giao ca =====
        handlers.put(CommandAction.GIAOCA_DASHBOARD_LOAD.name(), new GiaoCaDashboardLoadHandler());
        handlers.put(CommandAction.GIAOCA_START.name(), new GiaoCaStartHandler());
        handlers.put(CommandAction.GIAOCA_END.name(), new GiaoCaEndHandler());

        // ===== Đơn đặt món =====
        handlers.put(CommandAction.DONDATMON_GET_ALL_CHUA_NHAN.name(), new DonDatMonGetAllChuaNhanHandler());
        handlers.put(CommandAction.DONDATMON_SEARCH_CHUA_NHAN.name(), new DonDatMonSearchChuaNhanHandler());
        handlers.put(CommandAction.DONDATMON_GET_BAN_DA_DAT_TRONG_KHOANG.name(), new DonDatMonGetBanDaDatTrongKhoangHandler());
        handlers.put(CommandAction.DONDATMON_SAVE.name(), new DonDatMonSaveHandler());
        handlers.put(CommandAction.DONDATMON_HUY_DAT_BAN.name(), new DonDatMonHuyDatBanHandler());
        handlers.put(
                CommandAction.DONDATMON_GET_DAT_TRUOC_BY_BAN.name(),
                new DonDatMonGetDatTruocByBanHandler()
        );

        handlers.put(
                CommandAction.DONDATMON_GET_CHUA_NHAN_THEO_BAN_BAO_GOM_LINKED.name(),
                new DonDatMonGetChuaNhanTheoBanBaoGomLinkedHandler()
        );

        handlers.put(
                CommandAction.DONDATMON_GET_ALL_CHUA_NHAN_BAO_GOM_LINKED.name(),
                new DonDatMonGetAllChuaNhanBaoGomLinkedHandler()
        );

        handlers.put(CommandAction.MONAN_GET_ALL.name(), new MonAnGetAllHandler());

        handlers.put(
                CommandAction.CHITIETHOADON_GET_BY_MA_DON.name(),
                new ChiTietHoaDonGetByMaDonHandler()
        );

        handlers.put(
                CommandAction.CHITIETHOADON_REPLACE_BY_MA_DON.name(),
                new ChiTietHoaDonReplaceByMaDonHandler()
        );

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
