package iuh.fit.core.service;

import iuh.fit.core.entity.KhachHang;
import iuh.fit.core.dao.KhachHangRepository;

import java.util.List;

public class KhachHangService {

    private final KhachHangRepository khachHangRepo = new KhachHangRepository();

    public void addKhachHang(KhachHang kh) {
        if (khachHangRepo.findById(kh.getMaKH()) != null)
            throw new IllegalArgumentException("Mã khách hàng '" + kh.getMaKH() + "' đã tồn tại.");

        khachHangRepo.save(kh);
    }

    public KhachHang findById(String maKH) {
        return khachHangRepo.findById(maKH);
    }

    public List<KhachHang> findAll() {
        return khachHangRepo.findAll();
    }

    public void update(KhachHang kh) {
        if (khachHangRepo.findById(kh.getMaKH()) == null)
            throw new IllegalArgumentException("Khách hàng '" + kh.getMaKH() + "' không tồn tại.");

        khachHangRepo.update(kh);
    }

    public void delete(String maKH) {
        KhachHang kh = khachHangRepo.findById(maKH);

        if (kh == null)
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");

        khachHangRepo.delete(maKH);
    }

    public List<KhachHang> search(String keyword) {
        return khachHangRepo.search(keyword);
    }

    public void addChiTieu(String maKH, float soTien) {
        KhachHang kh = khachHangRepo.findById(maKH);

        if (kh == null)
            throw new IllegalArgumentException("Khách hàng '" + maKH + "' không tồn tại.");

        kh.capNhatTongChiTieu(soTien);
        khachHangRepo.update(kh);
    }

    public KhachHang findBySdt(String sdt) {
        return khachHangRepo.findBySdt(sdt);
    }
}