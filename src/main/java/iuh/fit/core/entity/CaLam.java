package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CaLam")
public class CaLam {

    @Id
    @Column(name = "maCa", length = 20)
    private String maCa;

    @Column(name = "tenCa", nullable = false, length = 50)
    private String tenCa;

    @Column(name = "gioBatDau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gioKetThuc", nullable = false)
    private LocalTime gioKetThuc;

    public CaLam(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        setMaCa(maCa);
        setTenCa(tenCa);
        setGioBatDau(gioBatDau);
        setGioKetThuc(gioKetThuc);
    }

    public CaLam(CaLam ca) {
        this.maCa = ca.maCa;
        this.tenCa = ca.tenCa;
        this.gioBatDau = ca.gioBatDau;
        this.gioKetThuc = ca.gioKetThuc;
    }

    public void setMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty())
            throw new IllegalArgumentException("Mã ca không được rỗng.");
        this.maCa = maCa;
    }

    public void setTenCa(String tenCa) {
        if (tenCa == null || tenCa.trim().isEmpty())
            throw new IllegalArgumentException("Tên ca không được rỗng");
        this.tenCa = tenCa;
    }

    public void setGioBatDau(LocalTime gioBatDau) {
        if (gioBatDau == null)
            throw new IllegalArgumentException("Giờ bắt đầu không được rỗng.");
        this.gioBatDau = gioBatDau;
    }

    public void setGioKetThuc(LocalTime gioKetThuc) {
        if (gioKetThuc == null)
            throw new IllegalArgumentException("Giờ kết thúc không được rỗng.");
        this.gioKetThuc = gioKetThuc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaLam caLam = (CaLam) o;
        return Objects.equals(maCa, caLam.maCa);
    }

    @Override
    public int hashCode() { return Objects.hash(maCa); }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return "CaLam{maCa='" + maCa + "', tenCa='" + tenCa +
                "', gioBatDau='" + (gioBatDau != null ? gioBatDau.format(fmt) : "null") +
                "', gioKetThuc='" + (gioKetThuc != null ? gioKetThuc.format(fmt) : "null") + "'}";
    }
}
