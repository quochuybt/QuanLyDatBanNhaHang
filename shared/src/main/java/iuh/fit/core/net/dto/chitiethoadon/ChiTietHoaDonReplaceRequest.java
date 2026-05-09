package iuh.fit.core.net.dto.chitiethoadon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iuh.fit.core.dto.ChiTietHoaDonDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChiTietHoaDonReplaceRequest {
    private String maDon;
    private List<ChiTietHoaDonDTO> items;
}