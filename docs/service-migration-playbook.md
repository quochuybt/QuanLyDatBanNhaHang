# Service Migration Playbook (Socket)

## Mục tiêu
Chuẩn hóa cách đưa các service hiện tại từ local-call sang client/server socket.

## Quy trình chuẩn
1. Định nghĩa `CommandAction` mới.
2. Tạo DTO transport request/response cho nghiệp vụ.
3. Tạo server `Handler` gọi service hiện có.
4. Đăng ký handler vào `CommandDispatcher`.
5. Tạo client facade (`*RemoteService`).
6. Cập nhật GUI gọi remote facade thay local service.

## Ví dụ áp dụng

### Hóa đơn (read-first)
- `HOADON_GET_BY_PAGE`
- `HOADON_GET_TOTAL`
- `HOADON_GET_DETAIL`

### Phân ca
- `PHANCONG_ADD`
- `PHANCONG_REMOVE`
- `PHANCONG_LIST_BY_DATE`

### Khách hàng
- `KH_ADD`
- `KH_UPDATE`
- `KH_SEARCH`

## Event push gợi ý
- `INVOICE_UPDATED`
- `TABLE_STATUS_CHANGED`
- `SHIFT_UPDATED`

## Checklist test mỗi service
- Command hợp lệ -> response đúng.
- Validation lỗi -> `BAD_REQUEST`.
- Lỗi nghiệp vụ -> error code đúng.
- Mất kết nối -> GUI không treo.
