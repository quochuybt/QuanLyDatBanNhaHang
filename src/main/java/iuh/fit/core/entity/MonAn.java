package iuh.fit.core.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "MonAn")
public class MonAn {

    @Id
    @Column(name = "maMonAn", length = 20)
    private String maMonAn;

    @Column(name = "tenMon", nullable = false, length = 100)
    private String tenMon;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "donGia")
    private float donGia;

    @Column(name = "donViTinh", length = 50)
    private String donViTinh;

    @Column(name = "trangThai", length = 20)
    private String trangThai;

    @Column(name = "hinhAnh", length = 255)
    private String hinhAnh;

    @Column(name = "maDM", length = 20)
    private String maDM;

    public MonAn() {}

    public String getMaMonAn() { return maMonAn; }
    public void setMaMonAn(String maMonAn) { this.maMonAn = maMonAn; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public float getDonGia() { return donGia; }
    public void setDonGia(float donGia) { this.donGia = donGia; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getMaDM() { return maDM; }
    public void setMaDM(String maDM) { this.maDM = maDM; }
}