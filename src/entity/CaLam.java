package entity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
// [THÊM MỚI] Import thư viện Objects để dùng cho equals/hashCode
import java.util.Objects;

/**
 * Lớp CaLam đại diện cho một ca làm việc.
 * (Đã sửa đổi để dùng LocalTime)
 */
public class CaLam {

    // 1. Khai báo thuộc tính
    private String maCa;
    private String tenCa;
    private LocalTime gioBatDau;  // Dùng LocalTime
    private LocalTime gioKetThuc; // Dùng LocalTime

    // --- (Constructor) ---

    public CaLam() {
        this.maCa = "Chưa có mã";
        this.tenCa = "Chưa đặt tên";
        this.gioBatDau = LocalTime.now();
        this.gioKetThuc = this.gioBatDau.plusHours(1);
    }

    public CaLam(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc)
            throws IllegalArgumentException {
        setMaCa(maCa);
        setTenCa(tenCa);
        setGioBatDau(gioBatDau);
        setGioKetThuc(gioKetThuc);
    }

    public CaLam(CaLam ca) {
        this.maCa = ca.maCa;
        this.tenCa = ca.tenCa;
        this.gioBatDau = ca.gioBatDau;
        this.gioKetThuc = ca.gioKetThuc;
    }

    // --- ( Getters và Setters ) ---

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

    public void setMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ca không được rỗng.");
        }
        this.maCa = maCa;
    }

    public void setTenCa(String tenCa) throws IllegalArgumentException {
        if (tenCa == null || tenCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca không được rỗng");
        }
        this.tenCa = tenCa;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        if (gioBatDau == null) {
            throw new IllegalArgumentException("Giờ bắt đầu không được rỗng.");
        }
        this.gioBatDau = gioBatDau;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) throws IllegalArgumentException {
        if (gioKetThuc == null) {
            throw new IllegalArgumentException("Giờ kết thúc không được rỗng.");
        }
        if (this.gioBatDau != null && !gioKetThuc.isAfter(this.gioBatDau)) {
        }
        this.gioKetThuc = gioKetThuc;
    }


    /**
     * Phương thức toString() (Giữ nguyên)
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String batDauStr = (gioBatDau != null) ? gioBatDau.format(formatter) : "null";
        String ketThucStr = (gioKetThuc != null) ? gioKetThuc.format(formatter) : "null";

        return "Ca lam{" +
                "maCa='" + maCa + '\'' +
                ", tenCa='" + tenCa + '\'' +
                ", gioBatDau='" + batDauStr + '\'' +
                ", gioKetThuc='" + ketThucStr + '\'' +
                '}';
    }


    /**
     * So sánh 2 CaLam dựa trên maCa.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaLam caLam = (CaLam) o;
        return Objects.equals(maCa, caLam.maCa);
    }

    /**
     * Băm đối tượng CaLam dựa trên maCa.
     */
    @Override
    public int hashCode() {
        return Objects.hash(maCa);
    }
}