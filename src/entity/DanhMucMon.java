package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "DanhMucMon")
public class DanhMucMon {
    @Id
    private String madm;
    private String tendm;
    private String mota;
}