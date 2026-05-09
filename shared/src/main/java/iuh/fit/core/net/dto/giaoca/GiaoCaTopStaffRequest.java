package iuh.fit.core.net.dto.giaoca;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiaoCaTopStaffRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private int limit;
}
