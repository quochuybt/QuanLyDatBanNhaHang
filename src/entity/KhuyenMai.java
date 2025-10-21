package entity;

import java.time.LocalDate;

public class KhuyenMai {
	private String maKM;
    private String tenChuongTrinh;
    private String loaiKhuyenMai; // "Giảm theo phần trăm", "Tặng món", "Giảm giá số tiền"
    private double giaTri; // Ví dụ: 12 (%), 100000 (VND)
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String trangThai; // "Đang áp dụng", "Ngưng áp dụng"

    // --- Constructor ---
    public KhuyenMai(String maKM, String tenChuongTrinh, String loaiKhuyenMai, 
                     double giaTri, LocalDate ngayBatDau, LocalDate ngayKetThuc, String trangThai) {
        this.maKM = maKM;
        this.tenChuongTrinh = tenChuongTrinh;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTri = giaTri;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

	public String getMaKM() {
		return maKM;
	}

	public void setMaKM(String maKM) {
		this.maKM = maKM;
	}

	public String getTenChuongTrinh() {
		return tenChuongTrinh;
	}

	public void setTenChuongTrinh(String tenChuongTrinh) {
		this.tenChuongTrinh = tenChuongTrinh;
	}

	public String getLoaiKhuyenMai() {
		return loaiKhuyenMai;
	}

	public void setLoaiKhuyenMai(String loaiKhuyenMai) {
		this.loaiKhuyenMai = loaiKhuyenMai;
	}

	public double getGiaTri() {
		return giaTri;
	}

	public void setGiaTri(double giaTri) {
		this.giaTri = giaTri;
	}

	public LocalDate getNgayBatDau() {
		return ngayBatDau;
	}

	public void setNgayBatDau(LocalDate ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}

	public LocalDate getNgayKetThuc() {
		return ngayKetThuc;
	}

	public void setNgayKetThuc(LocalDate ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
    

}
