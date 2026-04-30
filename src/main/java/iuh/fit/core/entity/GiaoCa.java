package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GiaoCa")
public class GiaoCa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maGiaoCa")
    private int maGiaoCa;

    @Column(name = "maNV", length = 20, nullable = false)
    private String maNV;

    @Column(name = "thoiGianBatDau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoiGianKetThuc")
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "tienDauCa")
    private double tienDauCa;

    @Column(name = "tienCuoiCa")
    private double tienCuoiCa;

    @Column(name = "tienHeThongTinh")
    private double tienHeThongTinh;

    @Column(name = "chenhLech")
    private double chenhLech;

    @Column(name = "ghiChu", columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, double tienDauCa) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.tienDauCa = tienDauCa;
    }

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                  double tienDauCa, double tienCuoiCa, double tienHeThongTinh, double chenhLech, String ghiChu) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.tienDauCa = tienDauCa;
        this.tienCuoiCa = tienCuoiCa;
        this.tienHeThongTinh = tienHeThongTinh;
        this.chenhLech = chenhLech;
        this.ghiChu = ghiChu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiaoCa giaoCa = (GiaoCa) o;
        return maGiaoCa == giaoCa.maGiaoCa;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maGiaoCa);
    }

    @Override
    public String toString() {
        return "GiaoCa{" +
                "maGiaoCa=" + maGiaoCa +
                ", maNV='" + maNV + '\'' +
                ", thoiGianBatDau=" + thoiGianBatDau +
                ", thoiGianKetThuc=" + thoiGianKetThuc +
                ", tienDauCa=" + tienDauCa +
                ", tienCuoiCa=" + tienCuoiCa +
                ", chenhLech=" + chenhLech +
                '}';
    }
}