package iuh.fit.core.net.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRangeRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
