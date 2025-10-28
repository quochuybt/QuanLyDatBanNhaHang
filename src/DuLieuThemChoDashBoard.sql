USE StarGuardianDB;
GO

-- ------------------------------------------------------------------
-- CẬP NHẬT TRẠNG THÁI BÀN (CHO THỐNG KÊ REAL-TIME)
-- ------------------------------------------------------------------
-- Chuyển 2 bàn 'Trống' thành 'Đang có khách'
UPDATE Ban SET trangThai = N'Đang có khách', gioMoBan = GETDATE() WHERE maBan = N'BAN11';
UPDATE Ban SET trangThai = N'Đang có khách', gioMoBan = GETDATE() WHERE maBan = N'BAN13';
GO

-- ------------------------------------------------------------------
-- TẠO THÊM DỮ LIỆU HÓA ĐƠN
-- ------------------------------------------------------------------

-- ----- ĐƠN HÀNG 8 -----
-- Mục tiêu: NV Chí (NV01103), Ngày 07/10, 250.000đ (Tăng doanh thu cho Chí)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2008', '2025-10-07 19:00:00', N'NV01103', N'KH20251025002', N'BAN01');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2008', N'MA106', 2, 100000.00), -- 2 Cá chiên xù
(N'DON2008', N'MA101', 1, 50000.00);  -- 1 Phở

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251007001', '2025-10-07 19:30:00', 250000.00, N'Đã thanh toán', N'Tiền mặt', 250000.00, N'NV01103', NULL, N'DON2008');
GO

-- ----- ĐƠN HÀNG 9 -----
-- Mục tiêu: NV An (Admin - NV02101), Ngày 08/10, 720.000đ (Cho admin cũng có doanh thu)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2009', '2025-10-08 20:00:00', N'NV02101', N'KH20251025004', N'BAN02');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2009', N'MA105', 2, 350000.00), -- 2 Vang đỏ
(N'DON2009', N'MA103', 2, 10000.00);  -- 2 Nước suối

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251008001', '2025-10-08 21:00:00', 720000.00, N'Đã thanh toán', N'Chuyển khoản', 720000.00, N'NV02101', NULL, N'DON2009');
GO

-- ----- ĐƠN HÀNG 10 -----
-- Mục tiêu: NV Chí (NV01103), Ngày 10/10, 249.000đ (Tăng doanh thu cho Chí)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2010', '2025-10-10 12:00:00', N'NV01103', N'KH20251025005', N'BAN04');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2010', N'MA107', 5, 49000.00), -- 5 Cá tra kho
(N'DON2010', N'MA104', 2, 2000.00); -- Sai giá trà đá (2k) -> test
-- Sửa giá trà đá cho đúng
UPDATE ChiTietHoaDon SET donGia = 3000.00 WHERE maDon = 'DON2010' AND maMonAn = 'MA104';

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251010001', '2025-10-10 13:00:00', 251000.00, N'Đã thanh toán', N'Tiền mặt', 300000.00, N'NV01103', NULL, N'DON2010');
GO

-- ----- ĐƠN HÀNG 11 -----
-- Mục tiêu: NV Bình (NV01102), Ngày 12/10, 180.000đ
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2011', '2025-10-12 18:30:00', N'NV01102', N'KH20251025001', N'BAN05');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2011', N'MA108', 3, 60000.00); -- 3 Cà ri vịt

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251012001', '2025-10-12 19:00:00', 180000.00, N'Đã thanh toán', N'Tiền mặt', 200000.00, N'NV01102', NULL, N'DON2011');
GO

-- ----- ĐƠN HÀNG 12 -----
-- Mục tiêu: NV Chí (NV01103), Ngày 15/10, 450.000đ (Tăng doanh thu cho Chí)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2012', '2025-10-15 20:00:00', N'NV01103', N'KH20251025006', N'BAN08');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2012', N'MA105', 1, 350000.00), -- 1 Vang đỏ
(N'DON2012', N'MA101', 2, 50000.00);  -- 2 Phở

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251015001', '2025-10-15 21:00:00', 450000.00, N'Đã thanh toán', N'Chuyển khoản', 450000.00, N'NV01103', NULL, N'DON2012');
GO

-- ----- ĐƠN HÀNG 13 -----
-- Mục tiêu: NV Dung (NV01104), Ngày 18/10, 300.000đ
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2013', '2025-10-18 19:30:00', N'NV01104', N'KH20251025002', N'BAN09');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2013', N'MA106', 3, 100000.00); -- 3 Cá chiên xù

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251018001', '2025-10-18 20:15:00', 300000.00, N'Đã thanh toán', N'Tiền mặt', 300000.00, N'NV01104', NULL, N'DON2013');
GO

-- ----- ĐƠN HÀNG 14 -----
-- Mục tiêu: NV Chí (NV01103), Ngày 20/10, 120.000đ (Tăng doanh thu cho Chí)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2014', '2025-10-20 18:00:00', N'NV01103', N'KH20251025003', N'BAN10');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2014', N'MA108', 2, 60000.00); -- 2 Cà ri vịt

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251020001', '2025-10-20 18:30:00', 120000.00, N'Đã thanh toán', N'Tiền mặt', 120000.00, N'NV01103', NULL, N'DON2014');
GO

-- ----- ĐƠN HÀNG 15 -----
-- Mục tiêu: NV Bình (NV01102), Ngày 20/10, 200.000đ
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON2015', '2025-10-20 19:00:00', N'NV01102', N'KH20251025005', N'BAN01');

INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
(N'DON2015', N'MA101', 4, 50000.00); -- 4 Phở

INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
(N'HD251020002', '2025-10-20 19:30:00', 200000.00, N'Đã thanh toán', N'Chuyển khoản', 200000.00, N'NV01102', NULL, N'DON2015');
GO

PRINT N'Đã thêm dữ liệu mẫu Dashboard (Phần 2).';
GO