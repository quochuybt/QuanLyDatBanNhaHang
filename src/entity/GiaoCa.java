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
    private double chenhLech; // Mới thêm
    private String ghiChu;

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, double tienDauCa) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.tienDauCa = tienDauCa;
    }

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                  double tienDauCa, double tienCuoiCa, double tienHeThongTinh, double chenhLech, String ghiChu) {
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

    public int getMaGiaoCa() { return maGiaoCa; }
    public void setMaGiaoCa(int maGiaoCa) { this.maGiaoCa = maGiaoCa; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) { this.thoiGianBatDau = thoiGianBatDau; }

    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }

    public double getTienDauCa() { return tienDauCa; }
    public void setTienDauCa(double tienDauCa) { this.tienDauCa = tienDauCa; }

    public double getTienCuoiCa() { return tienCuoiCa; }
    public void setTienCuoiCa(double tienCuoiCa) { this.tienCuoiCa = tienCuoiCa; }

    public double getTienHeThongTinh() { return tienHeThongTinh; }
    public void setTienHeThongTinh(double tienHeThongTinh) { this.tienHeThongTinh = tienHeThongTinh; }

    public double getChenhLech() { return chenhLech; } // Mới thêm
    public void setChenhLech(double chenhLech) { this.chenhLech = chenhLech; } // Mới thêm

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}