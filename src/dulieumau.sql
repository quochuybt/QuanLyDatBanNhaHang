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
('CA-01-20251025-101', N'Ca Sáng', '06:00:00', '14:00:00'),
('CA-02-20251025-102', N'Ca Tối', '14:00:00', '22:00:00'),
('CA-03-20251025-103', N'Ca Part-time Tối', '18:00:00', '22:00:00'),
('CA-04-20251025-104', N'Ca Part-time Sáng', '06:00:00', '10:00:00');
GO

-- 5. Bảng Bàn (Không phụ thuộc)
INSERT INTO Ban (maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc) VALUES
(N'BAN01', N'Bàn 1', 4, N'Trống', NULL, N'Tầng trệt'),
(N'BAN02', N'Bàn 2', 4, N'Trống', NULL, N'Tầng trệt'),
(N'BAN03', N'Bàn 3', 2, N'Đang có khách', '2025-10-24 18:30:00', N'Tầng trệt'), -- Giờ quá khứ
(N'BAN04', N'Bàn 4', 2, N'Trống', NULL, N'Tầng trệt'),
(N'BAN05', N'Bàn 5', 6, N'Trống', NULL, N'Tầng 1'),
(N'BAN06', N'Bàn 6', 6, N'Đã đặt trước', '2025-10-30 19:00:00', N'Tầng 1'), -- Giờ đặt (có thể là tương lai)
(N'BAN07', N'Bàn 7', 4, N'Đang có khách', '2025-10-24 20:00:00', N'Tầng 1'), -- Giờ quá khứ
(N'BAN08', N'Bàn 8', 4, N'Trống', NULL, N'Tầng 1'),
(N'BAN09', N'Bàn 9', 8, N'Trống', NULL, N'Tầng 1'),
(N'BAN10', N'Bàn 10', 2, N'Trống', NULL, N'Tầng trệt');
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
('DM0001', N'Món ăn', N'Các món ăn chính, món khai vị và tráng miệng', 'NV02101'),
('DM0002', N'Giải Khát', N'Các loại đồ uống không cồn như nước ngọt, trà, cà phê', 'NV02101'),
('DM0003', N'Rượu vang', N'Các loại rượu vang nhập khẩu và trong nước', 'NV02101');
GO

-- 8. Bảng Món Ăn (Phụ thuộc DanhMucMon)
INSERT INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) VALUES
('MA108', N'Cà ri vịt', N'Cà ri vịt truyền thống', 60000.00, N'Tô', N'Còn', 'images/pho_bo.jpg', 'DM0001'),
('MA107', N'Cá tra kho', N'Cá tra kho', 49000.00, N'Tô', N'Còn', 'images/pho_bo.jpg', 'DM0001'),
('MA106', N'Cá chiên xù', N'Cá tai tượng chiên xù', 100000.00, N'Dỉa', N'Còn', 'images/pho_bo.jpg', 'DM0001'),
('MA101', N'Phở Bò Tái', N'Phở bò truyền thống, nước lèo đậm đà', 50000.00, N'Tô', N'Còn', 'images/pho_bo.jpg', 'DM0001'),
('MA103', N'Nước Suối Aquafina', N'Nước suối tinh khiết 500ml', 10000.00, N'Chai', N'Còn', '', 'DM0002'),
('MA104', N'Trà Đá', N'Trà đá giải khát', 3000.00, N'Ly', N'Còn', '', 'DM0002'),
('MA105', N'Vang đỏ Chile', N'Vang đỏ nhập khẩu Chile, 750ml', 350000.00, N'Chai', N'Còn', 'images/vang_chile.jpg', 'DM0003');
GO

-- 9. Bảng Khách Hàng (Phụ thuộc HangThanhVien)
INSERT INTO KhachHang (maKH, tenKH, gioiTinh, sdt, hangThanhVien, tongChiTieu, ngaySinh, diaChi, ngayThamGia, email) VALUES
(N'KH20251025001', N'Khách vãng lai', N'Khác', '0900000000', N'NONE', 0.00, '1990-01-01', N'TP.HCM', '2025-01-01', N'vanglai@email.com'),
(N'KH20251025002', N'Trần Văn Bảo', N'Nam', '0912345678', N'MEMBER', 150000.00, '1995-05-15', N'123 Lê Lợi, Q1', '2025-02-10', N'bao.tran@email.com'),
(N'KH20251025003', N'Nguyễn Thị Lan', N'Nữ', '0987654321', N'SILVER', 2000000.00, '1992-11-30', N'456 Hai Bà Trưng, Q3', '2025-03-11', N'lan.nguyen@email.com'),
(N'KH20251025004', N'Lê Minh Hùng', N'Nam', '0933445566', N'DIAMOND', 6000000.00, '1988-02-10', N'789 Nguyễn Trãi, Q5', '2025-04-12', N'hung.le@email.com'),
(N'KH20251025005', N'Phạm Hoàng Yến', N'Nữ', '0977889900', N'BRONZE', 950000.00, '2000-07-22', N'321 CMT8, Q10', '2025-05-15', N'yen.pham@email.com'),
(N'KH20251025006', N'Võ Thành Trung', N'Nam', '0905112233', N'GOLD', 4000000.00, '1990-01-20', N'Bình Dương', '2025-06-20', N'trung.vo@email.com');
GO

-- 10. Bảng Mã Khuyến Mãi
INSERT INTO KhuyenMai (maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai) VALUES
(N'KMHE2025', N'Chào hè 2025', N'Giảm 15% tổng hóa đơn', '2025-06-01', '2025-08-30', N'Giảm theo phần trăm', 15.00, N'Đang áp dụng'),
(N'GIAM20K', N'Giảm 20.000đ', N'Giảm 20.000đ cho hóa đơn từ 100.000đ', '2025-10-01', '2025-10-31', N'Giảm giá số tiền', 20000.00, N'Đang áp dụng'),
(N'SINHNHAT', N'Chúc mừng sinh nhật', N'Giảm 30% cho khách hàng thành viên', '2025-01-01', '2025-12-31', N'Giảm theo phần trăm', 30.00, N'Đang áp dụng'),
(N'KMNGUNG', N'Khuyến mãi 20/10', N'Đã hết hạn', '2025-10-20', '2025-10-20', N'Giảm theo phần trăm', 10.00, N'Ngưng áp dụng');
GO

-- 11. Bảng Phân Công Ca (Phụ thuộc NhanVien, CaLam)
INSERT INTO PhanCongCa (maNV, maCa, ngayLam) VALUES
(N'NV01102', N'CA-01-20251025-101', '2025-10-22'),
(N'NV01103', N'CA-01-20251025-101', '2025-10-22'),
(N'NV01104', N'CA-02-20251025-102', '2025-10-22'),
(N'NV01105', N'CA-02-20251025-102', '2025-10-22'),

(N'NV01102', N'CA-01-20251025-101', '2025-10-23'),
(N'NV01103', N'CA-02-20251025-102', '2025-10-23'),
(N'NV01104', N'CA-01-20251025-101', '2025-10-23'),
(N'NV01105', N'CA-02-20251025-102', '2025-10-23'),

(N'NV01102', N'CA-02-20251025-102', '2025-10-24'),
(N'NV01103', N'CA-01-20251025-101', '2025-10-24'),
(N'NV01104', N'CA-01-20251025-101', '2025-10-24'),
(N'NV01105', N'CA-03-20251025-103', '2025-10-24');
GO

-- 12. Bảng Đơn Đặt Món (Phụ thuộc NhanVien, KhachHang, Ban)
INSERT INTO DonDatMon (maDon, ngayKhoiTao, maNV, maKH, maBan) VALUES
(N'DON1001', '2025-10-24 13:00:00', N'NV01102', N'KH20251025003', N'BAN07'),
(N'DON1002', '2025-10-24 14:00:00', N'NV01103', N'KH20251025004', N'BAN08'),
(N'DON1003', '2025-10-24 14:05:00', N'NV01104', N'KH20251025001', NULL),
(N'DON1004', '2025-10-24 14:10:00', N'NV01102', N'KH20251025005', N'BAN01'),
(N'DON1005', '2025-10-24 14:15:00', N'NV01103', N'KH20251025006', N'BAN02'),
(N'DON1007', '2025-10-30 19:00:00', N'NV01102', N'KH20251025002', N'BAN06'),
(N'DON1006', '2025-10-24 16:15:00', N'NV01102', N'KH20251025003', N'BAN03');
GO

-- 13. Bảng Chi Tiết Hóa Đơn (Phụ thuộc DonDatMon, MonAn)
INSERT INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia) VALUES
-- Đơn DD00001 (Tổng: 85,000)
-- Đơn DON1001 (Bàn 7)
(N'DON1001', N'MA101', 1, 50000.00), -- 1 Phở Bò Tái (50k)
(N'DON1001', N'MA104', 2, 3000.00),
-- Đơn DON1002 (Bàn 8)
(N'DON1002', N'MA108', 2, 60000.00), -- 2 Cà ri vịt (120k)
(N'DON1002', N'MA103', 2, 10000.00), -- 2 Nước Suối (20k)

-- Đơn DON1003 (Mang về)
(N'DON1003', N'MA107', 1, 49000.00), -- 1 Cá tra kho (49k)

-- Đơn DON1004 (Bàn 1)
(N'DON1004', N'MA106', 1, 100000.00), -- 1 Cá chiên xù (100k)

-- Đơn DON1005 (Bàn 2)
(N'DON1005', N'MA105', 1, 350000.00), -- 1 Vang đỏ Chile (350k)
(N'DON1006', N'MA108', 2, 60000.00);
GO

-- 14. Bảng Hóa Đơn (Phụ thuộc DonDatMon, NhanVien, MaKhuyenMai)
INSERT INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, maNV, maKM, maDon) VALUES
-- Hóa đơn cho DD00001 (85k).
(N'HD2410251001', '2025-10-24 13:30:00', 85000.00, N'Chưa thanh toán', N'Tiền mặt', 100000.00, N'NV01102', NULL, N'DON1001'),

-- Hóa đơn cho DON1002 (100k). Áp dụng GIAM20K -> 80k
(N'HD2410251002', '2025-10-24 14:10:00', 80000.00, N'Đã thanh toán', N'Chuyển khoản', 80000.00, N'NV01102', N'GIAM20K', N'DON1002'),

-- Hóa đơn cho DON1003 (30k).
(N'HD2410251003', '2025-10-24 14:06:00', 30000.00, N'Đã thanh toán', N'Tiền mặt', 50000.00, N'NV01104', NULL, N'DON1003'),

-- Hóa đơn cho DON1004 (80k).
(N'HD2410251004', '2025-10-24 14:11:00', 80000.00, N'Đã thanh toán', N'Chuyển khoản', 80000.00, N'NV01102', NULL, N'DON1004'),
(N'HD2410251006', GETDATE(), 0, N'Chưa thanh toán', N'Tiền mặt', 0, N'NV01102', NULL, N'DON1006'),
-- Hóa đơn cho DON1005 (150k). (Đã sửa từ 'Chưa thanh toán' sang 'Đã thanh toán' để hợp lệ hóa logic Java)
(N'HD2410251005', '2025-10-24 14:16:00', 150000.00, N'Đã thanh toán', N'Tiền mặt', 150000.00, N'NV01103', NULL, N'DON1005');
GO