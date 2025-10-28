package entity;

import java.time.LocalDate;
import java.util.Objects;

public class KhuyenMai {
    private String maKM;
    private String tenChuongTrinh; // tenKM trong CSDL
    private String moTa;
    private String loaiKhuyenMai; // loaiGiam trong CSDL
    private double giaTri;        // giaTriGiam trong CSDL
    private double dieuKienApDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai;

    /**
     * Constructor này dùng để tạo đối tượng từ GUI (Dialog)
     */
    public KhuyenMai(String maKM, String tenChuongTrinh, String moTa, String loaiKhuyenMai, double giaTri, double dieuKienApDung,LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
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

    // Getters
    public String getMaKM() {
        return maKM;
    }

    public String getTenChuongTrinh() {
        return tenChuongTrinh;
    }

    // Bổ sung getter cho moTa
    public String getMoTa() {
        return moTa;
    }

    public String getLoaiKhuyenMai() {
        return loaiKhuyenMai;
    }

    public double getGiaTri() {
        return giaTri;
    }

    public double getDieuKienApDung() { return dieuKienApDung; }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public void setTenChuongTrinh(String tenChuongTrinh) {
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public void setDieuKienApDung(double dieuKienApDung) { this.dieuKienApDung = dieuKienApDung >= 0 ? dieuKienApDung : 0; }
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