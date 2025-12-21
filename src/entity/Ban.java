package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

public class Ban {
    private static int soThuTuBan = 1;

    private String maBan;
    private String tenBan;
    private int soGhe;
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;
    private String khuVuc;

    public static void setSoThuTuBanHienTai(int maxSoThuTu) {
        soThuTuBan = maxSoThuTu + 1;
    }
    public Ban(String maBan, String tenBan, int soGhe, TrangThaiBan trangThai, LocalDateTime gioMoBan, String khuVuc) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.soGhe = soGhe;
        this.trangThai = trangThai;
        this.gioMoBan = gioMoBan;
        this.khuVuc = khuVuc;
    }
    public Ban() {
        this.maBan = phatSinhMaBan();
        this.tenBan = "Chưa đặt tên";
        this.soGhe = 2;
        this.trangThai = TrangThaiBan.TRONG;
        this.gioMoBan = LocalDateTime.now().plusHours(1);
        this.khuVuc = "Tầng trệt";
    }

    public Ban(String tenBan, int soGhe, TrangThaiBan trangThai, LocalDateTime gioMoBan, String khuVuc) {
        this.maBan = phatSinhMaBan();
        setTenBan(tenBan);
        setSoGhe(soGhe);
        setTrangThai(trangThai);
        setGioMoBan(gioMoBan);
        setKhuVuc(khuVuc);
    }

    public Ban(Ban other) {
        this.maBan = phatSinhMaBan();
        this.tenBan = other.tenBan;
        this.soGhe = other.soGhe;
        this.trangThai = other.trangThai;
        this.gioMoBan = other.gioMoBan;
        this.khuVuc = other.khuVuc;
    }

    private String phatSinhMaBan() {
        return String.format("BAN%02d", soThuTuBan++);
    }

    public String getMaBan() {
        return maBan;
    }

    private void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        if (tenBan == null || tenBan.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên bàn không được rỗng");
        }
        this.tenBan = tenBan;
    }

    public int getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(int soGhe) {
        if (soGhe <= 0) {
            throw new IllegalArgumentException("Số ghế phải > 0");
        }
        this.soGhe = soGhe;
    }

    public TrangThaiBan getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiBan trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getGioMoBan() {
        return gioMoBan;
    }

    public void setGioMoBan(LocalDateTime gioMoBan) {
        if (this.trangThai == TrangThaiBan.DA_DAT_TRUOC) {
            if (gioMoBan == null || !gioMoBan.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Giờ đặt trước phải lớn hơn giờ hiện tại.");
            }
        }
        this.gioMoBan = gioMoBan;
    }
    public String getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(String khuVuc) {
        if (khuVuc == null || khuVuc.trim().isEmpty()) {
            this.khuVuc = "Chưa phân loại";
        }
        this.khuVuc = khuVuc;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return "Ban{" +
                "maBan='" + maBan + '\'' +
                ", tenBan='" + tenBan + '\'' +
                ", soGhe=" + soGhe +
                ", trangThai='" + trangThai + '\'' +
                ", gioMoBan='" + gioMoBan.format(formatter) + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ban ban = (Ban) o;
        return Objects.equals(maBan, ban.maBan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBan);
    }

}
