package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DonDatMon {

    private String maDon;
    private LocalDateTime ngayKhoiTao;
    private String maNV;
    private String maKH;
    private LocalDateTime thoiGianDen;
    private String trangThai;
    private String maBan;
    private String ghiChu;

    private String generateMaDon() {
        Random rand = new Random();
        int xxxx = rand.nextInt(9000) + 1000;
        return "DON" + xxxx;
    }

    public DonDatMon() {
        this.maDon = generateMaDon();
        this.ngayKhoiTao = LocalDateTime.now();
        this.ghiChu = "";
    }
    public DonDatMon(String maDon, LocalDateTime ngayKhoiTao, String maNV, String maKH, String maBan, String ghiChu) {
        setMaDon(maDon);
        setNgayKhoiTao(ngayKhoiTao);
        setMaNV(maNV);
        setMaKH(maKH);
        setMaBan(maBan);
        setGhiChu(ghiChu);
    }

    public DonDatMon(DonDatMon donDatMon) {
        this.maDon = donDatMon.maDon;
        this.ngayKhoiTao = donDatMon.ngayKhoiTao;
        this.maNV = donDatMon.maNV;
        this.maKH = donDatMon.maKH;
        this.maBan = donDatMon.maBan;
        this.ghiChu = donDatMon.ghiChu;
    }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getMaDon() {
        return maDon;
    }

    public LocalDateTime getNgayKhoiTao() {
        return ngayKhoiTao;
    }
    public LocalDateTime getThoiGianDen() {
        return thoiGianDen;
    }

    public void setThoiGianDen(LocalDateTime thoiGianDen) {
        this.thoiGianDen = thoiGianDen;
    }
    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public void setMaDon(String maDon) throws IllegalArgumentException {
        if (maDon == null || maDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn không được rỗng.");
        }
        this.maDon = maDon;
    }

    public void setNgayKhoiTao(LocalDateTime ngayKhoiTao) throws IllegalArgumentException {
        if (ngayKhoiTao == null) {
            throw new IllegalArgumentException("Ngày khởi tạo không được rỗng.");
        }
        this.ngayKhoiTao = ngayKhoiTao;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        String ngayStr = (this.ngayKhoiTao != null) ? this.ngayKhoiTao.format(formatter) : "null";

        return "DonDatMon{" +
                "maDon='" + maDon + '\'' +
                ", ngayKhoiTao='" + ngayStr + '\'' +
                ", maKH='" + maKH + '\'' +
                ", maBan='" + maBan + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
