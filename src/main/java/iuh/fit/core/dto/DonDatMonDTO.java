package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.DonDatMon;
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
public class DonDatMonDTO {

    @JsonProperty("maDon")
    private String maDon;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ngayKhoiTao")
    private LocalDateTime ngayKhoiTao;

    @JsonProperty("maNV")
    private String maNV;

    @JsonProperty("maKH")
    private String maKH;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("thoiGianDen")
    private LocalDateTime thoiGianDen;

    @JsonProperty("trangThai")
    private String trangThai;

    @JsonProperty("maBan")
    private String maBan;

    @JsonProperty("ghiChu")
    private String ghiChu;

    public static DonDatMonDTO fromEntity(DonDatMon entity) {
        if (entity == null) return null;
        return DonDatMonDTO.builder()
                .maDon(entity.getMaDon())
                .ngayKhoiTao(entity.getNgayKhoiTao())
                .maNV(entity.getMaNV())
                .maKH(entity.getMaKH())
                .thoiGianDen(entity.getThoiGianDen())
                .trangThai(entity.getTrangThai())
                .maBan(entity.getMaBan())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    public DonDatMon toEntity() {
        DonDatMon entity = new DonDatMon(
                this.maDon,
                this.ngayKhoiTao,
                this.maNV,
                this.maKH,
                this.maBan,
                this.ghiChu
        );
        entity.setThoiGianDen(this.thoiGianDen);
        entity.setTrangThai(this.trangThai);
        return entity;
    }
}