package iuh.fit.core.net.dto.ban;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iuh.fit.core.dto.BanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BanActionRequest {
    private BanDTO ban;
    private BanDTO banCu;
    private BanDTO banMoi;
    private BanDTO banDich;
    private List<BanDTO> dsBanNguon;
}