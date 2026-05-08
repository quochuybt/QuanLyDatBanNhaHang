package iuh.fit.core.net.protocol;

public enum ErrorCode {
    // ===== Nhóm lỗi xác thực (Authentication) =====
    // Sai thông tin đăng nhập (user/pass không đúng)
    AUTH_INVALID,
    // Tài khoản tồn tại nhưng đang bị khóa/vô hiệu hóa
    AUTH_LOCKED,

    // ===== Nhóm lỗi request/protocol =====
    // Payload thiếu field bắt buộc, sai format, hoặc command không hợp lệ
    BAD_REQUEST,

    // ===== Nhóm lỗi hệ thống/server =====
    // Lỗi nội bộ phía server khi xử lý nghiệp vụ
    SERVER_ERROR,
    // Client không nhận được phản hồi trong timeout hoặc không connect được server
    SERVER_UNREACHABLE,

    // ===== Nhóm lỗi discovery (tìm server trong LAN) =====
    // Không tìm thấy server qua mDNS/UDP trong cùng mạng LAN
    DISCOVERY_NOT_FOUND
}
