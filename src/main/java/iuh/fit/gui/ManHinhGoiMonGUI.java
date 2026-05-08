package iuh.fit.gui;

import iuh.fit.core.dto.*;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.service.*;

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

public class ManHinhGoiMonGUI extends JPanel {

    private final MonAnService monAnService = new MonAnService();
    private final BanService banService = new BanService();
    private final HoaDonService hoaDonService = new HoaDonService();
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final DonDatMonService donDatMonService = new DonDatMonService();
    private final KhachHangService khachHangService = new KhachHangService();

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

    private JTable tblChiTietHoaDon;
    private DefaultTableModel modelChiTietHoaDon;
    private BillPanel billPanel;

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);

    private String maKHDangChon;

    public void setMaKHDangChon(String maKHDangChon) {
        this.maKHDangChon = maKHDangChon;
    }

    public ManHinhGoiMonGUI(DanhSachBanGUI parent, String maNVDangNhap) {
        super(new BorderLayout());
        this.parentDanhSachBanGUI = parent;
        this.maNVDangNhap = maNVDangNhap;

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
        splitPane.setDividerLocation(520);
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

        pnlMenuItemContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
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
        this.dsMonAnFull = monAnService.findAllDTO();

        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        if (dsMonAnFull.isEmpty()) {
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
            String trangThai = banDuocChon.getTrangThai() != null
                    ? banDuocChon.getTrangThai().toString()
                    : "TRONG";

            if ("DANG_PHUC_VU".equalsIgnoreCase(trangThai)) {
                statusColorBox.setBackground(Color.RED);

                activeHoaDon = hoaDonService.getHoaDonChuaThanhToan(banDuocChon.getMaBan());

                if (activeHoaDon == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Lỗi dữ liệu: Bàn đang phục vụ nhưng chưa có hóa đơn.",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return false;
                }

                ChiTietHoaDonDTO filter = ChiTietHoaDonDTO.builder()
                        .maDon(activeHoaDon.getMaDon())
                        .build();

                List<ChiTietHoaDonDTO> dsChiTiet = chiTietHoaDonService.getChiTietTheoMaDon(filter);

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

                DonDatMonDTO ddmPreview = donDatMonService.getDonDatMonDatTruoc(banDuocChon.getMaBan());

                String msg = "Bàn '" + banDuocChon.getTenBan()
                        + "' đã được đặt trước.\nBạn có muốn nhận bàn này không?";

                if (ddmPreview != null) {
                    String tenKhach = "Khách";

                    if (ddmPreview.getMaKH() != null) {
                        KhachHangDTO kh = khachHangService.findByIdDTO(ddmPreview.getMaKH());

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
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu bàn: " + ex.getMessage());
            return false;
        } finally {
            updateBillPanelTotals();
        }

        if (requireBanRefresh && parentDanhSachBanGUI != null) {
            parentDanhSachBanGUI.refreshManHinhBan();
        }

        return true;
    }

    private void nhanBanDatTruoc(BanDTO banChinh) throws Exception {
        DonDatMonDTO ddm = donDatMonService.getDonDatMonDatTruoc(banChinh.getMaBan());

        if (ddm == null) {
            throw new Exception("Không tìm thấy đơn đặt trước!");
        }

        String maKH = ddm.getMaKH();

        String ghiChu = "Nhận bàn đặt trước";
        if (ddm.getGhiChu() != null && !ddm.getGhiChu().trim().isEmpty()) {
            ghiChu = ddm.getGhiChu();
        }

        HoaDonDTO hd = hoaDonService.moBanVaTaoHoaDon(
                banChinh.getMaBan(),
                maNVDangNhap,
                maKH,
                LocalDateTime.now(),
                ghiChu
        );

        if (hd == null) {
            throw new Exception("Không tạo được hóa đơn cho bàn đặt trước.");
        }

        this.activeHoaDon = hd;

        banChinh.setTrangThai(TrangThaiBan.DANG_PHUC_VU);
        banChinh.setGioMoBan(LocalDateTime.now());

        boolean updateOK = banService.updateBan(banChinh);
        if (!updateOK) {
            throw new Exception("Lỗi cập nhật trạng thái bàn.");
        }

        this.banHienTai = banChinh;
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
                KhachHangDTO kh = khachHangService.findBySdtDTO(sdt.trim());
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

            HoaDonDTO hd = hoaDonService.moBanVaTaoHoaDon(
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
            JOptionPane.showMessageDialog(this, "Lỗi tạo đơn: " + e.getMessage());
        }
    }

    private void addMonAnToOrder(MonAnDTO monAn) {
        if (banHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi gọi món!");
            return;
        }

        String maMon = monAn.getMaMonAn();
        String tenMon = monAn.getTenMon();
        float donGia = monAn.getDonGia();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            if (maMon.equals(modelChiTietHoaDon.getValueAt(i, 1))) {
                int slHienTai = (int) modelChiTietHoaDon.getValueAt(i, 3);
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
            tongSoLuong += (int) modelChiTietHoaDon.getValueAt(i, 3);
            tongTien += (float) modelChiTietHoaDon.getValueAt(i, 5);
        }

        if (billPanel != null) {
            billPanel.loadBillTotals((long) tongTien, 0, (long) tongTien, tongSoLuong);
        }
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
            boolean show = true;

            if (!tuKhoa.isEmpty() && !mon.getTenMon().toLowerCase().contains(tuKhoa)) {
                show = false;
            }

            itemPanel.setVisible(show);
        }

        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    private JPanel createCategoryFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        return filterPanel;
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
                        float donGia = (Float) model.getValueAt(editingRow, 4);

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