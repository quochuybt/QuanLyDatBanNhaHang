package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.KhuyenMai;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KhuyenMaiDTO {

    @JsonProperty("maKM")
    private String maKM;

    @JsonProperty("tenChuongTrinh")
    private String tenChuongTrinh;

    @JsonProperty("moTa")
    private String moTa;

    @JsonProperty("loaiKhuyenMai")
    private String loaiKhuyenMai;

    @JsonProperty("giaTri")
    private double giaTri;

    @JsonProperty("dieuKienApDung")
    private double dieuKienApDung;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngayBatDau")
    private LocalDate ngayBatDau;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngayKetThuc")
    private LocalDate ngayKetThuc;

    @JsonProperty("trangThai")
    private String trangThai;

    @JsonProperty("soLuongGioiHan")
    private int soLuongGioiHan;

    @JsonProperty("soLuotDaDung")
    private int soLuotDaDung;

    public static KhuyenMaiDTO fromEntity(KhuyenMai km) {
        if (km == null) return null;

        return KhuyenMaiDTO.builder()
                .maKM(km.getMaKM())
                .tenChuongTrinh(km.getTenChuongTrinh())
                .moTa(km.getMoTa())
                .loaiKhuyenMai(km.getLoaiKhuyenMai())
                .giaTri(km.getGiaTri())
                .dieuKienApDung(km.getDieuKienApDung())
                .ngayBatDau(km.getNgayBatDau())
                .ngayKetThuc(km.getNgayKetThuc())
                .trangThai(km.getTrangThai())
                .soLuongGioiHan(km.getSoLuongGioiHan() == null ? 0 : km.getSoLuongGioiHan())
                .soLuotDaDung(km.getSoLuotDaDung() == null ? 0 : km.getSoLuotDaDung())
                .build();
    }

    public KhuyenMai toEntity() {
        KhuyenMai km = new KhuyenMai(
                this.maKM,
                this.tenChuongTrinh,
                this.moTa,
                this.loaiKhuyenMai,
                this.giaTri,
                this.dieuKienApDung,
                this.ngayBatDau,
                this.ngayKetThuc,
                this.trangThai
        );
        km.setSoLuongGioiHan(this.soLuongGioiHan);
        km.setSoLuotDaDung(this.soLuotDaDung);
        return km;
    }
}
