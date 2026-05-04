package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

public class Ban {
    private String maBan;
    private static int soThuTuBan = 1;
    private String tenBan;
    private int soGhe;
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;
    private String khuVuc;

    public Ban(String maBan, String tenBan, int soGhe, TrangThaiBan trangThai, LocalDateTime gioMoBan, String khuVuc) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.soGhe = soGhe;
        this.trangThai = trangThai;
        this.gioMoBan = gioMoBan;
        this.khuVuc = khuVuc;
    }

    public String getMaBan() {
        return maBan;
    }

    public static int getSoThuTuBan() {
        return soThuTuBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public int getSoGhe() {
        return soGhe;
    }

    public TrangThaiBan getTrangThai() {
        return trangThai;
    }

    public LocalDateTime getGioMoBan() {
        return gioMoBan;
    }

    public String getKhuVuc() {
        return khuVuc;
    }

    public static void setSoThuTuBan(int soThuTuBan) {
        Ban.soThuTuBan = soThuTuBan;
    }

    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }

    public void setSoGhe(int soGhe) {
        this.soGhe = soGhe;
    }

    public void setTrangThai(TrangThaiBan trangThai) {
        this.trangThai = trangThai;
    }

    public void setGioMoBan(LocalDateTime gioMoBan) {
        this.gioMoBan = gioMoBan;
    }

    public void setKhuVuc(String khuVuc) {
        this.khuVuc = khuVuc;
    }
}
