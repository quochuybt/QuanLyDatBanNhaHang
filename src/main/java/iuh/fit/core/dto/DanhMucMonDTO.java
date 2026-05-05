package iuh.fit.core.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DanhMucMonDTO {
    private String madm;
    private String tendm;
    private String mota;
}