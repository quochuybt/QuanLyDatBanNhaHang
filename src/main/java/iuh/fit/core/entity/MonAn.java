package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MonAn")
public class MonAn {

    @Id
    @Column(name = "maMonAn", length = 20)
    private String maMonAn;

    @Column(name = "tenMon", nullable = false, length = 100)
    private String tenMon;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "donGia")
    private float donGia;

    @Column(name = "donViTinh", length = 50)
    private String donViTinh;

    @Column(name = "trangThai", length = 20)
    private String trangThai;

    @Column(name = "hinhAnh", length = 255)
    private String hinhAnh;

    // ====== Quan hệ với DanhMucMon (N-1) ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maDM", referencedColumnName = "madm")
    private DanhMucMon danhMucMon;

    // ====== Quan hệ với ChiTietHoaDon (1-N) ======
    @OneToMany(mappedBy = "monAn")
    private Set<ChiTietHoaDon> chiTietHoaDons = new HashSet<>();

    public MonAn(String maMonAn, String tenMon, String moTa, float donGia,
                 String donViTinh, String trangThai, String hinhAnh, DanhMucMon danhMucMon) {
        this.maMonAn = maMonAn;
        this.tenMon = tenMon;
        this.moTa = moTa;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
        this.danhMucMon = danhMucMon;
    }

    // Constructor tiện ích: tạo từ mã danh mục String (dùng khi chưa load DanhMucMon)
    public MonAn(String maMonAn, String tenMon, String moTa, float donGia,
                 String donViTinh, String trangThai, String hinhAnh, String maDM) {
        this.maMonAn = maMonAn;
        this.tenMon = tenMon;
        this.moTa = moTa;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
        if (maDM != null && !maDM.isEmpty()) {
            DanhMucMon dm = new DanhMucMon();
            dm.setMadm(maDM);
            this.danhMucMon = dm;
        }
    }

    // Phương thức tiện ích lấy mã danh mục (tương thích code cũ)
    public String getMaDM() {
        return danhMucMon != null ? danhMucMon.getMadm() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonAn)) return false;
        MonAn monAn = (MonAn) o;
        return Objects.equals(maMonAn, monAn.maMonAn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maMonAn);
    }

    @Override
    public String toString() {
        return tenMon;
    }
}