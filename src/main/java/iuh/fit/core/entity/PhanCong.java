package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PhanCongCa")
public class PhanCong {

    @EmbeddedId
    private PhanCongId id;

    @ManyToOne
    @MapsId("maNV")
    @JoinColumn(name = "maNV")
    private NhanVien nhanVien;

    @ManyToOne
    @MapsId("maCa")
    @JoinColumn(name = "maCa")
    private CaLam caLam;

    public PhanCong(NhanVien nhanVien, CaLam caLam, LocalDate ngayLam) {
        this.nhanVien = nhanVien;
        this.caLam = caLam;
        this.id = new PhanCongId(nhanVien.getManv(), caLam.getMaCa(), ngayLam);
    }

    public LocalDate getNgayLam() {
        return id != null ? id.getNgayLam() : null;
    }
}
