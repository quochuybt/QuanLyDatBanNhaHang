package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;

public class HoaDon {

    private String maHD;
    private LocalDateTime ngayLap;
    private float tongTien;
    private String trangThai;
    private String hinhThucThanhToan;
    private float tienKhachDua; // 🌟 ĐÃ KHAI BÁO
    private String tenBan;


    private String maDon;
    private String maNV;
    private String maKM;
    private String maKH;
    private float giamGia;
    private float vat;
    private float tongThanhToan; // Tiền thực tế khách phải trả
    private List<ChiTietHoaDon> dsChiTiet;
    public HoaDon() {
        this.maHD = phatSinhMaHD(); // Tự sinh mã mới
        this.ngayLap = LocalDateTime.now();
        this.trangThai = "Chưa thanh toán";
        this.hinhThucThanhToan = "Tiền mặt"; // Mặc định
        // maDon, maNV, maKH, maKM sẽ được set sau khi có thông tin
        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
        this.vat = 0;
        this.tongThanhToan = 0;
    }

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
    }
    public void tinhLaiTongTienTuChiTiet() {
        this.tongTien = 0;
        if (this.dsChiTiet != null) {
            for (ChiTietHoaDon ct : dsChiTiet) {
                ct.tinhThanhTien(); // Đảm bảo thành tiền chi tiết đúng
                this.tongTien += ct.getThanhtien();
            }
        }
    }
    public void tinhLaiGiamGiaVaTongTien(KhachHangDAO khachHangDAO, KhuyenMaiDAO maKhuyenMaiDAO) {
        // 1. Đảm bảo tổng tiền món ăn (chưa giảm) đã được tính đúng
        tinhLaiTongTienTuChiTiet(); // Tính lại this.tongTien từ dsChiTiet

        float tongCong = this.tongTien; // Dùng tongTien vừa tính
        float giamGiaTV = 0;
        float giamGiaMa = 0;

        // 2. Tính giảm giá thành viên
        if (this.maKH != null && khachHangDAO != null) {
            KhachHang kh = khachHangDAO.timTheoMaKH(this.maKH);
            if (kh != null) {
                float phanTramGiamTV = getPhanTramGiamTheoHang(kh.getHangThanhVien());
                giamGiaTV = tongCong * phanTramGiamTV / 100;
            }
        }

        // 3. Tính giảm giá theo Mã KM (nếu có)
        if (this.maKM != null && !this.maKM.isEmpty() && maKhuyenMaiDAO != null) {
            // Giả sử MaKhuyenMaiDAO trả về entity KhuyenMai (đã sửa)
            entity.KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(this.maKM);
            if (km != null) {
                if (tongCong >= km.getDieuKienApDung()) { // Dùng getter mới
                    if ("Phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm theo phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai())) {
                        giamGiaMa = tongCong * (float)km.getGiaTri() / 100; // Dùng getter mới
                    } else if ("Số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm giá số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai())){
                        giamGiaMa = (float)km.getGiaTri(); // Dùng getter mới
                    }
                } else {
                    System.out.println("Hóa đơn không đủ ĐK áp dụng mã: " + this.maKM);
                    // Không tự hủy mã ở đây, để GUI xử lý nếu muốn
                }
            } else {
                System.out.println("Mã KM " + this.maKM + " không còn hợp lệ.");
                // Không tự hủy mã ở đây
            }
        }

        // 4. Tính tổng giảm giá (Cộng dồn)
        this.giamGia = giamGiaTV + giamGiaMa;

        // 5. Tính VAT (Ví dụ 0%)
        this.vat = 0; // Hoặc tính theo công thức

        // 6. Tính lại Tổng thanh toán cuối cùng
        tinhLaiTongThanhToan(); // Gọi hàm tính tổng cuối
    }
    private float getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) return 0.0f;
        switch (hang) {
            case DIAMOND: return 10.0f;
            case GOLD: return 5.0f; // Sửa theo bảng: Gold 5%
            case SILVER: return 3.0f;
            case BRONZE: return 2.0f;
            case MEMBER: return 0.0f; // Member không giảm
            case NONE: default: return 0.0f;
        }
    }
    public HoaDon(HoaDon other) {
        // KHÔNG sinh mã mới khi copy, giữ nguyên mã cũ
        this.maHD = other.maHD;
        this.ngayLap = other.ngayLap;
        this.tongTien = other.tongTien;
        this.trangThai = other.trangThai;
        this.hinhThucThanhToan = other.hinhThucThanhToan;
        this.tienKhachDua = other.tienKhachDua;
        this.maDon = other.maDon;
        this.maNV = other.maNV;
        this.maKM = other.maKM;
        this.maKH = other.maKH;
        this.giamGia = other.giamGia;
        this.vat = other.vat;
        this.tongThanhToan = other.tongThanhToan;
        // Copy danh sách chi tiết (nên tạo copy sâu nếu ChiTietHoaDon có thể thay đổi)
        this.dsChiTiet = new ArrayList<>();
        if (other.dsChiTiet != null) {
            for (ChiTietHoaDon ct : other.dsChiTiet) {
                // Giả sử ChiTietHoaDon có constructor copy
                this.dsChiTiet.add(new ChiTietHoaDon(ct));
            }
        }
    }

    // --- SETTER BỔ SUNG ---
    public void setTienKhachDua(float tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public void setTongTienTuDB(float tongTien) {
        // Khi load từ DB, cột tongTien thường là tổng cuối cùng
        this.tongTien = tongTien;
    }
    public void capNhatTongThanhToanTuCacThanhPhan() {
        this.tongThanhToan = this.tongTien - this.giamGia + this.vat;
        if (this.tongThanhToan < 0) this.tongThanhToan = 0;
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
    public String getTenBan() { return tenBan; }
    public String getMaKH() { return maKH; }
    public String getMaHD() { return maHD; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public String getTrangThai() { return trangThai; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public float getTienKhachDua() { return tienKhachDua; }
    public String getMaDon() { return maDon; }
    public String getMaNV() { return maNV; }
    public String getMaKM() { return maKM; }
    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }
    public List<ChiTietHoaDon> getDsChiTiet() { return dsChiTiet; }

    // Các giá trị đã tính toán
    public void setTenBan(String tenBan) { this.tenBan = tenBan; }
    public float getTongTien() { return tongTien; } // Tổng món ăn
    public float getGiamGia() { return giamGia; }
    public float getVat() { return vat; }
    public float getTongThanhToan() { return tongThanhToan;} // Tiền phải trả

    // (Bỏ các hàm set, validate, phatSinhMaHD... cũ để đơn giản hóa)
    public void setGiamGia(float giamGia) {
        if (giamGia < 0) { // Thêm validation cơ bản
            this.giamGia = 0;
        } else {
            this.giamGia = giamGia;
        }
    }
    public void setVat(float vat) { this.vat = (vat < 0) ? 0 : vat; }
    public void tinhLaiTongThanhToan() {
        // tongTien là tổng tiền gốc của các món ăn
        this.tongThanhToan = this.tongTien - this.giamGia + this.vat;
        if (this.tongThanhToan < 0) { // Đảm bảo không âm
            this.tongThanhToan = 0;
        }
        System.out.println("DEBUG HoaDon: tongTien=" + tongTien + ", giamGia=" + giamGia + ", vat=" + vat + " => tongThanhToan=" + tongThanhToan);
    }
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