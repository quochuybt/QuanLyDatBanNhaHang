# Socket LAN Architecture (StarGuardian Restaurant)

## Mục tiêu
- Ứng dụng client/server trong LAN, không cần nhập IP thủ công.
- Login-first qua socket.
- Nền tảng gần realtime để mở rộng service khác.

## Luồng tổng quát
1. Client mở `ServerConnectionGUI`.
2. Quét server theo chiến lược:
   - mDNS (`_starguardian._tcp.local.`)
   - fallback UDP broadcast (`9091`)
3. User chọn server -> kết nối TCP `9090`.
4. Mở `TaiKhoanGUI` và gửi command `AUTH_LOGIN`.
5. Server xử lý tại `AuthLoginHandler` và trả response.

## Protocol message
- `COMMAND`
- `RESPONSE`
- `EVENT`
- `PING`
- `PONG`

Envelope dùng chung: `MessageEnvelope`.

## Session policy
- 1 user = 1 phiên.
- Login từ máy mới sẽ kick phiên cũ (`SESSION_KICKED`).

## Thành phần chính
- Server: `SocketServerBootstrap`, `ClientSessionHandler`, `CommandDispatcher`, `SessionRegistry`, `HeartbeatMonitor`.
- Client: `SocketClientConnection`, `AuthRemoteService`, `ServerConnectionGUI`.
- Discovery: `MdnsAnnouncer`, `MdnsDiscoveryClient`, `UdpDiscoveryResponder`, `UdpDiscoveryClient`.
