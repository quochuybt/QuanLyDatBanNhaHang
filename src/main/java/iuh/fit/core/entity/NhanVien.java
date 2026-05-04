package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "NhanVien")
public class NhanVien {

    @Id
    @Column(name = "maNV", length = 20)
    private String manv;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoten;

    @Column(name = "ngaySinh")
    private LocalDate ngaysinh;

    @Column(name = "gioiTinh", nullable = false, length = 10)
    private String gioitinh;

    @Column(name = "sdt", unique = true, nullable = false, length = 15)
    private String sdt;

    @Column(name = "diaChi", length = 255)
    private String diachi;

    @Column(name = "ngayVaoLam", nullable = false)
    private LocalDate ngayvaolam;

    @Column(name = "luong", nullable = false)
    private float luong;

    @Enumerated(EnumType.STRING)
    @Column(name = "vaiTro", nullable = false, length = 20)
    private VaiTro vaiTro;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tenTK", unique = true, nullable = false)
    private TaiKhoan taiKhoan;

    @OneToMany(mappedBy = "nhanVien")
    private Set<DonDatMon> donDatMons = new HashSet<>();

    @OneToMany(mappedBy = "nhanVien")
    private Set<GiaoCa> giaoCas = new HashSet<>();

    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, VaiTro vaiTro, String email) {
        setVaiTro(vaiTro);
        this.manv = phatSinhMaNV(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        setEmail(email);
    }

    public NhanVien(String maNV, String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, VaiTro vaiTro, String email) {
        this.manv = maNV;
        setVaiTro(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        setEmail(email);
    }

    public NhanVien(String maNV, String hoTen) {
        this.manv = maNV;
        this.hoten = hoTen;
    }

    private String phatSinhMaNV(VaiTro vaiTro) {
        String maVaiTro = (vaiTro == VaiTro.QUANLY) ? "02" : "01";
        int soNgauNhien = ThreadLocalRandom.current().nextInt(100, 1000);
        return "NV" + maVaiTro + soNgauNhien;
    }

    public void setHoten(String hoten) {
        if (hoten == null || hoten.trim().isEmpty())
            throw new IllegalArgumentException("Họ tên không được rỗng");
        if (!hoten.trim().matches("^[\\p{L} .'-]+$"))
            throw new IllegalArgumentException("Họ tên không hợp lệ (Không được chứa số hoặc ký tự đặc biệt không được phép).");
        this.hoten = hoten.trim();
    }

    public void setNgaysinh(LocalDate ngaysinh) {
        if (ngaysinh == null || Period.between(ngaysinh, LocalDate.now()).getYears() < 18)
            throw new IllegalArgumentException("Nhân viên phải >= 18 tuổi");
        this.ngaysinh = ngaysinh;
    }

    public void setSdt(String sdt) {
        if (sdt == null || !sdt.matches("^0\\d{9}$"))
            throw new IllegalArgumentException("SĐT không hợp lệ (phải có 10 chữ số, bắt đầu bằng 0)");
        this.sdt = sdt;
    }

    public void setLuong(float luong) {
        if (luong <= 0) throw new IllegalArgumentException("Lương phải > 0");
        this.luong = luong;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email không được rỗng.");
        if (!email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
            throw new IllegalArgumentException("Email không đúng định dạng.");
        this.email = email.trim();
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "NhanVien{manv='" + manv + "', hoten='" + hoten + "', ngaysinh=" +
                (ngaysinh != null ? ngaysinh.format(fmt) : "null") +
                ", gioitinh='" + gioitinh + "', sdt='" + sdt + "', diachi='" + diachi +
                "', ngayvaolam=" + (ngayvaolam != null ? ngayvaolam.format(fmt) : "null") +
                ", luong=" + luong + ", vaiTro=" + vaiTro +
                ", email='" + email + "'}";
    }
}
