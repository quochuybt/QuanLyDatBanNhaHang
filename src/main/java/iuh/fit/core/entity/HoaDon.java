package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @Column(name = "maHD", length = 20)
    private String maHD;

    @Column(name = "ngayLap", nullable = false)
    private LocalDateTime ngayLap;

    @Column(name = "tongTien")
    private float tongTien;

    @Column(name = "trangThai", columnDefinition = "NVARCHAR(50)")
    private String trangThai;

    @Column(name = "hinhThucThanhToan", columnDefinition = "NVARCHAR(50)")
    private String hinhThucThanhToan;

    @Column(name = "tienKhachDua")
    private float tienKhachDua;

    @Column(name = "tenBan", columnDefinition = "NVARCHAR(50)")
    private String tenBan;

    @Column(name = "giamGia")
    private float giamGia;

    @Column(name = "tongThanhToan")
    private float tongThanhToan;

    // ====== Quan hệ với DonDatMon (1-1) ======
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "maDon", unique = true, nullable = false)
    private DonDatMon donDatMon;

    // ====== Quan hệ với NhanVien (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNV")
    private NhanVien nhanVien;

    // ====== Quan hệ với KhuyenMai (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKM")
    private KhuyenMai khuyenMai;

    // ====== Quan hệ với KhachHang (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKH")
    private KhachHang khachHang;

    public HoaDon(String maHD, LocalDateTime ngayLap, String trangThai,
                  String hinhThucThanhToan, DonDatMon donDatMon,
                  NhanVien nhanVien, KhuyenMai khuyenMai) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.donDatMon = donDatMon;
        this.nhanVien = nhanVien;
        this.khuyenMai = khuyenMai;
    }

    // Phương thức tiện ích lấy mã (tương thích code cũ)
    public String getMaDon() {
        return donDatMon != null ? donDatMon.getMaDon() : null;
    }

    public String getMaNV() {
        return nhanVien != null ? nhanVien.getManv() : null;
    }

    public String getMaKM() {
        return khuyenMai != null ? khuyenMai.getMaKM() : null;
    }

    public String getMaKH() {
        return khachHang != null ? khachHang.getMaKH() : null;
    }

    public void tinhLaiTongThanhToan() {
        this.tongThanhToan = Math.max(0, this.tongTien - this.giamGia);
    }

    public float tinhTienThoi() {
        return Math.max(0, this.tienKhachDua - this.tongThanhToan);
    }

    public String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoaDon)) return false;
        HoaDon that = (HoaDon) o;
        return Objects.equals(maHD, that.maHD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHD);
    }
}