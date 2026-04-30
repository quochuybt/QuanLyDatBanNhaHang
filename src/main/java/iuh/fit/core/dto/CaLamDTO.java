package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.CaLam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaLamDTO {

    @JsonProperty("maCa")
    private String maCa;

    @JsonProperty("tenCa")
    private String tenCa;

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("gioBatDau")
    private LocalTime gioBatDau;

    @JsonFormat(pattern = "HH:mm")
    @JsonProperty("gioKetThuc")
    private LocalTime gioKetThuc;

    public static CaLamDTO fromEntity(CaLam ca) {
        return CaLamDTO.builder()
                .maCa(ca.getMaCa())
                .tenCa(ca.getTenCa())
                .gioBatDau(ca.getGioBatDau())
                .gioKetThuc(ca.getGioKetThuc())
                .build();
    }

    public CaLam toEntity() {
        return new CaLam(maCa, tenCa, gioBatDau, gioKetThuc);
    }
}
