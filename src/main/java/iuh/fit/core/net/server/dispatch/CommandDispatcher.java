package iuh.fit.core.net.server.dispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iuh.fit.core.net.protocol.CommandAction;
import iuh.fit.core.net.protocol.ErrorCode;
import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.handler.*;
import iuh.fit.core.net.server.session.ClientSession;
import iuh.fit.core.net.server.session.SessionRegistry;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandDispatcher.class);

        private final Map<String, CommandHandler> handlers = new HashMap<>();

        public CommandDispatcher(SessionRegistry sessionRegistry) {
                handlers.put(CommandAction.AUTH_LOGIN.name(), new AuthLoginHandler(sessionRegistry));

                // ===== Hóa đơn (read-only) =====
                handlers.put(CommandAction.HOADON_GET_BY_PAGE.name(), new HoaDonGetByPageHandler());
                handlers.put(CommandAction.HOADON_GET_TOTAL.name(), new HoaDonGetTotalHandler());
                handlers.put(CommandAction.HOADON_GET_DETAIL.name(), new HoaDonGetDetailHandler());

                handlers.put(CommandAction.HOADON_GET_CHUA_THANH_TOAN_BY_BAN.name(),
                                new HoaDonGetChuaThanhToanByBanHandler());
                handlers.put(CommandAction.HOADON_MO_BAN_TAO_HOA_DON.name(),
                                new HoaDonMoBanTaoHoaDonHandler(sessionRegistry));
                handlers.put(CommandAction.HOADON_CAP_NHAT_TONG_TIEN.name(),
                                new HoaDonCapNhatTongTienHandler(sessionRegistry));

                // ===== Dashboard (read) =====
                handlers.put(CommandAction.DASHBOARD_DAILY_REVENUE.name(), new DashboardDailyRevenueHandler());
                handlers.put(CommandAction.DASHBOARD_TOP_SELLING.name(), new DashboardTopSellingHandler());
                handlers.put(CommandAction.DASHBOARD_LEAST_SELLING.name(), new DashboardLeastSellingHandler());
                handlers.put(CommandAction.DASHBOARD_TABLE_STATUS_COUNTS.name(), new DashboardTableStatusHandler());

                // ===== Phase 3 (write) - skeleton =====
                handlers.put(CommandAction.PHANCONG_ADD.name(), new PhanCongAddHandler(sessionRegistry));
                handlers.put(CommandAction.PHANCONG_REMOVE.name(), new PhanCongRemoveHandler(sessionRegistry));
                handlers.put(CommandAction.PHANCONG_LIST_BY_DATE.name(), new PhanCongListByDateHandler());
                handlers.put(CommandAction.PHANCONG_GET_BY_DATE_RANGE.name(), new PhanCongGetByDateRangeHandler());
                handlers.put(CommandAction.PHANCONG_GET_TONG_GIO_THEO_THANG.name(), new PhanCongGetTongGioTheoThangHandler());
                handlers.put(CommandAction.CALAM_GET_ALL_ORDER_BY_START.name(), new CaLamGetAllOrderByStartHandler());

                // ===== Nhân viên =====
                handlers.put(CommandAction.NHANVIEN_GET_ALL.name(), new NhanVienGetAllHandler());
                handlers.put(CommandAction.NHANVIEN_GET_BY_ID.name(), new NhanVienGetByIdHandler());
                handlers.put(CommandAction.NHANVIEN_ADD.name(), new NhanVienAddHandler(sessionRegistry));
                handlers.put(CommandAction.NHANVIEN_UPDATE.name(), new NhanVienUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.NHANVIEN_TOGGLE_STATUS.name(), new NhanVienToggleStatusHandler(sessionRegistry));
                handlers.put(CommandAction.NHANVIEN_GET_EMAIL_BY_TENTK.name(), new NhanVienGetEmailByTenTKHandler());
                handlers.put(CommandAction.NHANVIEN_GET_ACCOUNT_STATUS.name(), new NhanVienGetAccountStatusHandler());

                handlers.put(CommandAction.BAN_GET_ALL.name(), new BanGetAllHandler());
                handlers.put(CommandAction.BAN_UPDATE_STATUS.name(), new BanUpdateStatusHandler(sessionRegistry));
                handlers.put(CommandAction.BAN_CHUYEN_BAN.name(), new BanChuyenBanHandler(sessionRegistry));
                handlers.put(CommandAction.BAN_GHEP_BAN.name(), new BanGhepBanHandler(sessionRegistry));

                handlers.put(CommandAction.KHACHHANG_ADD.name(), new KhachHangAddHandler(sessionRegistry));
                handlers.put(CommandAction.KHACHHANG_UPDATE.name(), new KhachHangUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.KHACHHANG_SEARCH.name(), new KhachHangSearchHandler());

                // ===== Khuyến mãi =====
                handlers.put(CommandAction.KHUYENMAI_GET_ALL.name(), new KhuyenMaiGetAllHandler());
                handlers.put(CommandAction.KHUYENMAI_GET_BY_ID.name(), new KhuyenMaiGetByIdHandler());
                handlers.put(CommandAction.KHUYENMAI_ADD.name(), new KhuyenMaiAddHandler(sessionRegistry));
                handlers.put(CommandAction.KHUYENMAI_UPDATE.name(), new KhuyenMaiUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.KHUYENMAI_DELETE.name(), new KhuyenMaiDeleteHandler(sessionRegistry));
                handlers.put(CommandAction.KHUYENMAI_USE.name(), new KhuyenMaiUseHandler());

                // ===== Danh mục món / món ăn admin =====
                handlers.put(CommandAction.DANHMUCMON_GET_ALL.name(), new DanhMucMonGetAllHandler());
                handlers.put(CommandAction.DANHMUCMON_ADD.name(), new DanhMucMonAddHandler(sessionRegistry));
                handlers.put(CommandAction.DANHMUCMON_UPDATE.name(), new DanhMucMonUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.DANHMUCMON_DELETE.name(), new DanhMucMonDeleteHandler(sessionRegistry));
                handlers.put(CommandAction.MONAN_ADMIN_GET_ALL.name(), new MonAnAdminGetAllHandler());
                handlers.put(CommandAction.MONAN_ADMIN_GET_BY_ID.name(), new MonAnAdminGetByIdHandler());
                handlers.put(CommandAction.MONAN_ADMIN_ADD.name(), new MonAnAdminAddHandler(sessionRegistry));
                handlers.put(CommandAction.MONAN_ADMIN_UPDATE.name(), new MonAnAdminUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.MONAN_ADMIN_UPDATE_STATUS.name(), new MonAnAdminUpdateStatusHandler(sessionRegistry));
                handlers.put(CommandAction.MONAN_ADMIN_DELETE.name(), new MonAnAdminDeleteHandler(sessionRegistry));

                // ===== Dashboard nhân viên / giao ca =====
                handlers.put(CommandAction.GIAOCA_DASHBOARD_LOAD.name(), new GiaoCaDashboardLoadHandler());
                handlers.put(CommandAction.GIAOCA_START.name(), new GiaoCaStartHandler(sessionRegistry));
                handlers.put(CommandAction.GIAOCA_END.name(), new GiaoCaEndHandler(sessionRegistry));
                handlers.put(CommandAction.GIAOCA_GET_LICH_SU.name(), new GiaoCaGetLichSuHandler());
                handlers.put(CommandAction.GIAOCA_GET_ACTIVE_STAFF.name(), new GiaoCaGetActiveStaffHandler());
                handlers.put(CommandAction.GIAOCA_GET_TOP_STAFF_HOURS.name(), new GiaoCaGetTopStaffHoursHandler());

                // ===== Đơn đặt món =====
                handlers.put(CommandAction.DONDATMON_GET_ALL_CHUA_NHAN.name(), new DonDatMonGetAllChuaNhanHandler());
                handlers.put(CommandAction.DONDATMON_SEARCH_CHUA_NHAN.name(), new DonDatMonSearchChuaNhanHandler());
                handlers.put(CommandAction.DONDATMON_GET_BAN_DA_DAT_TRONG_KHOANG.name(),
                                new DonDatMonGetBanDaDatTrongKhoangHandler());
                handlers.put(CommandAction.DONDATMON_SAVE.name(), new DonDatMonSaveHandler(sessionRegistry));
                handlers.put(CommandAction.DONDATMON_HUY_DAT_BAN.name(), new DonDatMonHuyDatBanHandler(sessionRegistry));
                handlers.put(
                                CommandAction.DONDATMON_GET_DAT_TRUOC_BY_BAN.name(),
                                new DonDatMonGetDatTruocByBanHandler());
            handlers.put(CommandAction.DONDATMON_UPDATE_GHICHU.name(), new DonDatMonUpdateGhiChuHandler());
                handlers.put(
                                CommandAction.DONDATMON_GET_CHUA_NHAN_THEO_BAN_BAO_GOM_LINKED.name(),
                                new DonDatMonGetChuaNhanTheoBanBaoGomLinkedHandler());

                handlers.put(
                                CommandAction.DONDATMON_GET_ALL_CHUA_NHAN_BAO_GOM_LINKED.name(),
                                new DonDatMonGetAllChuaNhanBaoGomLinkedHandler());
                handlers.put(CommandAction.DONDATMON_GET_BY_ID.name(), new DonDatMonGetByIdHandler());
                handlers.put(CommandAction.MONAN_GET_ALL.name(), new MonAnGetAllHandler());

                handlers.put(
                                CommandAction.CHITIETHOADON_GET_BY_MA_DON.name(),
                                new ChiTietHoaDonGetByMaDonHandler());

                handlers.put(
                                CommandAction.CHITIETHOADON_REPLACE_BY_MA_DON.name(),
                                new ChiTietHoaDonReplaceByMaDonHandler());

                handlers.put(CommandAction.HOADON_THANH_TOAN.name(), new HoaDonThanhToanHandler(sessionRegistry));
                handlers.put(CommandAction.HOADON_CAP_NHAT_MA_KM.name(), new HoaDonCapNhatMaKMHandler(sessionRegistry));
                handlers.put(CommandAction.HOADON_CAP_NHAT_MA_KH.name(), new HoaDonCapNhatMaKHHandler(sessionRegistry));
                handlers.put(CommandAction.HOADON_TINH_LAI_GIAM_GIA_VA_TONG_TIEN.name(), new HoaDonTinhLaiGiamGiaVaTongTienHandler());

                handlers.put(CommandAction.CHITIETHOADON_ADD.name(), new ChiTietHoaDonAddHandler(sessionRegistry));
                handlers.put(CommandAction.CHITIETHOADON_UPDATE.name(), new ChiTietHoaDonUpdateHandler(sessionRegistry));
                handlers.put(CommandAction.CHITIETHOADON_DELETE.name(), new ChiTietHoaDonDeleteHandler(sessionRegistry));
        }


        public MessageEnvelope dispatch(ClientSession session, MessageEnvelope request) {
                // request.name đóng vai trò route key đến handler
                if (request.getName() == null) {
                        LOGGER.info("[SocketServer] Bad request: thiếu tên command (messageId="
                                        + request.getMessageId() + ")");
                        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                                        "Thiếu tên command");
                }

                CommandHandler handler = handlers.get(request.getName());
                if (handler == null) {
                        LOGGER.info("[SocketServer] Bad request: command không hỗ trợ: " + request.getName());
                        return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST,
                                        "Command không được hỗ trợ: " + request.getName());
                }

                // Giao xử lý nghiệp vụ cho handler tương ứng
                return handler.handle(session, request);
        }
}
