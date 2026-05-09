package iuh.fit.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PhanCongId implements Serializable {

    @Column(name = "maNV", length = 20)
    private String maNV;

    @Column(name = "maCa", length = 20)
    private String maCa;

    @Column(name = "ngayLam")
    private LocalDate ngayLam;
}
