# LAN Deployment Guide

## Cổng sử dụng
- TCP app: `9090`
- UDP discovery: `9091`
- mDNS multicast: `5353` (224.0.0.251)

## Chạy server
Chạy class `iuh.fit.ServerMain`.

## Chạy client
Chạy class `iuh.fit.ClientMain`.

## Firewall checklist
- Cho phép Java process lắng nghe TCP 9090.
- Cho phép UDP 9091 trong LAN.
- Cho phép mDNS multicast nếu mạng hỗ trợ.

## Kiểm thử nhanh
1. Mở server.
2. Mở client ở máy cùng LAN.
3. Client phải thấy server trong `ServerConnectionGUI`.
4. Login thành công qua socket.

## Ghi chú mạng
- Một số mạng doanh nghiệp có thể chặn multicast, khi đó mDNS sẽ không hoạt động.
- Hệ thống sẽ fallback sang UDP discovery tự động.
