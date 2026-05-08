package iuh.fit.core.entity;


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
    @ManyToOne
    @JoinColumn(name = "maDon")
    private DonDatMon donDatMon;

    @Id
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