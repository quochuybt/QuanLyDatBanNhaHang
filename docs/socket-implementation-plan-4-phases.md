# Kế hoạch triển khai Socket theo 4 phase (đã chốt scope)

**Trạng thái scope đã chốt:**
- Phase 2: gồm **Hóa đơn read-only + Dashboard read**
- Realtime event push để **Phase 4**

---

## Tổng quan phân công 4 người

- **Người 1 (Platform Server):** hạ tầng server, dispatcher, handler template, lifecycle/log.
- **Người 2 (Platform Client + Discovery + Connection UX):** socket client core, mDNS/UDP, màn kết nối.
- **Người 3 (Feature Migration):** migrate feature sang socket theo từng wave.
- **Người 4 (QA + Docs + Release Gate):** test matrix, regression, docs, nghiệm thu.

---

## Phase 1 — Ổn định nền tảng (Platform)

### Mục tiêu
Khóa kiến trúc server/client để đội feature làm nhanh và ít bug.

### Scope
1. Chuẩn hóa `CommandAction`, `ErrorCode`, quy tắc mapping lỗi.
2. Handler template dùng chung + refactor `AuthLoginHandler` làm chuẩn.
3. Cứng hóa `CommandDispatcher` (guard request null/type/name).
4. Cứng hóa lifecycle server (`start/stop`), log lỗi cổng bận rõ ràng.
5. Discovery mDNS ưu tiên, UDP fallback ổn định.
6. Chuẩn log tiếng Việt + keyword kỹ thuật không dịch.

### Owner
- Chính: Người 1, Người 2
- Hỗ trợ test/doc: Người 4

### Definition of Done
- Login socket chạy ổn định.
- Discovery hoạt động trong LAN (mDNS -> UDP fallback).
- BindException/log timeout rõ ràng, không mơ hồ.

---

## Phase 2 — Migrate feature đọc dữ liệu

### Mục tiêu
Chuyển các màn đọc dữ liệu quan trọng sang socket.

### Scope chốt

## A) Hóa đơn (read-only)
- `HOADON_GET_BY_PAGE`
- `HOADON_GET_TOTAL`
- `HOADON_GET_DETAIL`

## B) Dashboard (read)
- `DASHBOARD_DAILY_REVENUE`
- `DASHBOARD_TOP_SELLING`
- `DASHBOARD_LEAST_SELLING`
- `DASHBOARD_TABLE_STATUS_COUNTS`

> Tên action có thể điều chỉnh theo convention code hiện tại, nhưng phải nhất quán toàn hệ thống.

### Deliverables kỹ thuật
1. DTO transport cho từng command.
2. Server handlers + đăng ký dispatcher.
3. Client remote services:
   - `HoaDonRemoteService`
   - `DashboardRemoteService`
4. GUI integration:
   - `HoaDonGUI` dùng remote
   - `DashboardQuanLyGUI`/`DashboardNhanVienGUI` phần read dùng remote

### Owner
- Chính: Người 3
- Support platform: Người 1, Người 2
- QA/Doc: Người 4

### Definition of Done
- Hóa đơn: filter/search/page/detail chạy qua socket.
- Dashboard: widget read data chạy qua socket.
- GUI không block EDT.

---

## Phase 3 — Migrate feature ghi dữ liệu (write-critical)

### Mục tiêu
Chuyển các nghiệp vụ ghi cốt lõi sang socket.

### Scope
1. Phân ca:
   - add/remove/list
2. Bàn/Đặt bàn/Gọi món:
   - mở bàn/tạo đơn/cập nhật món/chuyển bàn-ghép bàn
3. Khách hàng:
   - add/update/search

### Deliverables
- Command + DTO + handler + remote service + GUI integration cho từng cụm.

### Owner
- Chính: Người 3
- Platform support: Người 1/2
- QA/Doc: Người 4

### Definition of Done
- Luồng ghi dữ liệu chạy qua socket đúng nghiệp vụ.
- Validation lỗi trả `BAD_REQUEST` nhất quán.

---

## Phase 4 — Realtime + Hardening + Release

### Mục tiêu
Kích hoạt event push realtime và đóng gói phát hành LAN.

### Scope
1. Event push:
   - `INVOICE_UPDATED`
   - `TABLE_STATUS_CHANGED`
   - `SHIFT_UPDATED`
2. Client listeners cập nhật UI realtime.
3. Soak test LAN + reconnect scenarios.
4. Release docs đầy đủ + UAT checklist.

### Owner
- Chính: Người 1, Người 2 (hạ tầng event)
- Feature hook: Người 3
- QA/Release: Người 4

### Definition of Done
- Màn chính cập nhật gần realtime.
- Chạy ổn định tối thiểu 1–2 giờ trong LAN test.

---

## Kế hoạch tuần khởi động (thực thi ngay)

### Ngày 1
- Người 1: chốt handler template + dispatcher guard.
- Người 2: discovery status UX và fallback path.
- Người 3: thiết kế DTO/action cho Hóa đơn + Dashboard read.
- Người 4: test matrix + acceptance criteria.

### Ngày 2
- Người 1: lifecycle/log hardening hoàn tất.
- Người 3: implement handler Hóa đơn read.
- Người 2: hỗ trợ client facade pattern.
- Người 4: integration test vòng 1.

### Ngày 3
- Người 3: implement handler Dashboard read + GUI wiring.
- Người 2: kiểm soát threading UI/retry timeout.
- Người 4: regression + bug report.

### Ngày 4
- Bugfix chéo + stabilize.

### Ngày 5
- Freeze Phase 2.
- Cập nhật docs + Go/No-Go sang Phase 3.

---

## Ràng buộc kỹ thuật bắt buộc

1. Không gửi entity qua socket.
2. Mọi command phải có DTO transport rõ ràng.
3. Mọi GUI call mạng phải ngoài EDT (`SwingWorker`).
4. Log tiếng Việt, giữ keyword kỹ thuật (`session`, `payload`, `event`, `command`, `timeout`, `fallback`).
