package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.PhanCong;
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
public class PhanCongDTO {

    @JsonProperty("maNV")
    private String maNV;

    @JsonProperty("hoTenNV")
    private String hoTenNV;

    @JsonProperty("maCa")
    private String maCa;

    @JsonProperty("tenCa")
    private String tenCa;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngayLam")
    private LocalDate ngayLam;

    public static PhanCongDTO fromEntity(PhanCong pc) {
        if (pc == null) {
            return null;
        }

        return PhanCongDTO.builder()
                .maNV(pc.getNhanVien() != null ? pc.getNhanVien().getManv() : null)
                .hoTenNV(pc.getNhanVien() != null ? pc.getNhanVien().getHoten() : null)
                .maCa(pc.getCaLam() != null ? pc.getCaLam().getMaCa() : null)
                .tenCa(pc.getCaLam() != null ? pc.getCaLam().getTenCa() : null)
                .ngayLam(pc.getNgayLam())
                .build();
    }
}