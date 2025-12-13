package entity;

public class MonAn {
    private String maMonAn;
    private String tenMon;
    private String mota;
    private float donGia;
    private String donViTinh;
    private String trangThai;
    private String hinhAnh;
    private String maDM;

    // Constructor rỗng
    public MonAn() {
        this.maMonAn = ""; // Sẽ được set bởi DAO hoặc GUI
        this.tenMon = "";
        this.donGia = 0;
        this.donViTinh = "";
        this.trangThai = "Còn";
        this.mota = "";
        this.hinhAnh = "";
        this.maDM = "";
    }

    // Constructor đầy đủ
    public MonAn(String maMonAn, String tenMon, String mota, float donGia,
                 String donViTinh, String trangThai, String hinhAnh, String maDM) {
        this.maMonAn = maMonAn;
        this.tenMon = tenMon;
        this.mota = mota;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
        this.maDM = maDM;
    }

    // Getters and Setters
    public String getMaMonAn() { return maMonAn; }
    public void setMaMonAn(String maMonAn) { this.maMonAn = maMonAn; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getMota() { return mota; }
    public void setMota(String mota) { this.mota = mota; }

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

    @Override
    public String toString() {
        return tenMon;
    }
}