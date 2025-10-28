package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class HoaDon {

    private String maHD;
    private LocalDateTime ngayLap;
    private float tongTien;
    private String trangThai;
    private String hinhThucThanhToan;
    private float tienKhachDua; // 🌟 ĐÃ KHAI BÁO


    private String maDon;
    private String maNV;
    private String maKM;
    private String maKH;
    private float giamGia;
    private float vat;
    private float tongThanhToan; // Tiền thực tế khách phải trả
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
        this.tienKhachDua = 0; // 🌟 KHỞI TẠO TIỀN KHÁCH ĐƯA
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
        this.giamGia = 0;

        // 3. TODO: Tính VAT (dựa vào quy định, ví dụ 8%)
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

    // --- SETTER BỔ SUNG ---
    public void setTienKhachDua(float tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public void setTongTienTuDB(float tongTien) {
        // Khi load từ DB, cột tongTien thường là tổng cuối cùng
        this.tongTien = tongTien;

        // 🌟 SỬA: Gán tongThanhToan bằng tongTien từ DB (giả định là tổng cuối)
        this.tongThanhToan = tongTien;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    // --- LOGIC TÍNH TIỀN THỐI ĐÃ SỬA ---
    public float tinhTienThoi() {
        // 🌟 Dùng tongThanhToan là tiền phải trả
        if (this.tienKhachDua >= this.tongThanhToan) {
            return this.tienKhachDua - this.tongThanhToan;
        }
        return 0;
    }

    // --- GETTER ---
    public String getMaKH() { return maKH; }
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

    // 🌟 SỬ DỤNG HÀM TÍNH TOÁN TIỀN THỐI
    public float getTienThoi() {
        return tinhTienThoi();
    }

    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "HD" + datePart + randomPart;
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