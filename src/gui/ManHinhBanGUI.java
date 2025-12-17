package gui;

import dao.*;
import entity.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManHinhBanGUI extends JPanel {

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);
    public static final Color COLOR_STATUS_OCCUPIED = new Color(239, 68, 68);
    public static final Color COLOR_STATUS_RESERVED = new Color(187, 247, 208);

    private javax.swing.Timer timerTuDongCapNhat;

    private List<Ban> allTablesFromDB;
    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;
    private KhachHangDAO khachHangDAO;
    private DonDatMonDAO donDatMonDAO;
    private Ban selectedTable = null;
    private JPanel leftTableContainer;
    private JPanel statsPanel;
    private JTextField txtMaKhuyenMai;
    private JButton btnApDungKM;
    private String currentLeftFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private DanhSachBanGUI parentDanhSachBanGUI;
    private KhuyenMaiDAO maKhuyenMaiDAO;

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

    public ManHinhBanGUI(DanhSachBanGUI parent) {
        super(new BorderLayout());
        this.parentDanhSachBanGUI = parent;
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.donDatMonDAO = new DonDatMonDAO();
        this.maKhuyenMaiDAO = new KhuyenMaiDAO();
        buildUI();
        khoiTaoTuDongCapNhat();
    }

    private void khoiTaoTuDongCapNhat() {
        int thoiGianLap = 60 * 1000;
        timerTuDongCapNhat = new javax.swing.Timer(thoiGianLap, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                refreshTableList();
            }
        });

        timerTuDongCapNhat.start();
    }

    private void xuLyApDungKhuyenMai() {
        HoaDon activeHoaDon = getActiveHoaDon();
        if (activeHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào đang hoạt động!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKM_input = txtMaKhuyenMai.getText().trim().toUpperCase();

        if (maKM_input.isEmpty()) {
            activeHoaDon.setMaKM(null);
            hoaDonDAO.capNhatMaKM(activeHoaDon.getMaHD(), null);
            activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
            updateBillPanelFromHoaDon(activeHoaDon);
            return;
        }

        activeHoaDon.tinhLaiTongTienTuChiTiet();
        double tongTien = activeHoaDon.getTongTien();
        String maKH = activeHoaDon.getMaKH();
        if (maKH == null) maKH = "";

        String ketQuaKiemTra = maKhuyenMaiDAO.kiemTraDieuKienSuDung(maKM_input, maKH, tongTien);

        if ("OK".equals(ketQuaKiemTra)) {

            entity.KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(maKM_input);

            activeHoaDon.setMaKM(maKM_input);

            if (hoaDonDAO.capNhatMaKM(activeHoaDon.getMaHD(), maKM_input)) {
                JOptionPane.showMessageDialog(this,
                        "Áp dụng thành công mã: " + km.getTenChuongTrinh() + "\n" +
                                "(Giảm: " + km.getGiaTri() + (km.getLoaiKhuyenMai().contains("tiền") ? " VNĐ" : "%") + ")",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: Không thể lưu mã vào hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể áp dụng mã này:\n" + ketQuaKiemTra,
                    "Lỗi áp dụng", JOptionPane.WARNING_MESSAGE);

            activeHoaDon.setMaKM(null);
            hoaDonDAO.capNhatMaKM(activeHoaDon.getMaHD(), null);
            txtMaKhuyenMai.requestFocus();
        }

        activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
        updateBillPanelFromHoaDon(activeHoaDon);
    }

    public HoaDon getActiveHoaDon() {
        if (selectedTable != null && selectedTable.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            return hoaDonDAO.getHoaDonChuaThanhToan(selectedTable.getMaBan());
        }
        return null;
    }

    private void updateBillPanelFromHoaDon(HoaDon hoaDon) {
        if (billPanel != null && hoaDon != null) {
            int tongSoLuong = 0;
            if (hoaDon.getDsChiTiet() != null) {
                for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
                    tongSoLuong += ct.getSoluong();
                }
            }
            billPanel.loadBillTotals(
                    (long) hoaDon.getTongTien(),
                    (long) hoaDon.getGiamGia(),
                    (long) hoaDon.getTongThanhToan(),
                    tongSoLuong
            );
        } else if (billPanel != null) {
            billPanel.clearBill();
        }
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
            this.allTablesFromDB = banDAO.getAllBan();
            int maxSoThuTu = banDAO.getSoThuTuBanLonNhat();
            Ban.setSoThuTuBanHienTai(maxSoThuTu);

        } catch (Exception e) {
            e.printStackTrace();
            this.allTablesFromDB = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối hoặc tải dữ liệu Bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi CSDL",
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
        String tenHienThi = null;
        if (selectedTable != null) {
            tenHienThi = banDAO.getTenHienThiGhep(selectedTable.getMaBan());
        }
        updateRightPanelDetails(selectedTable, tenHienThi);
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

        JPanel container = new JPanel(new BorderLayout(0, 10));
        container.setOpaque(false);

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

    private void timKiemThanhVienTuSDT() {
        String sdt = txtSDTKhach.getText().trim();
        HoaDon activeHoaDon = getActiveHoaDon();
        if (activeHoaDon == null) return;

        if (!sdt.isEmpty() && sdt.matches("\\d{10}")) {
            entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt);
            kh = khachHangDAO.timTheoSDT(sdt);
            if (kh != null) {
                txtHoTenKhach.setText(kh.getTenKH());
                txtThanhVien.setText(kh.getHangThanhVien().toString());

                activeHoaDon.setMaKH(kh.getMaKH());

                donDatMonDAO.capNhatMaKH(activeHoaDon.getMaDon(), kh.getMaKH());

                activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);

                updateBillPanelFromHoaDon(activeHoaDon);
            } else {
                txtHoTenKhach.setText("");
                txtThanhVien.setText("Vãng lai");
                activeHoaDon.setMaKH(null);
                donDatMonDAO.capNhatMaKH(activeHoaDon.getMaDon(), null);
            }
        }
    }

    private void tinhVaCapNhatGiamGia(HoaDon hoaDon) {
        if (hoaDon == null) return;

        float tongCong = hoaDon.getTongTien();
        float giamGiaTV = 0;
        float giamGiaMa = 0;

        if (hoaDon.getMaKH() != null) {
            KhachHang kh = khachHangDAO.timTheoMaKH(hoaDon.getMaKH());
            if (kh != null) {
                float phanTramGiamTV = getPhanTramGiamTheoHang(kh.getHangThanhVien());
                giamGiaTV = tongCong * phanTramGiamTV / 100;
            }
        }

        if (hoaDon.getMaKM() != null && !hoaDon.getMaKM().isEmpty()) {
            entity.KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(hoaDon.getMaKM());
            if (km != null) {
                if (tongCong >= km.getDieuKienApDung()) {
                    if ("Phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm theo phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai())) {
                        giamGiaMa = tongCong * (float) km.getGiaTri() / 100;
                    } else if ("Số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm giá số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai())) {
                        giamGiaMa = (float) km.getGiaTri();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Hóa đơn không đủ điều kiện (cần " + km.getDieuKienApDung() + "đ) để áp dụng mã " + hoaDon.getMaKM(), "Không đủ điều kiện", JOptionPane.WARNING_MESSAGE);
                    hoaDon.setMaKM(null);
                    txtMaKhuyenMai.setText("");
                    hoaDonDAO.capNhatMaKM(hoaDon.getMaHD(), null);
                }
            } else {
                hoaDon.setMaKM(null);
                txtMaKhuyenMai.setText("");
                hoaDonDAO.capNhatMaKM(hoaDon.getMaHD(), null);
            }
        }

        float tongGiamGia = giamGiaTV + giamGiaMa;

        hoaDon.setGiamGia(tongGiamGia);
        hoaDon.tinhLaiTongThanhToan();
    }

    private float getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) return 0.0f;
        switch (hang) {
            case DIAMOND:
                return 10.0f;
            case GOLD:
                return 5.0f;
            case SILVER:
                return 3.0f;
            case BRONZE:
                return 2.0f;
            case MEMBER:
                return 0.0f;
            case NONE:
            default:
                return 0.0f;
        }
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
        switch (ban.getTrangThai()) {
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
        String headerName = (tenHienThi != null && !tenHienThi.isEmpty()) ? tenHienThi : ban.getTenBan();
        lblTenBanHeader.setText(headerName + " -- " + ban.getKhuVuc());

        txtTinhTrang.setText(tinhTrangText);

        DateTimeFormatter dtfNgay = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter dtfGio = DateTimeFormatter.ofPattern("HH:mm");
        HoaDon activeHoaDon = null;
        String rawGhiChu = "";

        cmbPTThanhToan.setSelectedItem("Tiền mặt");
        txtSDTKhach.setText("");
        txtHoTenKhach.setText("");
        txtThanhVien.setText("");
        txtSoLuongKhach.setText("");
        txtGhiChu.setText("");
        txtNgayVao.setText("");
        txtGioVao.setText("");

        if (ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
            entity.DonDatMon ddm = donDatMonDAO.getDonDatMonDatTruoc(ban.getMaBan());
            LocalDateTime gioKhachHen = null;

            if (ddm != null) {
                gioKhachHen = ddm.getThoiGianDen();
                if (gioKhachHen == null) gioKhachHen = ddm.getNgayKhoiTao();
                rawGhiChu = (ddm.getGhiChu() != null) ? ddm.getGhiChu() : "";

                if (ddm.getMaKH() != null) {
                    entity.KhachHang kh = khachHangDAO.timTheoMaKH(ddm.getMaKH());
                    if (kh != null) {
                        txtSDTKhach.setText(kh.getSdt());
                        txtHoTenKhach.setText(kh.getTenKH());
                        txtThanhVien.setText(kh.getHangThanhVien().toString());
                    }
                }
            } else {
                gioKhachHen = ban.getGioMoBan();
            }

            txtNgayVao.setText(gioKhachHen != null ? gioKhachHen.format(dtfNgay) : "");
            txtGioVao.setText(gioKhachHen != null ? gioKhachHen.format(dtfGio) : "");
            txtSoLuongKhach.setText(String.valueOf(ban.getSoGhe()));
            cmbPTThanhToan.setSelectedItem("Chưa thanh toán");

        } else if (ban.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            activeHoaDon = hoaDonDAO.getHoaDonChuaThanhToan(ban.getMaBan());

            if (activeHoaDon != null) {
                LocalDateTime gioVao = activeHoaDon.getNgayLap();
                txtNgayVao.setText(gioVao != null ? gioVao.format(dtfNgay) : "");
                txtGioVao.setText(gioVao != null ? gioVao.format(dtfGio) : "");

                if (activeHoaDon.getHinhThucThanhToan() != null) {
                    cmbPTThanhToan.setSelectedItem(activeHoaDon.getHinhThucThanhToan());
                }

                entity.DonDatMon ddm = donDatMonDAO.getDonDatMonByMa(activeHoaDon.getMaDon());
                if (ddm != null) {
                    rawGhiChu = (ddm.getGhiChu() != null) ? ddm.getGhiChu() : "";
                }

                entity.KhachHang kh = null;
                if (activeHoaDon.getMaKH() != null) {
                    kh = khachHangDAO.timTheoMaKH(activeHoaDon.getMaKH());
                }

                if (kh != null) {
                    txtSDTKhach.setText(kh.getSdt());
                    txtHoTenKhach.setText(kh.getTenKH());
                    txtThanhVien.setText(kh.getHangThanhVien().toString());
                } else {
                    txtThanhVien.setText("Vãng lai");
                }
                txtMaKhuyenMai.setText(activeHoaDon.getMaKM() != null ? activeHoaDon.getMaKM() : "");

                activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
            }
        }
        if (!rawGhiChu.isEmpty()) {
            if (rawGhiChu.contains("LINKED:")) {
                String cleanNote = rawGhiChu.substring(0, rawGhiChu.indexOf("LINKED:")).trim();
                txtGhiChu.setText(cleanNote);
            } else {
                txtGhiChu.setText(rawGhiChu);
            }
        } else {
            txtGhiChu.setText("");
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
                switch (ban.getTrangThai()) {
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
        leftTableContainer.removeAll();
        leftBanPanelList.clear();
        int countAdded = 0;
        for (Ban ban : allTablesFromDB) {
            if (khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter)) {
                BanPanel banPanel = new BanPanel(ban);
                if (ban.equals(selectedTable)) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        for (Component c : leftTableContainer.getComponents()) {
                            if (c instanceof BanPanel) ((BanPanel) c).setSelected(false);
                        }
                        banPanel.setSelected(true);

                        Ban banThucTeCanLoad = ban;

                        String maBanChinh = banDAO.getMaBanChinh(ban.getMaBan());

                        if (!maBanChinh.equals(ban.getMaBan())) {
                            banThucTeCanLoad = banDAO.getBanByMa(maBanChinh);
                        }

                        String tenHienThiGhep = banDAO.getTenHienThiGhep(ban.getMaBan());

                        selectedTable = banThucTeCanLoad;

                        updateRightPanelDetails(banThucTeCanLoad, tenHienThiGhep);
                    }
                });
                leftTableContainer.add(banPanel);
                leftBanPanelList.add(banPanel);
                countAdded++;
            }
        }
        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    public Ban getSelectedTable() {
        return selectedTable;
    }

    public void refreshTableList() {
        new dao.DonDatMonDAO().capNhatTrangThaiBanTheoGio();
        if (donDatMonDAO != null) {
            donDatMonDAO.tuDongHuyDonQuaGio();
        }
        try {
            this.allTablesFromDB = banDAO.getAllBan();
            if (selectedTable != null) {
                for (Ban b : allTablesFromDB) {
                    if (b.getMaBan().equals(selectedTable.getMaBan())) {
                        break;
                    }
                }
            }
            populateLeftPanel(currentLeftFilter);

            updateStatsPanel();

            if (selectedTable != null) {
                boolean stillExists = false;
                for (Ban ban : allTablesFromDB) {
                    if (ban.equals(selectedTable)) {
                        selectedTable = ban;
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) {
                    selectedTable = null;
                }
                String tenHienThi = null;
                if (selectedTable != null) {
                    tenHienThi = banDAO.getTenHienThiGhep(selectedTable.getMaBan());
                }
                updateRightPanelDetails(selectedTable, tenHienThi);
            } else {
                updateRightPanelDetails(null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi làm mới danh sách bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyCapNhatGhiChu() {
        HoaDon activeHoaDon = getActiveHoaDon();
        if (activeHoaDon == null) return;

        String ghiChuMoi = txtGhiChu.getText().trim();
        String maDon = activeHoaDon.getMaDon();

        if (maDon != null && !maDon.isEmpty()) {
            entity.DonDatMon ddmHienTai = donDatMonDAO.getDonDatMonByMa(maDon);

            String ghiChuHoanChinh = ghiChuMoi;

            if (ddmHienTai != null && ddmHienTai.getGhiChu() != null && ddmHienTai.getGhiChu().contains("LINKED:")) {
                String linkedPart = ddmHienTai.getGhiChu().substring(ddmHienTai.getGhiChu().indexOf("LINKED:"));
                ghiChuHoanChinh = ghiChuMoi + " " + linkedPart;
            }

            if (donDatMonDAO.capNhatGhiChu(maDon, ghiChuHoanChinh)) {
                if (ddmHienTai != null) {
                    ddmHienTai.setGhiChu(ghiChuHoanChinh);
                }
            } else {
                System.err.println("System: Lỗi khi cập nhật ghi chú cho đơn " + maDon);
            }
        }
    }

    public String getHinhThucThanhToan() {
        if (cmbPTThanhToan != null && cmbPTThanhToan.getSelectedItem() != null) {
            return cmbPTThanhToan.getSelectedItem().toString();
        }
        return "Tiền mặt";
    }
}