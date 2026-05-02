package iuh.fit.core.dao;

import iuh.fit.core.entity.TaiKhoan;

public class TaiKhoanRepository extends GenericRepository<TaiKhoan, String> {

    public TaiKhoanRepository() {
        super(TaiKhoan.class);
    }
}
