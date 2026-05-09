package iuh.fit.core.net.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String tenTK;
    private String maNV;
    private String hoTen;
    private String vaiTro;
}
