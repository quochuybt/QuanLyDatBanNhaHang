package iuh.fit.core.service;

import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.TaiKhoan;
import iuh.fit.core.repository.NhanVienRepository;
import iuh.fit.core.repository.TaiKhoanRepository;
import java.util.List;

public class NhanVienService {

    private final NhanVienRepository nhanVienRepo = new NhanVienRepository();
    private final TaiKhoanRepository taiKhoanRepo = new TaiKhoanRepository();

    public void addNhanVien(NhanVien nv, String tenTK, String plainPassword) {
        if (taiKhoanRepo.findById(tenTK) != null)
            throw new IllegalArgumentException("Tên tài khoản '" + tenTK + "' đã tồn tại.");

        TaiKhoan tk = new TaiKhoan();
        tk.setTentk(tenTK);
        tk.setMatkhau(tk.hashPassword(plainPassword));
        tk.setTrangthai(true);

        nv.setTaiKhoan(tk);
        tk.setNhanVien(nv);

        nhanVienRepo.save(nv);
    }

    public NhanVien findById(String maNV) {
        return nhanVienRepo.findById(maNV);
    }

    public List<NhanVien> findAll() {
        return nhanVienRepo.findAll();
    }

    public void update(NhanVien nv) {
        if (nhanVienRepo.findById(nv.getManv()) == null)
            throw new IllegalArgumentException("Nhân viên '" + nv.getManv() + "' không tồn tại.");
        nhanVienRepo.update(nv);
    }

    public void delete(String maNV) {
        NhanVien nv = nhanVienRepo.findById(maNV);
        if (nv == null)
            throw new IllegalArgumentException("Nhân viên '" + maNV + "' không tồn tại.");
        nhanVienRepo.delete(maNV);
    }

    public String getEmailByTenTK(String tenTK) {
        if (tenTK == null || tenTK.trim().isEmpty()) {
            return null;
        }

        NhanVien nv = nhanVienRepo.findByTenTK(tenTK);
        // Kiểm tra xem nhân viên có tồn tại và có email hay không
        if (nv != null && nv.getEmail() != null && !nv.getEmail().trim().isEmpty()) {
            return nv.getEmail();
        }

        return null;
    }
}
