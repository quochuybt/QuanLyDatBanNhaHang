package iuh.fit.gui;

import iuh.fit.core.dto.*;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.net.client.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ManHinhGoiMonGUI extends JPanel {

    private final MonAnRemoteService monAnRemoteService;
    private final BanRemoteService banRemoteService;
    private final HoaDonGoiMonRemoteService hoaDonRemoteService;
    private final ChiTietHoaDonRemoteService chiTietHoaDonRemoteService;
    private final DonDatMonRemoteService donDatMonRemoteService;
    private final KhachHangRemoteService khachHangRemoteService;

    private BanDTO banHienTai;
    private HoaDonDTO activeHoaDon;

    private final String maNVDangNhap;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private DanhSachBanGUI parentDanhSachBanGUI;
    private List<MonAnDTO> dsMonAnFull;
    private List<MonAnItemPanel> dsMonAnPanel;
    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private String currentCategoryFilter = "Tất cả";
    private JLabel statusColorBox;
    private JLabel lblTenBanHeader;
    private JPanel pnlFilterButtons;

    private JTable tblChiTietHoaDon;
    private DefaultTableModel modelChiTietHoaDon;
    private BillPanel billPanel;

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);

    private String maKHDangChon;

    public void setMaKHDangChon(String maKHDangChon) {
        this.maKHDangChon = maKHDangChon;
    }

    public ManHinhGoiMonGUI(DanhSachBanGUI parent, String maNVDangNhap) {
        this(parent, maNVDangNhap, parent != null ? parent.getConnection() : null);
    }

    public ManHinhGoiMonGUI(
            DanhSachBanGUI parent,
            String maNVDangNhap,
            SocketClientConnection connection
    ) {
        super(new BorderLayout());

        this.parentDanhSachBanGUI = parent;
        this.maNVDangNhap = maNVDangNhap;

        Objects.requireNonNull(connection, "SocketClientConnection không được null.");

        this.monAnRemoteService = new MonAnRemoteService(connection);
        this.banRemoteService = new BanRemoteService(connection);
        this.hoaDonRemoteService = new HoaDonGoiMonRemoteService(connection);
        this.chiTietHoaDonRemoteService = new ChiTietHoaDonRemoteService(connection);
        this.donDatMonRemoteService = new DonDatMonRemoteService(connection);
        this.khachHangRemoteService = new KhachHangRemoteService(connection);

        this.dsMonAnFull = new ArrayList<>();
        this.dsMonAnPanel = new ArrayList<>();

        buildUI();
        loadDataFromDB();
        xoaThongTinGoiMon();
    }

    public DanhSachBanGUI getParentDanhSachBanGUI() {
        return parentDanhSachBanGUI;
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        JPanel pnlLeft = createMenuPanel();
        JPanel pnlRight = createOrderPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlLeft, pnlRight);
        splitPane.setDividerLocation(650);
        splitPane.setBorder(null);

        this.add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel pnlFilter = new JPanel(new BorderLayout(0, 5));
        pnlFilter.setOpaque(false);
        pnlFilter.add(createCategoryFilterPanel(), BorderLayout.NORTH);
        pnlFilter.add(createSearchPanel(), BorderLayout.SOUTH);
        panel.add(pnlFilter, BorderLayout.NORTH);

        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
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

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(createOrderHeaderPanel(), BorderLayout.NORTH);

        this.billPanel = new BillPanel(this);
        panel.add(billPanel, BorderLayout.SOUTH);

        String[] cols = {"X", "Mã Món", "Tên món", "SL", "Đơn giá", "Thành tiền"};
        modelChiTietHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class;
                if (columnIndex == 4 || columnIndex == 5) return Float.class;
                return String.class;
            }
        };

        tblChiTietHoaDon = new JTable(modelChiTietHoaDon);

        TableColumn columnX = tblChiTietHoaDon.getColumnModel().getColumn(0);
        columnX.setCellRenderer(new ButtonRenderer());
        columnX.setCellEditor(new ButtonEditor(new JCheckBox()));

        TableColumn columnSL = tblChiTietHoaDon.getColumnModel().getColumn(3);
        columnSL.setCellRenderer(new SpinnerRenderer());
        columnSL.setCellEditor(new SpinnerEditor());

        tblChiTietHoaDon.setRowHeight(30);

        TableColumn colMaMon = tblChiTietHoaDon.getColumnModel().getColumn(1);
        colMaMon.setMinWidth(0);
        colMaMon.setMaxWidth(0);
        colMaMon.setPreferredWidth(0);

        tblChiTietHoaDon.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblChiTietHoaDon.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblChiTietHoaDon.getColumnModel().getColumn(3).setPreferredWidth(50);

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
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

    private JPanel createOrderHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48));
        statusColorBox.setBackground(Color.GREEN);
        statusColorBox.setOpaque(true);

        lblTenBanHeader = new JLabel("Chưa chọn bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));

        panel.add(statusColorBox, BorderLayout.WEST);
        panel.add(lblTenBanHeader, BorderLayout.CENTER);

        return panel;
    }

    private void loadDataFromDB() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<List<MonAnDTO>, Void>() {
            @Override
            protected List<MonAnDTO> doInBackground() {
                return monAnRemoteService.findAll();
            }

            @Override
            protected void done() {
                try {
                    dsMonAnFull = get();
                    if (dsMonAnFull == null) {
                        dsMonAnFull = new ArrayList<>();
                    }

                    renderMonAnList();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ManHinhGoiMonGUI.this,
                            "Lỗi tải danh sách món ăn qua socket: " + getRootMessage(e),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void renderMonAnList() {
        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        if (dsMonAnFull == null || dsMonAnFull.isEmpty()) {
            pnlMenuItemContainer.add(new JLabel("Không có món ăn nào trong CSDL."));
        } else {
            for (MonAnDTO mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            addMonAnToOrder(itemPanel.getMonAn());
                        }
                    }
                });

                dsMonAnPanel.add(itemPanel);
                pnlMenuItemContainer.add(itemPanel);
            }
        }

        renderCategoryButtons();
        filterMonAn();

        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    public boolean loadDuLieuBan(BanDTO banDuocChon) {
        if (banDuocChon == null) return false;

        this.banHienTai = banDuocChon;
        lblTenBanHeader.setText(banDuocChon.getTenBan());
        modelChiTietHoaDon.setRowCount(0);
        this.activeHoaDon = null;

        boolean requireBanRefresh = false;

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            String trangThai = banDuocChon.getTrangThai() != null
                    ? banDuocChon.getTrangThai().toString()
                    : "TRONG";

            if ("DANG_PHUC_VU".equalsIgnoreCase(trangThai)) {
                statusColorBox.setBackground(Color.RED);

                activeHoaDon = hoaDonRemoteService.getHoaDonChuaThanhToan(banDuocChon.getMaBan());

                if (activeHoaDon == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Lỗi dữ liệu: Bàn đang phục vụ nhưng chưa có hóa đơn.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return false;
                }

                List<ChiTietHoaDonDTO> dsChiTiet =
                        chiTietHoaDonRemoteService.getChiTietTheoMaDon(activeHoaDon.getMaDon());

                if (dsChiTiet != null) {
                    for (ChiTietHoaDonDTO ct : dsChiTiet) {
                        modelChiTietHoaDon.addRow(new Object[]{
                                "X",
                                ct.getMaMonAn(),
                                ct.getTenMon(),
                                ct.getSoLuong(),
                                ct.getDonGia(),
                                ct.getThanhTien()
                        });
                    }
                }
            } else if ("TRONG".equalsIgnoreCase(trangThai)) {
                statusColorBox.setBackground(Color.GREEN);

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Bạn có muốn mở bàn '" + banDuocChon.getTenBan() + "' cho khách không?",
                        "Mở bàn mới",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    moBanMoi(banDuocChon);
                    requireBanRefresh = true;
                } else {
                    return false;
                }
            } else if ("DA_DAT_TRUOC".equalsIgnoreCase(trangThai)) {
                statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_RESERVED);

                DonDatMonDTO ddmPreview = donDatMonRemoteService.getDonDatMonDatTruoc(banDuocChon.getMaBan());

                String msg = "Bàn '" + banDuocChon.getTenBan()
                        + "' đã được đặt trước.\nBạn có muốn nhận bàn này không?";

                if (ddmPreview != null) {
                    String tenKhach = "Khách";

                    if (ddmPreview.getMaKH() != null) {
                        KhachHangDTO kh = findKhachHangByMa(ddmPreview.getMaKH());

                        if (kh != null && kh.getTenKH() != null && !kh.getTenKH().trim().isEmpty()) {
                            tenKhach = kh.getTenKH();
                        }
                    }

                    msg = "Bàn '" + banDuocChon.getTenBan()
                            + "' đặt bởi " + tenKhach
                            + ".\nNhận bàn ngay?";
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        msg,
                        "Nhận bàn đặt",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    nhanBanDatTruoc(banDuocChon);
                    requireBanRefresh = true;
                } else {
                    return false;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi tải dữ liệu bàn qua socket: " + getRootMessage(ex),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        } finally {
            updateBillPanelTotals();
            setCursor(Cursor.getDefaultCursor());
        }

        if (requireBanRefresh && parentDanhSachBanGUI != null) {
            parentDanhSachBanGUI.refreshManHinhBan();
        }

        return true;
    }

    private DonDatMonDTO timDonDatTruocTheoMaBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }

        try {
            DonDatMonDTO ddm = donDatMonRemoteService.getDonDatMonChuaNhanTheoMaBanBaoGomLinked(maBan);

            if (ddm != null) {
                return ddm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DonDatMonDTO ddm = donDatMonRemoteService.getDonDatMonDatTruoc(maBan);

            if (ddm != null) {
                return ddm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String layMaBanChinhTuGhiChu(String ghiChu) {
        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            return null;
        }

        int index = ghiChu.indexOf("LINKED:");

        if (index < 0) {
            return null;
        }

        String linkedPart = ghiChu.substring(index + "LINKED:".length()).trim();

        if (linkedPart.isEmpty()) {
            return null;
        }

        String[] parts = linkedPart.split("\\s+");
        return parts[0].trim();
    }

    private String layMaBanChinhCuaNhom(DonDatMonDTO ddmDangChon, String maBanDangChon) {
        if (ddmDangChon == null) {
            return maBanDangChon;
        }

        String maBanChinh = layMaBanChinhTuGhiChu(ddmDangChon.getGhiChu());

        if (maBanChinh != null && !maBanChinh.trim().isEmpty()) {
            return maBanChinh;
        }

        return ddmDangChon.getMaBan() != null ? ddmDangChon.getMaBan() : maBanDangChon;
    }

    private List<DonDatMonDTO> layNhomDonDatTheoBanChinh(String maBanChinh) {
        List<DonDatMonDTO> ketQua = new ArrayList<>();

        if (maBanChinh == null || maBanChinh.trim().isEmpty()) {
            return ketQua;
        }

        List<DonDatMonDTO> dsTatCa = donDatMonRemoteService.getAllDonDatMonChuaNhanBaoGomLinked();

        if (dsTatCa == null) {
            return ketQua;
        }

        for (DonDatMonDTO ddm : dsTatCa) {
            if (ddm == null) continue;

            String maBan = ddm.getMaBan();
            String ghiChu = ddm.getGhiChu();

            boolean laBanChinh = maBanChinh.equals(maBan);
            boolean laBanPhu = ghiChu != null && ghiChu.contains("LINKED:" + maBanChinh);

            if (laBanChinh || laBanPhu) {
                ketQua.add(ddm);
            }
        }

        return ketQua;
    }

    private DonDatMonDTO timDonChinhTrongNhom(List<DonDatMonDTO> nhomDon, String maBanChinh) {
        if (nhomDon == null || nhomDon.isEmpty()) {
            return null;
        }

        for (DonDatMonDTO ddm : nhomDon) {
            if (ddm != null && maBanChinh.equals(ddm.getMaBan())) {
                return ddm;
            }
        }

        return nhomDon.get(0);
    }

    private void capNhatTrangThaiBanDangPhucVu(String maBan, LocalDateTime gioMoBan) throws Exception {
        if (maBan == null || maBan.trim().isEmpty()) {
            return;
        }

        BanDTO banCanUpdate = findBanByMa(maBan);

        if (banCanUpdate == null) {
            throw new Exception("Không tìm thấy bàn để cập nhật trạng thái: " + maBan);
        }

        banCanUpdate.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
        banCanUpdate.setGioMoBan(gioMoBan);

        boolean updateOK = banRemoteService.updateBan(banCanUpdate);

        if (!updateOK) {
            throw new Exception("Lỗi cập nhật trạng thái bàn: " + banCanUpdate.getTenBan());
        }
    }

    private void nhanBanDatTruoc(BanDTO banDuocChon) throws Exception {
        if (banDuocChon == null || banDuocChon.getMaBan() == null) {
            throw new Exception("Bàn được chọn không hợp lệ.");
        }

        String maBanDangChon = banDuocChon.getMaBan();

        DonDatMonDTO ddmDangChon = timDonDatTruocTheoMaBan(maBanDangChon);

        if (ddmDangChon == null) {
            throw new Exception("Không tìm thấy đơn đặt trước cho bàn " + maBanDangChon);
        }

        String maBanChinh = layMaBanChinhCuaNhom(ddmDangChon, maBanDangChon);

        List<DonDatMonDTO> nhomDon = layNhomDonDatTheoBanChinh(maBanChinh);

        if (nhomDon == null || nhomDon.isEmpty()) {
            nhomDon = new ArrayList<>();
            nhomDon.add(ddmDangChon);
        }

        DonDatMonDTO donChinh = timDonChinhTrongNhom(nhomDon, maBanChinh);

        if (donChinh == null) {
            donChinh = ddmDangChon;
        }

        String maKH = null;

        if (donChinh.getMaKH() != null && !donChinh.getMaKH().trim().isEmpty()) {
            maKH = donChinh.getMaKH();
        } else if (ddmDangChon.getMaKH() != null && !ddmDangChon.getMaKH().trim().isEmpty()) {
            maKH = ddmDangChon.getMaKH();
        }

        LocalDateTime now = LocalDateTime.now();

        String ghiChu = donChinh.getGhiChu();

        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            ghiChu = "Nhận bàn đặt trước";
        }

        /*
         * QUAN TRỌNG:
         * Chỉ tạo 1 hóa đơn cho bàn chính.
         * Không tạo hóa đơn cho từng bàn phụ nữa.
         */
        HoaDonDTO hoaDonChinh = hoaDonRemoteService.moBanVaTaoHoaDon(
                maBanChinh,
                maNVDangNhap,
                maKH,
                now,
                ghiChu
        );

        if (hoaDonChinh == null) {
            throw new Exception("Không tạo được hóa đơn cho bàn chính " + maBanChinh);
        }

        /*
         * Các bàn trong nhóm chỉ cập nhật trạng thái đang phục vụ.
         * Không tạo thêm hóa đơn riêng.
         */
        for (DonDatMonDTO ddm : nhomDon) {
            if (ddm == null || ddm.getMaBan() == null || ddm.getMaBan().trim().isEmpty()) {
                continue;
            }

            capNhatTrangThaiBanDangPhucVu(ddm.getMaBan(), now);
        }

        this.activeHoaDon = hoaDonChinh;

        banDuocChon.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
        banDuocChon.setGioMoBan(now);

        this.banHienTai = banDuocChon;
        statusColorBox.setBackground(Color.RED);
    }

    private String layMaKHDeMoBan(BanDTO ban) {
        if (maKHDangChon != null && !maKHDangChon.trim().isEmpty()) {
            return maKHDangChon;
        }

        if (ban == null || ban.getMaBan() == null || ban.getMaBan().trim().isEmpty()) {
            return null;
        }

        ManHinhBanGUI.KhachHangTam khTam = ManHinhBanGUI.layKhachHangTamTheoBan(ban.getMaBan());

        if (khTam == null) {
            return null;
        }

        if (khTam.getMaKH() != null && !khTam.getMaKH().trim().isEmpty()) {
            return khTam.getMaKH();
        }

        String sdt = khTam.getSdt();

        if (sdt != null && !sdt.trim().isEmpty()) {
            try {
                KhachHangDTO kh = findKhachHangBySdt(sdt.trim());
                if (kh != null) {
                    return kh.getMaKH();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void moBanMoi(BanDTO ban) {
        try {
            String maKHMoBan = layMaKHDeMoBan(ban);

            HoaDonDTO hd = hoaDonRemoteService.moBanVaTaoHoaDon(
                    ban.getMaBan(),
                    maNVDangNhap,
                    maKHMoBan,
                    LocalDateTime.now(),
                    "Tạo từ màn hình gọi món"
            );

            this.activeHoaDon = hd;

            if (this.activeHoaDon != null && maKHMoBan != null && !maKHMoBan.trim().isEmpty()) {
                this.activeHoaDon.setMaKH(maKHMoBan);
            }

            statusColorBox.setBackground(Color.RED);

            if (maKHMoBan != null && !maKHMoBan.trim().isEmpty()) {
                ManHinhBanGUI.xoaKhachHangTamTheoBan(ban.getMaBan());
            }

            this.maKHDangChon = null;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tạo đơn: " + getRootMessage(e));
        }
    }

    private void addMonAnToOrder(MonAnDTO monAn) {
        if (banHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi gọi món!");
            return;
        }

        if (activeHoaDon == null || activeHoaDon.getMaDon() == null) {
            JOptionPane.showMessageDialog(this, "Bàn hiện tại chưa có hóa đơn hợp lệ!");
            return;
        }

        String maMon = monAn.getMaMonAn();
        String tenMon = monAn.getTenMon();
        float donGia = monAn.getDonGia();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            if (maMon.equals(modelChiTietHoaDon.getValueAt(i, 1))) {
                int slHienTai = toInt(modelChiTietHoaDon.getValueAt(i, 3));
                modelChiTietHoaDon.setValueAt(slHienTai + 1, i, 3);
                modelChiTietHoaDon.setValueAt((slHienTai + 1) * donGia, i, 5);
                updateBillPanelTotals();
                return;
            }
        }

        modelChiTietHoaDon.addRow(new Object[]{"X", maMon, tenMon, 1, donGia, donGia});
        updateBillPanelTotals();
    }

    public void updateBillPanelTotals() {
        float tongTien = 0;
        int tongSoLuong = 0;

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            int soLuong = toInt(modelChiTietHoaDon.getValueAt(i, 3));
            float thanhTien = toFloat(modelChiTietHoaDon.getValueAt(i, 5));

            tongSoLuong += soLuong;
            tongTien += thanhTien;
        }

        if (billPanel != null) {
            billPanel.loadBillTotals((long) tongTien, 0, (long) tongTien, tongSoLuong);
        }
    }

    private void luuChiTietHoaDonAsync() {
        if (activeHoaDon == null || activeHoaDon.getMaDon() == null) {
            return;
        }

        String maDon = activeHoaDon.getMaDon();
        List<ChiTietHoaDonDTO> items = layChiTietTuBang(maDon);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                boolean ok = chiTietHoaDonRemoteService.replaceByMaDon(maDon, items);

                capNhatTongTienHoaDonTrenServer();

                return ok;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ManHinhGoiMonGUI.this,
                            "Lỗi lưu chi tiết hóa đơn qua socket: " + getRootMessage(e),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private List<ChiTietHoaDonDTO> layChiTietTuBang(String maDon) {
        List<ChiTietHoaDonDTO> result = new ArrayList<>();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            String maMonAn = String.valueOf(modelChiTietHoaDon.getValueAt(i, 1));
            String tenMon = String.valueOf(modelChiTietHoaDon.getValueAt(i, 2));
            int soLuong = toInt(modelChiTietHoaDon.getValueAt(i, 3));
            float donGia = toFloat(modelChiTietHoaDon.getValueAt(i, 4));
            float thanhTien = toFloat(modelChiTietHoaDon.getValueAt(i, 5));

            if (maMonAn == null || maMonAn.trim().isEmpty() || soLuong <= 0) {
                continue;
            }

            result.add(ChiTietHoaDonDTO.builder()
                    .maDon(maDon)
                    .maMonAn(maMonAn)
                    .tenMon(tenMon)
                    .soLuong(soLuong)
                    .donGia(donGia)
                    .thanhTien(thanhTien)
                    .build());
        }

        return result;
    }

    private void capNhatTongTienHoaDonTrenServer() {
        if (activeHoaDon == null) {
            return;
        }

        float tongTien = 0f;

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            tongTien += toFloat(modelChiTietHoaDon.getValueAt(i, 5));
        }

        float giamGia = activeHoaDon.getGiamGia();
        float tongThanhToan = Math.max(0f, tongTien - giamGia);

        activeHoaDon.setTongTien(tongTien);
        activeHoaDon.setGiamGia(giamGia);
        activeHoaDon.setTongThanhToan(tongThanhToan);

        hoaDonRemoteService.capNhatTongTien(activeHoaDon);
    }

    public void xoaThongTinGoiMon() {
        lblTenBanHeader.setText("Chưa chọn bàn");
        modelChiTietHoaDon.setRowCount(0);

        if (billPanel != null) {
            billPanel.clearBill();
        }

        this.banHienTai = null;
        this.activeHoaDon = null;
        this.maKHDangChon = null;

        if (statusColorBox != null) {
            statusColorBox.setBackground(COLOR_STATUS_FREE);
        }
    }

    private void filterMonAn() {
        String tuKhoa = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";

        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAnDTO mon = itemPanel.getMonAn();

            boolean matchesSearch = tuKhoa.isEmpty()
                    || mon.getTenMon().toLowerCase().contains(tuKhoa);

            boolean matchesCategory = currentCategoryFilter.equals("Tất cả")
                    || (mon.getTenDM() != null && mon.getTenDM().equals(currentCategoryFilter));

            boolean dangKinhDoanh = "Còn".equalsIgnoreCase(mon.getTrangThai());

            itemPanel.setVisible(matchesSearch && matchesCategory && dangKinhDoanh);
        }

        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    private JPanel createCategoryFilterPanel() {
        pnlFilterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlFilterButtons.setOpaque(false);
        return pnlFilterButtons;
    }

    private void renderCategoryButtons() {
        if (pnlFilterButtons == null) return;

        pnlFilterButtons.removeAll();
        ButtonGroup group = new ButtonGroup();

        JToggleButton btnAll = createFilterButton("Tất cả", true);
        btnAll.addActionListener(e -> {
            currentCategoryFilter = "Tất cả";
            filterMonAn();
        });
        group.add(btnAll);
        pnlFilterButtons.add(btnAll);

        List<String> categories = dsMonAnFull.stream()
                .map(MonAnDTO::getTenDM)
                .filter(name -> name != null && !name.isEmpty())
                .distinct()
                .sorted()
                .toList();

        for (String catName : categories) {
            JToggleButton btn = createFilterButton(catName, false);
            btn.addActionListener(e -> {
                currentCategoryFilter = catName;
                filterMonAn();
            });
            group.add(btn);
            pnlFilterButtons.add(btn);
        }

        pnlFilterButtons.revalidate();
        pnlFilterButtons.repaint();
    }

    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton btn = new JToggleButton(text);
        btn.setSelected(selected);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return btn;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(0, 35));
        txtTimKiem.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                filterMonAn();
            }
        });

        panel.add(new JLabel("🔎"), BorderLayout.WEST);
        panel.add(txtTimKiem, BorderLayout.CENTER);

        return panel;
    }

    private BanDTO findBanByMa(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }

        List<BanDTO> dsBan = banRemoteService.getAllBan();

        if (dsBan == null) {
            return null;
        }

        for (BanDTO ban : dsBan) {
            if (ban != null && maBan.equals(ban.getMaBan())) {
                return ban;
            }
        }

        return null;
    }

    private KhachHangDTO findKhachHangByMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        List<KhachHangDTO> dsKH = khachHangRemoteService.findAll();

        if (dsKH == null) {
            return null;
        }

        for (KhachHangDTO kh : dsKH) {
            if (kh != null && maKH.equals(kh.getMaKH())) {
                return kh;
            }
        }

        return null;
    }

    private KhachHangDTO findKhachHangBySdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        List<KhachHangDTO> dsKH = khachHangRemoteService.search(sdt.trim());

        if (dsKH == null) {
            return null;
        }

        for (KhachHangDTO kh : dsKH) {
            if (kh != null && sdt.trim().equals(kh.getSdt())) {
                return kh;
            }
        }

        return null;
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private float toFloat(Object value) {
        if (value instanceof Number number) {
            return number.floatValue();
        }

        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            return 0f;
        }
    }

    private String getRootMessage(Exception e) {
        Throwable t = e;

        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t.getMessage() != null ? t.getMessage() : e.getMessage();
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
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private int editingRow;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton("X");
            button.setOpaque(true);
            button.setForeground(Color.RED);
            button.setBackground(Color.WHITE);
            button.setBorder(null);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            this.table = table;
            this.editingRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (table != null && editingRow >= 0) {
                final DefaultTableModel finalModel = (DefaultTableModel) table.getModel();

                SwingUtilities.invokeLater(() -> {
                    if (editingRow < finalModel.getRowCount()) {
                        finalModel.removeRow(editingRow);
                        updateBillPanelTotals();
                    }
                });
            }

            editingRow = -1;
            return "X";
        }
    }

    public BanDTO getBanHienTai() {
        return banHienTai;
    }

    public HoaDonDTO getActiveHoaDon() {
        return activeHoaDon;
    }

    public DefaultTableModel getModelChiTietHoaDon() {
        return modelChiTietHoaDon;
    }

    class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super(new SpinnerNumberModel(1, 1, 100, 1));
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            setValue(value instanceof Integer ? value : 1);
            return this;
        }
    }

    class SpinnerEditor extends DefaultCellEditor {
        JSpinner spinner;
        private int editingRow = -1;
        private JTable table;

        public SpinnerEditor() {
            super(new JTextField());

            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            spinner.setBorder(null);

            spinner.addChangeListener(e -> {
                if (table != null && editingRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();

                    if (editingRow < model.getRowCount()) {
                        int currentQuantity = (Integer) spinner.getValue();
                        float donGia = toFloat(model.getValueAt(editingRow, 4));

                        SwingUtilities.invokeLater(() -> {
                            if (editingRow < model.getRowCount()) {
                                model.setValueAt(currentQuantity * donGia, editingRow, 5);
                                updateBillPanelTotals();
                            }
                        });
                    }

                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            this.table = table;
            this.editingRow = row;
            spinner.setValue(value);
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }
}