-- =========================================================
-- StarGuardianDB - Fix ngày dữ liệu mẫu về gần hiện tại
-- DB: MariaDB
-- Cách dùng: chạy file INSERT gốc trước, sau đó chạy file này.
-- Ghi chú:
--   - Không đổi mã khóa chính như maHD/maDon/maKH để tránh lỗi khóa ngoại.
--   - Chỉ kéo các cột ngày nghiệp vụ về gần hiện tại.
--   - HoaDon/DonDatMon/GiaoCa kết thúc ở hôm qua.
--   - PhanCongCa kéo lịch tới hôm nay + 30 ngày.
-- =========================================================

USE StarGuardianDB;

-- =========================================================
-- 1. Kéo DonDatMon + HoaDon về gần hiện tại
-- Max ngày hóa đơn cũ sẽ thành ngày hôm qua.
-- =========================================================
SET @target_order_max_date = CURDATE() - INTERVAL 1 DAY;

SELECT @order_offset_days := DATEDIFF(@target_order_max_date, DATE(MAX(ngayLap)))
FROM HoaDon;

UPDATE DonDatMon
SET ngayKhoiTao = DATE_ADD(ngayKhoiTao, INTERVAL @order_offset_days DAY),
    thoiGianDen = DATE_ADD(thoiGianDen, INTERVAL @order_offset_days DAY)
WHERE ngayKhoiTao IS NOT NULL;

UPDATE HoaDon
SET ngayLap = DATE_ADD(ngayLap, INTERVAL @order_offset_days DAY)
WHERE ngayLap IS NOT NULL;

-- =========================================================
-- 2. Kéo GiaoCa về gần hiện tại
-- Max thời gian kết thúc ca cũ sẽ thành ngày hôm qua.
-- =========================================================
SELECT @giaoca_offset_days := DATEDIFF(@target_order_max_date, DATE(MAX(thoiGianKetThuc)))
FROM GiaoCa;

UPDATE GiaoCa
SET thoiGianBatDau = DATE_ADD(thoiGianBatDau, INTERVAL @giaoca_offset_days DAY),
    thoiGianKetThuc = DATE_ADD(thoiGianKetThuc, INTERVAL @giaoca_offset_days DAY)
WHERE thoiGianBatDau IS NOT NULL;

-- =========================================================
-- 3. Kéo lịch phân công ca về hiện tại + 30 ngày
-- =========================================================
SET @target_schedule_max_date = CURDATE() + INTERVAL 30 DAY;

SELECT @phancong_offset_days := DATEDIFF(@target_schedule_max_date, MAX(ngayLam))
FROM PhanCongCa;

UPDATE PhanCongCa
SET ngayLam = DATE_ADD(ngayLam, INTERVAL @phancong_offset_days DAY)
WHERE ngayLam IS NOT NULL;

-- =========================================================
-- 4. Cập nhật ngày tham gia khách hàng gần đây hơn
-- Không sửa ngaySinh để không ảnh hưởng validate đủ 18 tuổi.
-- Khách mới nhất sẽ cách hôm nay 7 ngày.
-- =========================================================
SET @target_customer_max_date = CURDATE() - INTERVAL 7 DAY;

SELECT @khachhang_offset_days := DATEDIFF(@target_customer_max_date, MAX(ngayThamGia))
FROM KhachHang
WHERE ngayThamGia IS NOT NULL;

UPDATE KhachHang
SET ngayThamGia = DATE_ADD(ngayThamGia, INTERVAL @khachhang_offset_days DAY)
WHERE ngayThamGia IS NOT NULL;

-- =========================================================
-- 5. Cập nhật khuyến mãi đang áp dụng quanh hiện tại
-- =========================================================
UPDATE KhuyenMai
SET ngayBatDau = CURDATE() - INTERVAL 30 DAY,
    ngayKetThuc = CURDATE() + INTERVAL 90 DAY,
    trangThai = 'Đang áp dụng'
WHERE maKM = 'KMHE2025';

UPDATE KhuyenMai
SET ngayBatDau = CURDATE() - INTERVAL 15 DAY,
    ngayKetThuc = CURDATE() + INTERVAL 60 DAY,
    trangThai = 'Đang áp dụng'
WHERE maKM = 'GIAM20K';

UPDATE KhuyenMai
SET ngayBatDau = STR_TO_DATE(CONCAT(YEAR(CURDATE()), '-01-01'), '%Y-%m-%d'),
    ngayKetThuc = STR_TO_DATE(CONCAT(YEAR(CURDATE()), '-12-31'), '%Y-%m-%d'),
    trangThai = 'Đang áp dụng'
WHERE maKM = 'SINHNHAT';

-- =========================================================
-- 6. Nếu có bàn đang mở sẵn thì kéo giờ mở bàn theo hóa đơn
-- =========================================================
UPDATE Ban
SET gioMoBan = DATE_ADD(gioMoBan, INTERVAL @order_offset_days DAY)
WHERE gioMoBan IS NOT NULL;

-- =========================================================
-- 7. Cập nhật lại tổng chi tiêu và hạng thành viên theo MariaDB
-- Bản gốc dùng ISNULL kiểu SQL Server, MariaDB dùng IFNULL.
-- =========================================================
UPDATE KhachHang kh
SET kh.tongChiTieu = (
    SELECT IFNULL(SUM(hd.tongTien), 0)
    FROM HoaDon hd
    INNER JOIN DonDatMon ddm ON hd.maDon = ddm.maDon
    WHERE ddm.maKH = kh.maKH
      AND hd.trangThai = 'Đã thanh toán'
);

UPDATE KhachHang
SET hangThanhVien = CASE
    WHEN tongChiTieu >= 50000000 THEN 'DIAMOND'
    WHEN tongChiTieu >= 25000000 THEN 'GOLD'
    WHEN tongChiTieu >= 10000000 THEN 'SILVER'
    WHEN tongChiTieu >= 5000000  THEN 'BRONZE'
    ELSE 'MEMBER'
END
WHERE hangThanhVien <> 'NONE';

-- =========================================================
-- 8. Kiểm tra nhanh sau khi sửa
-- =========================================================
SELECT MIN(ngayLap) AS minHoaDon, MAX(ngayLap) AS maxHoaDon
FROM HoaDon;

SELECT MIN(ngayKhoiTao) AS minDonDatMon, MAX(ngayKhoiTao) AS maxDonDatMon
FROM DonDatMon;

SELECT MIN(ngayLam) AS minPhanCong, MAX(ngayLam) AS maxPhanCong
FROM PhanCongCa;

SELECT MIN(thoiGianBatDau) AS minGiaoCa, MAX(thoiGianKetThuc) AS maxGiaoCa
FROM GiaoCa;

SELECT maKM, ngayBatDau, ngayKetThuc, trangThai
FROM KhuyenMai;
