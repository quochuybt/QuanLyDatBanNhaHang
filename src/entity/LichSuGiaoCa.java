package entity;

import java.time.LocalDateTime;

public class LichSuGiaoCa {
    private String maGiaoCa;
    private String maNV;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private double tienDauCa;
    private double tienCuoiCa;
    private double tienHeThongTinh;
    private double chenhLech;
    private String ghiChu;

    public LichSuGiaoCa() { }

    public LichSuGiaoCa(String maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc, double tienDauCa, double tienCuoiCa, double tienHeThongTinh, double chenhLech, String ghiChu) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.tienDauCa = tienDauCa;
        this.tienCuoiCa = tienCuoiCa;
        this.tienHeThongTinh = tienHeThongTinh;
        this.chenhLech = chenhLech;
        this.ghiChu = ghiChu;
    }

    public String getMaGiaoCa() { return maGiaoCa; }
    public String getMaNV() { return maNV; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public double getTienDauCa() { return tienDauCa; }
    public double getTienCuoiCa() { return tienCuoiCa; }
    public double getTienHeThongTinh() { return tienHeThongTinh; }
    public double getChenhLech() { return chenhLech; }
    public String getGhiChu() { return ghiChu; }
}