package iuh.fit.core.net.dto.nhanvien;

import iuh.fit.core.dto.NhanVienDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienAddRequest {
    private NhanVienDTO nhanVien;
    private String tenTK;
    private String matKhau;
}
