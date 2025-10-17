package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Ban {
    private static int soThuTuBan = 1;

    private String maBan;
    private String tenBan;
    private int soGhe;
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;

    public Ban() {
        this.maBan = phatSinhMaBan();
        this.tenBan = "Chưa đặt tên";
        this.soGhe = 2;
        this.trangThai = TrangThaiBan.TRONG;
        this.gioMoBan = LocalDateTime.now();
    }

    public Ban(String tenBan, int soGhe, TrangThaiBan trangThai, LocalDateTime gioMoBan) {
        this.maBan = phatSinhMaBan();
        setTenBan(tenBan);
        setSoGhe(soGhe);
        setTrangThai(trangThai);
        setGioMoBan(gioMoBan);
    }

    public Ban(Ban other) {
        this.maBan = phatSinhMaBan();
        this.tenBan = other.tenBan;
        this.soGhe = other.soGhe;
        this.trangThai = other.trangThai;
        this.gioMoBan = other.gioMoBan;
    }

    private String phatSinhMaBan() {
        // Format mã bàn thành dạng BANXX (ví dụ: BAN01, BAN02)
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
        if (gioMoBan == null || !gioMoBan.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Giờ mở bàn phải lớn hơn giờ hiện tại.");
        }
        this.gioMoBan = gioMoBan;
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

}
