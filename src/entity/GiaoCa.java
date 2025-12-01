package entity;
import java.time.LocalDateTime;

public class GiaoCa {
    private int maGiaoCa;
    private String maNV;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private double tienDauCa;
    private double tienCuoiCa;
    private double tienHeThongTinh;
    private String ghiChu;

    // Constructor đầy đủ
    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, double tienDauCa) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.tienDauCa = tienDauCa;
    }
    // ... Getter & Setter ...
    public int getMaGiaoCa() { return maGiaoCa; }

    public void setMaGiaoCa(int maGiaoCa) {
        this.maGiaoCa = maGiaoCa;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public double getTienCuoiCa() {
        return tienCuoiCa;
    }

    public void setTienCuoiCa(double tienCuoiCa) {
        this.tienCuoiCa = tienCuoiCa;
    }

    public double getTienDauCa() {
        return tienDauCa;
    }

    public void setTienDauCa(double tienDauCa) {
        this.tienDauCa = tienDauCa;
    }

    public double getTienHeThongTinh() {
        return tienHeThongTinh;
    }

    public void setTienHeThongTinh(double tienHeThongTinh) {
        this.tienHeThongTinh = tienHeThongTinh;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}