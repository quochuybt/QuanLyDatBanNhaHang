package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.dto.KhachHangDTO;
import iuh.fit.core.entity.HangThanhVien;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.net.client.BanRemoteService;
import iuh.fit.core.net.client.DonDatMonRemoteService;
import iuh.fit.core.net.client.KhachHangRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class ManHinhDatBanGUI extends JPanel {

    private final BanRemoteService banRemoteService;
    private final KhachHangRemoteService khachHangRemoteService;
    private final DonDatMonRemoteService donDatMonRemoteService;

    private final DanhSachBanGUI parentDanhSachBanGUI_DatBan;
    private final DashboardGUI mainGUI_DatBan;

    private JSpinner spinnerSoLuongKhach;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JTextField txtGhiChu;
    private JPanel pnlBanContainer;

    private List<BanDTO> dsTatCaBan = new ArrayList<>();
    private final List<BanDTO> dsBanDaChon = new ArrayList<>();
    private final List<BanPanel> dsBanPanelHienThi = new ArrayList<>();

    private JTextField txtSDTKhach;
    private JTextField txtHoTenKhach;
    private JButton btnDatBan;

    private JTextField txtTimKiemPhieuDat;
    private JList<DonDatMonDTO> listPhieuDat;
    private DefaultListModel<DonDatMonDTO> modelListPhieuDat;

    private final Map<String, KhachHangDTO> khachHangCacheTheoMa = new HashMap<>();

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final String PLACEHOLDER_SEARCH = " Tìm kiếm bàn đặt SĐT/Tên khách...";

    public ManHinhDatBanGUI(
            DanhSachBanGUI parent,
            DashboardGUI main,
            SocketClientConnection connection
    ) {
        this.parentDanhSachBanGUI_DatBan = parent;
        this.mainGUI_DatBan = main;

        Objects.requireNonNull(connection, "SocketClientConnection không được null.");

        this.banRemoteService = new BanRemoteService(connection);
        this.khachHangRemoteService = new KhachHangRemoteService(connection);
        this.donDatMonRemoteService = new DonDatMonRemoteService(connection);

        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(640);
        splitPane.setBorder(null);

        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 0, 10, 10));

        JPanel pnlLeft = createLeftPanel_DatBan();
        JPanel pnlRight = createRightPanel();

        splitPane.setLeftComponent(pnlLeft);
        splitPane.setRightComponent(pnlRight);

        add(splitPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createLeftPanel_DatBan() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);

        JPanel pnlInputNorth = createInputNorthPanel();
        panel.add(pnlInputNorth, BorderLayout.NORTH);

        JPanel listBanPanel = createListBanPanel_DatBan("Chọn Bàn Trống Phù Hợp");
        panel.add(listBanPanel, BorderLayout.CENTER);

        JPanel pnlInputSouth = createInputSouthPanel();
        panel.add(pnlInputSouth, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createListBanPanel_DatBan(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 5, 0, 5));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        pnlBanContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlBanContainer.setBackground(Color.WHITE);
        pnlBanContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(pnlBanContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.33;

        JLabel lblSoLuong = new JLabel("Số lượng khách");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        panel.add(lblSoLuong, gbc);

        JLabel lblNgayDat = new JLabel("Ngày đặt:");
        lblNgayDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(lblNgayDat, gbc);

        JLabel lblGioDat = new JLabel("Giờ đặt:");
        lblGioDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 2;
        panel.add(lblGioDat, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 10, 5);

        spinnerSoLuongKhach = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spinnerSoLuongKhach.addChangeListener(e -> hienThiBanPhuHop());
        applySpinnerStyle(spinnerSoLuongKhach);
        gbc.gridx = 0;
        panel.add(spinnerSoLuongKhach, gbc);

        Date earliestDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        SpinnerDateModel dateModel = new SpinnerDateModel(
                new Date(),
                earliestDate,
                null,
                Calendar.DAY_OF_MONTH
        );

        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.addChangeListener(e -> hienThiBanPhuHop());
        applySpinnerStyle(dateSpinner);
        gbc.gridx = 1;
        panel.add(dateSpinner, gbc);

        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        applySpinnerStyle(timeSpinner);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        timeSpinner.setValue(cal.getTime());

        timeSpinner.addChangeListener(e -> hienThiBanPhuHop());
        gbc.gridx = 2;
        panel.add(timeSpinner, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 2, 5);

        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblGhiChu, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 10, 5);

        txtGhiChu = new JTextField();
        applyTextFieldStyle(txtGhiChu);
        panel.add(txtGhiChu, gbc);

        return panel;
    }

    private JPanel createInputSouthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 2, 10);

        JLabel lblSDT = new JLabel("SĐT khách:");
        lblSDT.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridy = 0;
        panel.add(lblSDT, gbc);

        txtSDTKhach = new JTextField();
        applyTextFieldStyle(txtSDTKhach);
        txtSDTKhach.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                timKhachHangTheoSDT();
            }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 5, 15, 10);
        panel.add(txtSDTKhach, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 10, 2, 5);

        JLabel lblTen = new JLabel("Họ tên khách:");
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridy = 0;
        panel.add(lblTen, gbc);

        txtHoTenKhach = new JTextField();
        applyTextFieldStyle(txtHoTenKhach);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 10, 15, 5);
        panel.add(txtHoTenKhach, gbc);

        btnDatBan = new JButton("ĐẶT BÀN");
        btnDatBan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDatBan.setBackground(COLOR_ACCENT_BLUE);
        btnDatBan.setForeground(Color.WHITE);
        btnDatBan.setFocusPainted(false);
        btnDatBan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDatBan.addActionListener(e -> xuLyDatBan());
        btnDatBan.setBorder(BorderFactory.createCompoundBorder(
                btnDatBan.getBorder(),
                new EmptyBorder(10, 30, 10, 30)
        ));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 5, 5, 5);

        panel.add(btnDatBan, gbc);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 10, 15, 15));
        panel.setBackground(new Color(245, 245, 245));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        searchPanel.add(searchIcon, BorderLayout.WEST);

        txtTimKiemPhieuDat = new JTextField(PLACEHOLDER_SEARCH);
        txtTimKiemPhieuDat.setForeground(Color.GRAY);
        applyTextFieldStyle(txtTimKiemPhieuDat);

        txtTimKiemPhieuDat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().equals(PLACEHOLDER_SEARCH)) {
                    txtTimKiemPhieuDat.setText("");
                    txtTimKiemPhieuDat.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().isEmpty()) {
                    txtTimKiemPhieuDat.setForeground(Color.GRAY);
                    txtTimKiemPhieuDat.setText(PLACEHOLDER_SEARCH);
                }
            }
        });

        txtTimKiemPhieuDat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timKiemPhieuDat();
            }
        });

        searchPanel.add(txtTimKiemPhieuDat, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        modelListPhieuDat = new DefaultListModel<>();

        listPhieuDat = new JList<>(modelListPhieuDat);
        listPhieuDat.setCellRenderer(new PhieuDatListRenderer());
        listPhieuDat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPhieuDat.setBackground(Color.WHITE);

        listPhieuDat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listPhieuDat.locationToIndex(e.getPoint());

                if (index < 0 || index >= modelListPhieuDat.size()) {
                    return;
                }

                DonDatMonDTO ddm = modelListPhieuDat.getElementAt(index);

                if (ddm == null) {
                    return;
                }

                Rectangle bounds = listPhieuDat.getCellBounds(index, index);

                if (bounds == null) {
                    return;
                }

                int relativeX = e.getX() - bounds.x;

                if (relativeX >= bounds.width - 65) {
                    xuLyHuyDatBan(ddm, index);
                }
            }
        });

        JScrollPane scrollPaneList = new JScrollPane(listPhieuDat);
        scrollPaneList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(scrollPaneList, BorderLayout.CENTER);

        return panel;
    }

    private void applyTextFieldStyle(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 8, 5, 8)
        ));
        tf.setPreferredSize(new Dimension(100, 35));
    }

    private void applySpinnerStyle(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JComponent editor = spinner.getEditor();

        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField textField = defaultEditor.getTextField();

            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            textField.setBackground(Color.WHITE);
        }

        spinner.setPreferredSize(new Dimension(100, 35));
    }

    public void refreshData() {
        taiDanhSachBanTrong();
        loadDanhSachDatTruoc();
    }

    private void taiDanhSachBanTrong() {
        setBusy(true);

        new SwingWorker<List<BanDTO>, Void>() {
            @Override
            protected List<BanDTO> doInBackground() {
                return banRemoteService.getAllBan();
            }

            @Override
            protected void done() {
                try {
                    List<BanDTO> result = get();
                    dsTatCaBan = result != null ? result : new ArrayList<>();
                    hienThiBanPhuHop();
                } catch (Exception e) {
                    dsTatCaBan = new ArrayList<>();
                    showError("Lỗi tải danh sách bàn: " + getRootMessage(e));
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void hienThiBanPhuHop() {
        if (pnlBanContainer == null || spinnerSoLuongKhach == null || dateSpinner == null || timeSpinner == null) {
            return;
        }

        int soLuongKhach = (Integer) spinnerSoLuongKhach.getValue();

        LocalDateTime thoiGianDat;

        try {
            thoiGianDat = getSelectedDateTime();
        } catch (Exception e) {
            return;
        }

        LocalDateTime startCheck = thoiGianDat.minusHours(2);
        LocalDateTime endCheck = thoiGianDat.plusHours(2);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return donDatMonRemoteService.getMaBanDaDatTrongKhoang(startCheck, endCheck);
            }

            @Override
            protected void done() {
                try {
                    List<String> maBanDaDat = get();

                    if (maBanDaDat == null) {
                        maBanDaDat = new ArrayList<>();
                    }

                    renderBanPhuHop(soLuongKhach, thoiGianDat, maBanDaDat);
                } catch (Exception e) {
                    showError("Lỗi kiểm tra bàn đã đặt: " + getRootMessage(e));
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void renderBanPhuHop(int soLuongKhach, LocalDateTime thoiGianDat, List<String> maBanDaDat) {
        boolean isBookingNow = Math.abs(java.time.Duration.between(
                LocalDateTime.now(),
                thoiGianDat
        ).toMinutes()) < 30;

        pnlBanContainer.removeAll();
        dsBanPanelHienThi.clear();
        dsBanDaChon.clear();

        List<BanDTO> dsBanKhaDung = new ArrayList<>();

        if (dsTatCaBan != null) {
            for (BanDTO ban : dsTatCaBan) {
                if (ban == null || ban.getMaBan() == null) {
                    continue;
                }

                boolean isBusy = false;

                if (maBanDaDat.contains(ban.getMaBan())) {
                    isBusy = true;
                }

                if (isBookingNow && ban.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                    isBusy = true;
                }

                if (!isBusy) {
                    dsBanKhaDung.add(ban);
                }
            }
        }

        boolean coBanDon = false;

        for (BanDTO ban : dsBanKhaDung) {
            if (ban.getSoGhe() >= soLuongKhach) {
                coBanDon = true;
                addBanPanelToView(ban);
            }
        }

        if (!coBanDon) {
            List<List<BanDTO>> dsGoiY = timGoiYGhepBan(soLuongKhach, dsBanKhaDung);

            if (!dsGoiY.isEmpty()) {
                JLabel lblGoiY = new JLabel(
                        "<html>Không có bàn đơn đủ chỗ. Gợi ý ghép bàn trống lúc "
                                + thoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm"))
                                + ":</html>"
                );

                lblGoiY.setForeground(Color.BLUE);
                pnlBanContainer.add(lblGoiY);

                for (List<BanDTO> capBan : dsGoiY) {
                    createNutGhepBan(capBan);
                }
            } else {
                pnlBanContainer.add(new JLabel("Không có bàn trống phù hợp vào giờ này."));
            }
        }

        pnlBanContainer.revalidate();
        pnlBanContainer.repaint();
    }

    private void addBanPanelToView(BanDTO ban) {
        if (ban == null) {
            return;
        }

        BanPanel banPanel = new BanPanel(ban);

        banPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (ban.getMaBan() == null) {
                    return;
                }

                boolean removed = dsBanDaChon.removeIf(
                        b -> b != null && ban.getMaBan().equals(b.getMaBan())
                );

                if (!removed) {
                    dsBanDaChon.clear();
                    dsBanDaChon.add(ban);
                }

                updateBanPanelSelection();
            }
        });

        pnlBanContainer.add(banPanel);
        dsBanPanelHienThi.add(banPanel);
    }

    private void updateBanPanelSelection() {
        for (BanPanel panel : dsBanPanelHienThi) {
            boolean isSelected = false;

            for (BanDTO b : dsBanDaChon) {
                if (panel.getBan() != null
                        && panel.getBan().getMaBan() != null
                        && b.getMaBan() != null
                        && panel.getBan().getMaBan().equals(b.getMaBan())) {
                    isSelected = true;
                    break;
                }
            }

            panel.setSelected(isSelected);
        }

        if (!dsBanDaChon.isEmpty() && dsBanDaChon.size() == 1) {
            Component[] comps = pnlBanContainer.getComponents();

            for (Component c : comps) {
                if (c instanceof JToggleButton toggleButton) {
                    toggleButton.setSelected(false);
                    toggleButton.setBackground(Color.WHITE);
                    toggleButton.setForeground(Color.BLACK);
                    updateLabelsColor(toggleButton, Color.BLACK);
                }
            }
        }
    }

    private List<List<BanDTO>> timGoiYGhepBan(int soLuongKhach, List<BanDTO> sourceList) {
        List<List<BanDTO>> dsGoiY = new ArrayList<>();

        Map<String, List<BanDTO>> banTheoKhuVuc = new HashMap<>();

        for (BanDTO ban : sourceList) {
            banTheoKhuVuc.computeIfAbsent(ban.getKhuVuc(), k -> new ArrayList<>()).add(ban);
        }

        for (String khuVuc : banTheoKhuVuc.keySet()) {
            List<BanDTO> bansInZone = banTheoKhuVuc.get(khuVuc);

            int tongSucChuaKhuVuc = bansInZone.stream().mapToInt(BanDTO::getSoGhe).sum();

            if (tongSucChuaKhuVuc < soLuongKhach) {
                continue;
            }

            bansInZone.sort((b1, b2) -> Integer.compare(b2.getSoGhe(), b1.getSoGhe()));

            timToHopBan(bansInZone, soLuongKhach, 0, new ArrayList<>(), dsGoiY);
        }

        dsGoiY.sort(Comparator.comparingInt(List::size));

        if (dsGoiY.size() > 3) {
            return dsGoiY.subList(0, 3);
        }

        return dsGoiY;
    }

    private void timToHopBan(
            List<BanDTO> bans,
            int target,
            int index,
            List<BanDTO> current,
            List<List<BanDTO>> results
    ) {
        if (results.size() >= 50) {
            return;
        }

        int currentSeats = current.stream().mapToInt(BanDTO::getSoGhe).sum();

        if (currentSeats >= target) {
            if (currentSeats - target <= 8) {
                results.add(new ArrayList<>(current));
            }
            return;
        }

        if (current.size() >= 10) {
            return;
        }

        for (int i = index; i < bans.size(); i++) {
            current.add(bans.get(i));
            timToHopBan(bans, target, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    private void createNutGhepBan(List<BanDTO> groupBan) {
        int tongGhe = groupBan.stream().mapToInt(BanDTO::getSoGhe).sum();

        StringBuilder sb = new StringBuilder("<html><center>");
        sb.append("Ghép ").append(groupBan.size()).append(" bàn:<br><b>");

        for (int i = 0; i < groupBan.size(); i++) {
            sb.append(groupBan.get(i).getTenBan());

            if (i < groupBan.size() - 1) {
                sb.append(", ");
            }

            if ((i + 1) % 2 == 0 && i < groupBan.size() - 1) {
                sb.append("<br>");
            }
        }

        sb.append("</b><br><i>(Tổng ").append(tongGhe).append(" ghế)</i></center></html>");

        JToggleButton btnGhep = new JToggleButton();
        btnGhep.setLayout(new BorderLayout());
        btnGhep.setPreferredSize(new Dimension(180, 100));
        btnGhep.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGhep.setBackground(Color.WHITE);
        btnGhep.setForeground(Color.BLACK);

        JLabel lblIcon = new JLabel("🔗", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));

        JLabel lblInfo = new JLabel(sb.toString(), SwingConstants.CENTER);

        btnGhep.add(lblIcon, BorderLayout.WEST);
        btnGhep.add(lblInfo, BorderLayout.CENTER);

        btnGhep.addActionListener(e -> {
            dsBanDaChon.clear();
            dsBanDaChon.addAll(groupBan);

            for (BanPanel bp : dsBanPanelHienThi) {
                bp.setSelected(false);
            }

            Component[] comps = pnlBanContainer.getComponents();

            for (Component c : comps) {
                if (c instanceof JToggleButton toggleButton && c != btnGhep) {
                    toggleButton.setSelected(false);
                    toggleButton.setForeground(Color.BLACK);
                    toggleButton.setBackground(Color.WHITE);
                    updateLabelsColor(toggleButton, Color.BLACK);
                }
            }

            if (btnGhep.isSelected()) {
                btnGhep.setBackground(COLOR_ACCENT_BLUE);
                btnGhep.setForeground(Color.WHITE);
                updateLabelsColor(btnGhep, Color.WHITE);
            } else {
                dsBanDaChon.clear();
                btnGhep.setBackground(Color.WHITE);
                btnGhep.setForeground(Color.BLACK);
                updateLabelsColor(btnGhep, Color.BLACK);
            }
        });

        pnlBanContainer.add(btnGhep);
    }

    private void updateLabelsColor(JToggleButton button, Color color) {
        for (Component c : button.getComponents()) {
            if (c instanceof JLabel label) {
                label.setForeground(color);
            }
        }
    }

    private void timKhachHangTheoSDT() {
        String sdt = txtSDTKhach.getText().trim();

        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            txtHoTenKhach.setText("");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<KhachHangDTO, Void>() {
            @Override
            protected KhachHangDTO doInBackground() {
                return findKhachHangBySdt(sdt);
            }

            @Override
            protected void done() {
                try {
                    KhachHangDTO kh = get();

                    if (kh != null) {
                        txtHoTenKhach.setText(kh.getTenKH());
                    } else {
                        txtHoTenKhach.setText("");
                    }
                } catch (Exception e) {
                    showError("Lỗi tìm khách hàng: " + getRootMessage(e));
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private KhachHangDTO findKhachHangBySdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        List<KhachHangDTO> result = khachHangRemoteService.search(sdt.trim());

        if (result == null) {
            return null;
        }

        for (KhachHangDTO kh : result) {
            if (kh != null && sdt.trim().equals(kh.getSdt())) {
                return kh;
            }
        }

        return null;
    }

    private KhachHangDTO findKhachHangByMaKH(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        KhachHangDTO cached = khachHangCacheTheoMa.get(maKH);

        if (cached != null) {
            return cached;
        }

        List<KhachHangDTO> all = khachHangRemoteService.findAll();

        if (all == null) {
            return null;
        }

        for (KhachHangDTO kh : all) {
            if (kh != null && maKH.equals(kh.getMaKH())) {
                khachHangCacheTheoMa.put(maKH, kh);
                return kh;
            }
        }

        return null;
    }

    private void xuLyDatBan() {
        if (dsBanDaChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sdt = txtSDTKhach.getText().trim();

        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtSDTKhach.requestFocus();
            return;
        }

        String tenKH = txtHoTenKhach.getText().trim();

        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtHoTenKhach.requestFocus();
            return;
        }

        LocalDateTime thoiGianDat;

        try {
            thoiGianDat = getSelectedDateTime();

            if (thoiGianDat.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Thời gian đặt phải trong tương lai!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xử lý thời gian đặt bàn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = -1;

        try {
            KhachHangDTO khDTO = findKhachHangBySdt(sdt);

            if (khDTO == null) {
                choice = JOptionPane.showConfirmDialog(
                        this,
                        "Khách hàng mới (" + sdt + "). Thêm vào danh sách thành viên?",
                        "Khách mới",
                        JOptionPane.YES_NO_CANCEL_OPTION
                );

                if (choice == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        int finalChoice = choice;
        String ghiChuUser = txtGhiChu.getText().trim();
        List<BanDTO> dsBanCanDat = new ArrayList<>(dsBanDaChon);

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            private String errorMessage;

            @Override
            protected Boolean doInBackground() {
                try {
                    KhachHangDTO khDTO = findKhachHangBySdt(sdt);
                    String maKHCanDung;

                    if (khDTO == null) {
                        KhachHangDTO khMoi = KhachHangDTO.builder()
                                .tenKH(tenKH)
                                .sdt(sdt)
                                .hangThanhVien(finalChoice == JOptionPane.YES_OPTION ? HangThanhVien.MEMBER : HangThanhVien.NONE)
                                .gioiTinh("Khác")
                                .ngaySinh(LocalDate.of(2000, 1, 1))
                                .ngayThamGia(LocalDate.now())
                                .tongChiTieu(0f)
                                .build();

                        boolean addOK = khachHangRemoteService.addKhachHang(khMoi);

                        if (!addOK) {
                            errorMessage = "Không thể thêm khách hàng mới.";
                            return false;
                        }

                        khDTO = findKhachHangBySdt(sdt);
                    }

                    if (khDTO == null || khDTO.getMaKH() == null || khDTO.getMaKH().trim().isEmpty()) {
                        errorMessage = "Không lấy được mã khách hàng.";
                        return false;
                    }

                    maKHCanDung = khDTO.getMaKH();

                    boolean tatCaThanhCong = true;
                    boolean isGhepBan = dsBanCanDat.size() > 1;
                    BanDTO banChinh = dsBanCanDat.get(0);

                    String maNV = parentDanhSachBanGUI_DatBan != null
                            ? parentDanhSachBanGUI_DatBan.getMaNVDangNhap()
                            : "NV01102";

                    if (maNV == null || maNV.trim().isEmpty()) {
                        maNV = "NV01102";
                    }

                    for (BanDTO ban : dsBanCanDat) {
                        String ghiChu;

                        if (isGhepBan) {
                            if (ban.getMaBan().equals(banChinh.getMaBan())) {
                                ghiChu = ghiChuUser + " (Đặt chính nhóm " + dsBanCanDat.size() + " bàn)";
                            } else {
                                String noteHienThi = ghiChuUser + " (Đặt cùng " + banChinh.getTenBan() + ")";
                                String noteKyThuat = " LINKED:" + banChinh.getMaBan();
                                ghiChu = noteHienThi + noteKyThuat;
                            }
                        } else {
                            ghiChu = ghiChuUser;
                        }

                        DonDatMonDTO ddmDTO = DonDatMonDTO.builder()
                                .ngayKhoiTao(LocalDateTime.now())
                                .thoiGianDen(thoiGianDat)
                                .trangThai("Chưa thanh toán")
                                .maNV(maNV)
                                .maKH(maKHCanDung)
                                .maBan(ban.getMaBan())
                                .ghiChu(ghiChu)
                                .build();

                        boolean saveOK = donDatMonRemoteService.save(ddmDTO);

                        if (!saveOK) {
                            tatCaThanhCong = false;
                            continue;
                        }

                        long phutChenhLech = java.time.Duration.between(LocalDateTime.now(), thoiGianDat).toMinutes();

                        if (phutChenhLech <= 120 && ban.getTrangThai() != TrangThaiBan.DANG_PHUC_VU) {
                            ban.setTrangThai(TrangThaiBan.DA_DAT_TRUOC);
                            ban.setGioMoBan(thoiGianDat);

                            boolean updateBanOK = banRemoteService.updateBan(ban);

                            if (!updateBanOK) {
                                tatCaThanhCong = false;
                            }
                        }
                    }

                    return tatCaThanhCong;
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        spinnerSoLuongKhach.setValue(1);
                        txtGhiChu.setText("");
                        txtSDTKhach.setText("");
                        txtHoTenKhach.setText("");
                        dsBanDaChon.clear();

                        refreshData();

                        if (parentDanhSachBanGUI_DatBan != null) {
                            parentDanhSachBanGUI_DatBan.refreshManHinhBan();
                        }

                        JOptionPane.showMessageDialog(
                                ManHinhDatBanGUI.this,
                                "Đặt bàn thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        refreshData();
                        showError(errorMessage != null ? errorMessage : "Có lỗi xảy ra khi lưu đơn!");
                    }
                } catch (Exception e) {
                    showError("Lỗi đặt bàn: " + getRootMessage(e));
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void loadDanhSachDatTruoc() {
        new SwingWorker<PhieuDatResult, Void>() {
            @Override
            protected PhieuDatResult doInBackground() {
                List<DonDatMonDTO> dsDatTruoc = donDatMonRemoteService.getAllDonDatMonChuaNhan();
                List<KhachHangDTO> dsKhach = khachHangRemoteService.findAll();

                return new PhieuDatResult(dsDatTruoc, dsKhach);
            }

            @Override
            protected void done() {
                try {
                    PhieuDatResult result = get();

                    capNhatCacheKhachHang(result.dsKhachHang);
                    renderDanhSachPhieuDat(result.dsDonDatMon);
                } catch (Exception e) {
                    modelListPhieuDat.clear();
                    modelListPhieuDat.addElement(null);

                    showError("Lỗi khi tải danh sách đặt trước: " + getRootMessage(e));
                }

                listPhieuDat.setModel(modelListPhieuDat);
                listPhieuDat.revalidate();
                listPhieuDat.repaint();
            }
        }.execute();
    }

    private void timKiemPhieuDat() {
        String query = txtTimKiemPhieuDat.getText().trim();

        if (query.equals(PLACEHOLDER_SEARCH)) {
            query = "";
        }

        final String keyword = query;

        new SwingWorker<List<DonDatMonDTO>, Void>() {
            @Override
            protected List<DonDatMonDTO> doInBackground() {
                if (keyword.isEmpty()) {
                    return donDatMonRemoteService.getAllDonDatMonChuaNhan();
                }

                return donDatMonRemoteService.timDonDatMonChuaNhan(keyword);
            }

            @Override
            protected void done() {
                try {
                    renderDanhSachPhieuDat(get());
                } catch (Exception e) {
                    modelListPhieuDat.clear();
                    modelListPhieuDat.addElement(null);
                }

                listPhieuDat.setModel(modelListPhieuDat);
                listPhieuDat.repaint();
            }
        }.execute();
    }

    private void renderDanhSachPhieuDat(List<DonDatMonDTO> dsDatTruoc) {
        modelListPhieuDat.clear();

        if (dsDatTruoc == null || dsDatTruoc.isEmpty()) {
            modelListPhieuDat.addElement(null);
            return;
        }

        for (DonDatMonDTO ddm : dsDatTruoc) {
            modelListPhieuDat.addElement(ddm);
        }
    }

    private void capNhatCacheKhachHang(List<KhachHangDTO> dsKhach) {
        khachHangCacheTheoMa.clear();

        if (dsKhach == null) {
            return;
        }

        for (KhachHangDTO kh : dsKhach) {
            if (kh != null && kh.getMaKH() != null) {
                khachHangCacheTheoMa.put(kh.getMaKH(), kh);
            }
        }
    }

    private void xuLyHuyDatBan(DonDatMonDTO ddmToCancel, int index) {
        if (ddmToCancel == null || ddmToCancel.getMaDon() == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy đặt bàn cho mã đơn '" + ddmToCancel.getMaDon() + "'?",
                "Xác nhận hủy đặt bàn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return donDatMonRemoteService.huyDatBanVaGiaiPhongBanGhep(ddmToCancel.getMaDon());
            }

            @Override
            protected void done() {
                try {
                    boolean xoaDonOK = Boolean.TRUE.equals(get());

                    if (xoaDonOK) {
                        if (index >= 0 && index < modelListPhieuDat.size()) {
                            modelListPhieuDat.removeElementAt(index);
                        }

                        refreshData();

                        if (parentDanhSachBanGUI_DatBan != null) {
                            parentDanhSachBanGUI_DatBan.refreshManHinhBan();
                        }
                    } else {
                        showError("Hủy đặt bàn thất bại! Vui lòng thử lại.");
                    }
                } catch (Exception e) {
                    showError("Lỗi hủy đặt bàn: " + getRootMessage(e));
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private LocalDateTime getSelectedDateTime() {
        Date selectedDate = (Date) dateSpinner.getValue();
        Date selectedTime = (Date) timeSpinner.getValue();

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(selectedDate);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(selectedTime);

        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);

        return dateCal.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private String getTenBanHienThi(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return "";
        }

        if (dsTatCaBan != null) {
            for (BanDTO ban : dsTatCaBan) {
                if (ban != null && maBan.equals(ban.getMaBan())) {
                    return ban.getTenBan() != null ? ban.getTenBan() : maBan;
                }
            }
        }

        return maBan;
    }

    private void setBusy(boolean busy) {
        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );

        if (btnDatBan != null) {
            btnDatBan.setEnabled(!busy);
        }

        if (spinnerSoLuongKhach != null) {
            spinnerSoLuongKhach.setEnabled(!busy);
        }

        if (dateSpinner != null) {
            dateSpinner.setEnabled(!busy);
        }

        if (timeSpinner != null) {
            timeSpinner.setEnabled(!busy);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String getRootMessage(Exception e) {
        Throwable t = e;

        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t.getMessage() != null ? t.getMessage() : e.getMessage();
    }

    private static class PhieuDatResult {
        private final List<DonDatMonDTO> dsDonDatMon;
        private final List<KhachHangDTO> dsKhachHang;

        private PhieuDatResult(List<DonDatMonDTO> dsDonDatMon, List<KhachHangDTO> dsKhachHang) {
            this.dsDonDatMon = dsDonDatMon;
            this.dsKhachHang = dsKhachHang;
        }
    }

    private class PhieuDatListRenderer implements ListCellRenderer<DonDatMonDTO> {

        private final JPanel wrapperPanel;
        private final JPanel mainPanel;
        private final JPanel textPanel;
        private final JLabel lblLine1;
        private final JLabel lblLine2;
        private final JButton btnDelete;
        private final JSeparator separator;

        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        private final Font mainFont = new Font("Segoe UI", Font.BOLD, 14);
        private final Font subFont = new Font("Segoe UI", Font.PLAIN, 13);
        private final Color textColor = Color.DARK_GRAY;
        private final Color timeColor = Color.BLACK;
        private final Color separatorColor = new Color(220, 220, 220);

        public PhieuDatListRenderer() {
            wrapperPanel = new JPanel(new BorderLayout());

            mainPanel = new JPanel(new BorderLayout(10, 0));
            mainPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

            textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            lblLine1 = new JLabel(" ");
            lblLine1.setFont(mainFont);
            lblLine1.setForeground(textColor);

            lblLine2 = new JLabel(" ");
            lblLine2.setFont(subFont);
            lblLine2.setForeground(timeColor);

            textPanel.add(lblLine1);
            textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
            textPanel.add(lblLine2);

            btnDelete = new JButton("X");
            btnDelete.setFont(new Font("Arial", Font.BOLD, 16));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setBackground(new Color(239, 68, 68));
            btnDelete.setFocusPainted(false);
            btnDelete.setBorder(new EmptyBorder(5, 10, 5, 10));
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setPreferredSize(new Dimension(40, 40));

            separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setForeground(separatorColor);

            mainPanel.add(textPanel, BorderLayout.CENTER);
            mainPanel.add(btnDelete, BorderLayout.EAST);

            wrapperPanel.add(mainPanel, BorderLayout.CENTER);
            wrapperPanel.add(separator, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends DonDatMonDTO> list,
                DonDatMonDTO value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            if (value == null) {
                lblLine1.setText("Không có phiếu đặt nào");
                lblLine2.setText("");
                btnDelete.setVisible(false);
            } else {
                String tenBan = getTenBanHienThi(value.getMaBan());

                KhachHangDTO kh = value.getMaKH() != null
                        ? khachHangCacheTheoMa.get(value.getMaKH())
                        : null;

                String tenKH = kh != null && kh.getTenKH() != null ? kh.getTenKH() : "Vãng lai";
                String sdtKH = kh != null && kh.getSdt() != null ? kh.getSdt() : "";

                lblLine1.setText(tenBan + " - " + tenKH + (sdtKH.isEmpty() ? "" : " (" + sdtKH + ")"));

                String timeText = value.getThoiGianDen() != null
                        ? value.getThoiGianDen().format(timeFormatter)
                        : "Chưa rõ giờ đến";

                lblLine2.setText(timeText + " | Mã đơn: " + value.getMaDon());
                btnDelete.setVisible(true);
            }

            if (isSelected) {
                mainPanel.setBackground(new Color(232, 240, 254));
                wrapperPanel.setBackground(new Color(232, 240, 254));
            } else {
                mainPanel.setBackground(Color.WHITE);
                wrapperPanel.setBackground(Color.WHITE);
            }

            return wrapperPanel;
        }
    }
}