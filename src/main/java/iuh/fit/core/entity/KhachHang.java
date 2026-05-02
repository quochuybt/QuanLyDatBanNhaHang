package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "KhachHang")
public class KhachHang {

    @Id
    @Column(name = "maKH", length = 20)
    private String maKH;

    @Column(name = "tenKH", nullable = false, length = 100)
    private String tenKH;

    @Column(name = "gioiTinh", nullable = false, length = 10)
    private String gioitinh;

    @Column(name = "sdt", nullable = false, unique = true, length = 15)
    private String sdt;

    @Enumerated(EnumType.STRING)
    @Column(name = "hangThanhVien", nullable = false, length = 20)
    private HangThanhVien hangThanhVien;

    @Column(name = "tongChiTieu", nullable = false)
    private float tongChiTieu;

    @Column(name = "ngaySinh")
    private LocalDate ngaySinh;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "ngayThamGia")
    private LocalDate ngayThamGia;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    public KhachHang(String tenKH, String gioitinh, String sdt,
                     LocalDate ngaySinh, String diaChi, String email) {
        setTenKH(tenKH);
        setGioitinh(gioitinh);
        setSdt(sdt);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
        setEmail(email);

        this.maKH = generateMaKH();
        this.ngayThamGia = LocalDate.now();
        this.tongChiTieu = 0;
        this.hangThanhVien = HangThanhVien.MEMBER;
    }

    public KhachHang(String maKH, String tenKH, String gioitinh, String sdt,
                     LocalDate ngaySinh, String diaChi, String email,
                     LocalDate ngayThamGia, float tongChiTieu, HangThanhVien hangThanhVien) {

        this.maKH = maKH;
        setTenKH(tenKH);
        setGioitinh(gioitinh);
        setSdt(sdt);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
        setEmail(email);
        setNgayThamGia(ngayThamGia);
        setTongChiTieu(tongChiTieu);
        setHangThanhVien(hangThanhVien);
    }

    private String generateMaKH() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dtf);
        int xxx = new Random().nextInt(1000);
        return "KH" + datePart + String.format("%03d", xxx);
    }

    public void setTenKH(String tenKH) {
        if (tenKH == null || tenKH.trim().isEmpty())
            throw new IllegalArgumentException("Họ tên không được rỗng");
        this.tenKH = tenKH.trim();
    }

    public void setGioitinh(String gioitinh) {
        if (gioitinh == null || gioitinh.trim().isEmpty())
            throw new IllegalArgumentException("Giới tính không được rỗng");

        if (!gioitinh.equalsIgnoreCase("Nam") &&
                !gioitinh.equalsIgnoreCase("Nữ") &&
                !gioitinh.equalsIgnoreCase("Khác"))
            throw new IllegalArgumentException("Giới tính không hợp lệ (Nam/Nữ/Khác)");

        this.gioitinh = gioitinh;
    }

    public void setSdt(String sdt) {
        if (sdt == null || !sdt.matches("\\d{10}"))
            throw new IllegalArgumentException("SĐT phải có đúng 10 chữ số");
        this.sdt = sdt;
    }

    public void setTongChiTieu(float tongChiTieu) {
        if (tongChiTieu < 0)
            throw new IllegalArgumentException("Tổng chi tiêu phải >= 0");
        this.tongChiTieu = tongChiTieu;
        capNhatHangThanhVien();
    }

    public void setHangThanhVien(HangThanhVien hangThanhVien) {
        if (hangThanhVien == null)
            throw new IllegalArgumentException("Hạng thành viên không được rỗng");
        this.hangThanhVien = hangThanhVien;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty())
            return; // cho phép null

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
            throw new IllegalArgumentException("Email không hợp lệ");

        this.email = email;
    }
    public float capNhatTongChiTieu(float soTien) {
        if (soTien < 0)
            throw new IllegalArgumentException("Số tiền không hợp lệ");

        this.tongChiTieu += soTien;
        capNhatHangThanhVien();
        return this.tongChiTieu;
    }

    public void capNhatHangThanhVien() {
        if (this.hangThanhVien == HangThanhVien.NONE) return;

        if (tongChiTieu > 50_000_000)
            hangThanhVien = HangThanhVien.DIAMOND;
        else if (tongChiTieu > 25_000_000)
            hangThanhVien = HangThanhVien.GOLD;
        else if (tongChiTieu > 10_000_000)
            hangThanhVien = HangThanhVien.SILVER;
        else if (tongChiTieu > 5_000_000)
            hangThanhVien = HangThanhVien.BRONZE;
        else
            hangThanhVien = HangThanhVien.MEMBER;
    }
    @Override
    public String toString() {
        return "KhachHang{" +
                "maKH='" + maKH + '\'' +
                ", tenKH='" + tenKH + '\'' +
                ", sdt='" + sdt + '\'' +
                ", hangThanhVien=" + hangThanhVien +
                ", tongChiTieu=" + tongChiTieu +
                '}';
    }
}