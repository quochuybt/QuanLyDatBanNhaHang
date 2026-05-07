package iuh.fit.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    @Enumerated(EnumType.STRING)
    private TrangThaiBan trangThai;
    private LocalDateTime gioMoBan;
    private String khuVuc;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "ban")
    private Set<DonDatMon> donDatMons =  new HashSet<>();

}
