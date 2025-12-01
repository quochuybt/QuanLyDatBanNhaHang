package entity;

import java.time.LocalDate;
import java.util.Objects;

public class KhuyenMai {
    // --- Giữ nguyên toàn bộ thuộc tính cũ ---
    private String maKM;
    private String tenChuongTrinh;
    private String moTa;
    private String loaiKhuyenMai;
    private double giaTri;
    private double dieuKienApDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai;

    // [THÊM MỚI] Thuộc tính bổ sung
    private int soLuongGioiHan = 0; // Mặc định 0 (vô hạn)
    private int soLuotDaDung = 0;   // Mặc định 0

    // --- [GIỮ NGUYÊN] Constructor Cũ (Không sửa gì cả) ---
    public KhuyenMai(String maKM, String tenChuongTrinh, String moTa, String loaiKhuyenMai,
                     double giaTri, double dieuKienApDung, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this.maKM = maKM;
        this.tenChuongTrinh = tenChuongTrinh;
        this.moTa = moTa;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTri = giaTri;
        this.dieuKienApDung = dieuKienApDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    // --- [THÊM MỚI] Getter và Setter cho thuộc tính mới ---
    public int getSoLuongGioiHan() { return soLuongGioiHan; }
    public void setSoLuongGioiHan(int soLuongGioiHan) { this.soLuongGioiHan = soLuongGioiHan; }

    public int getSoLuotDaDung() { return soLuotDaDung; }
    public void setSoLuotDaDung(int soLuotDaDung) { this.soLuotDaDung = soLuotDaDung; }


    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }

    public String getTenChuongTrinh() { return tenChuongTrinh; }
    public void setTenChuongTrinh(String tenChuongTrinh) { this.tenChuongTrinh = tenChuongTrinh; }

    public String getMoTa() { return moTa; }
    public String getLoaiKhuyenMai() { return loaiKhuyenMai; }
    public double getGiaTri() { return giaTri; }
    public double getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(double dieuKienApDung) { this.dieuKienApDung = dieuKienApDung; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public LocalDate getNgayKetThuc() { return ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KhuyenMai that = (KhuyenMai) o;
        return Objects.equals(maKM, that.maKM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKM);
    }
}