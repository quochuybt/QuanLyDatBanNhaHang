-- Sử dụng CSDL đã tạo
USE StarGuardianDB;
GO

-- 1. Bảng Vai Trò (Không phụ thuộc)
INSERT INTO VaiTro (tenVaiTro) VALUES (N'NhanVien'), (N'QuanLy');

-- 2. Bảng Hạng Thành Viên (Không phụ thuộc)
INSERT INTO HangThanhVien (tenHang) VALUES (N'NONE'), (N'MEMBER'), (N'BRONZE'), (N'SILVER'), (N'GOLD'), (N'DIAMOND');

-- 3. Bảng Tài Khoản (Không phụ thuộc)
INSERT INTO TaiKhoan (tenTK, matKhau, trangThai) VALUES
-- Mật khẩu thô: 'admin12345' -> hashCode: -1657892252
(N'admin', N'hashed_643610180', 1),

-- Mật khẩu thô: 'binh12345' -> hashCode: -1893320093
(N'nv_binh', N'hashed_-860005230', 1),

-- Mật khẩu thô: 'chi12345' -> hashCode: 1271770635
(N'nv_chi', N'hashed_1603487247', 1),

-- Mật khẩu thô: 'dung12345' -> hashCode: -1880595469
(N'nv_dung', N'hashed_-769688471', 1),

-- Mật khẩu thô: 'duy12345' -> hashCode: 1271929021
(N'nv_duy', N'hashed_-1837957333', 0); -- Tài khoản bị khóa

GO

-- 4. Bảng Ca Làm (Không phụ thuộc)
INSERT INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) VALUES
(N'CS', N'Ca Sáng', '07:00:00', '15:00:00'),
(N'CT', N'Ca Tối', '15:00:00', '23:00:00'),
(N'CG', N'Ca Gãy (Part-time)', '10:00:00', '14:00:00');
GO

-- 5. Bảng Bàn (Không phụ thuộc)
INSERT INTO Ban (maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc) VALUES
(N'B001', N'Bàn 1A', 4, N'Trống', NULL, N'Tầng 1'),
(N'B002', N'Bàn 2A', 4, N'Trống', NULL, N'Tầng 1'),
(N'B003', N'Bàn 3A', 2, N'Trống', NULL, N'Tầng 1'),
(N'B004', N'Bàn 1B', 6, N'Trống', NULL, N'Tầng 2'),
(N'B005', N'Bàn 2B', 6, N'Trống', NULL, N'Tầng 2'),
(N'B006', N'Bàn 3B', 8, N'Trống', NULL, N'Tầng 2'),
(N'B007', N'Bàn 4B (VIP)', 10, N'Đang có khách', '2025-10-24 13:00:00', N'Phòng VIP'),
(N'B008', N'Bàn 1C', 4, N'Đang có khách', '2025-10-24 14:00:00', N'Ngoài trời'),
(N'B009', N'Bàn 2C', 4, N'Trống', NULL, N'Ngoài trời');
GO

-- 6. Bảng Nhân Viên (Phụ thuộc TaiKhoan, VaiTro)
INSERT INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, tenTK, vaiTro) VALUES
(N'NV02101', N'Nguyễn Văn An', '1990-01-15', N'Nam', '0988888888', N'1 Nguyễn Huệ, Q1', '2023-01-01', 15000000.00, N'admin', N'QuanLy'),
(N'NV01102', N'Trần Thị Bình', '1998-04-20', N'Nữ', '0977777777', N'2 Võ Thị Sáu, Q3', '2023-03-15', 7500000.00, N'nv_binh', N'NhanVien'),
(N'NV01103', N'Lê Văn Chí', '2000-07-10', N'Nam', '0966666666', N'3 Trần Hưng Đạo, Q5', '2023-03-15', 7000000.00, N'nv_chi', N'NhanVien'),
(N'NV01104', N'Phạm Thị Dung', '1995-11-05', N'Nữ', '0955555555', N'4 Lê Lai, Q1', '2023-06-01', 8000000.00, N'nv_dung', N'NhanVien'),
(N'NV01105', N'Hoàng Văn Duy', '1999-02-25', N'Nam', '0944444444', N'5 Pasteur, Q3', '2024-01-10', 6500000.00, N'nv_duy', N'NhanVien');
GO

-- 7. Bảng Danh Mục Món (Phụ thuộc NhanVien)
INSERT INTO DanhMucMon (maDM, tenDM, moTa, maNV) VALUES
(N'DM01', N'Cà Phê', N'Các loại cà phê truyền thống và pha máy', N'NV02101'),
(N'DM02', N'Trà & Trà Sữa', N'Các loại trà nóng, trà trái cây và trà sữa', N'NV01104'),
(N'DM03', N'Đá Xay', N'Các món đá xay, smoothies', N'NV01104'),
(N'DM04', N'Bánh Ngọt', N'Các loại bánh ăn kèm', N'NV02101'),
(N'DM05', N'Món Khác', N'Nước ép, soda và các món khác', N'NV02101');
GO

-- 8. Bảng Món Ăn (Phụ thuộc DanhMucMon)
INSERT INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) VALUES
(N'MA001', N'Cà phê đen đá', N'Cà phê rang xay nguyên chất', 25000.00, N'Ly', N'ConHang', N'images/den-da.jpg', N'DM01'),
(N'MA002', N'Cà phê sữa đá', N'Cà phê rang xay và sữa đặc', 30000.00, N'Ly', N'ConHang', N'images/sua-da.jpg', N'DM01'),
(N'MA003', N'Espresso', N'Pha máy', 40000.00, N'Tách', N'ConHang', N'images/espresso.jpg', N'DM01'),
(N'MA004', N'Trà đào cam sả', N'Trà đào và sả tươi', 45000.00, N'Ly', N'ConHang', N'images/tra-dao.jpg', N'DM02'),
(N'MA005', N'Trà sữa trân châu', N'Trà sữa truyền thống', 50000.00, N'Ly', N'ConHang', N'images/tra-sua.jpg', N'DM02'),
(N'MA007', N'Matcha đá xay', N'Matcha Nhật Bản', 55000.00, N'Ly', N'ConHang', N'images/matcha-dx.jpg', N'DM03'),
(N'MA009', N'Bánh Tiramisu', N'Bánh ngọt Ý', 35000.00, N'Phần', N'ConHang', N'images/tiramisu.jpg', N'DM04'),
(N'MA011', N'Nước ép cam', N'Cam vắt tươi', 40000.00, N'Ly', N'ConHang', N'images/nuoc-cam.jpg', N'DM05');
GO

-- 9. Bảng Khách Hàng (Phụ thuộc HangThanhVien)
INSERT INTO KhachHang (maKH, tenKH, gioiTinh, sdt, hangThanhVien, tongChiTieu, ngaySinh, diaChi, ngayThamGia, email) VALUES
(N'KH001', N'Khách vãng lai', N'Khác', '0900000000', N'NONE', 0.00, '1990-01-01', N'TP.HCM', '2025-01-01', N'vanglai@email.com'),
(N'KH002', N'Trần Văn Bảo', N'Nam', '0912345678', N'MEMBER', 150000.00, '1995-05-15', N'123 Lê Lợi, Q1', '2025-02-10', N'bao.tran@email.com'),
(N'KH003', N'Nguyễn Thị Lan', N'Nữ', '0987654321', N'SILVER', 2000000.00, '1992-11-30', N'456 Hai Bà Trưng, Q3', '2025-03-11', N'lan.nguyen@email.com'),
(N'KH004', N'Lê Minh Hùng', N'Nam', '0933445566', N'DIAMOND', 6000000.00, '1988-02-10', N'789 Nguyễn Trãi, Q5', '2025-04-12', N'hung.le@email.com'),
(N'KH005', N'Phạm Hoàng Yến', N'Nữ', '0977889900', N'BRONZE', 950000.00, '2000-07-22', N'321 CMT8, Q10', '2025-05-15', N'yen.pham@email.com'),
(N'KH006', N'Võ Thành Trung', N'Nam', '0905112233', N'GOLD', 4000000.00, '1990-01-20', N'Bình Dương', '2025-06-20', N'trung.vo@email.com');
GO

-- 10. Bảng Mã Khuyến Mãi
INSERT INTO MaKhuyenMai (maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai) VALUES
(N'KMHE2025', N'Chào hè 2025', N'Giảm 15% tổng hóa đơn', '2025-06-01', '2025-08-30', N'Giảm theo phần trăm', 15.00, N'Đang áp dụng'),
(N'GIAM20K', N'Giảm 20.000đ', N'Giảm 20.000đ cho hóa đơn từ 100.000đ', '2025-10-01', '2025-10-31', N'Giảm giá số tiền', 20000.00, N'Đang áp dụng'),
(N'SINHNHAT', N'Chúc mừng sinh nhật', N'Giảm 30% cho khách hàng thành viên', '2025-01-01', '2025-12-31', N'Giảm theo phần trăm', 30.00, N'Đang áp dụng'),
(N'KMNGUNG', N'Khuyến mãi 20/10', N'Đã hết hạn', '2025-10-20', '2025-10-20', N'Giảm theo phần trăm', 10.00, N'Ngưng áp dụng');
GO

-- 11. Bảng Phân Công Ca (Phụ thuộc NhanVien, CaLam)
INSERT INTO PhanCongCa (maNV, maCa, ngayLam) VALUES
(N'NV01102', N'CS', '2025-10-22'), (N'NV01103', N'CS', '2025-10-22'), (N'NV01104', N'CT', '2025-10-22'), (N'NV01105', N'CT', '2025-10-22'),
(N'NV01102', N'CS', '2025-10-23'), (N'NV01103', N'CT', '2025-10-23'), (N'NV01104', N'CS', '2025-10-23'), (N'NV01105', N'CT', '2025-10-23'),
(N'NV01102', N'CT', '2025-10-24'), (N'NV01103', N'CS', '2025-10-24'), (N'NV01104', N'CS', '2025-10-24'), (N'NV01105', N'CG', '2025-10-24');
GO

-- 12. Bảng Đơn Đặt Món (Phụ thuộc NhanVien, KhachHang, Ban)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DD00001', '2025-10-24 13:00:00', N'NV01102', N'KH003', N'B007'), -- Bàn 7 (VIP)
(N'DD00002', '2025-10-24 14:00:00', N'NV01103', N'KH004', N'B008'), -- Bàn 8 (Ngoài trời)
(N'DD00003', '2025-10-24 14:05:00', N'NV01104', N'KH001', NULL),   -- Mang về
(N'DD00004', '2025-10-24 14:10:00', N'NV01102', N'KH005', N'B001'), -- Bàn 1A
(N'DD00005', '2025-10-24 14:15:00', N'NV01103', N'KH006', N'B002'); -- Bàn 2A
GO

-- 13. Bảng Chi Tiết Hóa Đơn (Phụ thuộc DonDatMon, MonAn)
INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
-- Đơn DD00001 (Tổng: 85,000)
(N'DD00001', N'MA001', 2, 25000.00), -- 2 Cà phê đen (50k)
(N'DD00001', N'MA009', 1, 35000.00), -- 1 Tiramisu (35k)
-- Đơn DD00002 (Tổng: 100,000)
(N'DD00002', N'MA004', 1, 45000.00), -- 1 Trà đào
(N'DD00002', N'MA007', 1, 55000.00), -- 1 Matcha
-- Đơn DD00003 (Tổng: 30,000)
(N'DD00003', N'MA002', 1, 30000.00), -- 1 Sữa đá
-- Đơn DD00004 (Tổng: 80,000)
(N'DD00004', N'MA003', 2, 40000.00), -- 2 Espresso
-- Đơn DD00005 (Tổng: 150,000)
(N'DD00005', N'MA005', 3, 50000.00); -- 3 Trà sữa
GO

-- 14. Bảng Hóa Đơn (Phụ thuộc DonDatMon, NhanVien, MaKhuyenMai)
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
-- Hóa đơn cho DD00001 (85k).
(N'HD00001', '2025-10-24 13:30:00', 85000.00, N'Đã thanh toán', N'Tiền mặt', 100000.00, N'NV01102', NULL, N'DD00001'),
-- Hóa đơn cho DD00002 (100k). Áp dụng GIAM20K
(N'HD00002', '2025-10-24 14:10:00', 80000.00, N'Đã thanh toán', N'Chuyển khoản', 80000.00, N'NV01102', N'GIAM20K', N'DD00002'),
-- Hóa đơn cho DD00003 (30k).
(N'HD00003', '2025-10-24 14:06:00', 30000.00, N'Đã thanh toán', N'Tiền mặt', 50000.00, N'NV01104', NULL, N'DD00003'),
-- Hóa đơn cho DD00004 (80k).
(N'HD00004', '2025-10-24 14:11:00', 80000.00, N'Đã thanh toán', N'Chuyển khoản', 80000.00, N'NV01102', NULL, N'DD00004'),
-- Hóa đơn cho DD00005 (150k).
(N'HD00005', '2025-10-24 14:16:00', 150000.00, N'Chưa thanh toán', N'Tiền mặt', NULL, N'NV01103', NULL, N'DD00005');
GO

PRINT N'Đã chèn dữ liệu mẫu (tuân thủ logic Java Entities) thành công!';