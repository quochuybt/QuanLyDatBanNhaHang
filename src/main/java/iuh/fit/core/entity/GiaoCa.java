package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
@NoArgsConstructor
@AllArgsConstructor
@Data
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

}