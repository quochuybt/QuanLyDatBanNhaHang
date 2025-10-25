package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Lớp CaLam đại diện cho một ca làm việc.
 * Class này tuân thủ các yêu cầu nghiệp vụ đã mô tả.
 */
public class CaLam {

    // 1. Khai báo thuộc tính
    private String maCa;        // 1.1: CA-xx-yyyyMMDD-XXX
    private String tenCa;       // 1.2: Không rỗng
    private LocalDateTime gioBatDau;  // 1.3: Không rỗng
    private LocalDateTime gioKetThuc; // 1.4: Không rỗng, sau gioBatDau

    /**
     * Phương thức private hỗ trợ sinh mã ca theo yêu cầu 1.1 và 3.1
     * Format: CA-xx-yyyyMMDD-XXX
     *
     * @param soThuTuCa (xx) Số thứ tự ca (dạng String 2 chữ số, vd: "01", "02")
     * @return String mã ca đã được sinh
     */
    private String generateMaCa(String soThuTuCa) {
        // Lấy ngày tháng năm hiện tại (yyyyMMDD)
        String yyyyMMDD = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Sinh số ngẫu nhiên XXX (từ 100 đến 999)
        int xxx = new Random().nextInt(900) + 100;

        // Đảm bảo "xx" là 2 chữ số (nếu người dùng nhập "1" -> "01")
        String xx = String.format("%02d", Integer.parseInt(soThuTuCa));

        // Format: CA-xx-yyyyMMDD-XXX
        return String.format("CA-%s-%s-%d", xx, yyyyMMDD, xxx);
    }

    // 3. Viết các constructor

    /**
     * 3.1 Contructor mặc nhiên (mặc định).
     * Khởi tạo đối tượng rỗng với maCa phát sinh tự động.
     * "xx" (số thứ tự ca) sẽ được đặt mặc định là "00".
     */
    public CaLam() {
        // 1.1 & 3.1: Phát sinh mã ca tự động
        this.maCa = generateMaCa("00");

        // Khởi tạo các giá trị hợp lệ mặc định
        this.tenCa = "Chưa đặt tên";
        this.gioBatDau = LocalDateTime.now();
        // Giờ kết thúc mặc định là 1 tiếng sau giờ bắt đầu
        this.gioKetThuc = this.gioBatDau.plusHours(1);
    }

    /**
     * 3.2 Constructor có đầy đủ tham số.
     * Sử dụng setters để validate dữ liệu đầu vào.
     *
     * @param maCa Mã ca (phải hợp lệ, không được sinh tự động ở đây)
     * @param tenCa Tên ca (không rỗng)
     * @param gioBatDau Giờ bắt đầu (không rỗng)
     * @param gioKetThuc Giờ kết thúc (không rỗng, sau giờ bắt đầu)
     * @throws IllegalArgumentException Nếu có tham số không hợp lệ
     */
    public CaLam(String maCa, String tenCa, LocalDateTime gioBatDau, LocalDateTime gioKetThuc)
            throws IllegalArgumentException {

        // 2.1: setMaCa - Không tự động sinh
        setMaCa(maCa);
        // 2.2: setTenCa
        setTenCa(tenCa);
        // 2.3: setGioBatDau
        setGioBatDau(gioBatDau);
        // 2.4: setGioKetThuc (phải set sau gioBatDau)
        setGioKetThuc(gioKetThuc);
    }

    /**
     * 3.3 Copy Constructor.
     * Sao chép đối tượng.
     *
     * @param ca Đối tượng CaLam cần sao chép.
     */
    public CaLam(CaLam ca) {
        this.maCa = ca.maCa;
        this.tenCa = ca.tenCa;
        this.gioBatDau = ca.gioBatDau;
        this.gioKetThuc = ca.gioKetThuc;
    }

    // 2. Viết các phương thức getter, setter

    // --- Getters ---

    public String getMaCa() {
        return maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public LocalDateTime getGioBatDau() {
        return gioBatDau;
    }

    public LocalDateTime getGioKetThuc() {
        return gioKetThuc;
    }

    // --- Setters ---

    /**
     * 2.1 setMaCa
     * (Lưu ý: Bỏ qua mô tả "Phát sinh tự động" vì mâu thuẫn với chữ ký setter)
     * Thêm ràng buộc kiểm tra maCa không được rỗng.
     */
    public void setMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã ca không được rỗng.");
        }
        // Có thể thêm validation regex cho format CA-xx-yyyyMMDD-XXX nếu cần
        this.maCa = maCa;
    }

    /**
     * 2.2 setTenCa
     * Kiểm tra tham số tenCa không được rỗng.
     *
     * @throws IllegalArgumentException "Tên ca không được rỗng"
     */
    public void setTenCa(String tenCa) throws IllegalArgumentException {
        if (tenCa == null || tenCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca không được rỗng");
        }
        this.tenCa = tenCa;
    }

    /**
     * 2.3 setGioBatDau
     * Kiểm tra tham số gioBatDau không được rỗng.
     */
    public void setGioBatDau(LocalDateTime gioBatDau) {
        if (gioBatDau == null) {
            throw new IllegalArgumentException("Giờ bắt đầu không được rỗng.");
        }
        this.gioBatDau = gioBatDau;
    }

    /**
     * 2.4 setGioKetThuc
     * Kiểm tra gioKetThuc không rỗng và phải sau gioBatDau.
     *
     * @throws IllegalArgumentException "Giờ kết thúc phải sau giờ bắt đầu"
     */
    public void setGioKetThuc(LocalDateTime gioKetThuc) throws IllegalArgumentException {
        if (gioKetThuc == null) {
            throw new IllegalArgumentException("Giờ kết thúc không được rỗng.");
        }

        // Ràng buộc 1.4: Phải sau gioBatDau
        // (Phải đảm bảo gioBatDau đã được set và không null trước khi gọi hàm này)
        if (this.gioBatDau != null && !gioKetThuc.isAfter(this.gioBatDau)) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        this.gioKetThuc = gioKetThuc;
    }


    /**
     * 4. Viết phương thức toString()
     * Trả về "Ca lam{maCa”...”,tenCa”...”,ngayBatDau ”...”,ngayKetThuc”...”}"
     * (Lưu ý: Dùng nhãn "ngayBatDau", "ngayKetThuc" theo mô tả,
     * dù tên thuộc tính là "gioBatDau", "gioKetThuc")
     */
    @Override
    public String toString() {
        // Định dạng ngày giờ cho dễ đọc
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

        String batDauStr = (gioBatDau != null) ? gioBatDau.format(formatter) : "null";
        String ketThucStr = (gioKetThuc != null) ? gioKetThuc.format(formatter) : "null";

        return "Ca lam{" +
                "maCa='" + maCa + '\'' +
                ", tenCa='" + tenCa + '\'' +
                ", ngayBatDau='" + batDauStr + '\'' + // Theo yêu cầu 4
                ", ngayKetThuc='" + ketThucStr + '\'' + // Theo yêu cầu 4
                '}';
    }
}
