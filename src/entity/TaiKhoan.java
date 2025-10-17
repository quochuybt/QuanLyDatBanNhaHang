/* @description:
 *@author: Huy, Le NNguyen Quoc
 *@version: 1.0
 *@created: 10/15/2025
 */
package entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaiKhoan {
    private String tentk;
    private String matkhau;
    private VaiTro vaitro;
    private boolean trangthai;

    public TaiKhoan(String tentk, String matkhau, VaiTro vaitro, boolean trangthai) {
        this.setTentk(tentk);
        this.tentk = tentk;
        this.setMatkhau(matkhau);
        this.vaitro = vaitro;
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

    public void setMatkhau(String matkhau) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(matkhau);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Mật khẩu không hợp lệ. Mật khẩu phải dài ít nhất 8 ký tự, chứa chữ hoa, chữ thường và số.");
        }
        this.matkhau = hashPassword(matkhau);
    }

    public VaiTro getVaitro() {
        return vaitro;
    }

    public void setVaitro(VaiTro vaitro) {
        this.vaitro = vaitro;
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
                ", vaitro=" + vaitro +
                ", trangthai=" + trangthai +
                '}';
    }
}
