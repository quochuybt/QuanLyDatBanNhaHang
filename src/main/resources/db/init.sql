-- =============================================
-- Init Database: QuanLyNhaHang (MariaDB)
-- =============================================

CREATE DATABASE IF NOT EXISTS QuanLyNhaHang
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE QuanLyNhaHang;

-- =============================================
-- Bảng tham chiếu
-- =============================================

CREATE TABLE IF NOT EXISTS VaiTro (
    tenVaiTro VARCHAR(20) PRIMARY KEY
);

INSERT IGNORE INTO VaiTro (tenVaiTro) VALUES ('NHANVIEN'), ('QUANLY');

CREATE TABLE IF NOT EXISTS HangThanhVien (
    tenHang VARCHAR(20) PRIMARY KEY
);

INSERT IGNORE INTO HangThanhVien (tenHang) VALUES ('NONE'), ('MEMBER'), ('BRONZE'), ('SILVER'), ('GOLD'), ('DIAMOND');

-- =============================================
-- Bảng chính
-- =============================================

CREATE TABLE IF NOT EXISTS TaiKhoan (
    tenTK VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(255) NOT NULL,
    trangThai BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS CaLam (
    maCa VARCHAR(20) PRIMARY KEY,
    tenCa VARCHAR(50) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL
);

CREATE TABLE IF NOT EXISTS DanhMucMon (
    maDM VARCHAR(10) PRIMARY KEY,
    tenDM VARCHAR(100) NOT NULL,
    moTa VARCHAR(255),
    maNV VARCHAR(20) NULL
);

CREATE TABLE IF NOT EXISTS Ban (
    maBan VARCHAR(10) PRIMARY KEY,
    tenBan VARCHAR(50) NOT NULL,
    soGhe INT NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    gioMoBan DATETIME NULL,
    khuVuc VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS KhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenKM VARCHAR(100) NOT NULL,
    moTa VARCHAR(255),
    ngayBatDau DATETIME NOT NULL,
    ngayKetThuc DATETIME NOT NULL,
    loaiGiam VARCHAR(50) NOT NULL,
    giaTriGiam DECIMAL(18, 2) NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    dieuKienApDung DECIMAL(18, 2) NULL DEFAULT 0,
    soLuongGioiHan INT DEFAULT NULL,
    soLuotDaDung INT DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    tenKH VARCHAR(100) NOT NULL,
    gioiTinh VARCHAR(10) NOT NULL,
    sdt VARCHAR(15) UNIQUE,
    hangThanhVien VARCHAR(20) NOT NULL DEFAULT 'NONE',
    tongChiTieu DECIMAL(18, 2) NOT NULL DEFAULT 0,
    ngaySinh DATE NULL,
    diaChi VARCHAR(255) NULL,
    ngayThamGia DATE NOT NULL DEFAULT (CURRENT_DATE),
    email VARCHAR(100) NULL,
    CONSTRAINT FK_KhachHang_HangTV FOREIGN KEY (hangThanhVien) REFERENCES HangThanhVien(tenHang)
);

CREATE UNIQUE INDEX UQ_KhachHang_Email_NotNull ON KhachHang(email);

CREATE TABLE IF NOT EXISTS NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen VARCHAR(100) NOT NULL,
    ngaySinh DATE,
    gioiTinh VARCHAR(10) NOT NULL,
    sdt VARCHAR(15) UNIQUE NOT NULL,
    diaChi VARCHAR(255),
    ngayVaoLam DATE NOT NULL,
    luong DECIMAL(18, 2) NOT NULL,
    tenTK VARCHAR(50) NOT NULL UNIQUE,
    vaiTro VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (tenTK) REFERENCES TaiKhoan(tenTK),
    CONSTRAINT FK_NhanVien_VaiTro FOREIGN KEY (vaiTro) REFERENCES VaiTro(tenVaiTro)
);

ALTER TABLE DanhMucMon
    ADD CONSTRAINT FK_DanhMucMon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV);

CREATE TABLE IF NOT EXISTS MonAn (
    maMonAn VARCHAR(20) PRIMARY KEY,
    tenMon VARCHAR(100) NOT NULL,
    moTa VARCHAR(500),
    donGia DECIMAL(18, 2) NOT NULL,
    donViTinh VARCHAR(20) NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    hinhAnh VARCHAR(255),
    maDM VARCHAR(10) NOT NULL,
    CONSTRAINT FK_MonAn_DanhMucMon FOREIGN KEY (maDM) REFERENCES DanhMucMon(maDM)
);

CREATE TABLE IF NOT EXISTS PhanCongCa (
    maNV VARCHAR(20) NOT NULL,
    maCa VARCHAR(20) NOT NULL,
    ngayLam DATE NOT NULL,
    PRIMARY KEY (maNV, maCa, ngayLam),
    CONSTRAINT FK_PhanCong_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhanCong_CaLam FOREIGN KEY (maCa) REFERENCES CaLam(maCa)
);

CREATE TABLE IF NOT EXISTS DonDatMon (
    maDon VARCHAR(20) PRIMARY KEY,
    ngayKhoiTao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoiGianDen DATETIME NULL,
    trangThai VARCHAR(50) NOT NULL DEFAULT 'Chưa thanh toán',
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NULL,
    maBan VARCHAR(10) NULL,
    ghiChu TEXT NULL,
    CONSTRAINT FK_DonDatMon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_DonDatMon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_DonDatMon_Ban FOREIGN KEY (maBan) REFERENCES Ban(maBan)
);

CREATE TABLE IF NOT EXISTS ChiTietHoaDon (
    maDon VARCHAR(20) NOT NULL,
    maMonAn VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(18, 2) NOT NULL,
    thanhTien DECIMAL(18, 2) GENERATED ALWAYS AS (soLuong * donGia) STORED,
    PRIMARY KEY (maDon, maMonAn),
    CONSTRAINT FK_ChiTiet_DonDatMon FOREIGN KEY (maDon) REFERENCES DonDatMon(maDon),
    CONSTRAINT FK_ChiTiet_MonAn FOREIGN KEY (maMonAn) REFERENCES MonAn(maMonAn)
);

CREATE TABLE IF NOT EXISTS HoaDon (
    maHD VARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(18, 2) NOT NULL,
    tongThanhToan DECIMAL(18, 2) NOT NULL DEFAULT 0,
    trangThai VARCHAR(50) NOT NULL,
    hinhThucThanhToan VARCHAR(50),
    tienKhachDua DECIMAL(18, 2),
    giamGia DECIMAL(18, 2) NULL DEFAULT 0,
    tenBan VARCHAR(100),
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NULL,
    maKM VARCHAR(20) NULL,
    maDon VARCHAR(20) NOT NULL UNIQUE,
    maBan VARCHAR(10) NULL,
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HoaDon_DonDatMon FOREIGN KEY (maDon) REFERENCES DonDatMon(maDon)
);


CREATE TABLE IF NOT EXISTS LichSuSuDungKM (
    maLichSu INT AUTO_INCREMENT PRIMARY KEY,
    maKH VARCHAR(20) NOT NULL,
    maKM VARCHAR(20) NOT NULL,
    ngaySuDung DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LichSu_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_LichSu_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM)
);

CREATE TABLE IF NOT EXISTS GiaoCa (
    maGiaoCa INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20),
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NULL,
    tienDauCa DECIMAL(18, 0) NOT NULL,
    tienCuoiCa DECIMAL(18, 0) NULL,
    tienHeThongTinh DECIMAL(18, 0) NULL,
    chenhLech DECIMAL(18, 0) NULL,
    ghiChu VARCHAR(255),
    CONSTRAINT FK_GiaoCa_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
