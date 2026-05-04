package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Column(name = "maNV", length = 20)
    private String maNV;

    @Column(name = "maKH", length = 20)
    private String maKH;

    @Column(name = "thoiGianDen")
    private LocalDateTime thoiGianDen;

    @Column(name = "trangThai", columnDefinition = "NVARCHAR(50)")
    private String trangThai;

    @Column(name = "maBan", length = 20)
    private String maBan;

    @Column(name = "ghiChu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    @OneToOne(mappedBy = "donDatMon")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "mNV")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "mBan")
    private Ban ban;

    @ManyToOne
    @JoinColumn(name = "mKH")
    private KhachHang khachHang;

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

    public DonDatMon(String maDon, LocalDateTime ngayKhoiTao, String maNV, String maKH, String maBan, String ghiChu) {
        setMaDon(maDon);
        setNgayKhoiTao(ngayKhoiTao);
        this.maNV = maNV;
        this.maKH = maKH;
        this.maBan = maBan;
        this.ghiChu = ghiChu;
    }

    public DonDatMon(DonDatMon donDatMon) {
        this.maDon = donDatMon.maDon;
        this.ngayKhoiTao = donDatMon.ngayKhoiTao;
        this.maNV = donDatMon.maNV;
        this.maKH = donDatMon.maKH;
        this.maBan = donDatMon.maBan;
        this.ghiChu = donDatMon.ghiChu;
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
}