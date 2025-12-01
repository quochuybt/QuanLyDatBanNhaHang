package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Lớp DonDatMon đại diện cho một đơn đặt món.
 * Class này tuân thủ các yêu cầu nghiệp vụ đã mô tả.
 */
public class DonDatMon {

    // 1. Khai báo thuộc tính
    private String maDon;       // 1.1: DONXXXX (XXXX là số ngẫu nhiên)
    private LocalDateTime ngayKhoiTao; // 1.2: Không được rỗng
    private String maNV; // <-- BỔ SUNG
    private String maKH; // <-- BỔ SUNG
    private LocalDateTime thoiGianDen;
    private String trangThai;
    private String maBan;
    private String ghiChu;

    /**
     * Phương thức private hỗ trợ sinh mã đơn ngẫu nhiên.
     * 1.1: Format DONXXXX (XXXX là 1000-9999).
     * @return String mã đơn ngẫu nhiên.
     */
    private String generateMaDon() {
        Random rand = new Random();
        // Sinh số ngẫu nhiên XXXX (từ 1000 đến 9999)
        int xxxx = rand.nextInt(9000) + 1000;
        return "DON" + xxxx;
    }

    // 3. Viết các constructor

    /**
     * 3.1 Constructor mặc nhiên (mặc định).
     * Khởi tạo đối tượng rỗng với maDon được phát sinh ngẫu nhiên tự động.
     * ngayKhoiTao được đặt là thời gian hiện tại để đảm bảo không rỗng.
     */
    public DonDatMon() {
        this.maDon = generateMaDon();
        this.ngayKhoiTao = LocalDateTime.now();
        this.ghiChu = "";
    }
    public DonDatMon(String maDon, LocalDateTime ngayKhoiTao, String maNV, String maKH, String maBan, String ghiChu) {
        setMaDon(maDon);
        setNgayKhoiTao(ngayKhoiTao);
        setMaNV(maNV); // Giả sử không cần validate
        setMaKH(maKH); // Giả sử không cần validate
        setMaBan(maBan); // Giả sử không cần validate
        setGhiChu(ghiChu);
    }

    // (Copy constructor giữ nguyên)
    public DonDatMon(DonDatMon donDatMon) {
        this.maDon = donDatMon.maDon;
        this.ngayKhoiTao = donDatMon.ngayKhoiTao;
        this.maNV = donDatMon.maNV;
        this.maKH = donDatMon.maKH;
        this.maBan = donDatMon.maBan;
        this.ghiChu = donDatMon.ghiChu;
    }

    // 2. Viết các phương thức getter, setter

    // --- Getters ---
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getMaDon() {
        return maDon;
    }

    public LocalDateTime getNgayKhoiTao() {
        return ngayKhoiTao;
    }
    public LocalDateTime getThoiGianDen() {
        return thoiGianDen;
    }

    public void setThoiGianDen(LocalDateTime thoiGianDen) {
        this.thoiGianDen = thoiGianDen;
    }
    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public void setMaDon(String maDon) throws IllegalArgumentException {
        if (maDon == null || maDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn không được rỗng.");
        }
        this.maDon = maDon;
    }

    public void setNgayKhoiTao(LocalDateTime ngayKhoiTao) throws IllegalArgumentException {
        // Ràng buộc 1.2: Không được rỗng
        if (ngayKhoiTao == null) {
            throw new IllegalArgumentException("Ngày khởi tạo không được rỗng.");
        }
        this.ngayKhoiTao = ngayKhoiTao;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        String ngayStr = (this.ngayKhoiTao != null) ? this.ngayKhoiTao.format(formatter) : "null";

        return "DonDatMon{" +
                "maDon='" + maDon + '\'' +
                ", ngayKhoiTao='" + ngayStr + '\'' +
                ", maKH='" + maKH + '\'' + // Bổ sung
                ", maBan='" + maBan + '\'' + // Bổ sung
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
