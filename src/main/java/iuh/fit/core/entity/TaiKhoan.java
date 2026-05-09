package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TaiKhoan")
public class TaiKhoan extends BaseEntity {

    @Id
    @Column(name = "tenTK", length = 50)
    private String tentk;

    @Column(name = "matKhau", nullable = false, length = 255)
    private String matkhau;

    @Column(name = "trangThai", nullable = false)
    private boolean trangthai;

    @OneToOne(mappedBy = "taiKhoan")
    private NhanVien nhanVien;

    public TaiKhoan(String tentk, String matkhau, boolean trangthai) {
        setTentk(tentk);
        this.matkhau = matkhau;
        this.trangthai = trangthai;
    }

    public void setTentk(String tentk) {
        if (tentk == null || tentk.trim().isEmpty())
            throw new IllegalArgumentException("Tên tài khoản không được để rỗng.");
        this.tentk = tentk;
    }

    public String hashPassword(String plainPassword) {
        return "hashed_" + plainPassword.trim().toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "TaiKhoan{tentk='" + tentk + "', trangthai=" + trangthai + '}';
    }
}
