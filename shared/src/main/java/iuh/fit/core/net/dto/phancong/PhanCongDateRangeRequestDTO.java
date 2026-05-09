package iuh.fit.core.net.dto.phancong;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhanCongDateRangeRequestDTO {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate tuNgay;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate denNgay;
}
