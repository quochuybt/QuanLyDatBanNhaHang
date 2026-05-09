package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GiaoCa")
@Builder
public class GiaoCa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maGiaoCa")
    private String maGiaoCa;

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

    @ManyToOne
    @JoinColumn(name = "mNV")
    private NhanVien nhanVien;
}