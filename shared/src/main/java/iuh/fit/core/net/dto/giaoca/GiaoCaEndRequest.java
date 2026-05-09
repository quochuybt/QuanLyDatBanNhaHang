package iuh.fit.core.net.dto.giaoca;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiaoCaEndRequest {
    private String maGiaoCa;
    private double tienCuoiCa;
    private String ghiChu;
}