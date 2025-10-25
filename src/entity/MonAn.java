
package entity;
public class MonAn {

    // 1. Khai báo thuộc tính
    private String maMonAn;    // 1.1: Khóa chính, format MAXXX (bắt đầu từ 100)
    private String tenMon;     // 1.2: Không rỗng
    private String mota;       // 1.3
    private float donGia;      // 1.4: > 0
    private String donViTinh;  // 1.5: Không rỗng
    private String trangThai;  // 1.6: Còn / Hết món
    private String hinhAnh;    // 1.7: Có thể rỗng

    /**
     * Biến đếm tĩnh (static) để sinh mã món ăn tăng dần.
     * Bắt đầu từ 100 theo ví dụ "MA100".
     */
    private static int nextId = 100;

    /**
     * Phương thức hỗ trợ (private, static, synchronized)
     * 1.1: Sinh mã tự động tăng MAXXX.
     * "Synchronized" để đảm bảo an toàn khi nhiều luồng (thread) 
     * cùng lúc tạo món ăn.
     * * @return String mã món ăn mới (ví dụ: "MA100")
     * @throws IllegalStateException nếu mã vượt quá "MA999"
     */
    private static synchronized String generateNextMaMonAn() {
        if (nextId > 999) {
            // Tuân thủ ràng buộc 1.1 (MAXXX là 3 chữ số)
            throw new IllegalStateException("Không thể sinh mã món ăn mới. Đã đạt giới hạn (MA999).");
        }
        // Lấy ID hiện tại và sau đó tăng biến đếm lên 1
        return "MA" + (nextId++);
    }


    // 3. Viết các constructor

    /**
     * 3.1 Constructor mặc định.
     * Tự động sinh mã món ăn tăng dần (MA100, MA101, ...).
     */
    public MonAn() {
        // 1.1: Sinh mã tăng dần
        this.maMonAn = generateNextMaMonAn();

        // Gán giá trị mặc định hợp lệ
        this.tenMon = "Chưa đặt tên";
        this.donGia = 1.0f;
        this.donViTinh = "Chưa rõ";
        this.trangThai = "Còn";
        this.mota = "";
        this.hinhAnh = "";
    }

    /**
     * 3.2 Constructor đầy đủ.
     * Constructor này cũng cập nhật biến đếm tĩnh (nextId) nếu
     * mã được cung cấp lớn hơn mã hiện tại.
     *
     * @param maMonAn   Mã món (phải tuân thủ format MAXXX).
     * @param tenMon    Tên món (không rỗng).
     * @param moTa      Mô tả.
     * @param donGia    Đơn giá (phải > 0).
     * @param donViTinh Đơn vị tính (không rỗng).
     * @param trangThai Trạng thái (Còn / Hết món).
     * @param hinhAnh   Đường dẫn hình ảnh.
     * @throws Exception Ném ngoại lệ nếu đơn giá <= 0.
     * @throws IllegalArgumentException Ném ngoại lệ nếu các ràng buộc khác vi phạm.
     */
    public MonAn(String maMonAn, String tenMon, String moTa, float donGia,
                 String donViTinh, String trangThai, String hinhAnh) throws Exception {

        // 1.1: Ràng buộc: Dãy gồm 5 kí tự MAXXX (XXX là 3 chữ số)
        if (maMonAn == null || !maMonAn.matches("^MA\\d{3}$")) {
            throw new IllegalArgumentException("Mã món ăn không hợp lệ. Phải có dạng MAXXX (XXX là 3 chữ số).");
        }
        this.maMonAn = maMonAn;

        // Cập nhật biến đếm tĩnh nếu cần
        try {
            int idNum = Integer.parseInt(maMonAn.substring(2)); // Lấy phần số (ví dụ: 105)

            // Chỉ cập nhật nếu số này nằm trong hoặc lớn hơn phạm vi tự động tăng
            if (idNum >= 100) {
                // Đồng bộ hóa để cập nhật biến static an toàn
                synchronized (MonAn.class) {
                    if (idNum >= nextId) {
                        nextId = idNum + 1; // Đặt mã *tiếp theo* là mã này + 1
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Lỗi này không nên xảy ra nếu đã qua regex, nhưng để an toàn
            throw new IllegalArgumentException("Mã món ăn có phần số không hợp lệ.", e);
        }

        // Sử dụng setters để validate (2.1, 2.2, 2.3, 2.4)
        setTenMon(tenMon);
        this.mota = moTa;
        setDonGia(donGia);
        setDonViTinh(donViTinh);
        setTrangThai(trangThai);
        this.hinhAnh = hinhAnh;
    }

    /**
     * 3.3 Copy constructor.
     * Tạo một bản sao (shallow copy) của đối tượng MonAn.
     * @param other Đối tượng MonAn cần sao chép.
     */
    public MonAn(MonAn other) {
        this.maMonAn = other.maMonAn;
        this.tenMon = other.tenMon;
        this.mota = other.mota;
        this.donGia = other.donGia;
        this.donViTinh = other.donViTinh;
        this.trangThai = other.trangThai;
        this.hinhAnh = other.hinhAnh;
    }

    // 2. Viết các phương thức getter, setter
    // (Phần này không thay đổi so với phiên bản trước)

    // --- Getters ---
    public String getMaMonAn() {
        return maMonAn;
    }

    public String getTenMon() {
        return tenMon;
    }

    public String getMota() {
        return mota;
    }

    public float getDonGia() {
        return donGia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    // --- Setters (với validation) ---

    /**
     * 2.1 setTenMon
     * Ràng buộc: Không được rỗng (1.2).
     */
    public void setTenMon(String tenMon) {
        if (tenMon == null || tenMon.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên món không được rỗng.");
        }
        this.tenMon = tenMon;
    }

    /**
     * Setter cho mota (không có ràng buộc đặc biệt).
     */
    public void setMota(String mota) {
        this.mota = mota;
    }

    /**
     * 2.2 setDonGia
     * Ràng buộc: Phải > 0 (1.4).
     */
    public void setDonGia(float donGia) throws Exception {
        if (donGia <= 0) {
            throw new Exception("Đơn giá phải > 0");
        }
        this.donGia = donGia;
    }

    /**
     * 2.3 setDonViTinh
     * Ràng buộc: Không rỗng (1.5).
     */
    public void setDonViTinh(String donViTinh) {
        if (donViTinh == null || donViTinh.trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được rỗng.");
        }
        this.donViTinh = donViTinh;
    }

    /**
     * 2.4 setTrangThai
     * Ràng buộc: Không rỗng (2.4) và phải là "Còn" hoặc "Hết món" (1.6).
     */
    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được rỗng.");
        }
        if (!trangThai.equalsIgnoreCase("Còn") && !trangThai.equalsIgnoreCase("Hết món")) {
            throw new IllegalArgumentException("Trạng thái phải là 'Còn' hoặc 'Hết món'.");
        }
        this.trangThai = trangThai;
    }

    /**
     * Setter cho hinhAnh (không có ràng buộc đặc biệt).
     */
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }


    /**
     * 4. Viết phương thức toString()
     * (Đã sửa "loaiMon" thành "mota" để khớp với thuộc tính)
     */
    @Override
    public String toString() {
        return "MonAn{" +
                "maMon='" + maMonAn + '\'' +
                ", tenMon='" + tenMon + '\'' +
                ", donGia=" + donGia +
                ", mota='" + mota + '\'' + // Sửa từ "loaiMon"
                ", donViTinh='" + donViTinh + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", hinhAnh='" + hinhAnh + '\'' +
                '}';
    }
}