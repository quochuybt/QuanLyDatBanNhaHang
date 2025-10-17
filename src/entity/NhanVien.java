package entity;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
    private String viTri;

    public NhanVien() {
        this.viTri = "Nhân viên";
        this.manv = phatSinhMaNV(this.viTri);
        this.hoten = "Chưa có tên";
        this.ngaysinh = LocalDate.now().minusYears(18);
        this.gioitinh = "Khác";
        this.sdt = "0000000000";
        this.diachi = "Chưa cập nhật";
        this.ngayvaolam = LocalDate.now();
        this.luong = 2000000f;
    }

    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, String viTri) {
        setViTri(viTri);
        this.manv = phatSinhMaNV(this.viTri);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
    }

    public NhanVien(NhanVien other) {
        this.manv = phatSinhMaNV(other.viTri);
        this.hoten = other.hoten;
        this.ngaysinh = other.ngaysinh;
        this.gioitinh = other.gioitinh;
        this.sdt = other.sdt;
        this.diachi = other.diachi;
        this.ngayvaolam = other.ngayvaolam;
        this.luong = other.luong;
        this.viTri = other.viTri;
    }

    private String phatSinhMaNV(String viTri) {
        String maViTri;
        if (viTri.equalsIgnoreCase("Quản lý")) {
            maViTri = "02";
        } else {
            maViTri = "01";
        }
        int soNgauNhien = ThreadLocalRandom.current().nextInt(100, 1000);
        return "NV" + maViTri + soNgauNhien;
    }

    public String getManv() {
        return manv;
    }

    private void setManv(String manv) {
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

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        List<String> viTriHopLe = Arrays.asList("Nhân viên", "Quản lý");
        if (viTri == null || !viTriHopLe.contains(viTri)) {
            throw new IllegalArgumentException("Vị trí không hợp lệ. Chỉ chấp nhận: " + viTriHopLe);
        }
        this.viTri = viTri;
    }

    public float tinhLuongThang() {
        return this.luong;
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
                ", viTri='" + viTri + '\'' +
                ", luong=" + luong +
                '}';
    }
}
