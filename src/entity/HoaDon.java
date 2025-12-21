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
    private float tienKhachDua;
    private String tenBan;


    private String maDon;
    private String maNV;
    private String maKM;
    private String maKH;
    private float giamGia;
    private float tongThanhToan;
    private List<ChiTietHoaDon> dsChiTiet;
    public HoaDon() {
        this.maHD = phatSinhMaHD();
        this.ngayLap = LocalDateTime.now();
        this.trangThai = "Chưa thanh toán";
        this.hinhThucThanhToan = "Tiền mặt";
        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
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

        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
        this.tongThanhToan = 0;
        this.tienKhachDua = 0;
    }

    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
    }
    public void tinhLaiTongTienTuChiTiet() {
        this.tongTien = 0;
        if (this.dsChiTiet != null) {
            for (ChiTietHoaDon ct : dsChiTiet) {
                ct.tinhThanhTien();
                this.tongTien += ct.getThanhtien();
            }
        }
    }
    public void tinhLaiGiamGiaVaTongTien(KhachHangDAO khachHangDAO, KhuyenMaiDAO maKhuyenMaiDAO) {
        tinhLaiTongTienTuChiTiet();

        float tongCong = this.tongTien;
        float giamGiaTV = 0;
        float giamGiaMa = 0;

        if (this.maKH != null && khachHangDAO != null) {
            KhachHang kh = khachHangDAO.timTheoMaKH(this.maKH);
            if (kh != null) {
                float phanTramGiamTV = getPhanTramGiamTheoHang(kh.getHangThanhVien());
                giamGiaTV = tongCong * phanTramGiamTV / 100;
            }
        }

        if (this.maKM != null && !this.maKM.isEmpty() && maKhuyenMaiDAO != null) {
            entity.KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(this.maKM);
            if (km != null) {
                if (tongCong >= km.getDieuKienApDung()) {
                    if ("Phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm theo phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai())) {
                        giamGiaMa = tongCong * (float)km.getGiaTri() / 100;
                    } else if ("Số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm giá số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai())){
                        giamGiaMa = (float)km.getGiaTri();
                    }
                }
            }
        }

        this.giamGia = giamGiaTV + giamGiaMa;

        tinhLaiTongThanhToan();
    }
    private float getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) return 0.0f;
        switch (hang) {
            case DIAMOND: return 10.0f;
            case GOLD: return 5.0f;
            case SILVER: return 3.0f;
            case BRONZE: return 2.0f;
            case MEMBER: return 0.0f;
            case NONE: default: return 0.0f;
        }
    }
    public HoaDon(HoaDon other) {

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
        this.tongThanhToan = other.tongThanhToan;
        this.dsChiTiet = new ArrayList<>();
        if (other.dsChiTiet != null) {
            for (ChiTietHoaDon ct : other.dsChiTiet) {
                this.dsChiTiet.add(new ChiTietHoaDon(ct));
            }
        }
    }

    public void setTienKhachDua(float tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public void setTongTienTuDB(float tongTien) {
        this.tongTien = tongTien;
    }
    public void capNhatTongThanhToanTuCacThanhPhan() {
        this.tongThanhToan = this.tongTien - this.giamGia;
        if (this.tongThanhToan < 0) this.tongThanhToan = 0;
    }
    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public float tinhTienThoi() {
        if (this.tienKhachDua >= this.tongThanhToan) {
            return this.tienKhachDua - this.tongThanhToan;
        }
        return 0;
    }

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

    public void setTenBan(String tenBan) { this.tenBan = tenBan; }
    public float getTongTien() { return tongTien; }
    public float getGiamGia() { return giamGia; }
    public float getTongThanhToan() { return tongThanhToan;}

    public void setGiamGia(float giamGia) {
        if (giamGia < 0) {
            this.giamGia = 0;
        } else {
            this.giamGia = giamGia;
        }
    }
    public void tinhLaiTongThanhToan() {
        this.tongThanhToan = this.tongTien - this.giamGia;
        if (this.tongThanhToan < 0) {
            this.tongThanhToan = 0;
        }
    }
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