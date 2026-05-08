# Server Connection Screen Guide

## Màn hình
`ServerConnectionGUI`

## Hành vi
1. Tự quét server khi mở màn hình.
2. Hiển thị danh sách server + nguồn phát hiện (`mDNS` hoặc `UDP`).
3. User chọn server và bấm **Kết nối**.
4. Thành công -> mở `TaiKhoanGUI(connection, selectedServer)`.

## Trạng thái hiển thị
- Đang quét server
- Đã tìm thấy qua mDNS
- Không thấy mDNS, chuyển UDP fallback
- Không tìm thấy server
- Đang kết nối
- Kết nối thất bại

## UX đề xuất tiếp theo
- Nút “Quét lại”.
- Lưu server đã chọn gần nhất.
- Thêm trường nhập IP thủ công trong mục nâng cao.
