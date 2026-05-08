# Phân công theo màn hình cho 4 người (bạn là core)

## Vai trò tổng
- **Bạn (Core Owner):** kiến trúc, contract, chuẩn code, review merge gate.
- **Dev A:** màn hình kết nối + xác thực.
- **Dev B:** màn hình hóa đơn + dashboard quản lý.
- **Dev C:** màn hình nhân viên + phân ca + khách hàng (+ bàn sau).

---

## 1) Bạn (Core Owner) — Kiến trúc + chuẩn chung

### Trách nhiệm
1. Chốt protocol/contract:
   - `MessageEnvelope`, `CommandAction`, `ErrorCode`.
2. Giữ chuẩn framework:
   - `BaseCommandHandler`, `BaseRemoteService`.
3. Rule validate request dùng chung.
4. Review PR kiến trúc trước khi merge.
5. Chốt naming event cho Phase 4 realtime.

### Kết quả đầu ra
- Không lệch kiến trúc giữa các nhóm màn hình.
- Không còn duplicate pattern xử lý lỗi/try-catch.

---

## 2) Dev A — Màn hình kết nối + xác thực

### Màn hình phụ trách
- `ServerConnectionGUI`
- `TaiKhoanGUI`

### Nhiệm vụ
1. Ổn định discovery:
   - mDNS ưu tiên, UDP fallback.
2. Cải thiện UX màn kết nối:
   - trạng thái rõ: quét mDNS, fallback UDP, kết nối thành công/thất bại.
3. Login qua socket:
   - xử lý lỗi auth/network rõ ràng.
4. Lifecycle logout -> quay lại màn kết nối đúng flow.

### Definition of Done
- Mở app -> chọn server -> login chạy mượt.
- Không cần nhập IP thủ công.
- Lỗi mạng hiển thị thân thiện.

---

## 3) Dev B — Màn hình Hóa đơn + Dashboard quản lý

### Màn hình phụ trách
- `HoaDonGUI`
- `DashboardQuanLyGUI`

### Nhiệm vụ
1. Hoàn tất socket read cho Hóa đơn:
   - page/filter/search/total/detail.
2. Hoàn tất socket read cho Dashboard quản lý:
   - daily revenue
   - top selling / least selling
   - table status counts
3. Đảm bảo UI không block EDT (`SwingWorker`).

### Definition of Done
- Hóa đơn read chạy end-to-end qua socket.
- Dashboard quản lý read chạy qua socket đúng dữ liệu.
- Không regress pagination/filter/chart.

---

## 4) Dev C — Màn hình Nhân viên + Phân ca + Khách hàng (+ bàn sau)

### Màn hình phụ trách
- `DashboardNhanVienGUI`
- `AssignShiftDialog` / phần lịch làm
- `KhachHangGUI`
- sau đó: `DanhSachBanGUI`, `ManHinhDatBanGUI`, `ManHinhGoiMonGUI`

### Nhiệm vụ
1. Phase 3 pilot (write) cho Phân ca:
   - add/remove/list by date.
2. Migrate khách hàng:
   - search/add/update.
3. Chuẩn bị mở rộng cụm bàn/đặt bàn/gọi món sau khi phân ca + khách hàng ổn.

### Definition of Done
- Phân ca write chạy qua socket hoàn chỉnh.
- Khách hàng CRUD cơ bản chạy qua socket.
- UI flow ổn định, không treo.

---

## Thứ tự triển khai theo đợt

### Đợt 1
- Dev A: connection + login
- Dev B: hóa đơn read
- Dev C: phân ca write pilot

### Đợt 2
- Dev B: dashboard quản lý read
- Dev C: khách hàng CRUD

### Đợt 3
- Dev C: cụm bàn/đặt bàn/gọi món
- Bạn: bật event realtime theo Phase 4

---

## Quy tắc merge chung
Mọi PR phải đạt:
1. Handler dùng `BaseCommandHandler`.
2. Remote service dùng `BaseRemoteService`.
3. Error mapping dùng `ErrorCode` chuẩn.
4. GUI call mạng ngoài EDT.
5. Log tiếng Việt + giữ keyword kỹ thuật (`session`, `payload`, `event`, `command`, `timeout`, `fallback`).
