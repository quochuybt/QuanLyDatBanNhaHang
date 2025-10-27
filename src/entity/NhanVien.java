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
    private String tenTK; // THÊM: Thuộc tính Tên Tài khoản để dễ dàng cập nhật/truy vấn

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
    }

    // Constructor đầy đủ (dành cho việc tạo mới)
    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro) {
        // Tên TK sẽ được set sau hoặc lấy từ SĐT nếu cần mặc định
        this(hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, "");
    }

    // Constructor đầy đủ (có thêm tenTK)
    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro, String tenTK) {
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
    }

    // Constructor dùng để truyền mã NV khi cập nhật hoặc đọc từ DB
    public NhanVien(String maNV, String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro) {
        // Constructor này không gọi phatSinhMaNV
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
        this.hoten = hoten;
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
        // Có thể thêm validation cho tenTK nếu cần (ví dụ: không chứa ký tự đặc biệt)
        this.tenTK = tenTK;
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
                '}';
    }
}