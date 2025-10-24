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
    private VaiTro vaiTro;

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
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }
    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong ,VaiTro vaiTro) {
        setVaiTro(vaiTro);
        this.manv = phatSinhMaNV(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
    }
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
    }

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

    public String getManv() {
        return manv;
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
                '}';
    }
//        public static void main(String[] args) {
//            System.out.println("--- Bắt đầu chương trình test NhanVien ---");
//
//            // === KỊCH BẢN 1: TẠO ĐỐI TƯỢNG HỢP LỆ (HAPPY PATH) ===
//            System.out.println("\n--- Kịch bản 1: Tạo đối tượng hợp lệ ---");
//            try {
//                NhanVien nv1 = new NhanVien(
//                        "Nguyễn Văn An",
//                        LocalDate.of(1995, 10, 20),
//                        "Nam",
//                        "0912345678",
//                        "123 Lê Lợi, Q1, TPHCM",
//                        LocalDate.of(2023, 1, 15),
//                        12000000f,
//                        VaiTro.NHANVIEN
//                );
//                System.out.println("Tạo Nhân Viên thành công:");
//                System.out.println(nv1);
//
//                NhanVien ql1 = new NhanVien(
//                        "Trần Thị Bình",
//                        LocalDate.of(1990, 5, 1),
//                        "Nữ",
//                        "0987654321",
//                        "456 CMT8, Q3, TPHCM",
//                        LocalDate.of(2020, 2, 10),
//                        25000000f,
//                        VaiTro.QUANLY
//                );
//                System.out.println("\nTạo Quản Lý thành công:");
//                System.out.println(ql1);
//
//            } catch (IllegalArgumentException e) {
//                System.out.println("LỖI KHÔNG MONG MUỐN (Kịch bản 1): " + e.getMessage());
//            }
//
//            // === KỊCH BẢN 2: TẠO BẰNG CONSTRUCTOR MẶC ĐỊNH ===
//            System.out.println("\n--- Kịch bản 2: Dùng constructor mặc định ---");
//            NhanVien nvDefault = new NhanVien();
//            System.out.println(nvDefault);
//
//            // === KỊCH BẢN 3: TẠO BẰNG COPY CONSTRUCTOR ===
//            System.out.println("\n--- Kịch bản 3: Dùng copy constructor ---");
//            NhanVien nvToCopy = new NhanVien("Lê Văn C", LocalDate.of(2000, 1, 1), "Nam", "0333444555", "789 Nguyễn Trãi, Q5", LocalDate.now(), 8000000f, VaiTro.NHANVIEN);
//            NhanVien nvCopied = new NhanVien(nvToCopy);
//            System.out.println("Đối tượng gốc:");
//            System.out.println(nvToCopy);
//            System.out.println("Đối tượng sao chép (Lưu ý: mã NV sẽ khác nhau do logic phatSinhMaNV):");
//            System.out.println(nvCopied);
//
//            // === KỊCH BẢN 4: TEST VALIDATION (CÁC TRƯỜNG HỢP LỖI) ===
//            System.out.println("\n--- Kịch bản 4: Test các trường hợp lỗi validation ---");
//
//            // 4a: Lỗi Tên rỗng
//            try {
//                nvDefault.setHoten("   ");
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (Tên): " + e.getMessage());
//            }
//
//            // 4b: Lỗi Tuổi (dưới 18)
//            try {
//                nvDefault.setNgaysinh(LocalDate.now().minusYears(17)); // Mới 17 tuổi
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (Tuổi): " + e.getMessage());
//            }
//
//            // 4c: Lỗi SĐT (sai định dạng)
//            try {
//                nvDefault.setSdt("12345"); // Sai
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (SĐT ngắn): " + e.getMessage());
//            }
//            try {
//                nvDefault.setSdt("09123456789"); // 11 số (sai)
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (SĐT dài): " + e.getMessage());
//            }
//            try {
//                nvDefault.setSdt("a912345678"); // Có chữ (sai)
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (SĐT có chữ): " + e.getMessage());
//            }
//
//            // 4d: Lỗi Lương (<= 0)
//            try {
//                nvDefault.setLuong(0);
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (Lương = 0): " + e.getMessage());
//            }
//            try {
//                nvDefault.setLuong(-100000);
//            } catch (IllegalArgumentException e) {
//                System.out.println("Bắt lỗi thành công (Lương âm): " + e.getMessage());
//            }
//
//            // === KỊCH BẢN 5: CẬP NHẬT THÔNG TIN HỢP LỆ ===
//            System.out.println("\n--- Kịch bản 5: Cập nhật thông tin thành công ---");
//            try {
//                NhanVien nvUpdate = new NhanVien();
//                System.out.println("Trước khi cập nhật:");
//                System.out.println(nvUpdate);
//
//                nvUpdate.setHoten("Đỗ Thị Diễm");
//                nvUpdate.setNgaysinh(LocalDate.of(1999, 11, 20));
//                nvUpdate.setGioitinh("Nữ");
//                nvUpdate.setSdt("0888999111");
//                nvUpdate.setDiachi("Bình Dương");
//                nvUpdate.setLuong(9500000f);
//
//                System.out.println("Sau khi cập nhật:");
//                System.out.println(nvUpdate);
//            } catch (IllegalArgumentException e) {
//                System.out.println("LỖI KHÔNG MONG MUỐN (Kịch bản 5): " + e.getMessage());
//            }
//
//            System.out.println("\n--- Kết thúc test ---");
//        }

}
