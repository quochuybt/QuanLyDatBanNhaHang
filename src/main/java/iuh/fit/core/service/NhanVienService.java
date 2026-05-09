package iuh.fit.core.service;

import iuh.fit.core.dto.NhanVienDTO;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.TaiKhoan;
import iuh.fit.core.entity.VaiTro;
import iuh.fit.core.repository.NhanVienRepository;
import iuh.fit.core.repository.TaiKhoanRepository;

import java.util.List;

public class NhanVienService {

    private final NhanVienRepository nhanVienRepo = new NhanVienRepository();
    private final TaiKhoanRepository taiKhoanRepo = new TaiKhoanRepository();

    public void addNhanVien(NhanVien nv, String tenTK, String plainPassword) {
        if (taiKhoanRepo.findById(tenTK) != null) {
            throw new IllegalArgumentException("Tên tài khoản '" + tenTK + "' đã tồn tại.");
        }

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

    public NhanVienDTO getChiTietNhanVien(String maNV) {
        NhanVien nv = nhanVienRepo.findById(maNV);
        return nv == null ? null : NhanVienDTO.fromEntity(nv);
    }

    public List<NhanVien> findAll() {
        return nhanVienRepo.findAll();
    }

    public void update(NhanVien nv) {
        if (nhanVienRepo.findById(nv.getManv()) == null) {
            throw new IllegalArgumentException("Nhân viên '" + nv.getManv() + "' không tồn tại.");
        }
        nhanVienRepo.update(nv);
    }

    public void delete(String maNV) {
        NhanVien nv = nhanVienRepo.findById(maNV);
        if (nv == null) {
            throw new IllegalArgumentException("Nhân viên '" + maNV + "' không tồn tại.");
        }
        nv.softDelete();
        nhanVienRepo.update(nv);
    }

    public int getAccountStatus(String tenTK) {
        if (tenTK == null || tenTK.trim().isEmpty()) {
            return -1;
        }

        TaiKhoan tk = taiKhoanRepo.findById(tenTK);

        if (tk == null) {
            return -1;
        }

        return tk.isTrangthai() ? 1 : 0;
    }

    public void updateNhanVienAndAccount(
            NhanVienDTO dto,
            String oldTenTK,
            String newTenTK,
            String newMatKhau
    ) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }

        if (dto.getMaNV() == null || dto.getMaNV().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được rỗng.");
        }

        if (newTenTK == null || newTenTK.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài khoản không được rỗng.");
        }

        NhanVien nvCu = nhanVienRepo.findById(dto.getMaNV());
        if (nvCu == null) {
            throw new IllegalArgumentException("Nhân viên '" + dto.getMaNV() + "' không tồn tại.");
        }

        TaiKhoan tkCu = taiKhoanRepo.findById(oldTenTK);
        if (tkCu == null) {
            throw new IllegalArgumentException("Tài khoản cũ không tồn tại.");
        }

        if (!oldTenTK.equals(newTenTK) && taiKhoanRepo.findById(newTenTK) != null) {
            throw new IllegalArgumentException("Tên tài khoản '" + newTenTK + "' đã tồn tại.");
        }

        NhanVien nvMoi = dto.toEntity();

        /*
         * Trường hợp KHÔNG đổi tên tài khoản:
         * Chỉ cập nhật thông tin nhân viên và mật khẩu nếu có nhập.
         */
        if (oldTenTK.equals(newTenTK)) {
            if (newMatKhau != null && !newMatKhau.trim().isEmpty()) {
                tkCu.setMatkhau(tkCu.hashPassword(newMatKhau));
            }

            nvMoi.setTaiKhoan(tkCu);
            tkCu.setNhanVien(nvMoi);

            nhanVienRepo.update(nvMoi);
            taiKhoanRepo.update(tkCu);
            return;
        }

        /*
         * Trường hợp CÓ đổi tên tài khoản:
         * Vì tenTK thường là khóa chính nên không nên sửa trực tiếp ID.
         * Cách an toàn là tạo tài khoản mới, gán cho nhân viên, rồi xóa tài khoản cũ.
         */
        TaiKhoan tkMoi = new TaiKhoan();
        tkMoi.setTentk(newTenTK);

        if (newMatKhau != null && !newMatKhau.trim().isEmpty()) {
            tkMoi.setMatkhau(tkMoi.hashPassword(newMatKhau));
        } else {
            tkMoi.setMatkhau(tkCu.getMatkhau());
        }

        tkMoi.setTrangthai(tkCu.isTrangthai());

        nvMoi.setTaiKhoan(tkMoi);
        tkMoi.setNhanVien(nvMoi);

        nhanVienRepo.update(nvMoi);
        taiKhoanRepo.save(tkMoi);
        // Soft-delete tài khoản cũ thay vì xóa cứng
        TaiKhoan tkCuCanXoa = taiKhoanRepo.findById(oldTenTK);
        if (tkCuCanXoa != null) {
            tkCuCanXoa.softDelete();
            taiKhoanRepo.update(tkCuCanXoa);
        }
    }

    public void activateNhanVienAccount(String tenTK) {
        TaiKhoan tk = taiKhoanRepo.findById(tenTK);

        if (tk == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        }

        tk.setTrangthai(true);
        taiKhoanRepo.update(tk);
    }

    public void suspendNhanVienAndAccount(String maNV, String tenTK, VaiTro vaiTro) {
        if (vaiTro != VaiTro.NHANVIEN) {
            throw new IllegalArgumentException("Chỉ có thể tạm ngưng nhân viên có vai trò NHANVIEN.");
        }

        NhanVien nv = nhanVienRepo.findById(maNV);
        if (nv == null) {
            throw new IllegalArgumentException("Nhân viên không tồn tại.");
        }

        TaiKhoan tk = taiKhoanRepo.findById(tenTK);
        if (tk == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        }

        tk.setTrangthai(false);
        taiKhoanRepo.update(tk);
    }

    public String getEmailByTenTK(String tenTK) {
        if (tenTK == null || tenTK.trim().isEmpty()) {
            return null;
        }

        NhanVien nv = nhanVienRepo.findByTenTK(tenTK);

        if (nv != null && nv.getEmail() != null && !nv.getEmail().trim().isEmpty()) {
            return nv.getEmail();
        }

        return null;
    }
}