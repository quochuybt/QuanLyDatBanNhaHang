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
        HoaDon entity = new HoaDon(
                this.maHD,
                this.ngayLap,
                this.trangThai,
                this.hinhThucThanhToan,
                this.maDon,
                this.maNV,
                this.maKM
        );
        entity.setTongTien(this.tongTien);
        entity.setTienKhachDua(this.tienKhachDua);
        entity.setTenBan(this.tenBan);
        entity.setMaKH(this.maKH);
        entity.setGiamGia(this.giamGia);
        entity.setTongThanhToan(this.tongThanhToan);
        return entity;
    }
}