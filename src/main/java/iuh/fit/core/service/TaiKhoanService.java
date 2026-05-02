package iuh.fit.core.service;

import iuh.fit.core.entity.TaiKhoan;
import iuh.fit.core.dao.TaiKhoanRepository;

public class TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepo = new TaiKhoanRepository();

    public TaiKhoan login(String tenTK, String plainPassword) {
        TaiKhoan tk = taiKhoanRepo.findById(tenTK);
        if (tk == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        if (!tk.isTrangthai())
            throw new IllegalArgumentException("Tài khoản đã bị khóa.");
        String hashed = tk.hashPassword(plainPassword);
        if (!hashed.equals(tk.getMatkhau()))
            throw new IllegalArgumentException("Mật khẩu không đúng.");
        return tk;
    }

    public void changePassword(String tenTK, String oldPassword, String newPassword) {
        TaiKhoan tk = taiKhoanRepo.findById(tenTK);
        if (tk == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        if (!tk.hashPassword(oldPassword).equals(tk.getMatkhau()))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng.");
        tk.setMatkhau(tk.hashPassword(newPassword));
        taiKhoanRepo.update(tk);
    }

    public void toggleStatus(String tenTK) {
        TaiKhoan tk = taiKhoanRepo.findById(tenTK);
        if (tk == null)
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        tk.setTrangthai(!tk.isTrangthai());
        taiKhoanRepo.update(tk);
    }
}
