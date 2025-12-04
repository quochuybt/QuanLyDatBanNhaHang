package entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaiKhoan {
    private String tentk;
    private String matkhau;
    private boolean trangthai;

    public TaiKhoan(String tentk, String matkhau, VaiTro vaitro, boolean trangthai) {
        this.setTentk(tentk);
        this.tentk = tentk;
        this.matkhau = matkhau;
        this.trangthai = trangthai;
    }

    public String getTentk() {
        return tentk;
    }

    public void setTentk(String tentk) {
        if (tentk == null || tentk.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài khoản không được để rỗng.");
        }
        this.tentk = tentk;
    }

    public String getMatkhau() {
        return matkhau;
    }


    public Boolean getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(Boolean trangthai) {
        this.trangthai = trangthai;
    }
    private String hashPassword(String plainPassword) {
        return "hashed_" + plainPassword.hashCode();
    }
    @Override
    public String toString() {
        return "TaiKhoan{" +
                "tentk='" + tentk + '\'' +
                ", matkhau='***'" +
                ", trangthai=" + trangthai +
                '}';
    }
}