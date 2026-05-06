package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.DanhMucMon;
import iuh.fit.core.entity.MonAn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonAnDTO {

    @JsonProperty("maMonAn")
    private String maMonAn;

    @JsonProperty("tenMon")
    private String tenMon;

    @JsonProperty("moTa")
    private String moTa;

    @JsonProperty("donGia")
    private float donGia;

    @JsonProperty("donViTinh")
    private String donViTinh;

    @JsonProperty("trangThai")
    private String trangThai;

    @JsonProperty("hinhAnh")
    private String hinhAnh;

    @JsonProperty("maDM")
    private String maDM;

    @JsonProperty("tenDM")
    private String tenDM;

    public static MonAnDTO fromEntity(MonAn m) {
        if (m == null) return null;
        return MonAnDTO.builder()
                .maMonAn(m.getMaMonAn())
                .tenMon(m.getTenMon())
                .moTa(m.getMoTa())
                .donGia(m.getDonGia())
                .donViTinh(m.getDonViTinh())
                .trangThai(m.getTrangThai())
                .hinhAnh(m.getHinhAnh())
                .maDM(m.getDanhMucMon() != null ? m.getDanhMucMon().getMadm() : null)
                .tenDM(m.getDanhMucMon() != null ? m.getDanhMucMon().getTendm() : null)
                .build();
    }

    public MonAn toEntity() {
        MonAn m = new MonAn();
        m.setMaMonAn(this.maMonAn);
        m.setTenMon(this.tenMon);
        m.setMoTa(this.moTa);
        m.setDonGia(this.donGia);
        m.setDonViTinh(this.donViTinh);
        m.setTrangThai(this.trangThai);
        m.setHinhAnh(this.hinhAnh);
        if (this.maDM != null && !this.maDM.isEmpty()) {
            DanhMucMon dm = new DanhMucMon();
            dm.setMadm(this.maDM);
            dm.setTendm(this.tenDM);
            m.setDanhMucMon(dm);
        }
        return m;
    }
}
