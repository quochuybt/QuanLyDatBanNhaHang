package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iuh.fit.core.entity.TrangThaiBan;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BanDTO {
    private String maBan;
    private String tenBan;
    private int soGhe;
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;
    private String khuVuc;
}