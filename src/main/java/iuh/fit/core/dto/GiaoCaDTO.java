package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.GiaoCa;
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
public class GiaoCaDTO {

    @JsonProperty("maGiaoCa")
    private String maGiaoCa;

    @JsonProperty("maNV")
    private String maNV;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("thoiGianBatDau")
    private LocalDateTime thoiGianBatDau;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("thoiGianKetThuc")
    private LocalDateTime thoiGianKetThuc;

    @JsonProperty("tienDauCa")
    private double tienDauCa;

    @JsonProperty("tienCuoiCa")
    private double tienCuoiCa;

    @JsonProperty("tienHeThongTinh")
    private double tienHeThongTinh;

    @JsonProperty("chenhLech")
    private double chenhLech;

    @JsonProperty("ghiChu")
    private String ghiChu;

    public static GiaoCaDTO fromEntity(GiaoCa entity) {
        if (entity == null) {
            return null;
        }

        return GiaoCaDTO.builder()
                .maGiaoCa(entity.getMaGiaoCa())
                .maNV(entity.getMaNV())
                .thoiGianBatDau(entity.getThoiGianBatDau())
                .thoiGianKetThuc(entity.getThoiGianKetThuc())
                .tienDauCa(entity.getTienDauCa())
                .tienCuoiCa(entity.getTienCuoiCa())
                .tienHeThongTinh(entity.getTienHeThongTinh())
                .chenhLech(entity.getChenhLech())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    public GiaoCa toEntity() {
        iuh.fit.core.entity.NhanVien nvEntity = new iuh.fit.core.entity.NhanVien();
        nvEntity.setManv(this.maNV);

        return GiaoCa.builder()
                .maGiaoCa(this.maGiaoCa)
                .maNV(this.maNV)
                .thoiGianBatDau(this.thoiGianBatDau)
                .thoiGianKetThuc(this.thoiGianKetThuc)
                .tienDauCa(this.tienDauCa)
                .tienCuoiCa(this.tienCuoiCa)
                .tienHeThongTinh(this.tienHeThongTinh)
                .chenhLech(this.chenhLech)
                .ghiChu(this.ghiChu)
                .nhanVien(nvEntity)
                .build();
    }
}