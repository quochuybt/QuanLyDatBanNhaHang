package iuh.fit.gui;

import iuh.fit.core.dto.*;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.service.*;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ManHinhBanGUI extends JPanel {

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);
    public static final Color COLOR_STATUS_OCCUPIED = new Color(239, 68, 68);
    public static final Color COLOR_STATUS_RESERVED = new Color(187, 247, 208);

    private javax.swing.Timer timerTuDongCapNhat;

    private final BanService banService = new BanService();
    private final HoaDonService hoaDonService = new HoaDonService();
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiService();
    private final KhachHangService khachHangService = new KhachHangService();
    private final DonDatMonService donDatMonService = new DonDatMonService();

    private List<BanDTO> allTableDTOsFromDB = new ArrayList<>();
    private List<Ban> allTablesFromDB = new ArrayList<>();

    private Ban selectedTable = null;
    private JPanel leftTableContainer;
    private JPanel statsPanel;
    private JTextField txtMaKhuyenMai;
    private JButton btnApDungKM;
    private String currentLeftFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private DanhSachBanGUI parentDanhSachBanGUI;

    private JPanel rightPanel;

    private JLabel lblTenBanHeader;
    private JLabel lblKhuVucHeader;

    private JTextField txtNgayVao;
    private JTextField txtGioVao;
    private JTextField txtTinhTrang;
    private JComboBox<String> cmbPTThanhToan;

    private JTextField txtSDTKhach;
    private JTextField txtHoTenKhach;
    private JTextField txtThanhVien;

    private JTextField txtSoLuongKhach;
    private JTextField txtGhiChu;
    private JLabel statusColorBox;
    private BillPanel billPanel;

    public ManHinhBanGUI() {
        this(null);
    }

    public ManHinhBanGUI(DanhSachBanGUI parent) {
        super(new BorderLayout());
        this.parentDanhSachBanGUI = parent;
        buildUI();
        khoiTaoTuDongCapNhat();
    }

    private void khoiTaoTuDongCapNhat() {
        int thoiGianLap = 60 * 1000;
        timerTuDongCapNhat = new javax.swing.Timer(thoiGianLap, e -> refreshTableList());
        timerTuDongCapNhat.start();
    }

    private JTextField createStyledTextField(boolean isEditable) {
        JTextField tf = new JTextField();
        tf.setColumns(1);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(0, 5, 0, 5)
        ));

        if (isEditable) {
            tf.setEditable(true);
            tf.setOpaque(true);
            tf.setBackground(Color.WHITE);
        } else {
            tf.setEditable(false);
            tf.setOpaque(true);
            tf.setBackground(new Color(235, 235, 235));
        }

        return tf;
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        try {
            loadTablesFromService();
        } catch (Exception e) {
            e.printStackTrace();
            this.allTableDTOsFromDB = new ArrayList<>();
            this.allTablesFromDB = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối hoặc tải dữ liệu Bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }

        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);

        JPanel listPanel = createListPanel("Danh sách toàn bộ bàn");
        this.statsPanel = createStatsPanel();

        leftPanel.add(listPanel, BorderLayout.CENTER);
        leftPanel.add(statsPanel, BorderLayout.SOUTH);

        this.rightPanel = createRightPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );

        splitPane.setDividerLocation(520);
        splitPane.setBorder(null);
        splitPane.setOneTouchExpandable(true);

        this.add(splitPane, BorderLayout.CENTER);

        populateLeftPanel(currentLeftFilter);
        updateRightPanelDetails(selectedTable, null);
        updateStatsPanel();
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 15, 0, 10));

        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48));
        statusColorBox.setBackground(COLOR_STATUS_FREE);
        statusColorBox.setOpaque(true);

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        textPanel.setOpaque(false);

        lblTenBanHeader = new JLabel("Chọn một bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));

        lblKhuVucHeader = new JLabel("");
        lblKhuVucHeader.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblKhuVucHeader.setForeground(Color.DARK_GRAY);

        textPanel.add(lblTenBanHeader);
        textPanel.add(lblKhuVucHeader);

        headerPanel.add(statusColorBox, BorderLayout.WEST);
        headerPanel.add(textPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 10, 8);
        gbc.fill = GridBagConstraints.BOTH;

        txtNgayVao = createStyledTextField(false);
        txtGioVao = createStyledTextField(false);
        txtTinhTrang = createStyledTextField(false);
        txtThanhVien = createStyledTextField(false);
        txtSDTKhach = createStyledTextField(true);
        txtHoTenKhach = createStyledTextField(true);
        txtSoLuongKhach = createStyledTextField(true);
        txtGhiChu = createStyledTextField(true);

        txtGhiChu.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                xuLyCapNhatGhiChu();
            }
        });

        cmbPTThanhToan = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        cmbPTThanhToan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbPTThanhToan.setBackground(Color.WHITE);
        cmbPTThanhToan.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        cmbPTThanhToan.setPreferredSize(new Dimension(10, cmbPTThanhToan.getPreferredSize().height));

        txtSDTKhach.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                timKiemThanhVienTuSDT();
            }
        });

        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Ngày vào", txtNgayVao), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Giờ vào", txtGioVao), gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.5;
        infoPanel.add(createInfoBox("Tình trạng", txtTinhTrang), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("PT Thanh toán", cmbPTThanhToan), gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        infoPanel.add(createInfoBox("SĐT Khách hàng", txtSDTKhach), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        infoPanel.add(createInfoBox("Họ tên Khách", txtHoTenKhach), gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        infoPanel.add(createInfoBox("Thành viên", txtThanhVien), gbc);

        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(createInfoBox("Số lượng khách", txtSoLuongKhach), gbc);

        txtMaKhuyenMai = createStyledTextField(true);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Mã khuyến mãi", txtMaKhuyenMai), gbc);

        btnApDungKM = new JButton("Áp dụng");
        btnApDungKM.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApDungKM.setBackground(ManHinhBanGUI.COLOR_STATUS_RESERVED);
        btnApDungKM.setForeground(Color.DARK_GRAY);
        btnApDungKM.setFocusPainted(false);
        btnApDungKM.setPreferredSize(new Dimension(80, 35));

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(20, 0, 10, 8);
        infoPanel.add(btnApDungKM, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 8, 10, 8);
        infoPanel.add(createInfoBox("Ghi chú", txtGhiChu), gbc);

        this.billPanel = new BillPanel(this);

        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                infoPanel,
                this.billPanel
        );

        verticalSplitPane.setDividerLocation(230);
        verticalSplitPane.setBorder(null);

        panel.add(verticalSplitPane, BorderLayout.CENTER);

        btnApDungKM.addActionListener(e -> xuLyApDungKhuyenMai());

        return panel;
    }

    private JPanel createInfoBox(String labelText, Component field) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.BLACK);

        box.add(label, BorderLayout.NORTH);
        box.add(field, BorderLayout.CENTER);
        return box;
    }

    private void updateRightPanelDetails(Ban ban, String tenHienThi) {
        if (ban == null) {
            statusColorBox.setBackground(COLOR_STATUS_FREE);
            lblTenBanHeader.setText("Chọn một bàn");
            lblKhuVucHeader.setText("");

            txtTinhTrang.setText("");
            cmbPTThanhToan.setSelectedItem("Tiền mặt");
            txtSDTKhach.setText("");
            txtHoTenKhach.setText("");
            txtThanhVien.setText("");
            txtMaKhuyenMai.setText("");
            txtSoLuongKhach.setText("");
            txtGhiChu.setText("");
            txtNgayVao.setText("");
            txtGioVao.setText("");

            updateBillPanelFromHoaDon(null);
            return;
        }

        Color statusColor;
        String tinhTrangText;

        TrangThaiBan trangThai = ban.getTrangThai();
        if (trangThai == null) {
            trangThai = TrangThaiBan.TRONG;
        }

        switch (trangThai) {
            case TRONG:
                statusColor = COLOR_STATUS_FREE;
                tinhTrangText = "Trống";
                break;
            case DA_DAT_TRUOC:
                statusColor = COLOR_STATUS_RESERVED;
                tinhTrangText = "Đã đặt trước";
                break;
            case DANG_PHUC_VU:
            default:
                statusColor = COLOR_STATUS_OCCUPIED;
                tinhTrangText = "Đang phục vụ";
                break;
        }

        statusColorBox.setBackground(statusColor);

        String headerName = (tenHienThi != null && !tenHienThi.isEmpty())
                ? tenHienThi
                : ban.getTenBan();

        lblTenBanHeader.setText(headerName + " -- " + ban.getKhuVuc());

        txtTinhTrang.setText(tinhTrangText);

        cmbPTThanhToan.setSelectedItem("Tiền mặt");
        txtSDTKhach.setText("");
        txtHoTenKhach.setText("");
        txtThanhVien.setText("");
        txtSoLuongKhach.setText(String.valueOf(ban.getSoGhe()));
        txtGhiChu.setText("");
        txtMaKhuyenMai.setText("");

        LocalDateTime gioMoBan = ban.getGioMoBan();
        DateTimeFormatter dtfNgay = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter dtfGio = DateTimeFormatter.ofPattern("HH:mm");

        if (gioMoBan != null) {
            txtNgayVao.setText(gioMoBan.format(dtfNgay));
            txtGioVao.setText(gioMoBan.format(dtfGio));
        } else {
            txtNgayVao.setText("");
            txtGioVao.setText("");
        }

        HoaDonDTO activeHoaDon = null;

        if (trangThai == TrangThaiBan.DANG_PHUC_VU) {
            activeHoaDon = hoaDonService.getHoaDonChuaThanhToan(ban.getMaBan());

            if (activeHoaDon != null) {
                txtThanhVien.setText("Vãng lai");

                String hinhThucThanhToan = getStringValue(activeHoaDon,
                        "getHinhThucThanhToan",
                        "getHinhthucthanhtoan");

                if (hinhThucThanhToan != null && !hinhThucThanhToan.isBlank()) {
                    cmbPTThanhToan.setSelectedItem(hinhThucThanhToan);
                }

                String maKM = getStringValue(activeHoaDon,
                        "getMaKM",
                        "getMaKm",
                        "getMaKhuyenMai");

                txtMaKhuyenMai.setText(maKM != null ? maKM : "");
            }
        } else if (trangThai == TrangThaiBan.DA_DAT_TRUOC) {
            txtThanhVien.setText("Vãng lai");
            cmbPTThanhToan.setSelectedItem("Tiền mặt");
        }

        if (billPanel != null) {
            billPanel.setCustomHeader(lblTenBanHeader.getText());
        }

        updateBillPanelFromHoaDon(activeHoaDon);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        int countTrong = 0;
        int countDat = 0;
        int countPhucVu = 0;

        if (allTablesFromDB != null) {
            for (Ban ban : allTablesFromDB) {
                TrangThaiBan trangThai = ban.getTrangThai();
                if (trangThai == null) {
                    trangThai = TrangThaiBan.TRONG;
                }

                switch (trangThai) {
                    case TRONG:
                        countTrong++;
                        break;
                    case DA_DAT_TRUOC:
                        countDat++;
                        break;
                    case DANG_PHUC_VU:
                        countPhucVu++;
                        break;
                }
            }
        }

        statsPanel.add(createStatItem("Trống", countTrong, COLOR_STATUS_FREE));
        statsPanel.add(createStatItem("Đã đặt", countDat, COLOR_STATUS_RESERVED));
        statsPanel.add(createStatItem("Sử dụng", countPhucVu, COLOR_STATUS_OCCUPIED));

        return statsPanel;
    }

    private void updateStatsPanel() {
        if (statsPanel == null) return;

        Container parent = statsPanel.getParent();
        if (parent != null) {
            parent.remove(statsPanel);

            this.statsPanel = createStatsPanel();

            parent.add(this.statsPanel, BorderLayout.SOUTH);

            parent.revalidate();
            parent.repaint();
        } else {
            System.err.println("Không tìm thấy panel cha của statsPanel để cập nhật!");
        }
    }

    private JPanel createStatItem(String name, int count, Color color) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        itemPanel.setOpaque(false);

        JPanel iconBox = new JPanel();
        iconBox.setPreferredSize(new Dimension(28, 28));
        iconBox.setBackground(color);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLabel.setForeground(Color.RED);

        itemPanel.add(iconBox);
        itemPanel.add(nameLabel);
        itemPanel.add(countLabel);

        return itemPanel;
    }

    private JPanel createListPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(createFilterPanel(), BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.leftTableContainer = tableContainer;

        JScrollPane scrollPane = new JScrollPane(tableContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        String[] filters = {"Tất cả", "Tầng trệt", "Tầng 1"};

        ActionListener filterListener = e -> {
            String selectedFilter = e.getActionCommand();
            currentLeftFilter = selectedFilter;
            populateLeftPanel(currentLeftFilter);
        };

        for (String filter : filters) {
            JToggleButton button = createFilterButton(filter, filter.equals("Tất cả"));
            button.setActionCommand(filter);
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
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);

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

    private void populateLeftPanel(String khuVucFilter) {
        if (leftTableContainer == null) return;

        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        for (Ban ban : allTablesFromDB) {
            if (khuVucFilter.equals("Tất cả") || khuVucFilter.equals(ban.getKhuVuc())) {
                BanPanel banPanel = new BanPanel(JsonMapper.convert(ban, BanDTO.class));

                if (isSameBan(ban, selectedTable)) {
                    banPanel.setSelected(true);
                }

                banPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        for (Component c : leftTableContainer.getComponents()) {
                            if (c instanceof BanPanel) {
                                ((BanPanel) c).setSelected(false);
                            }
                        }

                        banPanel.setSelected(true);

                        selectedTable = ban;

                        updateRightPanelDetails(selectedTable, selectedTable.getTenBan());
                    }
                });

                leftTableContainer.add(banPanel);
                leftBanPanelList.add(banPanel);
            }
        }

        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    public Ban getSelectedTable() {
        return selectedTable;
    }

    public HoaDonDTO getActiveHoaDon() {
        if (selectedTable != null && selectedTable.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            return hoaDonService.getHoaDonChuaThanhToan(selectedTable.getMaBan());
        }
        return null;
    }

    public void refreshTableList() {
        try {
            String maBanDangChon = selectedTable != null ? selectedTable.getMaBan() : null;

            loadTablesFromService();

            if (maBanDangChon != null) {
                selectedTable = null;
                for (Ban ban : allTablesFromDB) {
                    if (maBanDangChon.equals(ban.getMaBan())) {
                        selectedTable = ban;
                        break;
                    }
                }
            }

            populateLeftPanel(currentLeftFilter);
            updateStatsPanel();

            if (selectedTable != null) {
                updateRightPanelDetails(selectedTable, selectedTable.getTenBan());
            } else {
                updateRightPanelDetails(null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi làm mới danh sách bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhatGhiChu() {
        HoaDonDTO activeHoaDon = getActiveHoaDon();
        if (activeHoaDon == null) return;

        String ghiChuMoi = txtGhiChu.getText().trim();
        String maDon = activeHoaDon.getMaDon();

        if (maDon != null && !maDon.isEmpty()) {
            try {
                DonDatMonDTO ddmHienTai = null;
                try {
                    ddmHienTai = donDatMonService.findById(maDon);
                } catch (NullPointerException npe) {
                    System.err.println("Hệ thống: Không tìm thấy entity cho mã đơn " + maDon);
                }

                if (ddmHienTai != null) {
                    String ghiChuHoanChinh = ghiChuMoi;

                    if (ddmHienTai.getGhiChu() != null && ddmHienTai.getGhiChu().contains("LINKED:")) {
                        String linkedPart = ddmHienTai.getGhiChu().substring(ddmHienTai.getGhiChu().indexOf("LINKED:"));
                        ghiChuHoanChinh = ghiChuMoi + " " + linkedPart;
                    }

                    ddmHienTai.setGhiChu(ghiChuHoanChinh);
                    donDatMonService.update(ddmHienTai);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Có lỗi xảy ra khi cập nhật ghi chú: " + e.getMessage(),
                        "Lỗi Dữ Liệu",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void timKiemThanhVienTuSDT() {
        String sdt = txtSDTKhach.getText().trim();

        // 1. Trường hợp để trống SĐT
        if (sdt.isEmpty()) {
            txtHoTenKhach.setText("");
            txtThanhVien.setText("Vãng lai");

            // Xóa khách hàng khỏi hóa đơn hiện tại trong DB nếu xóa SĐT
            HoaDonDTO activeHD = getActiveHoaDon();
            if (activeHD != null) {
                hoaDonService.capNhatMaKH(activeHD.getMaHD(), null);
            }
            return;
        }

        try {
            // 2. Tìm kiếm khách hàng theo SĐT qua Service
            KhachHangDTO khachHang = khachHangService.findBySdtDTO(sdt);

            if (khachHang != null) {
                // Cập nhật giao diện
                txtHoTenKhach.setText(khachHang.getTenKH());
                txtThanhVien.setText(khachHang.getHangThanhVien() != null
                        ? khachHang.getHangThanhVien().toString()
                        : "Thành viên");

                // QUAN TRỌNG: Gắn khách hàng vào hóa đơn trong DB ngay lập tức
                HoaDonDTO activeHD = getActiveHoaDon();
                if (activeHD != null) {
                    hoaDonService.capNhatMaKH(activeHD.getMaHD(), khachHang.getMaKH());
                }
            } else {
                // Trường hợp không tìm thấy khách
                txtHoTenKhach.setText("");
                txtThanhVien.setText("Vãng lai");

                // Trả về khách lẻ (null) trong DB
                HoaDonDTO activeHD = getActiveHoaDon();
                if (activeHD != null) {
                    hoaDonService.capNhatMaKH(activeHD.getMaHD(), null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Có lỗi xảy ra khi truy xuất dữ liệu khách hàng: " + e.getMessage(),
                    "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void xuLyApDungKhuyenMai() {
        HoaDonDTO activeHoaDon = getActiveHoaDon();
        if (activeHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào đang hoạt động!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKM_input = txtMaKhuyenMai.getText().trim().toUpperCase();

        try {
            if (maKM_input.isEmpty()) {
                hoaDonService.capNhatMaKM(activeHoaDon.getMaHD(), null);
                activeHoaDon.setMaKM(null);

                activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);
                updateBillPanelFromHoaDon(activeHoaDon);
                return;
            }

            KhuyenMaiDTO km = khuyenMaiService.findByIdDTO(maKM_input);

            if (km == null) {
                JOptionPane.showMessageDialog(this, "Mã khuyến mãi không tồn tại!", "Lỗi áp dụng", JOptionPane.WARNING_MESSAGE);
                txtMaKhuyenMai.setText("");
                txtMaKhuyenMai.requestFocus();
                return;
            }

            double tongTien = activeHoaDon.getTongTien();
            if (tongTien < km.getDieuKienApDung()) {
                JOptionPane.showMessageDialog(this,
                        "Không thể áp dụng mã này:\nHóa đơn chưa đạt giá trị tối thiểu (" + km.getDieuKienApDung() + " VNĐ)",
                        "Lỗi áp dụng", JOptionPane.WARNING_MESSAGE);
                txtMaKhuyenMai.requestFocus();
                return;
            }

            boolean isUpdated = hoaDonService.capNhatMaKM(activeHoaDon.getMaHD(), maKM_input);

            if (isUpdated) {
                activeHoaDon.setMaKM(maKM_input);
                activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);

                JOptionPane.showMessageDialog(this,
                        "Áp dụng thành công mã: " + km.getTenChuongTrinh() + "\n" +
                                "(Giảm: " + km.getGiaTri() + (km.getLoaiKhuyenMai().toLowerCase().contains("tiền") ? " VNĐ" : "%") + ")",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                updateBillPanelFromHoaDon(activeHoaDon);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: Không thể lưu mã vào hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getHinhThucThanhToan() {
        if (cmbPTThanhToan != null && cmbPTThanhToan.getSelectedItem() != null) {
            return cmbPTThanhToan.getSelectedItem().toString();
        }
        return "Tiền mặt";
    }

    private void loadTablesFromService() {
        this.allTableDTOsFromDB = banService.getAllBan();
        if (this.allTableDTOsFromDB == null) {
            this.allTableDTOsFromDB = new ArrayList<>();
        }

        this.allTablesFromDB = new ArrayList<>();

        for (BanDTO dto : allTableDTOsFromDB) {
            this.allTablesFromDB.add(toEntity(dto));
        }
    }

    private Ban toEntity(BanDTO dto) {
        if (dto == null) return null;

        Ban ban = new Ban();
        ban.setMaBan(dto.getMaBan());
        ban.setTenBan(dto.getTenBan());
        ban.setSoGhe(dto.getSoGhe());
        ban.setKhuVuc(dto.getKhuVuc());
        ban.setTrangThai(parseTrangThai(dto.getTrangThai()));
        ban.setGioMoBan(parseLocalDateTime(dto.getGioMoBan()));

        return ban;
    }

    private TrangThaiBan parseTrangThai(Object value) {
        if (value == null) return TrangThaiBan.TRONG;

        if (value instanceof TrangThaiBan) {
            return (TrangThaiBan) value;
        }

        try {
            return TrangThaiBan.valueOf(value.toString());
        } catch (Exception e) {
            return TrangThaiBan.TRONG;
        }
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) return null;

        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }

        try {
            return LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSameBan(Ban a, Ban b) {
        if (a == null || b == null) return false;
        if (a.getMaBan() == null || b.getMaBan() == null) return false;
        return a.getMaBan().equals(b.getMaBan());
    }

    private void updateBillPanelFromHoaDon(HoaDonDTO hoaDon) {
        if (billPanel == null) return;

        if (hoaDon == null) {
            billPanel.clearBill();
            return;
        }

        long tongTien = 0;
        long giamGia = getLongValue(hoaDon, 0,
                "getGiamGia",
                "getGiamgia");

        int tongSoLuong = 0;
        boolean coChiTiet = false;

        try {
            String maDon = hoaDon.getMaDon();

            if (maDon != null && !maDon.trim().isEmpty()) {
                ChiTietHoaDonDTO filterDTO = ChiTietHoaDonDTO.builder()
                        .maDon(maDon)
                        .build();

                List<ChiTietHoaDonDTO> dsChiTiet = chiTietHoaDonService.getChiTietTheoMaDon(filterDTO);

                if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                    coChiTiet = true;

                    for (ChiTietHoaDonDTO ct : dsChiTiet) {
                        tongSoLuong += ct.getSoLuong();
                        tongTien += Math.round(ct.getThanhTien());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Nếu không lấy được chi tiết thì fallback về tổng tiền trong HoaDonDTO
        if (!coChiTiet) {
            tongTien = getLongValue(hoaDon, 0,
                    "getTongTien",
                    "getTongtien");

            tongSoLuong = getTongSoLuongFromHoaDonDTO(hoaDon);
        }

        long tongThanhToan = tongTien - giamGia;

        if (tongThanhToan < 0) {
            tongThanhToan = 0;
        }

        billPanel.loadBillTotals(tongTien, giamGia, tongThanhToan, tongSoLuong);
    }

    private int getTongSoLuongFromHoaDonDTO(HoaDonDTO hoaDon) {
        Object dsChiTiet = getObjectValue(hoaDon,
                "getDsChiTiet",
                "getDsChiTietHoaDon",
                "getChiTietHoaDons",
                "getChiTietHoaDonDTOS");

        if (dsChiTiet instanceof Collection<?>) {
            int total = 0;

            for (Object item : (Collection<?>) dsChiTiet) {
                total += (int) getLongValue(item, 0,
                        "getSoluong",
                        "getSoLuong");
            }

            return total;
        }

        return 0;
    }

    private String getStringValue(Object target, String... methodNames) {
        Object value = getObjectValue(target, methodNames);
        return value == null ? null : value.toString();
    }

    private long getLongValue(Object target, long defaultValue, String... methodNames) {
        Object value = getObjectValue(target, methodNames);

        if (value == null) return defaultValue;

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Math.round(Double.parseDouble(value.toString()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Object getObjectValue(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;

        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}