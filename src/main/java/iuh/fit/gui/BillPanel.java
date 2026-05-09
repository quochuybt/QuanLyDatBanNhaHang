package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.dto.NhanVienDTO;

import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.net.client.NhanVienRemoteService;
import iuh.fit.core.net.client.NetClientContext;
import iuh.fit.core.service.BanService;
import iuh.fit.core.service.ChiTietHoaDonService;
import iuh.fit.core.service.DonDatMonService;
import iuh.fit.core.service.HoaDonService;
import iuh.fit.core.service.KhachHangService;
import iuh.fit.core.service.KhuyenMaiService;
import iuh.fit.core.service.MonAnService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class BillPanel extends JPanel {

    private static final Color COLOR_BUTTON_BLUE = new Color(56, 118, 243);

    private JLabel lblTongCong;
    private JLabel lblKhuyenMai;
    private JLabel lblVAT;
    private JLabel lblTongThanhToan;
    private JLabel lblTongSoLuong;
    private JLabel lblPhanTramVAT;
    private JLabel lblTienThoi;
    private JTextField txtKhachTra;
    private String customHeaderName = "";

    private JButton btnLuuMon, btnInTamTinh, btnThanhToan;

    private ManHinhGoiMonGUI parentGoiMonGUI;
    private ManHinhBanGUI parentBanGUI;

    private ChiTietHoaDonService chiTietHoaDonService;
    private HoaDonService hoaDonService;
    private BanService banService;
    private NhanVienRemoteService nhanVienRemoteService;
    private MonAnService monAnService;
    private KhachHangService khachHangService;
    private KhuyenMaiService khuyenMaiService;
    private DonDatMonService donDatMonService;

    private long currentTotal = 0;
    private JPanel suggestedCashPanel;
    private final JButton[] suggestedCashButtons = new JButton[6];
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public BillPanel(ManHinhGoiMonGUI parent) {
        super(new BorderLayout(0, 10));
        this.parentGoiMonGUI = parent;
        this.parentBanGUI = null;
        initCommon();
    }

    public BillPanel(ManHinhBanGUI parent) {
        super(new BorderLayout(0, 10));
        this.parentBanGUI = parent;
        this.parentGoiMonGUI = null;
        initCommon();
    }

    public BillPanel() {
        super(new BorderLayout(0, 10));
        this.parentGoiMonGUI = null;
        this.parentBanGUI = null;
        initCommon();
    }

    private void initCommon() {
        this.chiTietHoaDonService = new ChiTietHoaDonService();
        this.hoaDonService = new HoaDonService();
        this.banService = new BanService();
        this.khachHangService = new KhachHangService();
        this.khuyenMaiService = new KhuyenMaiService();
        this.nhanVienRemoteService = NetClientContext.isReady()
                ? new NhanVienRemoteService(NetClientContext.getConnection())
                : null;
        this.monAnService = new MonAnService();
        this.donDatMonService = new DonDatMonService();

        setBackground(Color.WHITE);

        JPanel checkoutPanel = createCheckoutPanel();
        add(checkoutPanel, BorderLayout.SOUTH);

        if (parentGoiMonGUI != null || parentBanGUI != null) {
            btnInTamTinh.addActionListener(e -> hienThiXemTamTinh());
            btnThanhToan.addActionListener(e -> xuLyThanhToan());

            if (parentGoiMonGUI != null) {
                btnLuuMon.addActionListener(e -> xuLyLuuMon_Clicked());
            } else {
                btnLuuMon.setEnabled(false);
            }
        } else {
            btnLuuMon.setEnabled(false);
            btnInTamTinh.setEnabled(false);
            btnThanhToan.setEnabled(false);
        }

        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        KeyStroke f2KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        String saveActionKey = "saveOrderAction";
        inputMap.put(f2KeyStroke, saveActionKey);

        actionMap.put(saveActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnLuuMon != null && btnLuuMon.isEnabled()) {
                    btnLuuMon.doClick();
                }
            }
        });

        KeyStroke f1KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        String checkoutActionKey = "checkoutAction";
        inputMap.put(f1KeyStroke, checkoutActionKey);

        actionMap.put(checkoutActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnThanhToan != null && btnThanhToan.isEnabled()) {
                    btnThanhToan.doClick();
                }
            }
        });
    }




    public void xuLyThanhToan() {
        BanDTO banHienTai = null;
        HoaDonDTO activeHoaDon = null;

        if (parentGoiMonGUI != null) {
            banHienTai = JsonMapper.convert(parentGoiMonGUI.getBanHienTai(), BanDTO.class);
            activeHoaDon = JsonMapper.convert(parentGoiMonGUI.getActiveHoaDon(), HoaDonDTO.class);
        } else if (parentBanGUI != null) {
            banHienTai = JsonMapper.convert(parentBanGUI.getSelectedTable(), BanDTO.class);
            activeHoaDon = JsonMapper.convert(parentBanGUI.getActiveHoaDon(), HoaDonDTO.class);
        }

        if (banHienTai == null || activeHoaDon == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Chưa có bàn/hóa đơn hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String ghiChuHoaDon = "";
        if (activeHoaDon.getMaDon() != null) {
            DonDatMonDTO ddm = donDatMonService.findById(activeHoaDon.getMaDon());

            if (ddm != null && ddm.getGhiChu() != null) {
                ghiChuHoaDon = ddm.getGhiChu();
            }
        }

        List<ChiTietHoaDonDTO> dsMonHienTai = null;
        int tongSoLuong = 0;

        if (parentGoiMonGUI != null) {
            if (parentGoiMonGUI.getModelChiTietHoaDon().getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Chưa có món nào để thanh toán!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!luuMonAnVaoCSDL(false)) {
                return;
            }

            ChiTietHoaDonDTO request = new ChiTietHoaDonDTO();
            request.setMaDon(activeHoaDon.getMaDon());

            dsMonHienTai = chiTietHoaDonService.getChiTietTheoMaDon(request);

            activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);

            if (dsMonHienTai != null) {
                for (ChiTietHoaDonDTO ct : dsMonHienTai) {
                    tongSoLuong += ct.getSoLuong();
                }
            }

            this.currentTotal = (long) activeHoaDon.getTongThanhToan();

            loadBillTotals(
                    (long) activeHoaDon.getTongTien(),
                    (long) activeHoaDon.getGiamGia(),
                    (long) activeHoaDon.getTongThanhToan(),
                    tongSoLuong
            );

        } else if (parentBanGUI != null) {
            ChiTietHoaDonDTO request = new ChiTietHoaDonDTO();
            request.setMaDon(activeHoaDon.getMaDon());

            dsMonHienTai = chiTietHoaDonService.getChiTietTheoMaDon(request);

            activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);

            if (dsMonHienTai != null) {
                for (ChiTietHoaDonDTO ct : dsMonHienTai) {
                    tongSoLuong += ct.getSoLuong();
                }
            }

            this.currentTotal = (long) activeHoaDon.getTongThanhToan();

            loadBillTotals(
                    (long) activeHoaDon.getTongTien(),
                    (long) activeHoaDon.getGiamGia(),
                    (long) activeHoaDon.getTongThanhToan(),
                    tongSoLuong
            );
        }

        long tienKhachTraLong;
        String maHDCuoiCung = activeHoaDon.getMaHD();
        long tongPhaiTraLong = this.currentTotal;

        try {
            String khachTraStr = txtKhachTra.getText()
                    .replace(",", "")
                    .replace(".", "")
                    .trim();

            tienKhachTraLong = Long.parseLong(khachTraStr);

            if (tienKhachTraLong < tongPhaiTraLong) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tiền khách đưa không đủ!",
                        "Lỗi",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số tiền không hợp lệ!",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        long tienThoiLong = tienKhachTraLong - tongPhaiTraLong;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format(
                        "Xác nhận thanh toán cho %s?\nTổng: %s\nKhách đưa: %s\nTiền thối: %s",
                        banHienTai.getTenBan(),
                        nf.format(tongPhaiTraLong),
                        nf.format(tienKhachTraLong),
                        nf.format(tienThoiLong)
                ),
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String hinhThucTT = "Tiền mặt";

            if (parentBanGUI != null) {
                hinhThucTT = parentBanGUI.getHinhThucThanhToan();
            }

            double tienGiamGia = activeHoaDon.getGiamGia();
            String maKM = activeHoaDon.getMaKM();
            long tongThanhToanFinal = this.currentTotal;

            BanDTO banRequest = new BanDTO();
            banRequest.setMaBan(banHienTai.getMaBan());

            String tenBanInHoaDon = banService.getTenBanByMa(banRequest);

            if (tenBanInHoaDon == null || tenBanInHoaDon.isEmpty()) {
                tenBanInHoaDon = banHienTai.getTenBan();
            }

            if (this.customHeaderName != null && !this.customHeaderName.isEmpty()) {
                tenBanInHoaDon = this.customHeaderName;
            }

            String tenBanLuuLichSu = tenBanInHoaDon;

            if (tenBanLuuLichSu == null || tenBanLuuLichSu.isEmpty()) {
                tenBanLuuLichSu = banHienTai.getTenBan();
            }

            HoaDonDTO thanhToanDTO = new HoaDonDTO();

            thanhToanDTO.setMaHD(maHDCuoiCung);
            thanhToanDTO.setMaDon(activeHoaDon.getMaDon());
            thanhToanDTO.setMaNV(activeHoaDon.getMaNV());
            thanhToanDTO.setMaKH(activeHoaDon.getMaKH());

            thanhToanDTO.setTongTien(activeHoaDon.getTongTien());
            thanhToanDTO.setTongThanhToan(activeHoaDon.getTongThanhToan());
            thanhToanDTO.setTienKhachDua(tienKhachTraLong);
            thanhToanDTO.setHinhThucThanhToan(hinhThucTT);
            thanhToanDTO.setTrangThai("Đã thanh toán");

            thanhToanDTO.setGiamGia((float) tienGiamGia);
            thanhToanDTO.setMaKM(maKM);
            thanhToanDTO.setTenBan(tenBanLuuLichSu);

            /*
             * QUAN TRỌNG:
             * Lấy danh sách món để in TRƯỚC khi gọi thanhToanHoaDon().
             * Vì sau khi thanh toán, parentBanGUI.getActiveHoaDon()
             * sẽ không còn tìm thấy hóa đơn "Chưa thanh toán" nữa.
             */
            List<ChiTietHoaDonDTO> listToPrint = getCurrentDetailList();

            int tongSoLuongIn = 0;
            if (listToPrint != null) {
                for (ChiTietHoaDonDTO ct : listToPrint) {
                    tongSoLuongIn += ct.getSoLuong();
                }
            }

            boolean thanhToanOK = hoaDonService.thanhToanHoaDon(thanhToanDTO);

            if (thanhToanOK) {
                String maKH = activeHoaDon.getMaKH();

                if (maKH != null && !maKH.trim().isEmpty()) {
                    KhachHangDTO khachHang = khachHangService.findByIdDTO(maKH);

                    if (khachHang != null
                            && khachHang.getHangThanhVien() != iuh.fit.core.entity.HangThanhVien.NONE) {
                        float soTienCongThem = (float) tongThanhToanFinal;

                        try {
                            khachHangService.addChiTieu(khachHang.getMaKH(), soTienCongThem);
                            KhachHangGUI.reloadKhachHangTableIfAvailable();
                        } catch (Exception ex) {
                            System.err.println("Lỗi CSDL khi cập nhật Khách Hàng: " + maKH);
                        }
                    }
                }

            String tenNVIn = "Admin";

            if (activeHoaDon.getMaNV() != null) {
                if (nhanVienRemoteService != null) {
                    NhanVienDTO nv = nhanVienRemoteService.findById(activeHoaDon.getMaNV());

                    if (nv != null) {
                        tenNVIn = nv.getHoTen();
                    }
                }
            }

                String tenKHIn = "Khách lẻ";

                if (activeHoaDon.getMaKH() != null) {
                    KhachHangDTO kh = khachHangService.findByIdDTO(activeHoaDon.getMaKH());

                    if (kh != null) {
                        tenKHIn = kh.getTenKH();
                    }
                }

                xuatPhieuIn(
                        "HÓA ĐƠN THANH TOÁN",
                        true,
                        tienKhachTraLong,
                        tienThoiLong,
                        activeHoaDon.getMaHD(),
                        listToPrint,
                        hinhThucTT,
                        tenBanInHoaDon,
                        tenNVIn,
                        tenKHIn,
                        ghiChuHoaDon
                );

                // Load lại bill lần cuối cho khách xem bằng hàm cũ
                loadBillTotals(
                        (long) activeHoaDon.getTongTien(),
                        (long) activeHoaDon.getGiamGia(),
                        (long) activeHoaDon.getTongThanhToan(),
                        tongSoLuongIn
                );

                this.revalidate();
                this.repaint();

                if (parentGoiMonGUI != null) {
                    parentGoiMonGUI.xoaThongTinGoiMon();

                    if (parentGoiMonGUI.getParentDanhSachBanGUI() != null) {
                        parentGoiMonGUI.getParentDanhSachBanGUI().refreshManHinhBan();
                    }

                } else if (parentBanGUI != null) {
                    parentBanGUI.refreshTableList();
                    clearBill();
                }

                maKM = activeHoaDon.getMaKM();

                if (maKM != null && !maKM.isEmpty()) {
                    khuyenMaiService.useKhuyenMai(maKM);
                }

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi cập nhật CSDL!",
                        "Lỗi CSDL",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void xuLyLuuMon_Clicked() {
        luuMonAnVaoCSDL(true);
    }

    private List<ChiTietHoaDonDTO> getCurrentDetailList() {
        List<ChiTietHoaDonDTO> list = new ArrayList<>();

        if (parentGoiMonGUI != null) {
            DefaultTableModel model = parentGoiMonGUI.getModelChiTietHoaDon();
            String maDon = null;

            if (parentGoiMonGUI.getActiveHoaDon() != null) {
                maDon = parentGoiMonGUI.getActiveHoaDon().getMaDon();
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    Object maMonObj = model.getValueAt(i, 1);
                    String maMon = (maMonObj != null) ? maMonObj.toString().trim() : "";

                    if (maMon.isEmpty()) {
                        System.err.println("Dòng " + i + " trong bảng bị thiếu mã món, bỏ qua.");
                        continue;
                    }

                    String tenMon = (String) model.getValueAt(i, 2);
                    Integer soLuong = (Integer) model.getValueAt(i, 3);
                    Float donGia = (Float) model.getValueAt(i, 4);

                    if (soLuong == null) soLuong = 1;
                    if (donGia == null) donGia = 0f;

                    ChiTietHoaDonDTO ct = new ChiTietHoaDonDTO();
                    ct.setMaDon(maDon);
                    ct.setMaMonAn(maMon);
                    ct.setTenMon(tenMon);
                    ct.setSoLuong(soLuong);
                    ct.setDonGia(donGia);
                    ct.setThanhTien(soLuong * donGia);

                    list.add(ct);

                } catch (Exception e) {
                    System.err.println("Lỗi khi đọc dòng " + i + " từ bảng: " + e.getMessage());
                }
            }
        } else if (parentBanGUI != null) {
            HoaDonDTO hd = JsonMapper.convert(parentBanGUI.getActiveHoaDon(), HoaDonDTO.class);

            if (hd != null) {
                ChiTietHoaDonDTO request = new ChiTietHoaDonDTO();
                request.setMaDon(hd.getMaDon());

                list = chiTietHoaDonService.getChiTietTheoMaDon(request);
            }
        }

        return list;
    }

    private boolean luuMonAnVaoCSDL(boolean hienThongBaoThanhCong) {
        if (parentGoiMonGUI == null) return false;

        BanDTO banHienTai = JsonMapper.convert(parentGoiMonGUI.getBanHienTai(), BanDTO.class);
        HoaDonDTO activeHoaDon = JsonMapper.convert(parentGoiMonGUI.getActiveHoaDon(), HoaDonDTO.class);
        DefaultTableModel model = parentGoiMonGUI.getModelChiTietHoaDon();

        if (banHienTai == null || activeHoaDon == null || activeHoaDon.getMaDon() == null) {
            if (hienThongBaoThanhCong) {
                JOptionPane.showMessageDialog(this, "Chưa có hóa đơn hợp lệ để lưu!", "Lỗi Lưu Món", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        String maDon = activeHoaDon.getMaDon();

        Map<String, Integer> itemsTrenGUI = new HashMap<>();
        Map<String, Float> donGiaTrenGUI = new HashMap<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            String maMon = (String) model.getValueAt(i, 1);
            Object soLuongObj = model.getValueAt(i, 3);
            Object donGiaObj = model.getValueAt(i, 4);

            Integer soLuong = 0;
            Float donGia = 0f;

            try {
                if (soLuongObj != null) {
                    if (soLuongObj instanceof Number) {
                        soLuong = ((Number) soLuongObj).intValue();
                    } else {
                        soLuong = Integer.parseInt(soLuongObj.toString().trim());
                    }
                }

                if (donGiaObj != null) {
                    if (donGiaObj instanceof Number) {
                        donGia = ((Number) donGiaObj).floatValue();
                    } else {
                        donGia = Float.parseFloat(donGiaObj.toString().trim());
                    }
                }
            } catch (Exception parseEx) {
                System.err.println("Lỗi chuyển đổi kiểu dữ liệu tại hàng " + i + ": " + parseEx.getMessage());
                continue;
            }

            if (maMon != null && soLuong > 0) {
                itemsTrenGUI.put(maMon, soLuong);
                donGiaTrenGUI.put(maMon, donGia);
            }
        }

        ChiTietHoaDonDTO request = new ChiTietHoaDonDTO();
        request.setMaDon(maDon);

        List<ChiTietHoaDonDTO> itemsTrongDBList = chiTietHoaDonService.getChiTietTheoMaDon(request);

        Map<String, ChiTietHoaDonDTO> itemsTrongDB = new HashMap<>();
        for (ChiTietHoaDonDTO ct : itemsTrongDBList) {
            itemsTrongDB.put(ct.getMaMonAn(), ct);
        }

        boolean coLoi = false;

        try {
            for (Map.Entry<String, Integer> entryGUI : itemsTrenGUI.entrySet()) {
                String maMonGUI = entryGUI.getKey();
                int soLuongGUI = entryGUI.getValue();

                if (!itemsTrongDB.containsKey(maMonGUI)) {
                    float donGia = donGiaTrenGUI.getOrDefault(maMonGUI, 0f);

                    if (donGia <= 0) {
                        iuh.fit.core.dto.MonAnDTO monAn = monAnService.findByIdDTO(maMonGUI);
                        if (monAn != null) {
                            donGia = monAn.getDonGia();
                        }
                    }

                    if (donGia > 0) {
                        ChiTietHoaDonDTO ctMoi = new ChiTietHoaDonDTO();
                        ctMoi.setMaDon(maDon);
                        ctMoi.setMaMonAn(maMonGUI);
                        ctMoi.setSoLuong(soLuongGUI);
                        ctMoi.setDonGia(donGia);
                        ctMoi.setThanhTien(soLuongGUI * donGia);

                        if (!chiTietHoaDonService.themChiTiet(ctMoi)) {
                            coLoi = true;
                            System.err.println("Lỗi khi thêm chi tiết: " + maMonGUI);
                        }
                    } else {
                        System.err.println("Không tìm thấy đơn giá cho món mới: " + maMonGUI);
                    }
                }
            }

            for (Map.Entry<String, ChiTietHoaDonDTO> entryDB : itemsTrongDB.entrySet()) {
                String maMonDB = entryDB.getKey();

                if (!itemsTrenGUI.containsKey(maMonDB)) {
                    ChiTietHoaDonDTO deleteDTO = new ChiTietHoaDonDTO();
                    deleteDTO.setMaDon(maDon);
                    deleteDTO.setMaMonAn(maMonDB);

                    if (!chiTietHoaDonService.xoaChiTiet(deleteDTO)) {
                        coLoi = true;
                        System.err.println("Lỗi khi xóa chi tiết: " + maMonDB);
                    }
                }
            }

            for (Map.Entry<String, Integer> entryGUI : itemsTrenGUI.entrySet()) {
                String maMonGUI = entryGUI.getKey();
                int soLuongGUI = entryGUI.getValue();

                if (itemsTrongDB.containsKey(maMonGUI)) {
                    ChiTietHoaDonDTO ctTrongDB = itemsTrongDB.get(maMonGUI);

                    if (ctTrongDB.getSoLuong() != soLuongGUI) {
                        ChiTietHoaDonDTO updateDTO = new ChiTietHoaDonDTO();
                        updateDTO.setMaDon(maDon);
                        updateDTO.setMaMonAn(maMonGUI);
                        updateDTO.setSoLuong(soLuongGUI);
                        updateDTO.setDonGia(ctTrongDB.getDonGia());

                        if (!chiTietHoaDonService.suaChiTiet(updateDTO)) {
                            coLoi = true;
                            System.err.println("Lỗi khi sửa chi tiết: " + maMonGUI);
                        }
                    }
                }
            }

            if (!coLoi) {
                float tongTienGoc = 0;

                for (int i = 0; i < model.getRowCount(); i++) {
                    Object donGiaObj = model.getValueAt(i, 4);
                    Object soLuongObj = model.getValueAt(i, 3);

                    float donGia = 0f;
                    int soLuong = 0;

                    if (donGiaObj instanceof Number) {
                        donGia = ((Number) donGiaObj).floatValue();
                    }

                    if (soLuongObj instanceof Number) {
                        soLuong = ((Number) soLuongObj).intValue();
                    }

                    tongTienGoc += donGia * soLuong;
                }

                HoaDonDTO updateTongTienDTO = new HoaDonDTO();
                updateTongTienDTO.setMaHD(activeHoaDon.getMaHD());
                updateTongTienDTO.setTongTien(tongTienGoc);

                if (!hoaDonService.capNhatTongTien(updateTongTienDTO)) {
                    coLoi = true;
                    System.err.println("Lỗi khi cập nhật tổng tiền hóa đơn!");
                }
            }

        } catch (Exception ex) {
            coLoi = true;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lưu món ăn:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }

        if (parentGoiMonGUI != null) {
            parentGoiMonGUI.updateBillPanelTotals();
        }

        if (!coLoi) {
            if (hienThongBaoThanhCong) {
                JOptionPane.showMessageDialog(this, "Đã lưu các thay đổi món ăn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình lưu món ăn.", "Lỗi Lưu Món", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void setCustomHeader(String name) {
        this.customHeaderName = name;
    }

    private JPanel createCheckoutPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel leftActionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        leftActionPanel.setOpaque(false);

        btnLuuMon = createBigButton("Lưu món (F2)", COLOR_BUTTON_BLUE);
        btnInTamTinh = createBigButton("Xem tạm tính", COLOR_BUTTON_BLUE);

        leftActionPanel.add(btnLuuMon);
        leftActionPanel.add(btnInTamTinh);
        mainPanel.add(leftActionPanel, BorderLayout.WEST);

        btnThanhToan = createBigButton("Thanh toán (F1)", COLOR_BUTTON_BLUE);
        mainPanel.add(btnThanhToan, BorderLayout.SOUTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        detailsPanel.add(createSummaryPanel());
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(createKhachTraPanel());
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(createSuggestedCashPanel());

        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private void xuatPhieuIn(
            String tieuDe,
            boolean daThanhToan,
            long tienKhachDua,
            long tienThoi,
            String maHD,
            List<ChiTietHoaDonDTO> dsMon,
            String hinhThucTT,
            String tenBanThucTe,
            String tenNV,
            String tenKH,
            String ghiChu
    ) {
        BanDTO banHienTai = null;

        if (parentGoiMonGUI != null) {
            banHienTai = JsonMapper.convert(parentGoiMonGUI.getBanHienTai(), BanDTO.class);
        } else if (parentBanGUI != null) {
            banHienTai = JsonMapper.convert(parentBanGUI.getSelectedTable(), BanDTO.class);
        }

        if (banHienTai == null || dsMon == null || dsMon.isEmpty()) return;

        String tenBanHienThi = tenBanThucTe;
        if (this.customHeaderName != null && !this.customHeaderName.isEmpty()) {
            tenBanHienThi = this.customHeaderName;
        }

        try {
            BanService banService = new BanService();
            String chuoiBanGhep = banService.getChuoiTenBanGhep(banHienTai.getMaBan(),maHD);

            if (chuoiBanGhep != null && !chuoiBanGhep.isEmpty()) {
                String khuVuc = "";
                String tenBanChinh = tenBanHienThi;

                // Nếu tên bàn có chứa dấu "--", ta sẽ chẻ nó ra làm đôi
                if (tenBanHienThi.contains("--")) {
                    int index = tenBanHienThi.indexOf("--");
                    tenBanChinh = tenBanHienThi.substring(0, index).trim(); // Lấy "Bàn 10"
                    khuVuc = " -- " + tenBanHienThi.substring(index + 2).trim(); // Lấy " -- Tầng trệt"
                }

                // Ráp lại theo đúng thứ tự: Bàn chính + Số bàn ghép + Khu vực
                tenBanHienThi = tenBanChinh + chuoiBanGhep + khuVuc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder billText = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        billText.append("===================================================\n");
        billText.append("                   ").append(tieuDe).append("\n");
        billText.append("===================================================\n");
        billText.append("Mã HĐ: ").append(maHD != null ? maHD : "---").append("\n");
        billText.append("Ngày:  ").append(LocalDateTime.now().format(dtf)).append("\n");
        billText.append("Thu ngân: ").append(tenNV).append("\n");
        billText.append("---------------------------------------------------\n");
        billText.append("Bàn:   ").append(tenBanHienThi).append("\n");
        billText.append("Khách:    ").append(tenKH).append("\n");

        String ghiChuDisplay = ghiChu;
        if (ghiChuDisplay != null && ghiChuDisplay.contains("LINKED:")) {
            ghiChuDisplay = ghiChuDisplay.substring(0, ghiChuDisplay.indexOf("LINKED:")).trim();
        }

        if (ghiChuDisplay != null && !ghiChuDisplay.isEmpty()) {
            billText.append("Ghi chú:  ").append(ghiChuDisplay).append("\n");
        }

        billText.append("---------------------------------------------------\n");
        billText.append(String.format("%-20s %5s %10s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));
        billText.append("---------------------------------------------------\n");

        for (ChiTietHoaDonDTO ct : dsMon) {
            String tenMon = ct.getTenMon() != null ? ct.getTenMon() : ct.getMaMonAn();
            String tenMonDisplay = tenMon.length() > 18 ? tenMon.substring(0, 17) + "." : tenMon;

            billText.append(String.format("%-20s %5d %10s %12s\n",
                    tenMonDisplay,
                    ct.getSoLuong(),
                    nf.format(ct.getDonGia()),
                    nf.format(ct.getThanhTien())));
        }

        billText.append("---------------------------------------------------\n");
        billText.append(String.format("%-28s %20s\n", "Tổng cộng:", safeLabelText(lblTongCong)));
        if (!safeLabelText(lblKhuyenMai).equals("0 ₫") && !safeLabelText(lblKhuyenMai).equals("0")) {
            billText.append(String.format("%-28s %20s\n", "Giảm giá:", safeLabelText(lblKhuyenMai)));
        }

        billText.append("===================================================\n");
        billText.append(String.format("%-28s %20s\n", "TỔNG THANH TOÁN:", safeLabelText(lblTongThanhToan)));

        if (daThanhToan) {
            billText.append(String.format("%-28s %20s\n", "HTTT:", hinhThucTT));
            billText.append(String.format("%-28s %20s\n", "Tiền khách đưa:", nf.format(tienKhachDua)));
            billText.append(String.format("%-28s %20s\n", "Tiền thối lại:", nf.format(tienThoi)));
            billText.append("---------------------------------------------------\n");
            billText.append("               XIN CẢM ƠN VÀ HẸN GẶP LẠI!       \n");
        } else {
            billText.append("\n(Phiếu này chỉ để kiểm tra, chưa thanh toán)\n");
        }

        billText.append("===================================================\n");

        JDialog previewDialog = new JDialog(SwingUtilities.getWindowAncestor(this), tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        previewDialog.setSize(420, 600);
        previewDialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea(billText.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> previewDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnClose);

        previewDialog.add(scrollPane, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }

    private void hienThiXemTamTinh() {
        HoaDonDTO hd = null;
        BanDTO banHienTai = null;

        if (parentGoiMonGUI != null) {
            hd = JsonMapper.convert(parentGoiMonGUI.getActiveHoaDon(), HoaDonDTO.class);
            banHienTai = JsonMapper.convert(parentGoiMonGUI.getBanHienTai(), BanDTO.class);
        } else if (parentBanGUI != null) {
            hd = JsonMapper.convert(parentBanGUI.getActiveHoaDon(), HoaDonDTO.class);
            banHienTai = JsonMapper.convert(parentBanGUI.getSelectedTable(), BanDTO.class);
        }

        if (hd != null && banHienTai != null) {
            List<ChiTietHoaDonDTO> listToPrint = getCurrentDetailList();

            String tenBanHienThi;
            if (this.customHeaderName != null && !this.customHeaderName.isEmpty()) {
                tenBanHienThi = this.customHeaderName;
            } else {
                tenBanHienThi = banHienTai.getTenBan() + " -- " + banHienTai.getKhuVuc();
            }

            String tenNV = "Admin";
            if (hd.getMaNV() != null) {
                if (nhanVienRemoteService != null) {
                    NhanVienDTO nv = nhanVienRemoteService.findById(hd.getMaNV());
                    if (nv != null) tenNV = nv.getHoTen();
                }
            }

            String tenKH = "Khách lẻ";
            if (hd.getMaKH() != null) {
                KhachHangDTO kh = khachHangService.findByIdDTO(hd.getMaKH());
                if (kh != null) tenKH = kh.getTenKH();
            }

            String ghiChuHoaDon = "";
            if (hd.getMaDon() != null) {
                DonDatMonDTO ddmRequest = new DonDatMonDTO();
                ddmRequest.setMaDon(hd.getMaDon());

                DonDatMonDTO ddm = donDatMonService.findById(ddmRequest.getMaDon());
                if (ddm != null && ddm.getGhiChu() != null) {
                    ghiChuHoaDon = ddm.getGhiChu();
                }
            }

            xuatPhieuIn(
                    "PHIẾU TẠM TÍNH",
                    false,
                    0,
                    0,
                    hd.getMaHD(),
                    listToPrint,
                    "---",
                    tenBanHienThi,
                    tenNV,
                    tenKH,
                    ghiChuHoaDon
            );
        }
    }

    private long roundUpToNearest(long number, long nearest) {
        if (nearest <= 0) return number;
        if (number % nearest == 0) return number;
        return ((number / nearest) + 1) * nearest;
    }

    private void tinhTienThoi() {
        if (txtKhachTra == null || lblTienThoi == null) return;

        try {
            long khachTra = Long.parseLong(txtKhachTra.getText().replace(",", "").replace(".", ""));
            long tienThoi = khachTra - this.currentTotal;

            NumberFormat nfNumber = NumberFormat.getInstance();
            lblTienThoi.setText(nfNumber.format(tienThoi));
            lblTienThoi.setForeground(tienThoi < 0 ? Color.RED : Color.BLUE);

        } catch (NumberFormatException ex) {
            lblTienThoi.setText("...");
        }
    }

    private void updateSuggestedCash(long total) {
        this.currentTotal = total;

        for (JButton btn : suggestedCashButtons) {
            if (btn != null) {
                btn.setVisible(false);
            }
        }

        if (total <= 0) {
            return;
        }

        long[] suggestions = new long[6];
        suggestions[0] = roundUpToNearest(total, 1000);
        suggestions[1] = roundUpToNearest(total, 50000);
        suggestions[2] = roundUpToNearest(total, 100000);
        suggestions[3] = suggestions[2] + 20000;
        suggestions[4] = suggestions[2] + 50000;
        suggestions[5] = 500000;

        LinkedHashSet<Long> uniqueSuggestions = new LinkedHashSet<>();
        for (long s : suggestions) {
            if (s >= total) {
                uniqueSuggestions.add(s);
            }
        }

        int i = 0;
        NumberFormat nfNumber = NumberFormat.getInstance();

        for (Long s : uniqueSuggestions) {
            if (i >= 6) break;

            if (suggestedCashButtons[i] != null) {
                suggestedCashButtons[i].setText(nfNumber.format(s));
                suggestedCashButtons[i].setVisible(true);
            }

            i++;
        }
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblTongCong = new JLabel("0");
        lblKhuyenMai = new JLabel("0");
        lblTongThanhToan = new JLabel("0");
        lblTongSoLuong = new JLabel("0");

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font valueFont = new Font("Segoe UI", Font.BOLD, 14);
        Font totalFont = new Font("Segoe UI", Font.BOLD, 16);

        lblTongCong.setFont(valueFont);
        lblTongCong.setHorizontalAlignment(SwingConstants.RIGHT);

        lblKhuyenMai.setFont(valueFont);
        lblKhuyenMai.setHorizontalAlignment(SwingConstants.RIGHT);

        lblTongThanhToan.setFont(totalFont);
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);

        lblTongSoLuong.setFont(valueFont);
        lblTongSoLuong.setHorizontalAlignment(SwingConstants.RIGHT);

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        JLabel lbl1 = new JLabel("Tổng cộng:");
        lbl1.setFont(labelFont);
        panel.add(lbl1, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        panel.add(lblTongSoLuong, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.5;
        panel.add(lblTongCong, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lbl2 = new JLabel("Khuyến mãi + giảm TV:");
        lbl2.setFont(labelFont);
        panel.add(lbl2, gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(""), gbc);

        gbc.gridx = 2;
        panel.add(lblKhuyenMai, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lbl4 = new JLabel("TỔNG THANH TOÁN:");
        lbl4.setFont(totalFont);
        panel.add(lbl4, gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(""), gbc);

        gbc.gridx = 2;
        panel.add(lblTongThanhToan, gbc);

        return panel;
    }

    private JPanel createKhachTraPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Khách trả:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtKhachTra = new JTextField("0", 10);
        txtKhachTra.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtKhachTra.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachTra.setMaximumSize(txtKhachTra.getPreferredSize());

        txtKhachTra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                tinhTienThoi();
            }
        });

        lblTienThoi = new JLabel("0");
        lblTienThoi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienThoi.setForeground(Color.BLUE);
        lblTienThoi.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lbl);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(txtKhachTra);
        panel.add(Box.createHorizontalGlue());
        panel.add(lblTienThoi);

        return panel;
    }

    private JPanel createSuggestedCashPanel() {
        suggestedCashPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        suggestedCashPanel.setOpaque(false);

        for (int i = 0; i < 6; i++) {
            JButton btn = new JButton("...");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBackground(COLOR_BUTTON_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setVisible(false);

            btn.addActionListener(e -> {
                String buttonText = ((JButton) e.getSource()).getText();
                txtKhachTra.setText(buttonText.replace(",", "").replace(".", ""));
                tinhTienThoi();
            });

            suggestedCashButtons[i] = btn;
            suggestedCashPanel.add(btn);
        }

        return suggestedCashPanel;
    }

    private JButton createBigButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 60));
        return btn;
    }

    public void loadBillTotals(long tongCong, long khuyenMai, long tongThanhToan, int tongSoLuong) {
        if (!isSummaryReady()) {
            return;
        }

        lblTongSoLuong.setText(String.valueOf(tongSoLuong));
        lblTongCong.setText(nf.format(tongCong));
        lblKhuyenMai.setText(nf.format(khuyenMai));
        lblTongThanhToan.setText(nf.format(tongThanhToan));

        updateSuggestedCash(tongThanhToan);
        tinhTienThoi();

        if (tongThanhToan == 0 && txtKhachTra != null) {
            txtKhachTra.setText("0");
            tinhTienThoi();
        }
    }

    public void clearBill() {
        if (!isSummaryReady()) {
            return;
        }

        lblTongSoLuong.setText("0");
        lblTongCong.setText(nf.format(0));
        lblKhuyenMai.setText(nf.format(0));
        lblTongThanhToan.setText(nf.format(0));

        if (lblTienThoi != null) {
            lblTienThoi.setText(nf.format(0));
        }

        if (txtKhachTra != null) {
            txtKhachTra.setText("0");
        }

        updateSuggestedCash(0);
    }

    private boolean isSummaryReady() {
        return lblTongSoLuong != null
                && lblTongCong != null
                && lblKhuyenMai != null
                && lblTongThanhToan != null;
    }

    private String safeLabelText(JLabel label) {
        return label == null ? "" : label.getText();
    }
}
