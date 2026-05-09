package iuh.fit.core.dto;

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