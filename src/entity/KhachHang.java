package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private String gioitinh;
    private String sdt;
    // THAY THẾ thuộc tính 'thanhVien' (boolean) trong yêu cầu bằng cách kiểm tra hangThanhVien != NONE
    private HangThanhVien hangThanhVien;
    private float tongChiTieu;
    // Thêm các thuộc tính khác cần thiết cho giao diện (Ngày sinh, Địa chỉ, Ngày tham gia, Email)
    private LocalDate ngaySinh;
    private String diaChi;
    private LocalDate ngayThamGia;
    private String email;

    // Định dạng: KHyyyyMMDDXXX
    private String generateMaKH() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dtf);
        Random random = new Random();
        int xxx = random.nextInt(1000); // Số ngẫu nhiên từ 0 đến 999
        return "KH" + datePart + String.format("%03d", xxx); // %03d đảm bảo luôn có 3 chữ số
    }

    // Constructor 3.1: Mặc nhiên
    public KhachHang() {
        this.maKH = generateMaKH();
        this.tenKH = "";
        this.gioitinh = "Nam";
        this.sdt = "";
        this.hangThanhVien = HangThanhVien.NONE;
        this.tongChiTieu = 0.0f;
        this.ngaySinh = LocalDate.of(2000, 1, 1); // Giá trị mặc định
        this.diaChi = "";
        this.ngayThamGia = LocalDate.now(); // Ngày tạo tài khoản
        this.email = "";
    }

    // Constructor 3.2: Có đầy đủ tham số (sử dụng các thuộc tính cơ bản trong yêu cầu)
    // NOTE: Cập nhật constructor để phù hợp với Entity class hoàn chỉnh hơn
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
        setTongChiTieu(tongChiTieu);
        setHangThanhVien(hangThanhVien);
    }

    // Constructor 3.3: Copy Constructor
    public KhachHang(KhachHang khachHang) {
        this(khachHang.maKH, khachHang.tenKH, khachHang.gioitinh, khachHang.sdt,
                khachHang.ngaySinh, khachHang.diaChi, khachHang.email, khachHang.ngayThamGia,
                khachHang.tongChiTieu, khachHang.hangThanhVien);
    }

    // Constructor đơn giản cho dữ liệu mẫu
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

    // 2.1 setMaKH(string maKH)
    // Giữ nguyên phương thức này để kiểm tra tính hợp lệ nếu cần, nhưng không dùng để phát sinh tự động ở đây.
    public void setMaKH(String maKH) {
        if (maKH == null || maKH.isEmpty()) {
            this.maKH = generateMaKH(); // Phát sinh mã nếu rỗng
            return;
        }
        // Kiểm tra định dạng nếu cần
        this.maKH = maKH;
    }

    // 2.2 setTenKH(string tenKH)
    public void setTenKH(String tenKH) {
        if (tenKH == null || tenKH.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được rỗng");
        }
        this.tenKH = tenKH;
    }

    // 2.3 setGioiTinh(string gioitinh)
    public void setGioitinh(String gioitinh) {
        if (gioitinh == null || gioitinh.trim().isEmpty()) {
            throw new IllegalArgumentException("Giới tính không được rỗng");
        }
        if (!gioitinh.equalsIgnoreCase("Nam") && !gioitinh.equalsIgnoreCase("Nữ") && !gioitinh.equalsIgnoreCase("Khác")) {
            throw new IllegalArgumentException("Giới tính không hợp lệ (Nam/Nữ/Khác)");
        }
        this.gioitinh = gioitinh;
    }

    // 2.4 setSdt(string sdt)
    public void setSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty() || !sdt.matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại không được để rỗng và phải đúng định dạng (10 ký tự chữ số)");
        }
        this.sdt = sdt;
    }

    // 2.5 setThanhVien(Boolean thanhVien) (Đã được thay thế/kiểm soát bởi setHangThanhVien)
    // Để giữ nguyên yêu cầu 2.5:
    public void setThanhVien(boolean thanhVien) {
        if (!thanhVien) {
            setHangThanhVien(HangThanhVien.NONE);
        } else if (this.hangThanhVien == HangThanhVien.NONE) {
            // Nếu chuyển từ không thành viên sang có, kiểm tra để nâng hạng lần đầu
            capNhatHangThanhVien();
        }
    }

    // 2.6 setTongChiTieu(Float tongChiTieu)
    public void setTongChiTieu(float tongChiTieu) {
        if (tongChiTieu < 0) {
            throw new IllegalArgumentException("Tổng chi tiêu không được rỗng và phải >= 0");
        }
        this.tongChiTieu = tongChiTieu;
        capNhatHangThanhVien(); // Cập nhật hạng thành viên khi chi tiêu thay đổi
    }

    // 2.7 setHangThanhVien(HangThanhVien hangThanhVien)
    public void setHangThanhVien(HangThanhVien hangThanhVien) {
        if (hangThanhVien == null) {
            throw new IllegalArgumentException("Hạng thành viên không được rỗng");
        }
        this.hangThanhVien = hangThanhVien;
    }

    // --- Các phương thức Getter ---

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

    // --- Phương thức cập nhật (4.1 & 4.2) ---

    // 4.1 Viết phương thức cập nhật tổng chi tiêu: tongChiTieu += soTien
    public float capNhatTongChiTieu(float soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền cộng thêm không hợp lệ");
        }
        this.tongChiTieu += soTien;
        capNhatHangThanhVien();
        return this.tongChiTieu;
    }

    // 4.2 Viết phương thức cập nhật hạng thành viên
    public void capNhatHangThanhVien() {
        if (this.tongChiTieu < 100000 || this.hangThanhVien == HangThanhVien.NONE) {
            // Giả định nếu đã là thành viên (MEMBER trở lên) thì không tự động giáng về NONE.
            // Trừ khi setThanhVien(false) được gọi, hoặc chi tiêu < 100k và chưa bao giờ là thành viên.
            return;
        }

        if (tongChiTieu > 5500000) {
            hangThanhVien = HangThanhVien.DIAMOND;
        } else if (tongChiTieu > 3500000) {
            hangThanhVien = HangThanhVien.GOLD;
        } else if (tongChiTieu > 1500000) {
            hangThanhVien = HangThanhVien.SILVER;
        } else if (tongChiTieu > 800000) {
            hangThanhVien = HangThanhVien.BRONE;
        } else if (tongChiTieu > 100000) {
            hangThanhVien = HangThanhVien.MEMBER;
        }
    }

    // --- Các Setter mở rộng ---

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


    // 4. Viết phương thức toString()
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