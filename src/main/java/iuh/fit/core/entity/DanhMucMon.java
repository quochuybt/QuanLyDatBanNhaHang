package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "monAns")
@Table(name = "DanhMucMon")
public class DanhMucMon extends BaseEntity {
    @Id
    @Column(name = "madm", length = 20)
    private String madm;

    @Column(name = "tendm", length = 100)
    private String tendm;

    @Column(name = "mota", columnDefinition = "NVARCHAR(255)")
    private String mota;

    @OneToMany(mappedBy = "danhMucMon", fetch = FetchType.LAZY)
    private Set<MonAn> monAns = new HashSet<>();
}