package iuh.fit.core.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChiTietHoaDonDTO {

    private Long id;

    private String maDon;
    private String maMonAn;
    private String tenMon;

    private int soLuong;
    private float donGia;
    private float thanhTien;
}