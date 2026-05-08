package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DonDatMon")
public class DonDatMon {

    @Id
    @Column(name = "maDon", length = 20)
    private String maDon;

    @Column(name = "ngayKhoiTao", nullable = false)
    private LocalDateTime ngayKhoiTao;

    @Column(name = "thoiGianDen")
    private LocalDateTime thoiGianDen;

    
    @Column(name = "trangThai", columnDefinition = "NVARCHAR(50)")
    private String trangThai;

    @Column(name = "ghiChu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    // ====== Quan hệ với NhanVien (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNV")
    private NhanVien nhanVien;

    // ====== Quan hệ với Ban (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maBan")
    private Ban ban;

    // ====== Quan hệ với KhachHang (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKH")
    private KhachHang khachHang;

    // ====== Quan hệ với HoaDon (1-1) ======
    @OneToOne(mappedBy = "donDatMon")
    private HoaDon hoaDon;

    private String generateMaDon() {
        Random rand = new Random();
        int xxxx = rand.nextInt(9000) + 1000;
        return "DON" + xxxx;
    }

    // Constructor mặc định khởi tạo giá trị ban đầu
    public DonDatMon(boolean isNew) {
        if (isNew) {
            this.maDon = generateMaDon();
            this.ngayKhoiTao = LocalDateTime.now();
            this.ghiChu = "";
        }
    }

    public DonDatMon(String maDon, LocalDateTime ngayKhoiTao,
                     NhanVien nhanVien, KhachHang khachHang, Ban ban, String ghiChu) {
        setMaDon(maDon);
        setNgayKhoiTao(ngayKhoiTao);
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.ban = ban;
        this.ghiChu = ghiChu;
    }

    // Phương thức tiện ích lấy mã (tương thích code cũ)
    public String getMaNV() {
        return nhanVien != null ? nhanVien.getManv() : null;
    }

    public String getMaKH() {
        return khachHang != null ? khachHang.getMaKH() : null;
    }

    public String getMaBan() {
        return ban != null ? ban.getMaBan() : null;
    }

    public void setMaDon(String maDon) {
        if (maDon == null || maDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn không được rỗng.");
        }
        this.maDon = maDon;
    }

    public void setNgayKhoiTao(LocalDateTime ngayKhoiTao) {
        if (ngayKhoiTao == null) {
            throw new IllegalArgumentException("Ngày khởi tạo không được rỗng.");
        }
        this.ngayKhoiTao = ngayKhoiTao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DonDatMon)) return false;
        DonDatMon that = (DonDatMon) o;
        return Objects.equals(maDon, that.maDon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDon);
    }
}