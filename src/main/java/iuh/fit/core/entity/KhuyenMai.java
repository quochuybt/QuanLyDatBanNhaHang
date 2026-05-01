package iuh.fit.core.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {

    @Id
    @Column(name = "maKM", length = 20)
    private String maKM;

    @Column(name = "tenKM", nullable = false, length = 100)
    private String tenChuongTrinh;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "loaiGiam", length = 50)
    private String loaiKhuyenMai;

    @Column(name = "giaTriGiam")
    private double giaTri;

    @Column(name = "dieuKienApDung")
    private double dieuKienApDung;

    @Column(name = "ngayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "ngayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "soLuongGioiHan")
    private int soLuongGioiHan;

    @Column(name = "soLuotDaDung")
    private int soLuotDaDung;

    public KhuyenMai() {}

    public KhuyenMai(String maKM, String tenChuongTrinh, String moTa, String loaiKhuyenMai,
                     double giaTri, double dieuKienApDung,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
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

    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) { this.maKM = maKM; }

    public String getTenChuongTrinh() { return tenChuongTrinh; }
    public void setTenChuongTrinh(String tenChuongTrinh) { this.tenChuongTrinh = tenChuongTrinh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getLoaiKhuyenMai() { return loaiKhuyenMai; }
    public void setLoaiKhuyenMai(String loaiKhuyenMai) { this.loaiKhuyenMai = loaiKhuyenMai; }

    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }

    public double getDieuKienApDung() { return dieuKienApDung; }
    public void setDieuKienApDung(double dieuKienApDung) { this.dieuKienApDung = dieuKienApDung; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoLuongGioiHan() { return soLuongGioiHan; }
    public void setSoLuongGioiHan(int soLuongGioiHan) { this.soLuongGioiHan = soLuongGioiHan; }

    public int getSoLuotDaDung() { return soLuotDaDung; }
    public void setSoLuotDaDung(int soLuotDaDung) { this.soLuotDaDung = soLuotDaDung; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhuyenMai)) return false;
        KhuyenMai that = (KhuyenMai) o;
        return Objects.equals(maKM, that.maKM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKM);
    }
}