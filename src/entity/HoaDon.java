package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HoaDon {

    private String maHD;
    private LocalDateTime ngayLap;
    private float tongTien;
    private String trangThai;
    private String hinhThucThanhToan;
    private float tienKhachDua;

    public HoaDon() {
        this.maHD = phatSinhMaHD();
        this.ngayLap = LocalDateTime.now();
        this.tongTien = 0;
        this.trangThai = "Chưa thanh toán";
        this.hinhThucThanhToan = "Tiền mặt";
        this.tienKhachDua = 0;
    }

    public HoaDon(LocalDateTime ngayLap, float tongTien, String trangThai, String hinhThucThanhToan, float tienKhachDua) {
        this.maHD = phatSinhMaHD();
        setNgayLap(ngayLap);
        setTongTien(tongTien);
        setTrangThai(trangThai);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTienKhachDua(tienKhachDua);
    }

    public HoaDon(HoaDon other) {
        this.maHD = phatSinhMaHD();
        this.ngayLap = other.ngayLap;
        this.tongTien = other.tongTien;
        this.trangThai = other.trangThai;
        this.hinhThucThanhToan = other.hinhThucThanhToan;
        this.tienKhachDua = other.tienKhachDua;
    }

    public String getMaHD() {
        return maHD;
    }

    private void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        if (ngayLap == null || ngayLap.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày lập không hợp lệ (không được rỗng và phải nhỏ hơn hoặc bằng ngày hiện tại)");
        }
        this.ngayLap = ngayLap;
    }

    public float getTongTien() {
        return tongTien;
    }

    public void setTongTien(float tongTien) {
        if (tongTien < 0) {
            throw new IllegalArgumentException("Tổng tiền phải ≥ 0");
        }
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        List<String> trangThaiHopLe = Arrays.asList("Đã thanh toán", "Chưa thanh toán");
        if (trangThai == null || !trangThaiHopLe.contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận: " + trangThaiHopLe);
        }
        this.trangThai = trangThai;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        List<String> hinhThucHopLe = Arrays.asList("Tiền mặt", "Chuyển khoản");
        if (hinhThucThanhToan == null || !hinhThucHopLe.contains(hinhThucThanhToan)) {
            throw new IllegalArgumentException("Hình thức không hợp lệ. Chỉ chấp nhận: " + hinhThucHopLe);
        }
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public float getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(float tienKhachDua) {
        if (tienKhachDua < this.tongTien) {
            throw new IllegalArgumentException("Tiền khách đưa không đủ (phải lớn hơn hoặc bằng tổng tiền)");
        }
        this.tienKhachDua = tienKhachDua;
    }

    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
    }

    public int tinhTienThoi() {
        if (this.tienKhachDua >= this.tongTien) {
            return (int) (this.tienKhachDua - this.tongTien);
        }
        return 0;
    }

    public int getTienThoi() {
        return tinhTienThoi();
    }

     public float tinhTongTien(List<ChiTietHoaDon> chiTietHoaDons) {
         float total = 0;
         for (ChiTietHoaDon ct : chiTietHoaDons) {
             total += ct.getThanhtien();
         }
         this.tongTien = total;
         return total;
     }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return "HoaDon{" +
                "maHD='" + maHD + '\'' +
                ", ngayLap='" + ngayLap.format(formatter) + '\'' +
                ", tongTien=" + tongTien +
                ", trangThai='" + trangThai + '\'' +
                ", hinhThucThanhToan='" + hinhThucThanhToan + '\'' +
                ", tienKhachDua=" + tienKhachDua +
                ", tienThoi=" + getTienThoi() +
                '}';
    }
}
