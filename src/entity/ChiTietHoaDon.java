package entity;

import jakarta.persistence.*;
import lombok.*;


public class ChiTietHoaDon {

    private String maDon;
    private String maMon;
    private String tenMon;

    private int soluong;
    private float dongia;
    private float thanhtien;


    public ChiTietHoaDon(String maDon, String maMon, String tenMon, int soluong, float dongia) {
        this.maDon = maDon;
        this.maMon = maMon;
        this.soluong = soluong;
        this.dongia = dongia;
        this.tenMon = tenMon;
    }

    public ChiTietHoaDon(String maDon, String maMon, int soluong, float dongia) {
        this.maDon = maDon;
        this.maMon = maMon;
        this.soluong = soluong;
        this.dongia = dongia;
    }

    public ChiTietHoaDon(ChiTietHoaDon other) {
        this.maDon = other.maDon;
        this.maMon = other.maMon;
        this.tenMon = other.tenMon;
        this.soluong = other.soluong;
        this.dongia = other.dongia;
        this.thanhtien = other.thanhtien;
    }

    public ChiTietHoaDon(String maDon) {
        this.maDon = maDon;
    }

    public String getMaDon() {
        return maDon;
    }

    public String getMaMon() {
        return maMon;
    }

    public int getSoluong() {
        return soluong;
    }

    public float getDongia() {
        return dongia;
    }

    public float getThanhtien() {
        return thanhtien;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public void setDongia(float dongia) {
        this.dongia = dongia;
    }

    public void setThanhtien(float thanhtien) {
        this.thanhtien = thanhtien;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public void tinhThanhTien() {
        this.thanhtien = this.soluong * this.dongia;
    }
}