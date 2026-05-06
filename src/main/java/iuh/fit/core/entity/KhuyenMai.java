package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {

    @Id
    @Column(name = "maKM", length = 20)
    private String maKM;

    @Column(name = "tenKM", nullable = false, length = 100)
    private String tenChuongTrinh;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "loaiGiam", length = 50)
    private String loaiKhuyenMai;

    @Column(name = "giaTriGiam")
    private double giaTri;

    @Column(name = "dieuKienApDung")
    private double dieuKienApDung;

    @Column(name = "ngayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "ngayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "soLuongGioiHan")
    private int soLuongGioiHan;

    @Column(name = "soLuotDaDung")
    private int soLuotDaDung;

    // ====== Quan hệ với HoaDon (1-N) ======
    @OneToMany(mappedBy = "khuyenMai")
    private Set<HoaDon> hoaDons = new HashSet<>();

    public KhuyenMai(String maKM, String tenChuongTrinh, String moTa, String loaiKhuyenMai,
                     double giaTri, double dieuKienApDung,
                     LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this.maKM = maKM;
        this.tenChuongTrinh = tenChuongTrinh;
        this.moTa = moTa;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTri = giaTri;
        this.dieuKienApDung = dieuKienApDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    /**
     * Kiểm tra khuyến mãi còn hiệu lực không
     */
    public boolean isActive() {
        return "Đang áp dụng".equals(trangThai)
                && (ngayKetThuc == null || !ngayKetThuc.isBefore(LocalDate.now()));
    }

    /**
     * Kiểm tra còn lượt sử dụng không (0 = không giới hạn)
     */
    public boolean conLuotSuDung() {
        return soLuongGioiHan == 0 || soLuotDaDung < soLuongGioiHan;
    }

    /**
     * Tăng số lượt đã dùng
     */
    public void tangLuotDaDung() {
        this.soLuotDaDung++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhuyenMai)) return false;
        KhuyenMai that = (KhuyenMai) o;
        return Objects.equals(maKM, that.maKM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maKM);
    }

    @Override
    public String toString() {
        return "KhuyenMai{" +
                "maKM='" + maKM + '\'' +
                ", tenChuongTrinh='" + tenChuongTrinh + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}