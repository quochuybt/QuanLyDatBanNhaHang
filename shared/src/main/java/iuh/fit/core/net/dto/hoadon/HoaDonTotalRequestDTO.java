package iuh.fit.core.net.dto.hoadon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonTotalRequestDTO {
    private String trangThai;
    private String keyword;
    private LocalDateTime tuNgay;
    private LocalDateTime denNgay;
}
