package entity;

import java.time.LocalDate;

public class PhanCong {
    private CaLam caLam;
    private NhanVien nhanVien;
    private LocalDate ngayLam;

    public PhanCong(CaLam caLam, NhanVien nhanVien, LocalDate ngayLam) {
        this.caLam = caLam;
        this.nhanVien = nhanVien;
        this.ngayLam = ngayLam;
    }

    // Getters
    public CaLam getCaLam() {
        return caLam;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public LocalDate getNgayLam() {
        return ngayLam;
    }
}