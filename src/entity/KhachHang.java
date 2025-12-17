package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private String gioitinh;
    private String sdt;
    private HangThanhVien hangThanhVien;
    private float tongChiTieu;
    private LocalDate ngaySinh;
    private String diaChi;
    private LocalDate ngayThamGia;
    private String email;

    private String generateMaKH() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dtf);
        Random random = new Random();
        int xxx = random.nextInt(1000);
        return "KH" + datePart + String.format("%03d", xxx);
    }

    public KhachHang() {
        this.maKH = generateMaKH();
        this.tenKH = "";
        this.gioitinh = "Nam";
        this.sdt = "";
        this.hangThanhVien = HangThanhVien.MEMBER;
        this.tongChiTieu = 0.0f;
        this.ngaySinh = LocalDate.of(2000, 1, 1);
        this.diaChi = "";
        this.ngayThamGia = LocalDate.now();
        this.email = null;
    }

    public KhachHang(String maKH, String tenKH, String gioitinh, String sdt,
                     LocalDate ngaySinh, String diaChi, String email, LocalDate ngayThamGia,
                     float tongChiTieu, HangThanhVien hangThanhVien) {
        setMaKH(maKH);
        setTenKH(tenKH);
        setGioitinh(gioitinh);
        setSdt(sdt);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
        setEmail(email);
        setNgayThamGia(ngayThamGia);
        setHangThanhVien(hangThanhVien);
        setTongChiTieu(tongChiTieu);

    }

    public KhachHang(KhachHang khachHang) {
        this(khachHang.maKH, khachHang.tenKH, khachHang.gioitinh, khachHang.sdt,
                khachHang.ngaySinh, khachHang.diaChi, khachHang.email, khachHang.ngayThamGia,
                khachHang.tongChiTieu, khachHang.hangThanhVien);
    }

    public KhachHang(String maKH, String tenKH, String gioitinh, String sdt, String email, float tongChiTieu, HangThanhVien hang) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.gioitinh = gioitinh;
        this.sdt = sdt;
        this.email = email;
        this.tongChiTieu = tongChiTieu;
        this.hangThanhVien = hang;
        this.ngaySinh = LocalDate.of(2005, 11, 19);
        this.diaChi = "213, Nguyễn Văn Lượng, Gò Vấp";
        this.ngayThamGia = LocalDate.of(2019, 9, 13);
    }

    public void setMaKH(String maKH) {
        if (maKH == null || maKH.isEmpty()) {
            this.maKH = generateMaKH();
            return;
        }
        this.maKH = maKH;
    }

    public void setTenKH(String tenKH) {
        if (tenKH == null || tenKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được rỗng");
        }
        this.tenKH = tenKH;
    }

    public void setGioitinh(String gioitinh) {
        if (gioitinh == null || gioitinh.trim().isEmpty()) {
            throw new IllegalArgumentException("Giới tính không được rỗng");
        }
        if (!gioitinh.equalsIgnoreCase("Nam") && !gioitinh.equalsIgnoreCase("Nữ") && !gioitinh.equalsIgnoreCase("Khác")) {
            throw new IllegalArgumentException("Giới tính không hợp lệ (Nam/Nữ/Khác)");
        }
        this.gioitinh = gioitinh;
    }

    public void setSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty() || !sdt.matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại không được để rỗng và phải đúng định dạng (10 ký tự chữ số)");
        }
        this.sdt = sdt;
    }

    public void setThanhVien(boolean thanhVien) {
        if (!thanhVien) {
            setHangThanhVien(HangThanhVien.NONE);
        } else if (this.hangThanhVien == HangThanhVien.NONE) {
            capNhatHangThanhVien();
        }
    }

    public void setTongChiTieu(float tongChiTieu) {
        if (tongChiTieu < 0) {
            throw new IllegalArgumentException("Tổng chi tiêu không được rỗng và phải >= 0");
        }
        this.tongChiTieu = tongChiTieu;
        capNhatHangThanhVien();
    }

    public void setHangThanhVien(HangThanhVien hangThanhVien) {
        if (hangThanhVien == null) {
            throw new IllegalArgumentException("Hạng thành viên không được rỗng");
        }
        this.hangThanhVien = hangThanhVien;
    }


    public String getMaKH() { return maKH; }
    public String getTenKH() { return tenKH; }
    public String getGioitinh() { return gioitinh; }
    public String getSdt() { return sdt; }
    public HangThanhVien getHangThanhVien() { return hangThanhVien; }
    public float getTongChiTieu() { return tongChiTieu; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public String getDiaChi() { return diaChi; }
    public LocalDate getNgayThamGia() { return ngayThamGia; }
    public String getEmail() { return email; }


    public float capNhatTongChiTieu(float soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền cộng thêm không hợp lệ");
        }
        this.tongChiTieu += soTien;
        capNhatHangThanhVien();
        return this.tongChiTieu;
    }

    public void capNhatHangThanhVien() {
        if (this.hangThanhVien == HangThanhVien.NONE) {
            return;
        }
        if (tongChiTieu > 50000000) {
            hangThanhVien = HangThanhVien.DIAMOND;
        } else if (tongChiTieu > 25000000) {
            hangThanhVien = HangThanhVien.GOLD;
        } else if (tongChiTieu > 10000000) {
            hangThanhVien = HangThanhVien.SILVER;
        } else if (tongChiTieu > 5000000) {
            hangThanhVien = HangThanhVien.BRONZE;
        } else {
            if (hangThanhVien.ordinal() < HangThanhVien.MEMBER.ordinal()) {
                hangThanhVien = HangThanhVien.MEMBER;
            }
        }
    }


    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public void setNgayThamGia(LocalDate ngayThamGia) {
        this.ngayThamGia = ngayThamGia;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public String toString() {
        return "KhachHang{" +
                "maKH='" + maKH + '\'' +
                ", tenKH='" + tenKH + '\'' +
                ", gioitinh='" + gioitinh + '\'' +
                ", sdt='" + sdt + '\'' +
                ", hangThanhVien=" + hangThanhVien +
                ", tongChiTieu=" + tongChiTieu +
                '}';
    }
}