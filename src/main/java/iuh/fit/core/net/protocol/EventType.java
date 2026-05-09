package iuh.fit.core.net.protocol;

public enum EventType {
    SESSION_KICKED,
    SERVER_NOTICE,

    // Realtime nghiệp vụ giữa các máy trong LAN
    TABLE_STATUS_CHANGED,
    SHIFT_UPDATED,
    INVOICE_UPDATED
}
