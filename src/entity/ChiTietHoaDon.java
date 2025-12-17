package entity;

public class ChiTietHoaDon {

    private String maDon;
    private String maMon;
    private int soluong;
    private float dongia;
    private float thanhtien;
    private String tenMon;

    public ChiTietHoaDon(String maMon, String maDon, int soluong, float dongia) {
        setMaMon(maMon);
        setMaDon(maDon);
        setDongia(dongia);
        setSoluong(soluong);
    }
    public ChiTietHoaDon(String maDon, String maMon, String tenMon, int soluong, float dongia) {
        setMaDon(maDon);
        setMaMon(maMon);
        setTenMon(tenMon);
        setDongia(dongia);
        setSoluong(soluong);
    }

    public ChiTietHoaDon(ChiTietHoaDon other) {
        this.maMon = other.maMon;
        this.maDon = other.maDon;
        this.soluong = other.soluong;
        this.dongia = other.dongia;
        this.thanhtien = other.thanhtien;
    }

    public void tinhThanhTien() {
        this.thanhtien = this.soluong * this.dongia;
    }

    public String getMaDon() {
        return maDon;
    }

    public void setMaDon(String maDon) {
        if (maDon == null || maDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn không được rỗng.");
        }
        this.maDon = maDon;
    }
    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        if (maMon == null || maMon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã món không được rỗng.");
        }
        this.maMon = maMon;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        if (soluong < 1) {
            throw new IllegalArgumentException("Số lượng phải >= 1.");
        }
        this.soluong = soluong;
        tinhThanhTien();
    }

    public float getDongia() {
        return dongia;
    }

    public void setDongia(float dongia) {
        if (dongia < 0) {
            throw new IllegalArgumentException("Đơn giá phải >= 0.");
        }
        this.dongia = dongia;
        tinhThanhTien();
    }

    public float getThanhtien() {
        return thanhtien;
    }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" +
                "maMon='" + maMon + '\'' +
                ", tenMon='" + tenMon + '\'' + // Thêm
                ", soluong=" + soluong +
                ", thanhtien=" + thanhtien +
                '}';
    }
}