package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


public class DanhMucMon {
    private String madm;
    private String tendm;
    private String mota;

    public DanhMucMon(String madm, String tendm, String mota) {
        this.madm = madm;
        this.tendm = tendm;
        this.mota = mota;
    }

    public String getMadm() {
        return madm;
    }

    public String getTendm() {
        return tendm;
    }

    public String getMota() {
        return mota;
    }

    public void setMadm(String madm) {
        this.madm = madm;
    }
    public void setTendm(String tendm) {
        this.tendm = tendm;
    }

    public void setMota(String mota) {
        this.mota = mota;
    }
}