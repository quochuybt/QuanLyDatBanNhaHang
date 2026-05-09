package iuh.fit.gui;

import iuh.fit.core.dto.*;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.net.client.BanRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ManHinhBanGUI extends JPanel {

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);
    public static final Color COLOR_STATUS_OCCUPIED = new Color(239, 68, 68);
    public static final Color COLOR_STATUS_RESERVED = new Color(187, 247, 208);

    private javax.swing.Timer timerTuDongCapNhat;

    private final BanRemoteService banRemoteService;

    /*
     * Tạm thời giữ local service cho các nghiệp vụ chưa chuyển socket:
     * hóa đơn, chi tiết hóa đơn, khuyến mãi, khách hàng, đơn đặt món.
     */
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
    private final List<BanPanel> leftBanPanelList = new ArrayList<>();
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

    private JTextField txtGhiChu;
    private JLabel statusColorBox;
    private BillPanel billPanel;

    private KhachHangDTO khachHangDangChon;

    public static class KhachHangTam {
        private final String maKH;
        private final String sdt;
        private final String tenKH;

        public KhachHangTam(String maKH, String sdt, String tenKH) {
            this.maKH = maKH;
            this.sdt = sdt;
            this.tenKH = tenKH;
        }

        public String getMaKH() {
            return maKH;
        }

        public String getSdt() {
            return sdt;
        }

        public String getTenKH() {
            return tenKH;
        }
    }

    private static final Map<String, KhachHangTam> KHACH_HANG_TAM_THEO_BAN = new ConcurrentHashMap<>();

    public static KhachHangTam layKhachHangTamTheoBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }

        return KHACH_HANG_TAM_THEO_BAN.get(maBan);
    }

    public static void xoaKhachHangTamTheoBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return;
        }

        KHACH_HANG_TAM_THEO_BAN.remove(maBan);
    }

    public ManHinhBanGUI() {
        this(null, null);
    }

    public ManHinhBanGUI(DanhSachBanGUI parent) {
        this(parent, parent != null ? parent.getConnection() : null);
    }

    public ManHinhBanGUI(DanhSachBanGUI parent, SocketClientConnection connection) {
        super(new BorderLayout());

        this.parentDanhSachBanGUI = parent;

        SocketClientConnection socketConnection = connection;

        if (socketConnection == null && parent != null) {
            socketConnection = parent.getConnection();
        }

        this.banRemoteService = new BanRemoteService(
                Objects.requireNonNull(socketConnection, "SocketClientConnection không được null.")
        );

        buildUI();
        khoiTaoTuDongCapNhat();

        SwingUtilities.invokeLater(this::refreshTableList);
    }

    public String getMaKHDangChon() {
        luuKhachHangTamTheoBan(true);
        return khachHangDangChon != null ? khachHangDangChon.getMaKH() : null;
    }

    public String getSDTKhachDangNhap() {
        return txtSDTKhach != null ? txtSDTKhach.getText().trim() : "";
    }

    public String getTenKhachDangNhap() {
        return txtHoTenKhach != null ? txtHoTenKhach.getText().trim() : "";
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
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 0, 10, 10));

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

        add(splitPane, BorderLayout.CENTER);

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
        txtGhiChu = createStyledTextField(true);

        txtGhiChu.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
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
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                timKiemThanhVienTuSDT();
            }
        });

        ganListenerLuuKhachHangTam();

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

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(createInfoBox("Ghi chú", txtGhiChu), gbc);

        txtMaKhuyenMai = createStyledTextField(true);
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Mã khuyến mãi", txtMaKhuyenMai), gbc);

        btnApDungKM = new JButton("Áp dụng");
        btnApDungKM.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApDungKM.setBackground(ManHinhBanGUI.COLOR_STATUS_RESERVED);
        btnApDungKM.setForeground(Color.DARK_GRAY);
        btnApDungKM.setFocusPainted(false);
        btnApDungKM.setPreferredSize(new Dimension(80, 35));

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(20, 0, 10, 8);
        infoPanel.add(btnApDungKM, gbc);

        gbc.insets = new Insets(5, 8, 10, 8);

        this.billPanel = new BillPanel(this);

        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                infoPanel,
                this.billPanel
        );

        verticalSplitPane.setDividerLocation(200);
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

    private void ganListenerLuuKhachHangTam() {
        DocumentListener listener = new DocumentListener() {
            private void save() {
                if ((txtSDTKhach != null && txtSDTKhach.hasFocus())
                        || (txtHoTenKhach != null && txtHoTenKhach.hasFocus())) {
                    luuKhachHangTamTheoBan(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                save();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                save();
            }
        };

        txtSDTKhach.getDocument().addDocumentListener(listener);
        txtHoTenKhach.getDocument().addDocumentListener(listener);
    }

    private void luuKhachHangTamTheoBan(boolean timTheoSDT) {
        if (selectedTable == null || selectedTable.getMaBan() == null) {
            return;
        }

        String maBan = selectedTable.getMaBan();
        String sdt = txtSDTKhach != null ? txtSDTKhach.getText().trim() : "";
        String tenKH = txtHoTenKhach != null ? txtHoTenKhach.getText().trim() : "";

        if (sdt.isEmpty() && tenKH.isEmpty()) {
            KHACH_HANG_TAM_THEO_BAN.remove(maBan);
            khachHangDangChon = null;
            return;
        }

        String maKH = khachHangDangChon != null ? khachHangDangChon.getMaKH() : null;

        if (timTheoSDT && !sdt.isEmpty()) {
            try {
                KhachHangDTO kh = khachHangService.findBySdtDTO(sdt);

                if (kh != null) {
                    khachHangDangChon = kh;
                    maKH = kh.getMaKH();

                    if (kh.getTenKH() != null && !kh.getTenKH().trim().isEmpty()) {
                        tenKH = kh.getTenKH();
                        txtHoTenKhach.setText(tenKH);
                    }

                    txtThanhVien.setText(kh.getHangThanhVien() != null
                            ? kh.getHangThanhVien().toString()
                            : "Thành viên");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        KHACH_HANG_TAM_THEO_BAN.put(maBan, new KhachHangTam(maKH, sdt, tenKH));
    }

    private void hienThiKhachHangTamNeuCo(String maBan) {
        KhachHangTam khTam = layKhachHangTamTheoBan(maBan);

        if (khTam == null) {
            return;
        }

        txtSDTKhach.setText(khTam.getSdt() != null ? khTam.getSdt() : "");
        txtHoTenKhach.setText(khTam.getTenKH() != null ? khTam.getTenKH() : "");

        if (khTam.getMaKH() != null && !khTam.getMaKH().trim().isEmpty()) {
            try {
                KhachHangDTO kh = khachHangService.findByIdDTO(khTam.getMaKH());

                if (kh != null) {
                    khachHangDangChon = kh;
                    txtThanhVien.setText(kh.getHangThanhVien() != null
                            ? kh.getHangThanhVien().toString()
                            : "Thành viên");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        khachHangDangChon = null;
        txtThanhVien.setText("Vãng lai");
    }

    private void updateRightPanelDetails(Ban ban, String tenHienThi) {
        if (ban == null) {
            khachHangDangChon = null;
            statusColorBox.setBackground(COLOR_STATUS_FREE);
            lblTenBanHeader.setText("Chọn một bàn");
            lblKhuVucHeader.setText("");
            txtTinhTrang.setText("");
            cmbPTThanhToan.setSelectedItem("Tiền mặt");
            txtSDTKhach.setText("");
            txtHoTenKhach.setText("");
            txtThanhVien.setText("");
            txtMaKhuyenMai.setText("");
            txtGhiChu.setText("");
            txtNgayVao.setText("");
            txtGioVao.setText("");
            updateBillPanelFromHoaDon(null);
            return;
        }

        khachHangDangChon = null;

        String maBanThucTe = ban.getMaBan();

        try {
            DonDatMonDTO checkDon = donDatMonService.getDonDatMonChuaNhanTheoMaBanBaoGomLinked(ban.getMaBan());

            if (checkDon != null && checkDon.getGhiChu() != null && checkDon.getGhiChu().startsWith("LINKED:")) {
                maBanThucTe = checkDon.getGhiChu().replace("LINKED:", "").trim();
            }
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra bàn ghép: " + e.getMessage());
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
            default:
                statusColor = COLOR_STATUS_OCCUPIED;
                tinhTrangText = "Đang phục vụ";
                break;
        }

        statusColorBox.setBackground(statusColor);

        String headerName = tenHienThi != null && !tenHienThi.isEmpty()
                ? tenHienThi
                : ban.getTenBan();

        lblTenBanHeader.setText(headerName + " -- " + ban.getKhuVuc());
        txtTinhTrang.setText(tinhTrangText);

        cmbPTThanhToan.setSelectedItem("Tiền mặt");
        txtSDTKhach.setText("");
        txtHoTenKhach.setText("");
        txtThanhVien.setText("Vãng lai");
        txtGhiChu.setText("");
        txtMaKhuyenMai.setText("");

        DateTimeFormatter dtfNgay = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter dtfGio = DateTimeFormatter.ofPattern("HH:mm");
        HoaDonDTO activeHoaDon = null;

        if (trangThai == TrangThaiBan.DANG_PHUC_VU) {
            activeHoaDon = hoaDonService.getHoaDonChuaThanhToan(maBanThucTe);

            if (activeHoaDon != null) {
                activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);

                fillCustomerInfo(activeHoaDon.getMaKH());

                if (activeHoaDon.getNgayLap() != null) {
                    txtNgayVao.setText(activeHoaDon.getNgayLap().format(dtfNgay));
                    txtGioVao.setText(activeHoaDon.getNgayLap().format(dtfGio));
                }

                try {
                    DonDatMonDTO donGoc = donDatMonService.findById(activeHoaDon.getMaDon());

                    if (donGoc != null && donGoc.getGhiChu() != null) {
                        String gc = donGoc.getGhiChu();

                        if (gc.contains("LINKED:")) {
                            gc = gc.substring(0, gc.indexOf("LINKED:")).trim();
                        }

                        txtGhiChu.setText(gc);
                    }
                } catch (Exception ignored) {
                }

                txtMaKhuyenMai.setText(activeHoaDon.getMaKM() != null ? activeHoaDon.getMaKM() : "");

                String httt = getStringValue(activeHoaDon, "getHinhThucThanhToan", "getHinhthucthanhtoan");
                if (httt != null && !httt.isBlank()) {
                    cmbPTThanhToan.setSelectedItem(httt);
                }
            } else {
                hienThiKhachHangTamNeuCo(ban.getMaBan());
            }
        } else if (trangThai == TrangThaiBan.DA_DAT_TRUOC) {
            try {
                DonDatMonDTO donDatTruoc = donDatMonService.getDonDatMonDatTruoc(maBanThucTe);

                if (donDatTruoc != null) {
                    fillCustomerInfo(donDatTruoc.getMaKH());

                    String gc = donDatTruoc.getGhiChu();

                    if (gc != null) {
                        if (gc.contains("LINKED:")) {
                            gc = gc.substring(0, gc.indexOf("LINKED:")).trim();
                        }

                        txtGhiChu.setText(gc);
                    }

                    if (donDatTruoc.getThoiGianDen() != null) {
                        txtNgayVao.setText(donDatTruoc.getThoiGianDen().format(dtfNgay));
                        txtGioVao.setText(donDatTruoc.getThoiGianDen().format(dtfGio));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            hienThiKhachHangTamNeuCo(ban.getMaBan());
        }

        if (billPanel != null) {
            billPanel.setCustomHeader(lblTenBanHeader.getText());
        }

        updateBillPanelFromHoaDon(activeHoaDon);
    }

    private void fillCustomerInfo(String maKH) {
        if (maKH != null && !maKH.trim().isEmpty()) {
            try {
                KhachHangDTO kh = khachHangService.findByIdDTO(maKH);

                if (kh != null) {
                    khachHangDangChon = kh;
                    txtSDTKhach.setText(kh.getSdt() != null ? kh.getSdt() : "");
                    txtHoTenKhach.setText(kh.getTenKH() != null ? kh.getTenKH() : "");
                    txtThanhVien.setText(kh.getHangThanhVien() != null
                            ? kh.getHangThanhVien().toString()
                            : "Thành viên");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        khachHangDangChon = null;
        txtThanhVien.setText("Vãng lai");
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
        if (statsPanel == null) {
            return;
        }

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
        if (leftTableContainer == null) {
            return;
        }

        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        for (Ban ban : allTablesFromDB) {
            if (ban == null) {
                continue;
            }

            if ("Tất cả".equals(khuVucFilter) || khuVucFilter.equals(ban.getKhuVuc())) {
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
        String maBanDangChon = selectedTable != null ? selectedTable.getMaBan() : null;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<List<BanDTO>, Void>() {
            @Override
            protected List<BanDTO> doInBackground() {
                return banRemoteService.getAllBan();
            }

            @Override
            protected void done() {
                try {
                    allTableDTOsFromDB = get();

                    if (allTableDTOsFromDB == null) {
                        allTableDTOsFromDB = new ArrayList<>();
                    }

                    allTablesFromDB = new ArrayList<>();

                    for (BanDTO dto : allTableDTOsFromDB) {
                        Ban ban = toEntity(dto);

                        if (ban != null) {
                            allTablesFromDB.add(ban);
                        }
                    }

                    selectedTable = null;

                    if (maBanDangChon != null) {
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
                    JOptionPane.showMessageDialog(
                            ManHinhBanGUI.this,
                            "Lỗi khi làm mới danh sách bàn qua socket.\nChi tiết: " + getRootMessage(e),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void xuLyCapNhatGhiChu() {
        HoaDonDTO activeHoaDon = getActiveHoaDon();

        if (activeHoaDon == null) {
            return;
        }

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
                JOptionPane.showMessageDialog(
                        this,
                        "Có lỗi xảy ra khi cập nhật ghi chú: " + e.getMessage(),
                        "Lỗi Dữ Liệu",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void timKiemThanhVienTuSDT() {
        String sdt = txtSDTKhach.getText().trim();

        if (sdt.isEmpty()) {
            khachHangDangChon = null;

            txtHoTenKhach.setText("");
            txtThanhVien.setText("Vãng lai");

            if (selectedTable != null && selectedTable.getMaBan() != null) {
                KHACH_HANG_TAM_THEO_BAN.remove(selectedTable.getMaBan());
            }

            HoaDonDTO activeHD = getActiveHoaDon();

            if (activeHD != null) {
                hoaDonService.capNhatMaKH(activeHD.getMaHD(), null);
                activeHD = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHD);
                updateBillPanelFromHoaDon(activeHD);
            }

            return;
        }

        try {
            KhachHangDTO khachHang = khachHangService.findBySdtDTO(sdt);

            if (khachHang != null) {
                khachHangDangChon = khachHang;

                txtHoTenKhach.setText(khachHang.getTenKH());
                txtThanhVien.setText(khachHang.getHangThanhVien() != null
                        ? khachHang.getHangThanhVien().toString()
                        : "Thành viên");

                HoaDonDTO activeHD = getActiveHoaDon();

                if (activeHD != null) {
                    hoaDonService.capNhatMaKH(activeHD.getMaHD(), khachHang.getMaKH());
                    activeHD = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHD);
                    updateBillPanelFromHoaDon(activeHD);
                }
            } else {
                khachHangDangChon = null;

                if (txtHoTenKhach.getText() == null || txtHoTenKhach.getText().trim().isEmpty()) {
                    txtHoTenKhach.setText("");
                }

                txtThanhVien.setText("Vãng lai");

                HoaDonDTO activeHD = getActiveHoaDon();

                if (activeHD != null) {
                    hoaDonService.capNhatMaKH(activeHD.getMaHD(), null);
                    activeHD = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHD);
                    updateBillPanelFromHoaDon(activeHD);
                }
            }

            luuKhachHangTamTheoBan(false);

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

    private boolean isKhuyenMaiHopLe(KhuyenMaiDTO km) {
        if (km == null) {
            return false;
        }

        LocalDate now = LocalDate.now();

        String trangThai = km.getTrangThai() == null ? "" : km.getTrangThai().trim();

        boolean dungTrangThai = "Đang áp dụng".equalsIgnoreCase(trangThai);
        boolean daBatDau = km.getNgayBatDau() == null || !km.getNgayBatDau().isAfter(now);
        boolean chuaHetHan = km.getNgayKetThuc() == null || !km.getNgayKetThuc().isBefore(now);

        int gioiHan = km.getSoLuongGioiHan();
        int daDung = km.getSoLuotDaDung();

        boolean conLuot = gioiHan <= 0 || daDung < gioiHan;

        return dungTrangThai && daBatDau && chuaHetHan && conLuot;
    }

    private void xuLyApDungKhuyenMai() {
        HoaDonDTO activeHoaDon = getActiveHoaDon();

        if (activeHoaDon == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn bàn đang phục vụ có hóa đơn trước khi áp dụng khuyến mãi.",
                    "Chưa có hóa đơn",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String maKMInput = txtMaKhuyenMai.getText().trim();

        if (maKMInput.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập mã khuyến mãi.",
                    "Thiếu mã khuyến mãi",
                    JOptionPane.WARNING_MESSAGE
            );
            txtMaKhuyenMai.requestFocus();
            return;
        }

        try {
            KhuyenMaiDTO km = khuyenMaiService.findByIdDTO(maKMInput);

            if (km == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Mã khuyến mãi không tồn tại!",
                        "Lỗi áp dụng",
                        JOptionPane.WARNING_MESSAGE
                );
                txtMaKhuyenMai.setText("");
                txtMaKhuyenMai.requestFocus();
                return;
            }

            if (!isKhuyenMaiHopLe(km)) {
                hoaDonService.capNhatMaKM(activeHoaDon.getMaHD(), null);
                activeHoaDon.setMaKM(null);

                activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);
                updateBillPanelFromHoaDon(activeHoaDon);

                JOptionPane.showMessageDialog(
                        this,
                        "Mã khuyến mãi đã hết hạn, chưa bắt đầu, hết lượt dùng hoặc ngưng áp dụng.",
                        "Không thể áp dụng",
                        JOptionPane.WARNING_MESSAGE
                );

                txtMaKhuyenMai.setText("");
                txtMaKhuyenMai.requestFocus();
                return;
            }

            double tongTien = activeHoaDon.getTongTien();

            if (tongTien < km.getDieuKienApDung()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể áp dụng mã này:\nHóa đơn chưa đạt giá trị tối thiểu ("
                                + km.getDieuKienApDung() + " VNĐ)",
                        "Lỗi áp dụng",
                        JOptionPane.WARNING_MESSAGE
                );
                txtMaKhuyenMai.requestFocus();
                return;
            }

            boolean isUpdated = hoaDonService.capNhatMaKM(activeHoaDon.getMaHD(), maKMInput);

            if (isUpdated) {
                activeHoaDon.setMaKM(maKMInput);
                activeHoaDon = hoaDonService.tinhLaiGiamGiaVaTongTien(activeHoaDon);

                JOptionPane.showMessageDialog(
                        this,
                        "Áp dụng thành công mã: " + km.getTenChuongTrinh() + "\n"
                                + "(Giảm: " + km.getGiaTri()
                                + (km.getLoaiKhuyenMai() != null
                                && km.getLoaiKhuyenMai().toLowerCase().contains("tiền")
                                ? " VNĐ" : "%") + ")",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                updateBillPanelFromHoaDon(activeHoaDon);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi hệ thống: Không thể lưu mã vào hóa đơn.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hệ thống: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public String getHinhThucThanhToan() {
        if (cmbPTThanhToan != null && cmbPTThanhToan.getSelectedItem() != null) {
            return cmbPTThanhToan.getSelectedItem().toString();
        }

        return "Tiền mặt";
    }

    private void loadTablesFromService() {
        this.allTableDTOsFromDB = banRemoteService.getAllBan();

        if (this.allTableDTOsFromDB == null) {
            this.allTableDTOsFromDB = new ArrayList<>();
        }

        this.allTablesFromDB = new ArrayList<>();

        for (BanDTO dto : allTableDTOsFromDB) {
            Ban ban = toEntity(dto);

            if (ban != null) {
                this.allTablesFromDB.add(ban);
            }
        }
    }

    private Ban toEntity(BanDTO dto) {
        if (dto == null) {
            return null;
        }

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
        if (value == null) {
            return TrangThaiBan.TRONG;
        }

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
        if (value == null) {
            return null;
        }

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
        if (a == null || b == null) {
            return false;
        }

        if (a.getMaBan() == null || b.getMaBan() == null) {
            return false;
        }

        return a.getMaBan().equals(b.getMaBan());
    }

    private void updateBillPanelFromHoaDon(HoaDonDTO hoaDon) {
        if (billPanel == null) {
            return;
        }

        if (hoaDon == null) {
            billPanel.clearBill();
            return;
        }

        long tongTien = getLongValue(
                hoaDon,
                0,
                "getTongTien",
                "getTongtien"
        );

        long giamGia = getLongValue(
                hoaDon,
                0,
                "getGiamGia",
                "getGiamgia"
        );

        long tongThanhToan = getLongValue(
                hoaDon,
                0,
                "getTongThanhToan",
                "getTongthanhtoan"
        );

        if (tongThanhToan <= 0 && tongTien > 0) {
            tongThanhToan = Math.max(0, tongTien - giamGia);
        }

        int tongSoLuong = tinhTongSoLuongTheoHoaDon(hoaDon);

        billPanel.loadBillTotals(tongTien, giamGia, tongThanhToan, tongSoLuong);
    }

    private int tinhTongSoLuongTheoHoaDon(HoaDonDTO hoaDon) {
        if (hoaDon == null || hoaDon.getMaDon() == null || hoaDon.getMaDon().trim().isEmpty()) {
            return 0;
        }

        try {
            ChiTietHoaDonDTO filter = ChiTietHoaDonDTO.builder()
                    .maDon(hoaDon.getMaDon())
                    .build();

            List<ChiTietHoaDonDTO> dsChiTiet = chiTietHoaDonService.getChiTietTheoMaDon(filter);

            if (dsChiTiet == null) {
                return 0;
            }

            int tong = 0;

            for (ChiTietHoaDonDTO ct : dsChiTiet) {
                if (ct != null) {
                    tong += ct.getSoLuong();
                }
            }

            return tong;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private long getLongValue(Object target, long defaultValue, String... getterNames) {
        if (target == null || getterNames == null) {
            return defaultValue;
        }

        for (String getterName : getterNames) {
            try {
                Method method = target.getClass().getMethod(getterName);
                Object value = method.invoke(target);

                if (value instanceof Number) {
                    return Math.round(((Number) value).doubleValue());
                }

                if (value != null) {
                    return Math.round(Double.parseDouble(value.toString()));
                }

            } catch (Exception ignored) {
            }
        }

        return defaultValue;
    }

    private String getStringValue(Object target, String... getterNames) {
        if (target == null || getterNames == null) {
            return null;
        }

        for (String getterName : getterNames) {
            try {
                Method method = target.getClass().getMethod(getterName);
                Object value = method.invoke(target);

                if (value != null) {
                    return value.toString();
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getRootMessage(Exception e) {
        Throwable t = e;

        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t.getMessage() != null ? t.getMessage() : e.getMessage();
    }

    public void disposeTimer() {
        if (timerTuDongCapNhat != null) {
            timerTuDongCapNhat.stop();
        }
    }
}