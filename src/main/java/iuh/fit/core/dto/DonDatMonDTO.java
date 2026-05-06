package iuh.fit.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.core.entity.DonDatMon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DonDatMonDTO {

    @JsonProperty("maDon")
    private String maDon;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("ngayKhoiTao")
    private LocalDateTime ngayKhoiTao;

    @JsonProperty("maNV")
    private String maNV;

    @JsonProperty("maKH")
    private String maKH;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("thoiGianDen")
    private LocalDateTime thoiGianDen;

    @JsonProperty("trangThai")
    private String trangThai;

    @JsonProperty("maBan")
    private String maBan;

    @JsonProperty("ghiChu")
    private String ghiChu;

    public static DonDatMonDTO fromEntity(DonDatMon entity) {
        if (entity == null) return null;
        return DonDatMonDTO.builder()
                .maDon(entity.getMaDon())
                .ngayKhoiTao(entity.getNgayKhoiTao())
                .maNV(entity.getMaNV())
                .maKH(entity.getMaKH())
                .thoiGianDen(entity.getThoiGianDen())
                .trangThai(entity.getTrangThai())
                .maBan(entity.getMaBan())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    public DonDatMon toEntity() {
        DonDatMon entity = new DonDatMon();

        // 1. Gán các trường dữ liệu cơ bản
        if (this.maDon != null) {
            entity.setMaDon(this.maDon);
        }
        entity.setNgayKhoiTao(this.ngayKhoiTao);
        entity.setThoiGianDen(this.thoiGianDen);
        entity.setTrangThai(this.trangThai);
        entity.setGhiChu(this.ghiChu);

        // 2. Tạo đối tượng giả để gán khóa ngoại (chỉ cần chứa ID)
        if (this.maNV != null && !this.maNV.isEmpty()) {
            iuh.fit.core.entity.NhanVien nv = new iuh.fit.core.entity.NhanVien();
            nv.setManv(this.maNV); // Gọi đúng hàm set mã nhân viên trong Entity của bạn
            entity.setNhanVien(nv);
        }

        if (this.maKH != null && !this.maKH.isEmpty()) {
            iuh.fit.core.entity.KhachHang kh = new iuh.fit.core.entity.KhachHang();
            kh.setMaKH(this.maKH);
            entity.setKhachHang(kh);
        }

        if (this.maBan != null && !this.maBan.isEmpty()) {
            iuh.fit.core.entity.Ban ban = new iuh.fit.core.entity.Ban();
            ban.setMaBan(this.maBan);
            entity.setBan(ban);
        }

        return entity;
    }
}