# Task board Phase 2 (Hóa đơn read + Dashboard read)

## Ticket P2-01 — Bổ sung CommandAction cho Hóa đơn + Dashboard
- Owner: Người 1
- Files:
  - `core/net/protocol/CommandAction.java`
- Thêm action:
  - `HOADON_GET_BY_PAGE`
  - `HOADON_GET_TOTAL`
  - `HOADON_GET_DETAIL`
  - `DASHBOARD_DAILY_REVENUE`
  - `DASHBOARD_TOP_SELLING`
  - `DASHBOARD_LEAST_SELLING`
  - `DASHBOARD_TABLE_STATUS_COUNTS`
- Done khi compile pass, naming action thống nhất.

## Ticket P2-02 — DTO transport cho Hóa đơn read
- Owner: Người 3
- Files (mới):
  - `core/net/dto/hoadon/HoaDonPageRequestDTO.java`
  - `core/net/dto/hoadon/HoaDonDetailRequestDTO.java`
  - `core/net/dto/hoadon/HoaDonTotalRequestDTO.java`
- Done khi DTO đủ field cho filter/page/detail.

## Ticket P2-03 — DTO transport cho Dashboard read
- Owner: Người 3
- Files (mới):
  - `core/net/dto/dashboard/DashboardRangeRequestDTO.java`
  - `core/net/dto/dashboard/TopItemsRequestDTO.java`
- Done khi DTO đủ cho daily revenue + top/least + table status.

## Ticket P2-04 — Server handlers Hóa đơn
- Owner: Người 3
- Files (mới):
  - `core/net/server/handler/HoaDonGetByPageHandler.java`
  - `core/net/server/handler/HoaDonGetTotalHandler.java`
  - `core/net/server/handler/HoaDonGetDetailHandler.java`
- Done khi handler parse/validate/call service/response chuẩn.

## Ticket P2-05 — Server handlers Dashboard
- Owner: Người 3
- Files (mới):
  - `core/net/server/handler/DashboardDailyRevenueHandler.java`
  - `core/net/server/handler/DashboardTopSellingHandler.java`
  - `core/net/server/handler/DashboardLeastSellingHandler.java`
  - `core/net/server/handler/DashboardTableStatusHandler.java`
- Done khi handler gọi service tương ứng và trả payload đúng.

## Ticket P2-06 — Đăng ký handlers vào dispatcher
- Owner: Người 1
- Files:
  - `core/net/server/dispatch/CommandDispatcher.java`
- Done khi toàn bộ action Phase 2 route được.

## Ticket P2-07 — Client facade cho Hóa đơn
- Owner: Người 2
- Files (mới):
  - `core/net/client/HoaDonRemoteService.java`
- Done khi có methods getByPage/getTotal/getDetail và map lỗi chuẩn.

## Ticket P2-08 — Client facade cho Dashboard
- Owner: Người 2
- Files (mới):
  - `core/net/client/DashboardRemoteService.java`
- Done khi có methods cho 4 action dashboard read.

## Ticket P2-09 — Tích hợp HoaDonGUI
- Owner: Người 3
- Files:
  - `gui/HoaDonGUI.java`
- Done khi bảng hóa đơn + chi tiết dùng remote service và không block EDT.

## Ticket P2-10 — Tích hợp Dashboard GUI
- Owner: Người 3
- Files:
  - `gui/DashboardQuanLyGUI.java`
  - `gui/DashboardNhanVienGUI.java`
- Done khi phần dữ liệu read chuyển sang remote.

## Ticket QA-02 — Test matrix Phase 2
- Owner: Người 4
- Test chính:
  - Hóa đơn filter/search/page/detail
  - Dashboard chart/top/least/table status
  - Server down/timeout
- Done khi có báo cáo pass/fail + bug list.

## Ticket DOC-02 — Update docs sau Phase 2
- Owner: Người 4
- Files:
  - `docs/socket-feature-migration-end-to-end.md`
  - `docs/protocol-reference.md`
- Done khi docs phản ánh đúng command/DTO mới.
