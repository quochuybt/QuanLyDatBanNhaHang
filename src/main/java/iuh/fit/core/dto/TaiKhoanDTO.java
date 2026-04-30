package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.TaiKhoan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaiKhoanDTO {

    @JsonProperty("tenTK")
    private String tenTK;

    @JsonProperty("trangThai")
    private boolean trangThai;

    public static TaiKhoanDTO fromEntity(TaiKhoan tk) {
        return TaiKhoanDTO.builder()
                .tenTK(tk.getTentk())
                .trangThai(tk.isTrangthai())
                .build();
    }

    public TaiKhoan toEntity() {
        return new TaiKhoan(tenTK, null, trangThai);
    }
}
