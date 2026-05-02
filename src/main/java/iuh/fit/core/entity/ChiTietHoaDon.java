package iuh.fit.core.entity;

import entity.DonDatMon;
import entity.MonAn;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "ChiTietHoaDon")
public class ChiTietHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTietHoaDon")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "maDon")
    private DonDatMon donDatMon;
    @ManyToOne
    @JoinColumn(name = "maMonAn")
    private MonAn monAn;
    @Column(name = "soLuong")
    private int soluong;
    @Column(name = "donGia")
    private float dongia;
    @Column(name = "thanhTien")
    private float thanhtien;
    private String tenMon;

}