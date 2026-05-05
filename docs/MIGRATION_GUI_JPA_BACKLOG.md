# Migration Backlog: GUI cũ -> JPA mới (`iuh.fit`)

Tài liệu này là checklist thực thi để chuyển các màn hình cũ từ `src/gui` sang `src/main/java/iuh/fit/gui`.

## 1) Phạm vi

Các màn hình cần migrate:

1. `KhachHangGUI`
2. `KhuyenMaiGUI`
3. `LichLamViecGUI`
4. `MainGUI`
5. `ManHinhBanGUI`
6. `ManHinhDatBanGUI`
7. `ManHinhGoiMonGUI`
8. `MonAnDialog`

Nguyên tắc:

- GUI mới chỉ gọi `core.service.*`
- Không import `dao.*`, `entity.*` cũ
- Ưu tiên tạo class mới trong `iuh.fit.gui`, giữ class cũ để rollback

---

## 2) Mapping tổng quát DAO cũ -> Service/Repository mới

| DAO cũ | Service/Repository mới đề xuất | Ghi chú |
|---|---|---|
| `KhachHangDAO` | `KhachHangService`, `KhachHangRepository` | Đã có service/repo |
| `KhuyenMaiDAO` | `KhuyenMaiService`, `KhuyenMaiRepository` | Đã có service/repo |
| `PhanCongDAO` | `PhanCongService`, `PhanCongRepository` | Đã có service/repo |
| `NhanVienDAO` | `NhanVienService`, `NhanVienRepository` | Đã có service/repo |
| `MonAnDAO` | `MonAnService`, `MonAnRepository` | Đã có service/repo |
| `DanhMucMonDAO` | `DanhMucMonRepository` (+ tạo `DanhMucMonService`) | Chưa có service, cần bổ sung |
| `BanDAO` | `BanRepository` (+ tạo `BanService`) | Chưa có service, cần bổ sung |
| `DonDatMonDAO` | `DonDatMonService`, `DonDatMonRepository` | Có service, cần mở rộng method nghiệp vụ |
| `HoaDonDAO` | `HoaDonService`, `HoaDonRepository` | Có service, cần mở rộng transaction nghiệp vụ |
| `ChiTietHoaDonDAO` | `ChiTietHoaDonRepository` (+ tạo `ChiTietHoaDonService`) | Chưa có service, cần bổ sung |
| `CaLamDAO` (nếu có dùng) | `CaLamRepository` (+ tạo `CaLamService`) | Có repo, chưa có service |
| `GiaoCaDAO` | `GiaoCaService`, `GiaoCaRepository` | Đã có service/repo |

---

## 3) Backlog chi tiết theo màn hình

## 3.1 `MonAnDialog` (ưu tiên cao)

### File nguồn cũ

- `src/gui/MonAnDialog.java`

### File đích

- `src/main/java/iuh/fit/gui/MonAnDialog.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| `monAnDAO.getNextMaMonAn()` | `MonAnService.getNextMaMonAn()` |
| `danhMucMonDAO.getAllDanhMuc()` | `DanhMucMonService.findAll()` |
| Lưu món mới | `MonAnService.save(MonAnDTO)` hoặc `save(MonAn)` |
| Cập nhật món | `MonAnService.update(MonAnDTO)` hoặc `update(MonAn)` |

### Task implement

- [x] Tạo `DanhMucMonService`
- [x] Thêm method `getNextMaMonAn` trong `MonAnService`
- [x] Copy UI cũ sang package `iuh.fit.gui`
- [x] Thay import entity sang `iuh.fit.core.entity.*`
- [x] Thay toàn bộ DAO call bằng service call
- [ ] Test thêm/sửa món, chọn danh mục, upload ảnh

---

## 3.2 `KhachHangGUI`

### File nguồn cũ

- `src/gui/KhachHangGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/KhachHangGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| `getAllKhachHang()` | `KhachHangService.findAll()` |
| Thêm khách hàng | `KhachHangService.save(KhachHangDTO)` |
| Sửa khách hàng | `KhachHangService.update(KhachHangDTO)` |
| Tìm theo từ khóa/SDT | `KhachHangService.search(...)` (cần thêm nếu thiếu) |
| Tìm theo mã | `KhachHangService.findById(maKH)` |

### Task implement

- [x] Rà field mapping `KhachHang` (giới tính, ngày sinh, hạng TV, tổng chi tiêu)
- [x] Migrate form parse date `dd/MM/yyyy` -> `LocalDate`
- [x] Chuyển logic tính/refresh hạng thành viên vào service mức cơ bản
- [x] Hoàn tất add/edit/search và reload table
- [ ] Test role NHANVIEN dùng màn hình qua MainGUI

---

## 3.3 `KhuyenMaiGUI`

### File nguồn cũ

- `src/gui/KhuyenMaiGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/KhuyenMaiGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| `getAllKhuyenMai()` | `KhuyenMaiService.findAll()` |
| `timKiemVaLoc(tuKhoa, trangThai)` | `KhuyenMaiService.searchAndFilter(...)` |
| `updateKhuyenMai(km)` | `KhuyenMaiService.update(...)` |
| Ngưng áp dụng | `KhuyenMaiService.setTrangThai(maKM, "Ngưng áp dụng")` |
| Kiểm tra điều kiện mã | `KhuyenMaiService.kiemTraDieuKienSuDung(...)` |

### Task implement

- [x] Bổ sung xử lý search/filter ở GUI với data từ service
- [x] Migrate dialog thêm/sửa khuyến mãi
- [x] Chuẩn hóa enum/string trạng thái KM
- [ ] Test filter + tìm kiếm + ngưng áp dụng

---

## 3.4 `LichLamViecGUI`

### File nguồn cũ

- `src/gui/LichLamViecGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/LichLamViecGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| `getPhanCongChiTiet(dauTuan, cuoiTuan)` | `PhanCongService.findByDateRange(...)` |
| Lấy nhân viên cho phân ca | `NhanVienService.findAll()` / filter theo vai trò |
| Tạo phân ca | `PhanCongService.save(...)` |
| Xóa/đổi phân ca | `PhanCongService.delete(...)` / `update(...)` |

### Task implement

- [x] Bổ sung `AssignShiftDialog` dùng service mới
- [x] Migrate màn hình lịch làm việc bản mới (dạng bảng)
- [ ] Hoàn thiện UI tuần 7 cột như bản cũ
- [ ] Test quyền QUANLY: mở dialog phân ca, sửa/xóa ca
- [ ] Test quyền NHANVIEN: chỉ xem

---

## 3.5 `MainGUI`

### File nguồn cũ

- `src/gui/MainGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/MainGUI.java`

### Mapping màn hình con

| Menu item | Panel mới |
|---|---|
| Dashboard | `DashboardGUI` / `DashboardNhanVienGUI` |
| Danh mục món ăn | `DanhMucMonGUI` |
| Lịch làm việc | `LichLamViecGUI` (mới) |
| Khuyến mãi | `KhuyenMaiGUI` (mới) |
| Hóa đơn | panel hóa đơn mới hiện tại |
| Nhân viên | `NhanVienGUI` mới nếu có / tạm cũ |
| Danh sách bàn | `DanhSachBanGUI` |
| Thành viên | `KhachHangGUI` (mới) |

### Task implement

- [x] Dùng CardLayout và mount panel trong package `iuh.fit.gui` (trong `DashboardGUI`)
- [x] Chuẩn hóa truyền `userRole`, `userName`, `maNV`
- [x] Thêm class `MainGUI` tương thích để chuẩn tên migration
- [x] Kiểm tra/logout quay về `LoginGUI`

---

## 3.6 `ManHinhDatBanGUI`

### File nguồn cũ

- `src/gui/ManHinhDatBanGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/ManHinhDatBanGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| Tải bàn trống | `BanService.findAll()` + filter trạng thái |
| Tạo phiếu đặt trước | `DonDatMonService.createReservation(...)` |
| Tìm khách qua SDT | `KhachHangService.findByPhone(...)` |
| Gợi ý ghép bàn | `BanService.suggestTables(...)` hoặc giữ tạm tại GUI |
| Danh sách phiếu đặt | `DonDatMonService.findReservations(...)` |

### Task implement

- [x] Tạo `BanService`
- [x] Dùng API hiện có của `DonDatMonService` để kiểm tra bàn đã đặt theo khoảng giờ
- [x] Migrate logic chọn bàn và xác nhận đặt (phiên bản nền)
- [ ] Test đặt bàn theo ngày/giờ/số khách

---

## 3.7 `ManHinhGoiMonGUI`

### File nguồn cũ

- `src/gui/ManHinhGoiMonGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/ManHinhGoiMonGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| Mở bàn mới + tạo đơn + tạo HĐ | `HoaDonService.moBanVaTaoHoaDon(...)` (transaction) |
| Lấy HĐ chưa thanh toán theo bàn | `HoaDonService.getHoaDonChuaThanhToan(maBan)` |
| Lấy menu món ăn | `MonAnService.findAll()` / filter theo danh mục |
| Lưu chi tiết món gọi | `ChiTietHoaDonService.saveOrUpdateByOrder(...)` |
| Nhận bàn đặt trước | `DonDatMonService.nhanBanDatTruoc(...)` |

### Task implement

- [x] Tạo `ChiTietHoaDonService`
- [x] Migrate add/remove món và cập nhật tổng bill
- [x] Lưu/nạp chi tiết món theo `maDon`
- [x] Viết transaction mở bàn + tạo đơn + tạo hóa đơn trong `HoaDonService`
- [ ] Test luồng: bàn trống -> mở bàn -> gọi món -> lưu

---

## 3.8 `ManHinhBanGUI`

### File nguồn cũ

- `src/gui/ManHinhBanGUI.java`

### File đích

- `src/main/java/iuh/fit/gui/ManHinhBanGUI.java`

### Mapping method

| Nghiệp vụ cũ | API mới cần có |
|---|---|
| Danh sách bàn + trạng thái | `BanService.findAll()` + thống kê |
| HĐ active theo bàn | `HoaDonService.getHoaDonChuaThanhToan(maBan)` |
| Áp mã khuyến mãi | `HoaDonService.capNhatMaKM(maHD, maKM)` + `KhuyenMaiService.kiemTraDieuKien...` |
| Thanh toán hóa đơn | `HoaDonService.thanhToanHoaDon(...)` (transaction) |
| Cập nhật trạng thái bàn sau thanh toán | `BanService.updateTrangThai(...)` trong cùng transaction |

### Task implement

- [x] Migrate màn hình quản lý bàn bản mới + tab Đặt bàn/Gọi món
- [ ] Hoàn thiện panel chi tiết bill theo bàn như bản cũ
- [ ] Di chuyển logic tính tổng, giảm giá vào service nơi hợp lý
- [ ] Hoàn tất flow thanh toán end-to-end
- [ ] Test đa case: bàn trống/đặt trước/đang phục vụ

---

## 4) Backlog bổ sung ở tầng Service

Các service cần tạo mới hoặc mở rộng:

- [x] `BanService` (mới)
- [x] `DanhMucMonService` (mới)
- [x] `ChiTietHoaDonService` (mới)
- [x] `CaLamService`
- [x] Bổ sung transaction methods trong `HoaDonService`
- [ ] Bổ sung reservation methods trong `DonDatMonService`
- [ ] Bổ sung search/filter methods trong `KhuyenMaiService`, `KhachHangService`

---

## 5) Thứ tự thực hiện khuyến nghị

1. `MonAnDialog`
2. `KhachHangGUI`
3. `KhuyenMaiGUI`
4. `LichLamViecGUI`
5. `MainGUI`
6. `ManHinhDatBanGUI`
7. `ManHinhGoiMonGUI`
8. `ManHinhBanGUI`

---

## 6) Checklist nghiệm thu cho mỗi màn hình

- [ ] Build: `mvn clean package` pass
- [ ] Không còn import `dao.*`, `entity.*` cũ
- [ ] Chạy được từ `iuh.fit.gui.MainGUI`
- [ ] CRUD/chức năng chính hoạt động đúng với MariaDB Docker
- [ ] Không phá flow đăng nhập và phân quyền

---

## 7) Lệnh test nhanh sau mỗi cụm migrate

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="iuh.fit.gui.LoginGUI"
```

Nếu cần rollback nhanh, tạm chuyển card/menu về panel cũ trong `MainGUI`.
