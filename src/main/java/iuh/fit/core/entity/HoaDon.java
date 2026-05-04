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

    @Column(name = "maDon", length = 20)
    private String maDon;

    @Column(name = "maNV", length = 20)
    private String maNV;

    @Column(name = "maKM", length = 20)
    private String maKM;

    @Column(name = "maKH", length = 20)
    private String maKH;

    @Column(name = "giamGia")
    private float giamGia;

    @Column(name = "tongThanhToan")
    private float tongThanhToan;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "maDonDatMon", unique = true, nullable = false)
    private DonDatMon donDatMon;

    public HoaDon(String maHD, LocalDateTime ngayLap, String trangThai,
                  String hinhThucThanhToan, String maDon,
                  String maNV, String maKM) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.maDon = maDon;
        this.maNV = maNV;
        this.maKM = maKM;
    }

    public void tinhLaiTongThanhToan() {
        this.tongThanhToan = Math.max(0, this.tongTien - this.giamGia);
    }

    public float tinhTienThoi() {
        return Math.max(0, this.tienKhachDua - this.tongThanhToan);
    }

    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
    }
}