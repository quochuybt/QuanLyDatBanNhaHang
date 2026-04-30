package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.VaiTro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NhanVienDTO {

    @JsonProperty("maNV")
    private String maNV;

    @JsonProperty("hoTen")
    private String hoTen;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngaySinh")
    private LocalDate ngaySinh;

    @JsonProperty("gioiTinh")
    private String gioiTinh;

    @JsonProperty("sdt")
    private String sdt;

    @JsonProperty("diaChi")
    private String diaChi;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngayVaoLam")
    private LocalDate ngayVaoLam;

    @JsonProperty("luong")
    private float luong;

    @JsonProperty("vaiTro")
    private VaiTro vaiTro;

    @JsonProperty("email")
    private String email;

    @JsonProperty("tenTK")
    private String tenTK;

    public static NhanVienDTO fromEntity(NhanVien nv) {
        return NhanVienDTO.builder()
                .maNV(nv.getManv())
                .hoTen(nv.getHoten())
                .ngaySinh(nv.getNgaysinh())
                .gioiTinh(nv.getGioitinh())
                .sdt(nv.getSdt())
                .diaChi(nv.getDiachi())
                .ngayVaoLam(nv.getNgayvaolam())
                .luong(nv.getLuong())
                .vaiTro(nv.getVaiTro())
                .email(nv.getEmail())
                .tenTK(nv.getTaiKhoan() != null ? nv.getTaiKhoan().getTentk() : null)
                .build();
    }

    public NhanVien toEntity() {
        NhanVien nv = new NhanVien(maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, email);
        nv.setManv(maNV);
        return nv;
    }
}
