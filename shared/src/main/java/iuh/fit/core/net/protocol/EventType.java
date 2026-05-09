package iuh.fit.core.net.protocol;

public enum EventType {
    SESSION_KICKED,
    SERVER_NOTICE,

    // Realtime nghiệp vụ giữa các máy trong LAN
    TABLE_STATUS_CHANGED,
    SHIFT_UPDATED,
    INVOICE_UPDATED,

    // Món ăn & danh mục
    MENU_UPDATED,

    // Khuyến mãi
    KHUYENMAI_UPDATED,

    // Khách hàng
    KHACHHANG_UPDATED,

    // Nhân viên
    NHANVIEN_UPDATED,

    // Đơn đặt món
    DONDATMON_UPDATED,

    // Giao ca
    GIAOCA_UPDATED
}
