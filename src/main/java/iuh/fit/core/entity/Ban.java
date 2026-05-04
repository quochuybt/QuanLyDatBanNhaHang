package iuh.fit.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "Ban")
public class Ban {
    @Id
    private String maBan;
    private static int soThuTuBan = 1;
    private String tenBan;
    private int soGhe;
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;
    private String khuVuc;

    @OneToMany(mappedBy = "ban")
    private Set<DonDatMon> donDatMons =  new HashSet<>();

}
