package entity;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class NhanVien {

    private String manv;
    private String hoten;
    private LocalDate ngaysinh;
    private String gioitinh;
    private String sdt;
    private String diachi;
    private LocalDate ngayvaolam;
    private float luong;
    private VaiTro vaiTro;
    private String tenTK;
    private String email; // 🌟 THÊM: Thuộc tính email

    // Constructor mặc định
    public NhanVien() {
        this.vaiTro = VaiTro.NHANVIEN;
        this.manv = phatSinhMaNV(vaiTro);
        this.hoten = "Chưa có tên";
        this.ngaysinh = LocalDate.now().minusYears(18);
        this.gioitinh = "Khác";
        this.sdt = "0000000000";
        this.diachi = "Chưa cập nhật";
        this.ngayvaolam = LocalDate.now();
        this.luong = 2000000f;
        this.tenTK = "";
        this.email = "example@starguardian.com"; // Giá trị mặc định
    }

    // Constructor đầy đủ (dành cho việc tạo mới) - Đã sửa
    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro, String email) {
        this(hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, "", email);
    }

    // Constructor đầy đủ (có thêm tenTK) - Đã sửa
    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro, String tenTK, String email) {
        setVaiTro(vaiTro);
        this.manv = phatSinhMaNV(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        setTenTK(tenTK);
        setEmail(email); // 🌟 GỌI SETTER MỚI
    }

    // Constructor dùng để truyền mã NV khi cập nhật hoặc đọc từ DB - Đã sửa
    public NhanVien(String maNV, String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro, String email) {
        this.manv = maNV;
        setVaiTro(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        this.tenTK = "";
        setEmail(email); // 🌟 GỌI SETTER MỚI
    }

    // Constructor copy
    public NhanVien(NhanVien other) {
        this.vaiTro = other.vaiTro;
        this.manv = phatSinhMaNV(other.vaiTro);
        this.hoten = other.hoten;
        this.ngaysinh = other.ngaysinh;
        this.gioitinh = other.gioitinh;
        this.sdt = other.sdt;
        this.diachi = other.diachi;
        this.ngayvaolam = other.ngayvaolam;
        this.luong = other.luong;
        this.tenTK = other.tenTK;
        this.email = other.email; // 🌟 THÊM
    }

    public NhanVien(String maNV, String hoTen) {
        this.manv = maNV;
        this.hoten = hoTen;
    }

    // Logic phát sinh mã NV
    private String phatSinhMaNV(VaiTro vaiTro) {
        String maVaiTro;
        if (vaiTro == VaiTro.QUANLY) {
            maVaiTro = "02";
        } else {
            maVaiTro = "01";
        }
        int soNgauNhien = ThreadLocalRandom.current().nextInt(100, 1000);
        return "NV" + maVaiTro + soNgauNhien;
    }

    // =================================================================
    // GETTERS & SETTERS (Có Validation)
    // =================================================================

    public String getManv() {
        return manv;
    }

    public void setManv(String manv) {
        this.manv = manv;
    }

    public String getHoten() {
        return hoten;
    }

    public void setHoten(String hoten) {
        if (hoten == null || hoten.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được rỗng");
        }

        // Regex: Chấp nhận chữ cái (bao gồm tiếng Việt có dấu, không dấu), khoảng trắng,
        // dấu chấm, gạch ngang, nháy đơn. Loại bỏ số hoàn toàn.
        String namePattern = "^[\\p{L} .'-]+$";

        if (!hoten.trim().matches(namePattern)) {
            throw new IllegalArgumentException("Họ tên không hợp lệ (Không được chứa số hoặc ký tự đặc biệt không được phép).");
        }

        this.hoten = hoten.trim();
    }

    public LocalDate getNgaysinh() {
        return ngaysinh;
    }

    public void setNgaysinh(LocalDate ngaysinh) {
        if (ngaysinh == null || Period.between(ngaysinh, LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Nhân viên phải >= 18 tuổi");
        }
        this.ngaysinh = ngaysinh;
    }

    public String getGioitinh() {
        return gioitinh;
    }

    public void setGioitinh(String gioitinh) {
        this.gioitinh = gioitinh;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        String sdtPattern = "^0\\d{9}$";
        if (sdt == null || !sdt.matches(sdtPattern)) {
            throw new IllegalArgumentException("SĐT không hợp lệ (phải có 10 chữ số, bắt đầu bằng 0)");
        }
        this.sdt = sdt;
    }

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }

    public LocalDate getNgayvaolam() {
        return ngayvaolam;
    }

    public void setNgayvaolam(LocalDate ngayvaolam) {
        this.ngayvaolam = ngayvaolam;
    }

    public float getLuong() {
        return luong;
    }

    public void setLuong(float luong) {
        if (luong <= 0) {
            throw new IllegalArgumentException("Lương phải > 0");
        }
        this.luong = luong;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTenTK() {
        return tenTK;
    }

    public void setTenTK(String tenTK) {
        this.tenTK = tenTK;
    }

    // 🌟 THÊM: Getter cho email
    public String getEmail() {
        return email;
    }

    // 🌟 THÊM: Setter cho email (có validation đơn giản)
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được rỗng.");
        }
        // Regex đơn giản để kiểm tra định dạng email
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        if (!email.trim().matches(emailPattern)) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        this.email = email.trim();
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "NhanVien{" +
                "manv='" + manv + '\'' +
                ", hoten='" + hoten + '\'' +
                ", ngaysinh=" + ngaysinh.format(formatter) +
                ", gioitinh='" + gioitinh + '\'' +
                ", sdt='" + sdt + '\'' +
                ", diachi='" + diachi + '\'' +
                ", ngayvaolam=" + ngayvaolam.format(formatter) +
                ", luong=" + luong +
                ", vaiTro=" + vaiTro.name() +
                ", tenTK='" + tenTK + '\'' +
                ", email='" + email + '\'' + // 🌟 THÊM EMAIL VÀO toString
                '}';
    }
}