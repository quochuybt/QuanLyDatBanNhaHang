package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList; // Thêm
import java.util.List; // Thêm
import java.util.concurrent.ThreadLocalRandom;

public class HoaDon {

    private String maHD;
    private LocalDateTime ngayLap;
    private float tongTien;
    private String trangThai;
    private String hinhThucThanhToan;
    private float tienKhachDua;


    private String maDon; // BẮT BUỘC: Để liên kết với ChiTietHoaDon
    private String maNV;
    private String maKM;
    private String maKH;
    private float giamGia; // Tiền giảm giá (từ maKM)
    private float vat; // Thuế VAT
    private float tongThanhToan;
    private List<ChiTietHoaDon> dsChiTiet;

    public HoaDon(String maHD, LocalDateTime ngayLap, String trangThai,
                  String hinhThucThanhToan, String maDon,
                  String maNV, String maKM) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.maDon = maDon;
        this.maNV = maNV;
        this.maKM = maKM;

        // Khởi tạo các giá trị
        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
        this.vat = 0;
        this.tongThanhToan = 0;
    }
    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
        tinhTatCaTien(); // Tính lại tổng tiền khi có chi tiết
    }
    private void tinhTatCaTien() {
        // 1. Tính tổng tiền món ăn
        this.tongTien = 0;
        for (ChiTietHoaDon ct : dsChiTiet) {
            this.tongTien += ct.getThanhtien();
        }

        // 2. TODO: Tính giảm giá (dựa vào this.maKM)
        // Ví dụ: if (this.maKM.equals("GIAM20K")) { this.giamGia = 20000; }
        // Hiện tại để là 0
        this.giamGia = 0;

        // 3. TODO: Tính VAT (dựa vào quy định, ví dụ 8%)
        // Hiện tại để là 0
        this.vat = 0;

        // 4. Tính tổng thanh toán
        this.tongThanhToan = this.tongTien - this.giamGia + this.vat;
    }

    public HoaDon(HoaDon other) {
        this.maHD = phatSinhMaHD();
        this.ngayLap = other.ngayLap;
        this.tongTien = other.tongTien;
        this.trangThai = other.trangThai;
        this.hinhThucThanhToan = other.hinhThucThanhToan;
        this.tienKhachDua = other.tienKhachDua;
    }
    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }
    public String getMaHD() { return maHD; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public String getTrangThai() { return trangThai; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public float getTienKhachDua() { return tienKhachDua; }
    public String getMaDon() { return maDon; }
    public String getMaNV() { return maNV; }
    public String getMaKM() { return maKM; }
    public List<ChiTietHoaDon> getDsChiTiet() { return dsChiTiet; }

    // Các giá trị đã tính toán
    public float getTongTien() { return tongTien; } // Tổng món ăn
    public float getGiamGia() { return giamGia; }
    public float getVat() { return vat; }
    public float getTongThanhToan() { return tongThanhToan; } // Tiền phải trả

    // (Bỏ các hàm set, validate, phatSinhMaHD... cũ để đơn giản hóa)



    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
    }
    public void setTongTienTuDB(float tongTien) {
        this.tongTien = tongTien;

        // Cũng cập nhật tongThanhToan để GUI hiển thị đúng
        // (Tạm thời giả định tongTien DB là tiền cuối)
        this.tongThanhToan = tongTien;
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

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHD='" + maHD + '\'' +
                ", maDon='" + maDon + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", tongThanhToan=" + tongThanhToan +
                ", soLuongMon=" + dsChiTiet.size() +
                '}';
    }
}
