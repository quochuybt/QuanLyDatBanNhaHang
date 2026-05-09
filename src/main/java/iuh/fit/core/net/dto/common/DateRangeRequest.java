package iuh.fit.core.net.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
}
