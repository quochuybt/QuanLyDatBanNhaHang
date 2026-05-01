package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.entity.KhachHang;
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
public class KhachHangDTO {

    @JsonProperty("maKH")
    private String maKH;

    @JsonProperty("tenKH")
    private String tenKH;

    @JsonProperty("gioiTinh")
    private String gioiTinh;

    @JsonProperty("sdt")
    private String sdt;

    @JsonProperty("hangThanhVien")
    private HangThanhVien hangThanhVien;

    @JsonProperty("tongChiTieu")
    private float tongChiTieu;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngaySinh")
    private LocalDate ngaySinh;

    @JsonProperty("diaChi")
    private String diaChi;

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("ngayThamGia")
    private LocalDate ngayThamGia;

    @JsonProperty("email")
    private String email;

    public static KhachHangDTO fromEntity(KhachHang kh) {
        return KhachHangDTO.builder()
                .maKH(kh.getMaKH())
                .tenKH(kh.getTenKH())
                .gioiTinh(kh.getGioitinh())
                .sdt(kh.getSdt())
                .hangThanhVien(kh.getHangThanhVien())
                .tongChiTieu(kh.getTongChiTieu())
                .ngaySinh(kh.getNgaySinh())
                .diaChi(kh.getDiaChi())
                .ngayThamGia(kh.getNgayThamGia())
                .email(kh.getEmail())
                .build();
    }

    public KhachHang toEntity() {
        return new KhachHang(
                maKH,
                tenKH,
                gioiTinh,
                sdt,
                ngaySinh,
                diaChi,
                email,
                ngayThamGia,
                tongChiTieu,
                hangThanhVien
        );
    }
}