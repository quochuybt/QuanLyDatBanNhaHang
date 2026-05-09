package iuh.fit.core.net.dto.phancong;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhanCongTongGioTheoThangRequestDTO {
    private int thang;
    private int nam;
}
