package entity;

public class DanhMucMon {
    private String madm;
    private String tendm;
    private String mota;

    public DanhMucMon(String madm, String tendm, String mota) {
        this.madm = madm;
        this.tendm = tendm;
        this.mota = mota;
    }

    public DanhMucMon() {}

    public String getMadm() { return madm; }
    public void setMadm(String madm) { this.madm = madm; }

    public String getTendm() { return tendm; }
    public void setTenDM(String tendm) { this.tendm = tendm; }

    public String getMota() { return mota; }
    public void setMota(String mota) { this.mota = mota; }

    @Override
    public String toString() {
        return tendm;
    }
}