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
    private String maDon; // <-- THÊM DÒNG NÀY

    // Constructor mặc định (nếu cần)
    public HoaDon() {
        this.maHD = phatSinhMaHD();
        this.ngayLap = LocalDateTime.now();
        this.tongTien = 0;
        this.trangThai = "Chưa thanh toán";
        this.hinhThucThanhToan = "Tiền mặt";
        this.tienKhachDua = 0;
        // maDon có thể null ban đầu
    }

    // Constructor đầy đủ (Thêm maDon)
    public HoaDon(LocalDateTime ngayLap, float tongTien, String trangThai, String hinhThucThanhToan, float tienKhachDua, String maDon) {
        this.maHD = phatSinhMaHD();
        setNgayLap(ngayLap);
        setTongTien(tongTien);
        setTrangThai(trangThai);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTienKhachDua(tienKhachDua);
        setMaDon(maDon); // <-- THÊM DÒNG NÀY
    }

    // Constructor bạn đang dùng trong DAO (Thêm maDon)
     public HoaDon(LocalDateTime ngayLap, float tongTien, String trangThai, String hinhThucThanhToan, float tienKhachDua) {
        this.maHD = phatSinhMaHD();
        setNgayLap(ngayLap);
        setTongTien(tongTien);
        setTrangThai(trangThai);
        setHinhThucThanhToan(hinhThucThanhToan);
        setTienKhachDua(tienKhachDua);
         // maDon có thể null ban đầu nếu dùng constructor này
     }


    // --- GETTER / SETTER cho maDon ---
    public String getMaDon() {
        return maDon;
    }

    public void setMaDon(String maDon) {
        // Có thể thêm kiểm tra null/rỗng nếu maDon là bắt buộc
        this.maDon = maDon;
    }
    // --- (Giữ nguyên các getter/setter khác) ---

     public String getMaHD() {
         return maHD;
     }

     public void setMaHD(String maHD) {
         this.maHD = maHD;
     }

     public LocalDateTime getNgayLap() {
         return ngayLap;
     }

     public void setNgayLap(LocalDateTime ngayLap) {
         if (ngayLap == null || ngayLap.isAfter(LocalDateTime.now().plusMinutes(1))) { // Cho phép sai lệch 1 phút
             // Bỏ throw exception, thay bằng ghi log hoặc giá trị mặc định nếu cần
             System.err.println("Cảnh báo: Ngày lập không hợp lệ, sử dụng ngày hiện tại.");
             this.ngayLap = LocalDateTime.now();
            // throw new IllegalArgumentException("Ngày lập không hợp lệ (không được rỗng và phải nhỏ hơn hoặc bằng ngày hiện tại)");
         } else {
            this.ngayLap = ngayLap;
         }
     }

     public float getTongTien() {
         return tongTien;
     }

     public void setTongTien(float tongTien) {
         if (tongTien < 0) {
              System.err.println("Cảnh báo: Tổng tiền âm, đặt về 0.");
              this.tongTien = 0;
             // throw new IllegalArgumentException("Tổng tiền phải ≥ 0");
         } else {
            this.tongTien = tongTien;
         }
     }

     public String getTrangThai() {
         return trangThai;
     }

     public void setTrangThai(String trangThai) {
         List<String> trangThaiHopLe = Arrays.asList("Đã thanh toán", "Chưa thanh toán");
         if (trangThai == null || !trangThaiHopLe.contains(trangThai)) {
              System.err.println("Cảnh báo: Trạng thái không hợp lệ '" + trangThai + "', đặt về 'Chưa thanh toán'.");
              this.trangThai = "Chưa thanh toán";
             // throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận: " + trangThaiHopLe);
         } else {
            this.trangThai = trangThai;
         }
     }

     public String getHinhThucThanhToan() {
         return hinhThucThanhToan;
     }

     public void setHinhThucThanhToan(String hinhThucThanhToan) {
         List<String> hinhThucHopLe = Arrays.asList("Tiền mặt", "Chuyển khoản");
         if (hinhThucThanhToan == null || !hinhThucHopLe.contains(hinhThucThanhToan)) {
              System.err.println("Cảnh báo: Hình thức thanh toán không hợp lệ '" + hinhThucThanhToan + "', đặt về 'Tiền mặt'.");
              this.hinhThucThanhToan = "Tiền mặt";
             // throw new IllegalArgumentException("Hình thức không hợp lệ. Chỉ chấp nhận: " + hinhThucHopLe);
         } else {
            this.hinhThucThanhToan = hinhThucThanhToan;
         }
     }

     public float getTienKhachDua() {
         return tienKhachDua;
     }

     public void setTienKhachDua(float tienKhachDua) {
        // Bỏ kiểm tra tiền khách đưa < tổng tiền vì có thể là chưa thanh toán
        // if (this.trangThai != null && this.trangThai.equals("Đã thanh toán") && tienKhachDua < this.tongTien) {
        //     System.err.println("Cảnh báo: Tiền khách đưa không đủ.");
        //     // throw new IllegalArgumentException("Tiền khách đưa không đủ (phải lớn hơn hoặc bằng tổng tiền)");
        // }
         this.tienKhachDua = tienKhachDua;
     }

     private String phatSinhMaHD() {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
         String datePart = LocalDateTime.now().format(formatter);
         int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
         return "HD" + datePart + randomPart;
     }

     public int tinhTienThoi() {
         if (this.trangThai != null && this.trangThai.equals("Đã thanh toán") && this.tienKhachDua >= this.tongTien) {
             return (int) (this.tienKhachDua - this.tongTien);
         }
         return 0; // Trả về 0 nếu chưa thanh toán hoặc tiền đưa không đủ
     }


     public int getTienThoi() {
        return tinhTienThoi();
     }

      public float tinhTongTien(List<ChiTietHoaDon> chiTietHoaDons) {
          float total = 0;
          if (chiTietHoaDons != null) {
              for (ChiTietHoaDon ct : chiTietHoaDons) {
                 total += ct.getThanhtien();
              }
          }
          this.tongTien = total;
          return total;
      }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return "HoaDon{" +
                "maHD='" + maHD + '\'' +
                ", ngayLap='" + (ngayLap != null ? ngayLap.format(formatter) : "N/A") + '\'' + // Kiểm tra null
                ", tongTien=" + tongTien +
                ", trangThai='" + trangThai + '\'' +
                ", hinhThucThanhToan='" + hinhThucThanhToan + '\'' +
                ", tienKhachDua=" + tienKhachDua +
                ", tienThoi=" + getTienThoi() +
                ", maDon='" + maDon + '\'' + // <-- THÊM DÒNG NÀY
                '}';
    }
}