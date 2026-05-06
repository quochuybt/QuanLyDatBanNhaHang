package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoaDonDTO {

    @JsonProperty("maHD")
    private String maHD;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ngayLap")
    private LocalDateTime ngayLap;

    @JsonProperty("tongTien")
    private float tongTien;

    @JsonProperty("trangThai")
    private String trangThai;

    @JsonProperty("hinhThucThanhToan")
    private String hinhThucThanhToan;

    @JsonProperty("tienKhachDua")
    private float tienKhachDua;

    @JsonProperty("tenBan")
    private String tenBan;

    @JsonProperty("maDon")
    private String maDon;

    @JsonProperty("maNV")
    private String maNV;

    @JsonProperty("maKM")
    private String maKM;

    @JsonProperty("maKH")
    private String maKH;

    @JsonProperty("giamGia")
    private float giamGia;

    @JsonProperty("tongThanhToan")
    private float tongThanhToan;

    public static HoaDonDTO fromEntity(HoaDon entity) {
        if (entity == null) return null;
        return HoaDonDTO.builder()
                .maHD(entity.getMaHD())
                .ngayLap(entity.getNgayLap())
                .tongTien(entity.getTongTien())
                .trangThai(entity.getTrangThai())
                .hinhThucThanhToan(entity.getHinhThucThanhToan())
                .tienKhachDua(entity.getTienKhachDua())
                .tenBan(entity.getTenBan())
                .maDon(entity.getMaDon())
                .maNV(entity.getMaNV())
                .maKM(entity.getMaKM())
                .maKH(entity.getMaKH())
                .giamGia(entity.getGiamGia())
                .tongThanhToan(entity.getTongThanhToan())
                .build();
    }

    public HoaDon toEntity() {
        HoaDon entity = new HoaDon();

        // 1. Gán các trường dữ liệu cơ bản
        if (this.maHD != null) {
            entity.setMaHD(this.maHD);
        }
        entity.setNgayLap(this.ngayLap);
        entity.setTrangThai(this.trangThai);
        entity.setHinhThucThanhToan(this.hinhThucThanhToan);
        entity.setTongTien(this.tongTien);
        entity.setTienKhachDua(this.tienKhachDua);
        entity.setTenBan(this.tenBan);
        entity.setGiamGia(this.giamGia);
        entity.setTongThanhToan(this.tongThanhToan);

        // 2. Tạo đối tượng giả để gán khóa ngoại (chỉ cần chứa ID)
        if (this.maDon != null && !this.maDon.isEmpty()) {
            iuh.fit.core.entity.DonDatMon don = new iuh.fit.core.entity.DonDatMon();
            don.setMaDon(this.maDon);
            entity.setDonDatMon(don);
        }

        if (this.maNV != null && !this.maNV.isEmpty()) {
            iuh.fit.core.entity.NhanVien nv = new iuh.fit.core.entity.NhanVien();
            nv.setManv(this.maNV); // Nhớ check lại tên hàm set mã NV trong entity nhé
            entity.setNhanVien(nv);
        }

        if (this.maKM != null && !this.maKM.isEmpty()) {
            iuh.fit.core.entity.KhuyenMai km = new iuh.fit.core.entity.KhuyenMai();
            km.setMaKM(this.maKM);
            entity.setKhuyenMai(km);
        }

        if (this.maKH != null && !this.maKH.isEmpty()) {
            iuh.fit.core.entity.KhachHang kh = new iuh.fit.core.entity.KhachHang();
            kh.setMaKH(this.maKH);
            entity.setKhachHang(kh);
        }

        return entity;
    }
}