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
public class GiaoCaHistoryRequest {
    private LocalDate fromDate;
    private LocalDate toDate;
}
