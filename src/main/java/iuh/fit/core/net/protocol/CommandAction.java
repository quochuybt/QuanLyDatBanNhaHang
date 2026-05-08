package iuh.fit.core.net.protocol;

public enum CommandAction {
    AUTH_LOGIN,
    AUTH_LOGOUT,
    PING,

    // ===== Hóa đơn (read-only) =====
    HOADON_GET_BY_PAGE,
    HOADON_GET_TOTAL,
    HOADON_GET_DETAIL,

    // ===== Dashboard (read) =====
    DASHBOARD_DAILY_REVENUE,
    DASHBOARD_TOP_SELLING,
    DASHBOARD_LEAST_SELLING,
    DASHBOARD_TABLE_STATUS_COUNTS,

    // ===== Phase 3 (write features) - skeleton =====
    PHANCONG_ADD,
    PHANCONG_REMOVE,
    PHANCONG_LIST_BY_DATE,

    BAN_UPDATE_STATUS,
    BAN_CHUYEN_BAN,
    BAN_GHEP_BAN,

    KHACHHANG_ADD,
    KHACHHANG_UPDATE,
    KHACHHANG_SEARCH
}
