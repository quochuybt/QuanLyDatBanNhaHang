package gui;

import dao.*;
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.TableColumn;

public class ManHinhGoiMonGUI extends JPanel {
    private Ban banHienTai;
    private HoaDonDAO hoaDonDAO_GoiMon;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // 🌟 BIẾN MỚI: LƯU MÃ NV ĐANG ĐĂNG NHẬP
    private final String maNVDangNhap;

    private DanhSachBanGUI parentDanhSachBanGUI_GoiMon;
    // Panel bên trái
    private MonAnDAO monAnDAO;
    private List<MonAn> dsMonAnFull;
    private List<MonAnItemPanel> dsMonAnPanel;
    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private String currentCategoryFilter = "Tất cả";
    private JLabel statusColorBox;
    private DonDatMonDAO donDatMonDAO;
    private BanDAO banDAO;
    private ChiTietHoaDonDAO chiTietDAO;
    private KhachHangDAO khachHangDAO;
    private KhuyenMaiDAO maKhuyenMaiDAO;

    // Panel bên phải
    private JLabel lblTenBanHeader;
    private JTable tblChiTietHoaDon;
    private DefaultTableModel modelChiTietHoaDon;
    private BillPanel billPanel;

    public ManHinhGoiMonGUI(DanhSachBanGUI parent) {
        this(parent, "NV_UNKNOWN"); // Dùng giá trị mặc định nếu không truyền
    }

    public ManHinhGoiMonGUI(DanhSachBanGUI parent, String maNVDangNhap) { // 🌟 CONSTRUCTOR MỚI
        super(new BorderLayout());
        this.parentDanhSachBanGUI_GoiMon = parent;
        this.maNVDangNhap = maNVDangNhap; // 🌟 LƯU MÃ NV
        this.monAnDAO = new MonAnDAO();
        this.dsMonAnFull = new ArrayList<>();
        this.hoaDonDAO_GoiMon = new HoaDonDAO();
        this.donDatMonDAO = new DonDatMonDAO();
        this.banDAO = new BanDAO();
        this.dsMonAnPanel = new ArrayList<>();
        this.chiTietDAO = new ChiTietHoaDonDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.maKhuyenMaiDAO = new KhuyenMaiDAO();

        buildUI();
        loadDataFromDB();
        xoaThongTinGoiMon();
    }
    private String phatSinhMaHD() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        String datePart = LocalDateTime.now().format(formatter);
        java.util.concurrent.ThreadLocalRandom current = java.util.concurrent.ThreadLocalRandom.current();
        int randomPart = current.nextInt(1000, 10000); // 4 chữ số ngẫu nhiên
        return "HD" + datePart + randomPart;
    }
    public DanhSachBanGUI getParentDanhSachBanGUI() {
        return parentDanhSachBanGUI_GoiMon;
    }
    public boolean loadDuLieuBan(Ban banDuocChon) {
        System.out.println("loadDuLieuBan được gọi cho: " + banDuocChon.getTenBan() + " - Trạng thái: " + banDuocChon.getTrangThai());
        this.banHienTai = banDuocChon;

        // 1. Cập nhật Header và Màu sắc
        lblTenBanHeader.setText(banDuocChon.getTenBan() + " - " + banDuocChon.getKhuVuc());
        Color statusColor;
        switch (banDuocChon.getTrangThai()) {
            case TRONG: statusColor = ManHinhBanGUI.COLOR_STATUS_FREE; break;
            case DA_DAT_TRUOC: statusColor = ManHinhBanGUI.COLOR_STATUS_RESERVED; break;
            case DANG_PHUC_VU: default: statusColor = ManHinhBanGUI.COLOR_STATUS_OCCUPIED; break;
        }
        statusColorBox.setBackground(statusColor);

        // 2. Xóa chi tiết đơn hàng cũ trên bảng
        modelChiTietHoaDon.setRowCount(0);

        HoaDon activeHoaDon = null;
        boolean requireBanRefresh = false;

        try {
            if (banDuocChon.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                // --- BÀN ĐANG PHỤC VỤ: Tải hóa đơn hiện có ---
                activeHoaDon = hoaDonDAO_GoiMon.getHoaDonChuaThanhToan(banDuocChon.getMaBan());
                if (activeHoaDon == null) {
                    System.err.println("Lỗi logic: Bàn ĐPV nhưng không có HĐ!");
                    JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy hóa đơn hiện tại.", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
                    updateBillPanelTotals();
                    return false;
                }
                System.out.println("Đang tải hóa đơn: " + activeHoaDon.getMaHD());

            } else if (banDuocChon.getTrangThai() == TrangThaiBan.TRONG) {
                // --- BÀN TRỐNG: Hỏi và Mở bàn mới ---
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có muốn mở bàn '" + banDuocChon.getTenBan() + "' cho khách không?",
                        "Xác nhận mở bàn", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    // --- Code xử lý MỞ BÀN (update Ban, them DDM, them HD) ---
                    // 🌟 SỬA: Dùng maNVDangNhap đã lưu
                    String maNV_LoggedIn = this.maNVDangNhap;

                    banDuocChon.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
                    banDuocChon.setGioMoBan(LocalDateTime.now());
                    if (!banDAO.updateBan(banDuocChon)) throw new Exception("Lỗi cập nhật trạng thái bàn!");
                    requireBanRefresh = true;

                    DonDatMon ddmMoi = new DonDatMon();
                    ddmMoi.setNgayKhoiTao(LocalDateTime.now());
                    ddmMoi.setMaNV(maNV_LoggedIn); // 🌟 GÁN MA NV ĐANG ĐĂNG NHẬP
                    ddmMoi.setMaBan(banDuocChon.getMaBan());
                    if (!donDatMonDAO.themDonDatMon(ddmMoi)) throw new Exception("Lỗi tạo đơn đặt món mới!");

                    String newMaHD = phatSinhMaHD();

                    // 🌟 GÁN MA NV ĐANG ĐĂNG NHẬP
                    HoaDon hdMoi = new HoaDon(newMaHD, LocalDateTime.now(), "Chưa thanh toán", "Tiền mặt", ddmMoi.getMaDon(), maNV_LoggedIn, null);

                    hdMoi.setMaKH(null);
                    hdMoi.setTongTienTuDB(0);
                    if (!hoaDonDAO_GoiMon.themHoaDon(hdMoi)) throw new Exception("Lỗi tạo hóa đơn mới!");

                    activeHoaDon = hoaDonDAO_GoiMon.getHoaDonTheoMaDon(ddmMoi.getMaDon());
                    if(activeHoaDon == null){ activeHoaDon = hdMoi; }
                    statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_OCCUPIED);
                } else {
                    System.out.println("Người dùng không muốn mở bàn.");
                    updateBillPanelTotals();
                    return false;
                }

            } else if (banDuocChon.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                // --- BÀN ĐÃ ĐẶT: Hỏi và Nhận bàn ---
                DonDatMon ddmDaDat = donDatMonDAO.getDonDatMonDatTruoc(banDuocChon.getMaBan());
                String tenKH = "Khách vãng lai";
                String gioDenStr = "chưa rõ";
                if (ddmDaDat != null) {
                    if (ddmDaDat.getMaKH() != null && khachHangDAO != null) {
                        KhachHang kh = khachHangDAO.timTheoMaKH(ddmDaDat.getMaKH());
                        if (kh != null) {
                            tenKH = kh.getTenKH();
                        }
                    }
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM");
                    if (ddmDaDat.getThoiGianDen() != null) {
                        // Lấy từ thoiGianDen (Chính xác nhất)
                        gioDenStr = ddmDaDat.getThoiGianDen().format(dtf);
                    } else if (ddmDaDat.getNgayKhoiTao() != null) {
                        // Fallback dữ liệu cũ
                        gioDenStr = ddmDaDat.getNgayKhoiTao().format(dtf);
                    } else if (banDuocChon.getGioMoBan() != null) {
                        // Fallback cuối cùng lấy từ Bàn
                        gioDenStr = banDuocChon.getGioMoBan().format(dtf);
                    }
                } else {
                    System.err.println("Lỗi: Bàn " + banDuocChon.getMaBan() + " DA_DAT_TRUOC nhưng không tìm thấy DonDatMon tương ứng!");
                    JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy thông tin đặt bàn!", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
                    updateBillPanelTotals();
                    return false;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bàn '" + banDuocChon.getTenBan() + "' đã được đặt trước lúc " + gioDenStr + ".\nBạn có muốn nhận bàn này không?",
                        "Xác nhận nhận bàn",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (ddmDaDat == null) throw new Exception("Lỗi: Không tìm thấy đơn đặt món!");
                    banDuocChon.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
                    banDuocChon.setGioMoBan(LocalDateTime.now());
                    if (!banDAO.updateBan(banDuocChon)) throw new Exception("Lỗi cập nhật trạng thái bàn!");
                    requireBanRefresh = true;
                    String newMaHD = phatSinhMaHD();

                    // Logic này dùng maNV của người tạo đơn đặt (ddmDaDat.getMaNV()), giữ nguyên vì hợp lý
                    HoaDon hdMoi = new HoaDon(newMaHD, LocalDateTime.now(), "Chưa thanh toán", "Tiền mặt", ddmDaDat.getMaDon(), ddmDaDat.getMaNV(), null);
                    hdMoi.setMaKH(ddmDaDat.getMaKH());
                    hdMoi.setTongTienTuDB(0);
                    if (!hoaDonDAO_GoiMon.themHoaDon(hdMoi)) throw new Exception("Lỗi tạo hóa đơn mới!");

                    activeHoaDon = hoaDonDAO_GoiMon.getHoaDonTheoMaDon(ddmDaDat.getMaDon());
                    if(activeHoaDon == null){ activeHoaDon = hdMoi;}
                    statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_OCCUPIED);
                } else {
                    System.out.println("Người dùng không muốn nhận bàn đặt.");
                    updateBillPanelTotals();
                    return false;
                }
            }

            // 4. Tải chi tiết món ăn nếu có activeHoaDon
            if (activeHoaDon != null) {
                List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(activeHoaDon.getMaDon());
                if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                    activeHoaDon.setDsChiTiet(dsChiTiet);
                    for (ChiTietHoaDon ct : dsChiTiet) {
                        Object[] rowData = { "X", ct.getMaMon(), ct.getTenMon(), Integer.valueOf(ct.getSoluong()), ct.getDongia(), ct.getThanhtien() };
                        modelChiTietHoaDon.addRow(rowData);
                    }
                } else {
                    System.out.println("Hóa đơn " + activeHoaDon.getMaHD() + " chưa có món nào.");
                    if (activeHoaDon.getDsChiTiet() == null || !activeHoaDon.getDsChiTiet().isEmpty()) {
                        activeHoaDon.setDsChiTiet(new ArrayList<>());
                    }
                }
            } else {
                System.out.println("Không có hóa đơn nào đang hoạt động.");
                activeHoaDon = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            xoaThongTinGoiMon();
            requireBanRefresh = true;
            updateBillPanelTotals();
            return false;
        } finally {
            // Cập nhật BillPanel cuối cùng
            updateBillPanelTotals();
        }

        if (requireBanRefresh && parentDanhSachBanGUI_GoiMon != null) {
            parentDanhSachBanGUI_GoiMon.refreshManHinhBan();
        }
        return true;
    }

    public void xoaThongTinGoiMon() {
        lblTenBanHeader.setText("Chưa chọn bàn");
        modelChiTietHoaDon.setRowCount(0);
        billPanel.clearBill();
        this.banHienTai = null;
        if (statusColorBox != null) {
            statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        }
    }

    private void addMonAnToOrder(MonAn monAn) {
        if (banHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi gọi món!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maMon = monAn.getMaMonAn();
        String tenMon = monAn.getTenMon();
        float donGia = monAn.getDonGia();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            String maMonTrongBang = (String) modelChiTietHoaDon.getValueAt(i, 1);
            if (maMon.equals(maMonTrongBang)) {
                int soLuongHienTai = (int) modelChiTietHoaDon.getValueAt(i, 3);
                int soLuongMoi = soLuongHienTai + 1;
                modelChiTietHoaDon.setValueAt(soLuongMoi, i, 3);
                float thanhTienMoi = soLuongMoi * donGia;
                modelChiTietHoaDon.setValueAt(thanhTienMoi, i, 5);
                updateBillPanelTotals();
                return;
            }
        }

        Object[] rowData = {
                "X",
                maMon,
                tenMon,
                Integer.valueOf(1),
                donGia,
                donGia
        };
        modelChiTietHoaDon.addRow(rowData);
        updateBillPanelTotals();
    }
    private List<ChiTietHoaDon> layChiTietTuTable() {
        List<ChiTietHoaDon> dsChiTiet = new ArrayList<>();
        HoaDon currentHD = getActiveHoaDon(); // Lấy HĐ hiện tại để lấy maDon

        String maDon = null;
        if (currentHD != null) {
            maDon = currentHD.getMaDon();
        }

        // Nếu không có mã đơn (ví dụ: bàn chưa mở), không thể tạo chi tiết
        if (maDon == null) {
            System.err.println("layChiTietTuTable: Không tìm thấy maDon, không thể tạo List ChiTietHoaDon.");
            return dsChiTiet; // Trả về danh sách rỗng
        }

        // Lặp qua các dòng trong JTable
        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            try {
                // Lấy dữ liệu từ các cột (dựa trên thứ tự bạn đã định nghĩa)
                // 0: "X"
                String maMon = (String) modelChiTietHoaDon.getValueAt(i, 1);    // Cột 1: Mã Món
                String tenMon = (String) modelChiTietHoaDon.getValueAt(i, 2);   // Cột 2: Tên Món
                Integer soLuong = (Integer) modelChiTietHoaDon.getValueAt(i, 3); // Cột 3: SL
                Float donGia = (Float) modelChiTietHoaDon.getValueAt(i, 4);   // Cột 4: Đơn Giá

                // Kiểm tra null (dù getColumnClass đã định nghĩa)
                if (maMon != null && tenMon != null && soLuong != null && donGia != null) {
                    // Tạo đối tượng ChiTietHoaDon
                    // Giả sử ChiTietHoaDon có constructor (maDon, maMon, tenMon, soLuong, donGia)
                    // (Vì ChiTietHoaDonDAO của bạn cũng dùng constructor này)
                    ChiTietHoaDon ct = new ChiTietHoaDon(maDon, maMon, tenMon, soLuong.intValue(), donGia.floatValue());

                    // Hàm tạo ChiTietHoaDon đã tự động gọi tinhThanhTien() bên trong

                    dsChiTiet.add(ct);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi khi đọc dữ liệu từ JTable hàng " + i + ": " + e.getMessage());
                // Có thể bỏ qua hàng này hoặc ném lỗi
            }
        }
        System.out.println("layChiTietTuTable: Đã tạo được list " + dsChiTiet.size() + " chi tiết."); // Debug
        return dsChiTiet;
    }
    public void updateBillPanelTotals() {
        // Lấy hóa đơn hiện tại
        HoaDon currentHD = getActiveHoaDon(); // Dùng hàm getter đã có

        if (currentHD != null) {
            // 1. Cập nhật danh sách chi tiết trong HĐ từ bảng
            currentHD.setDsChiTiet(layChiTietTuTable()); // Hàm này lấy dữ liệu từ modelChiTietHoaDon

            // 2. Gọi hàm tính toán mới trong HoaDon (truyền DAO vào)
            currentHD.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);

            // 3. Lấy tổng số lượng từ dsChiTiet đã cập nhật
            int tongSoLuong = 0;
            if(currentHD.getDsChiTiet() != null){
                for(ChiTietHoaDon ct : currentHD.getDsChiTiet()) {
                    tongSoLuong += ct.getSoluong();
                }
            }

            // 4. Cập nhật BillPanel với các giá trị đã tính toán từ HoaDon
            billPanel.loadBillTotals(
                    (long) currentHD.getTongTien(),
                    (long) currentHD.getGiamGia(),
                    (long) currentHD.getVat(),
                    (long) currentHD.getTongThanhToan(),
                    tongSoLuong
            );
        } else {
            // Không có hóa đơn -> Xóa trắng BillPanel
            billPanel.clearBill();
        }
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        // 1. Panel bên trái (Menu)
        JPanel pnlLeft = createMenuPanel();

        // 2. Panel bên phải (Hóa đơn)
        JPanel pnlRight = createOrderPanel();

        // 3. Tạo JSplitPane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                pnlLeft,
                pnlRight
        );
        splitPane.setDividerLocation(520);
        splitPane.setBorder(null);

        this.add(splitPane, BorderLayout.CENTER);
    }
    private void loadDataFromDB() {
        // 1. Tải danh sách từ DAO
        this.dsMonAnFull = monAnDAO.getAllMonAn();
        System.out.println("Đã tải " + dsMonAnFull.size() + " món ăn từ CSDL.");

        // 2. Tạo các Panel Item và thêm vào container
        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        if (dsMonAnFull.isEmpty()) {
            pnlMenuItemContainer.add(new JLabel("Không có món ăn nào trong CSDL."));
        } else {
            for (MonAn mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

                // --- THÊM SỰ KIỆN CLICK VÀO MÓN ĂN ---
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Chỉ xử lý click chuột trái
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            System.out.println("Clicked on: " + itemPanel.getMonAn().getTenMon());
                            addMonAnToOrder(itemPanel.getMonAn());
                        }
                    }
                });
                // ----------------------------------------

                dsMonAnPanel.add(itemPanel);
                pnlMenuItemContainer.add(itemPanel);
            }
        }

        // Lọc hiển thị theo danh mục mặc định ban đầu
        filterMonAn();

        // Cập nhật lại giao diện panel cuộn
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }
    private void filterMonAn() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        System.out.println("Filtering: Category='" + currentCategoryFilter + "', Keyword='" + tuKhoa + "'");

        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true;

            // 1. Lọc theo Danh mục (currentCategoryFilter là mã DM)
            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) {
                    show = false;
                }
            }

            // 2. Lọc theo Từ khóa (chỉ lọc nếu show vẫn là true)
            if (show && !tuKhoa.isEmpty()) {
                if (!mon.getTenMon().toLowerCase().contains(tuKhoa)) {
                    show = false;
                }
            }

            itemPanel.setVisible(show);
        }
        // Cập nhật lại layout sau khi ẩn/hiện
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    /**
     * Tạo Panel Menu bên trái
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // 1. NORTH: Bộ lọc (Category + Search)
        JPanel pnlFilter = new JPanel(new BorderLayout(0, 5));
        pnlFilter.setOpaque(false);
        pnlFilter.add(createCategoryFilterPanel(), BorderLayout.NORTH);
        pnlFilter.add(createSearchPanel(), BorderLayout.SOUTH);
        panel.add(pnlFilter, BorderLayout.NORTH);

        // 2. CENTER: Danh sách món ăn
        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlMenuItemContainer.setBackground(Color.WHITE);
        pnlMenuItemContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(pnlMenuItemContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo Panel Hóa đơn bên phải
     */
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));
        panel.setBackground(Color.WHITE);

        // 1. NORTH: Header (Tên bàn)
        panel.add(createOrderHeaderPanel(), BorderLayout.NORTH);

        // 2. SOUTH: Panel thanh toán (Tái sử dụng BillPanel)
        this.billPanel = new BillPanel(this);
        panel.add(billPanel, BorderLayout.SOUTH);

        // 3. CENTER: Bảng chi tiết hóa đơn
        String[] cols = {"X", "Mã Món", "Tên món", "SL", "Đơn giá", "Thành tiền"};
        modelChiTietHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0|| column == 3;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return String.class;
                    case 1: return String.class;
                    case 2: return String.class;
                    case 3: return Integer.class;
                    case 4: return Float.class;
                    case 5: return Float.class;
                    default: return Object.class;
                }
            }
        };
        tblChiTietHoaDon = new JTable(modelChiTietHoaDon);
        TableColumn columnX = tblChiTietHoaDon.getColumnModel().getColumn(0);
        columnX.setCellRenderer(new ButtonRenderer());
        columnX.setCellEditor(new ButtonEditor(new JCheckBox()));

        TableColumn columnSL = tblChiTietHoaDon.getColumnModel().getColumn(3);
        columnSL.setCellRenderer(new SpinnerRenderer());
        columnSL.setCellEditor(new SpinnerEditor());

        // Cấu hình cột
        tblChiTietHoaDon.setRowHeight(30);

        TableColumn colMaMon = tblChiTietHoaDon.getColumnModel().getColumn(1);
        colMaMon.setMinWidth(0);
        colMaMon.setMaxWidth(0);
        colMaMon.setPreferredWidth(0);

        tblChiTietHoaDon.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblChiTietHoaDon.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblChiTietHoaDon.getColumnModel().getColumn(3).setPreferredWidth(50);
        tblChiTietHoaDon.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblChiTietHoaDon.getColumnModel().getColumn(5).setPreferredWidth(90);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = nf.format(((Number) value).doubleValue());
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        tblChiTietHoaDon.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        tblChiTietHoaDon.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        JScrollPane scrollPane = new JScrollPane(tblChiTietHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public DefaultTableModel getModelChiTietHoaDon() {
        return modelChiTietHoaDon;
    }

    public Ban getBanHienTai() {
        return banHienTai;
    }
    public HoaDon getActiveHoaDon() {
        if (banHienTai != null && banHienTai.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            return hoaDonDAO_GoiMon.getHoaDonChuaThanhToan(banHienTai.getMaBan());
        }
        return null;
    }

    // 🌟 HÀM GETTER MỚI CHO MA_NV
    public String getMaNVDangNhap() {
        return maNVDangNhap;
    }

    private JPanel createCategoryFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();

        String[][] categories = {
                {"Tất cả", "Tất cả"},
                {"DM0001", "Món ăn"},
                {"DM0002", "Giải khát"},
                {"DM0003", "Rượu vang"}
        };

        ActionListener filterListener = e -> {
            String selectedCategory = e.getActionCommand();
            currentCategoryFilter = selectedCategory;
            filterMonAn();
        };

        for (int i = 0; i < categories.length; i++) {
            String maDM = categories[i][0];
            String tenDM = categories[i][1];

            JToggleButton button = createFilterButton(tenDM, i == 0);
            button.setActionCommand(maDM);
            button.addActionListener(filterListener);
            group.add(button);
            filterPanel.add(button);
        }
        return filterPanel;
    }
    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        if (selected) {
            button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(4, 14, 4, 14)
            ));
        }
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)
                ));
            }
        });
        button.setSelected(selected);
        return button;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        panel.add(searchIcon, BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setPreferredSize(new Dimension(0, 35));

        // Thêm sự kiện gõ phím để lọc
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterMonAn();
            }
        });

        panel.add(txtTimKiem, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrderHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // --- TẠO Ô MÀU ---
        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48));
        statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        statusColorBox.setOpaque(true);
        // --- KẾT THÚC TẠO Ô MÀU ---

        // Tên bàn
        lblTenBanHeader = new JLabel("Chưa chọn bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));

        panel.add(statusColorBox, BorderLayout.WEST);
        panel.add(lblTenBanHeader, BorderLayout.CENTER);

        return panel;
    }
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.RED);
            setBackground(Color.WHITE);
            setBorder(null);
            setText("X");
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int editingRow;
        private JTable table;


        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.RED);
            button.setBackground(Color.WHITE);
            button.setBorder(null);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && table != null) {
                final DefaultTableModel finalModel = (DefaultTableModel) table.getModel();
                final int rowToRemove = editingRow;

                SwingUtilities.invokeLater(() -> {

                    if (rowToRemove >= 0 && rowToRemove < finalModel.getRowCount()) {
                        finalModel.removeRow(rowToRemove);
                        updateBillPanelTotals();
                    } else {
                        System.err.println("ButtonEditor (invokeLater): Lỗi index dòng khi xóa: " + rowToRemove);
                    }
                });
            }
            isPushed = false;
            editingRow = -1;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            boolean stopped = super.stopCellEditing();
            editingRow = -1;
            return stopped;
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // --- Inner class cho Spinner Số lượng ---
    class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super(new SpinnerNumberModel(1, 1, 100, 1));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setValue(value);
            } else {
                setValue(1);
            }
            return this;
        }
    }

    class SpinnerEditor extends DefaultCellEditor {
        JSpinner spinner;
        JSpinner.DefaultEditor editor;
        JTextField textField;
        boolean valueSet;
        private int editingRow = -1;
        private JTable table;


        public SpinnerEditor() {
            super(new JTextField());

            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            editor = ((JSpinner.DefaultEditor) spinner.getEditor());
            textField = editor.getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBorder(null);
            spinner.setBorder(null);

            spinner.addChangeListener(e -> {
                if (table != null && editingRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();

                    if (editingRow < model.getRowCount()) {
                        int currentQuantity = (Integer) spinner.getValue();
                        float donGia = (Float) model.getValueAt(editingRow, 4);
                        float thanhTienMoi = currentQuantity * donGia;

                        SwingUtilities.invokeLater(() -> {

                            if (editingRow < model.getRowCount()) {
                                model.setValueAt(thanhTienMoi, editingRow, 5);
                                updateBillPanelTotals();
                            }
                        });
                    }
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;

            spinner.setValue(value);
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public boolean stopCellEditing() {
            try {

                editor.commitEdit();
                spinner.commitEdit();
            } catch (java.text.ParseException e) {

                Object oldValue = spinner.getValue();
                spinner.setValue(oldValue);

            }
            editingRow = -1;
            return super.stopCellEditing();
        }
    }
}