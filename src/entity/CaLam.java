package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
// [THÊM MỚI] Import thư viện Objects để dùng cho equals/hashCode
import java.util.Objects;

public class CaLam {
    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;

    public CaLam(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public String getMaCa() {
        return maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public LocalTime getGioBatDau() {
        return gioBatDau;
    }

    public LocalTime getGioKetThuc() {
        return gioKetThuc;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }
}