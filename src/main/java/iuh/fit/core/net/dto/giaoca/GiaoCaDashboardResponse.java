package iuh.fit.core.net.dto.giaoca;

import iuh.fit.core.dto.GiaoCaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiaoCaDashboardResponse {

    private GiaoCaDTO caHienTai;

    private double tongGioTuan;
    private double tongGioThang;

    private double doanhThuHomNay;
    private double doanhThuCaHienTai;
    private double tienMatTrongKet;

    private Map<String, Double> gioLamTheoNgay;

    private List<String> cacCaLamSapToi;

    private String caTruoc;
    private String caSau;
}