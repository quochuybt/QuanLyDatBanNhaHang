package gui;

import dao.GiaoCaDAO;
import dao.HoaDonDAO;
import dao.PhanCongDAO;
import dao.ChiTietHoaDonDAO;
import dao.BanDAO;
import entity.CaLam;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.List;

public class EmployeeDashboardGUI extends JPanel {
    // --- Data ---
    private String maNV;
    private String tenNV;
    private int currentShiftId = -1;
    private double tienDauCa = 0;
    private LocalDateTime thoiGianVaoCa;
    private CaLam thongTinCaChuan;

    // --- DAOs ---
    private GiaoCaDAO giaoCaDAO;
    private HoaDonDAO hoaDonDAO;
    private PhanCongDAO phanCongDAO;
    private BanDAO banDAO;
    private ChiTietHoaDonDAO chiTietDAO;

    // --- UI Components ---
    // Money Section
    private JLabel lblTienDauCa, lblThuTienMat, lblThuChuyenKhoan, lblTongKet;

    // Time & Shift Section
    private JLabel lblClock, lblCountDown;
    private JProgressBar progressCaLam;
    private JLabel lblCaTruoc, lblCaSau;

    // Stats Section (Đã thêm Đã đặt trước)
    private JLabel lblBanTrong, lblBanCoKhach, lblBanDatTruoc;
    private JList<String> listTopSelling;

    // History components
    private JDateChooser dateChooserStart, dateChooserEnd;
    private JTable tableHistory;
    private DefaultTableModel historyModel;
    private JLabel lblTongGioLam, lblMucTieuGio;

    private Timer timerRealTime;
    private final DecimalFormat dfMoney = new DecimalFormat("#,##0"); // Bỏ chữ đ để gọn, hoặc thêm vào nếu thích
    private final DecimalFormat dfHour = new DecimalFormat("0.0");

    public EmployeeDashboardGUI(String maNV, String tenNV) {
        this.maNV = maNV;
        this.tenNV = tenNV;

        this.giaoCaDAO = new GiaoCaDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.phanCongDAO = new PhanCongDAO();
        this.banDAO = new BanDAO();
        this.chiTietDAO = new ChiTietHoaDonDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        this.currentShiftId = giaoCaDAO.getMaCaDangLamViec(maNV);

        if (currentShiftId == -1) {
            buildStartShiftScreen();
        } else {
            // Lấy lại thông tin ca đang mở
            entity.GiaoCa gc = giaoCaDAO.getThongTinCaDangLam(maNV);
            if (gc != null) {
                this.thoiGianVaoCa = gc.getThoiGianBatDau();
                this.tienDauCa = gc.getTienDauCa();
                this.currentShiftId = gc.getMaGiaoCa();
            } else {
                this.thoiGianVaoCa = LocalDateTime.now();
                this.tienDauCa = 0;
            }
            this.thongTinCaChuan = phanCongDAO.getCaLamViecCuaNhanVien(maNV, LocalDate.now());
            buildMainDashboard();
        }
    }

    // ===== MÀN HÌNH 1: NHẬP TIỀN ĐẦU CA =====
    private void buildStartShiftScreen() {
        removeAll();
        setLayout(new GridBagLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 118, 243), 2),
                new EmptyBorder(40, 60, 40, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("BẮT ĐẦU CA LÀM VIỆC");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(56, 118, 243));
        p.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("Vui lòng kiểm tra tiền trong két và nhập vào bên dưới");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);
        p.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        JTextField txtMoney = new JTextField(15);
        txtMoney.setFont(new Font("Segoe UI", Font.BOLD, 24));
        txtMoney.setHorizontalAlignment(SwingConstants.CENTER);
        txtMoney.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        p.add(txtMoney, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 10, 0);
        JButton btn = new JButton("XÁC NHẬN VÀO CA");
        styleButton(btn, new Color(40, 167, 69));
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btn.addActionListener(e -> {
            try {
                String input = txtMoney.getText().trim().replace(",", "");
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double money = Double.parseDouble(input);
                if (money < 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (giaoCaDAO.batDauCa(maNV, money)) {
                    this.tienDauCa = money;
                    this.thoiGianVaoCa = LocalDateTime.now();
                    this.currentShiftId = giaoCaDAO.getMaCaDangLamViec(maNV);
                    this.thongTinCaChuan = phanCongDAO.getCaLamViecCuaNhanVien(maNV, LocalDate.now());
                    buildMainDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể bắt đầu ca!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        });
        p.add(btn, gbc);

        add(p);
        revalidate();
        repaint();
    }

    // ===== MÀN HÌNH 2: DASHBOARD CHÍNH =====
    private void buildMainDashboard() {
        removeAll();
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // TOP: Header
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblHello = new JLabel("<html>Xin chào, <b style='color:#3876F3;'>" + tenNV + "</b></html>");
        lblHello.setFont(new Font("Segoe UI", Font.PLAIN, 22));

        lblClock = new JLabel("00:00:00");
        lblClock.setFont(new Font("Consolas", Font.BOLD, 26));
        lblClock.setForeground(new Color(56, 118, 243));

        pnlTop.add(lblHello, BorderLayout.WEST);
        pnlTop.add(lblClock, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // CENTER: Main Content (Tab)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Ca hiện tại", createCurrentShiftPanel());
        tabbedPane.addTab("Lịch sử làm việc", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // BOTTOM: Nút chức năng
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setOpaque(false);

        JButton btnEndShift = new JButton("⏹ KẾT THÚC CA");
        styleButton(btnEndShift, new Color(220, 53, 69)); // Màu đỏ
        btnEndShift.setPreferredSize(new Dimension(180, 45));
        btnEndShift.addActionListener(e -> actionKetCa());

        pnlBottom.add(btnEndShift);
        add(pnlBottom, BorderLayout.SOUTH);

        startTimer();
        SwingUtilities.invokeLater(this::loadHistoryData); // Load lịch sử lần đầu
        revalidate();
        repaint();
    }

    // ===== TAB 1: CA HIỆN TẠI =====
    private JPanel createCurrentShiftPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 15, 15);

        // Row 1: Két tiền (trái) + Thông tin ca (phải)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2; gbc.weightx = 0.6; gbc.weighty = 0.5;
        mainPanel.add(createCashRegisterPanel(), gbc);

        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(createShiftInfoPanel(), gbc);

        // Row 2: Trạng thái bàn + Top món bán
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.weightx = 0.3; gbc.weighty = 0.5;
        gbc.insets = new Insets(0, 0, 0, 15);
        mainPanel.add(createQuickStatsPanel(), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(createTopSellingPanel(), gbc);

        return mainPanel;
    }

    // --- Panel Két tiền (Loại bỏ Icon) ---
    private JPanel createCashRegisterPanel() {
        JPanel p = createRoundedPanel();
        p.setLayout(new GridLayout(2, 2, 15, 15));
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                "QUẢN LÝ TIỀN MẶT",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15),
                new Color(56, 118, 243)
        ));

        lblTienDauCa = new JLabel(dfMoney.format(tienDauCa));
        p.add(createMoneyCardNoIcon("Vốn đầu ca", lblTienDauCa, new Color(108, 117, 125)));

        lblThuChuyenKhoan = new JLabel("0 ₫");
        p.add(createMoneyCardNoIcon("Thu Chuyển khoản", lblThuChuyenKhoan, new Color(23, 162, 184)));

        lblThuTienMat = new JLabel("0 ₫");
        p.add(createMoneyCardNoIcon("Thu Tiền mặt", lblThuTienMat, new Color(40, 167, 69)));

        lblTongKet = new JLabel(dfMoney.format(tienDauCa));
        JPanel pTong = createMoneyCardNoIcon("TỔNG TIỀN TRONG KÉT", lblTongKet, new Color(220, 53, 69));
        pTong.setBackground(new Color(255, 243, 243)); // Nền đỏ nhạt
        pTong.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        p.add(pTong);

        return p;
    }

    // --- Panel Thông tin ca (Loại bỏ Icon) ---
    private JPanel createShiftInfoPanel() {
        JPanel p = createRoundedPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("THÔNG TIN CA LÀM VIỆC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(56, 118, 243));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitle);
        p.add(Box.createVerticalStrut(15));

        if (thongTinCaChuan != null) {
            JLabel lblCaInfo = new JLabel("<html><center>" + thongTinCaChuan.getTenCa() + "<br>" +
                    thongTinCaChuan.getGioBatDau() + " - " + thongTinCaChuan.getGioKetThuc() + "</center></html>");
            lblCaInfo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblCaInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblCaInfo);
            p.add(Box.createVerticalStrut(15));
        }

        JLabel lblCountLabel = new JLabel("Thời gian còn lại:");
        lblCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCountLabel);
        p.add(Box.createVerticalStrut(5));

        lblCountDown = new JLabel("--:--:--");
        lblCountDown.setFont(new Font("Consolas", Font.BOLD, 40));
        lblCountDown.setForeground(new Color(220, 53, 69));
        lblCountDown.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCountDown);
        p.add(Box.createVerticalStrut(10));

        progressCaLam = new JProgressBar(0, 100);
        progressCaLam.setPreferredSize(new Dimension(250, 15));
        progressCaLam.setMaximumSize(new Dimension(250, 15));
        progressCaLam.setForeground(new Color(56, 118, 243));
        progressCaLam.setStringPainted(true);
        progressCaLam.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(progressCaLam);

        p.add(Box.createVerticalStrut(20));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(15));

        // Thông tin ca trước
        JLabel lblCaTruocTitle = new JLabel("Ca trước:");
        lblCaTruocTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
//        lblCaTruocTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCaTruocTitle);

        lblCaTruoc = new JLabel("Đang tải...");
        lblCaTruoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        lblCaTruoc.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCaTruoc);

        p.add(Box.createVerticalStrut(10));

        // Thông tin ca sau
        JLabel lblCaSauTitle = new JLabel("Ca sau:");
        lblCaSauTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
//        lblCaSauTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCaSauTitle);

        lblCaSau = new JLabel("Đang tải...");
        lblCaSau.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        lblCaSau.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblCaSau);

        p.add(Box.createVerticalGlue());
        return p;
    }

    // --- Panel Trạng thái bàn (Thêm Trạng thái Đặt trước) ---
    private JPanel createQuickStatsPanel() {
        JPanel p = createRoundedPanel();
        p.setLayout(new BorderLayout(0, 15));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Tình trạng Bàn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(title, BorderLayout.NORTH);

        // Grid 3 dòng cho 3 trạng thái
        JPanel pGrid = new JPanel(new GridLayout(3, 1, 5, 10));
        pGrid.setOpaque(false);

        lblBanTrong = new JLabel("Trống: 0");
        lblBanTrong.setForeground(new Color(40, 167, 69)); // Xanh
        lblBanTrong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBanTrong.setHorizontalAlignment(SwingConstants.CENTER);

        lblBanCoKhach = new JLabel("Có khách: 0");
        lblBanCoKhach.setForeground(new Color(220, 53, 69)); // Đỏ
        lblBanCoKhach.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBanCoKhach.setHorizontalAlignment(SwingConstants.CENTER);

        lblBanDatTruoc = new JLabel("Đã đặt: 0");
        lblBanDatTruoc.setForeground(new Color(255, 193, 7)); // Vàng/Cam
        lblBanDatTruoc.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBanDatTruoc.setHorizontalAlignment(SwingConstants.CENTER);

        pGrid.add(lblBanTrong);
        pGrid.add(lblBanCoKhach);
        pGrid.add(lblBanDatTruoc); // Thêm dòng này

        p.add(pGrid, BorderLayout.CENTER);
        return p;
    }

    // Panel Top món bán
    private JPanel createTopSellingPanel() {
        JPanel p = createRoundedPanel();
        p.setLayout(new BorderLayout(0, 10));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Top Món Bán Chạy (Hôm nay)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(title, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        listTopSelling = new JList<>(model);
        listTopSelling.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listTopSelling.setBackground(new Color(255, 250, 240));
        listTopSelling.setBorder(new EmptyBorder(5, 10, 5, 10));

        JScrollPane scroll = new JScrollPane(listTopSelling);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ===== TAB 2: LỊCH SỬ LÀM VIỆC =====
    private JPanel createHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(new Color(240, 242, 245));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Từ ngày:"));
        dateChooserStart = new JDateChooser();
        dateChooserStart.setDateFormatString("dd/MM/yyyy");
        dateChooserStart.setPreferredSize(new Dimension(130, 30));
        filterPanel.add(dateChooserStart);

        filterPanel.add(new JLabel("Đến ngày:"));
        dateChooserEnd = new JDateChooser();
        dateChooserEnd.setDateFormatString("dd/MM/yyyy");
        dateChooserEnd.setPreferredSize(new Dimension(130, 30));
        filterPanel.add(dateChooserEnd);

        JButton btnWeek = new JButton("Tuần này");
        JButton btnMonth = new JButton("Tháng này");
        JButton btnRefresh = new JButton("Xem");

        styleSmallButton(btnWeek);
        styleSmallButton(btnMonth);
        styleButton(btnRefresh, new Color(56, 118, 243));
        btnRefresh.setPreferredSize(new Dimension(80, 30));

        btnWeek.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            dateChooserStart.setDate(java.sql.Date.valueOf(startOfWeek));
            dateChooserEnd.setDate(java.sql.Date.valueOf(today));
            loadHistoryData();
        });

        btnMonth.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
            dateChooserStart.setDate(java.sql.Date.valueOf(startOfMonth));
            dateChooserEnd.setDate(java.sql.Date.valueOf(today));
            loadHistoryData();
        });

        btnRefresh.addActionListener(e -> loadHistoryData());

        filterPanel.add(btnWeek);
        filterPanel.add(btnMonth);
        filterPanel.add(btnRefresh);

        topPanel.add(filterPanel, BorderLayout.NORTH);

        // Stats
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        lblTongGioLam = new JLabel("0.0 giờ");
        lblMucTieuGio = new JLabel("Mục tiêu: 40 giờ");

        statsPanel.add(createStatBoxHistory("Tổng giờ làm", lblTongGioLam, new Color(56, 118, 243)));
        statsPanel.add(createStatBoxHistory("Tiến độ", lblMucTieuGio, new Color(255, 193, 7)));

        topPanel.add(statsPanel, BorderLayout.CENTER);
        p.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Ngày", "Ca", "Giờ vào", "Giờ ra", "Số giờ", "Tiền đầu ca", "Tiền cuối ca", "Chênh lệch"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableHistory = new JTable(historyModel);
        tableHistory.setRowHeight(30);
        tableHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHistory.getTableHeader().setBackground(new Color(230, 230, 230));

        tableHistory.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String s = value.toString().replaceAll("[^0-9-]", "");
                    try {
                        double val = Double.parseDouble(s);
                        if (val < 0) c.setForeground(Color.RED);
                        else if (val > 0) c.setForeground(new Color(0, 150, 50));
                        else c.setForeground(Color.BLACK);
                    } catch (Exception e) { c.setForeground(Color.BLACK); }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tableHistory);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        p.add(scroll, BorderLayout.CENTER);

        // Init default date
        LocalDate today = LocalDate.now();
        dateChooserStart.setDate(java.sql.Date.valueOf(today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))));
        dateChooserEnd.setDate(java.sql.Date.valueOf(today));

        return p;
    }

    private void loadHistoryData() {
        if (dateChooserStart.getDate() == null || dateChooserEnd.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate start = dateChooserStart.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate end = dateChooserEnd.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        new SwingWorker<Void, Void>() {
            java.util.List<Map<String, Object>> data;
            double tongGio = 0;

            @Override
            protected Void doInBackground() {
                data = giaoCaDAO.getLichSuGiaoCaChiTiet(maNV, start, end);
                tongGio = giaoCaDAO.getTongGioLamTheoKhoang(maNV, start, end);
                return null;
            }

            @Override
            protected void done() {
                try {
                    historyModel.setRowCount(0);
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

                    if (data != null) {
                        for (Map<String, Object> row : data) {
                            LocalDateTime batDau = (LocalDateTime) row.get("thoiGianBatDau");
                            LocalDateTime ketThuc = (LocalDateTime) row.get("thoiGianKetThuc");
                            String tenCa = (String) row.get("tenCa");

                            double soGio = 0;
                            if (ketThuc != null) soGio = Duration.between(batDau, ketThuc).toMinutes() / 60.0;

                            historyModel.addRow(new Object[]{
                                    batDau.toLocalDate().format(dtf),
                                    tenCa != null ? tenCa : "N/A",
                                    batDau.format(timeFmt),
                                    ketThuc != null ? ketThuc.format(timeFmt) : "Chưa kết",
                                    dfHour.format(soGio),
                                    dfMoney.format((Double) row.get("tienDauCa")),
                                    row.get("tienCuoiCa") != null ? dfMoney.format((Double) row.get("tienCuoiCa")) : "-",
                                    row.get("chenhLech") != null ? dfMoney.format((Double) row.get("chenhLech")) : "-"
                            });
                        }
                    }

                    lblTongGioLam.setText(dfHour.format(tongGio) + " giờ");
                    double mucTieu = 40.0;
                    double percent = (tongGio / mucTieu) * 100;
                    lblMucTieuGio.setText(String.format("Đạt: %.1f%%", percent));

                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // ===== TIMER & UPDATE =====
    private void startTimer() {
        timerRealTime = new Timer(1000, e -> {
            updateClock();
            if (LocalTime.now().getSecond() % 5 == 0) {
                updateMoney();
                updateTableStatus();
                updateTopSelling();
                updateShiftInfo();
            }
        });
        timerRealTime.start();
        updateMoney();
        updateShiftInfo();
        updateTableStatus();
        updateTopSelling();
    }

    private void updateClock() {
        lblClock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        if (thongTinCaChuan != null) {
            LocalTime now = LocalTime.now();
            LocalTime end = thongTinCaChuan.getGioKetThuc();
            long secondsLeft = Duration.between(now, end).getSeconds();

            if (secondsLeft > 0) {
                long h = secondsLeft / 3600;
                long m = (secondsLeft % 3600) / 60;
                lblCountDown.setText(String.format("%02d:%02d", h, m));

                long total = Duration.between(thongTinCaChuan.getGioBatDau(), end).getSeconds();
                long elapsed = total - secondsLeft;
                progressCaLam.setValue((int) ((double) elapsed / total * 100));
            } else {
                lblCountDown.setText("OVER");
                lblCountDown.setForeground(Color.RED);
                progressCaLam.setValue(100);
            }
        } else {
            lblCountDown.setText("--:--");
        }
    }

    private void updateMoney() {
        new SwingWorker<Double[], Void>() {
            @Override
            protected Double[] doInBackground() {
                Double cash = hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, thoiGianVaoCa, "Tiền mặt");
                Double transfer = hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, thoiGianVaoCa, "Chuyển khoản");
                return new Double[]{cash, transfer};
            }

            @Override
            protected void done() {
                try {
                    Double[] data = get();
                    double cash = data[0] != null ? data[0] : 0;
                    double transfer = data[1] != null ? data[1] : 0;

                    lblThuTienMat.setText(dfMoney.format(cash));
                    lblThuChuyenKhoan.setText(dfMoney.format(transfer));

                    lblTongKet.setText(dfMoney.format(tienDauCa + cash));
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void updateTableStatus() {
        new SwingWorker<Map<String, Integer>, Void>() {
            @Override protected Map<String, Integer> doInBackground() {
                return banDAO.getTableStatusCounts();
            }
            @Override protected void done() {
                try {
                    Map<String, Integer> map = get();
                    lblBanTrong.setText("Trống: " + map.getOrDefault("Trống", 0));
                    lblBanCoKhach.setText("Có khách: " + map.getOrDefault("Đang có khách", 0));
                    lblBanDatTruoc.setText("Đã đặt: " + map.getOrDefault("Đã đặt trước", 0));
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void updateTopSelling() {
        new SwingWorker<java.util.List<String>, Void>() {
            @Override protected java.util.List<String> doInBackground() {
                return chiTietDAO.getTopMonBanChayTrongNgay();
            }
            @Override protected void done() {
                try {
                    DefaultListModel<String> model = (DefaultListModel<String>) listTopSelling.getModel();
                    model.clear();
                    java.util.List<String> list = get();
                    if (list.isEmpty()) model.addElement("Chưa có dữ liệu.");
                    else for (String s : list) model.addElement(s);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void updateShiftInfo() {
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() {
                return phanCongDAO.getThongTinCaTruocSau(null, LocalDate.now());
            }
            @Override protected void done() {
                try {
                    String[] info = get();
                    lblCaTruoc.setText(info[0]);
                    lblCaSau.setText(info[1]);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // ===== ACTIONS =====
    private void actionKetCa() {
        if (timerRealTime != null) timerRealTime.stop();

        String sysMoneyStr = lblTongKet.getText();

        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.add(new JLabel("Hệ thống tính tổng tiền trong két: " + sysMoneyStr));
        p.add(new JLabel("Nhập số tiền THỰC TẾ bạn đếm được:"));
        JTextField txtReal = new JTextField();
        p.add(txtReal);
        p.add(new JLabel("Ghi chú (nếu lệch):"));
        JTextField txtNote = new JTextField();
        p.add(txtNote);

        int opt = JOptionPane.showConfirmDialog(this, p, "Kết ca & Bàn giao", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            try {
                String input = txtReal.getText().trim().replace(",", "");
                if (input.isEmpty()) throw new NumberFormatException();

                double realMoney = Double.parseDouble(input);

                if (giaoCaDAO.ketThucCa(currentShiftId, realMoney, txtNote.getText())) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Kết ca thành công! Bạn có muốn đăng xuất ngay?",
                            "Hoàn tất", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        logout(true);
                    } else {
                        currentShiftId = -1;
                        buildStartShiftScreen();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu kết ca!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    timerRealTime.start();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                timerRealTime.start();
            }
        } else {
            timerRealTime.start();
        }
    }

    private void logout(boolean closeApp) {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            win.dispose();
            SwingUtilities.invokeLater(() -> new TaiKhoanGUI().setVisible(true));
        }
    }

    // ===== UI HELPERS (NO ICONS) =====
    private JPanel createRoundedPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel createMoneyCardNoIcon(String title, JLabel val, Color color) {
        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(Color.GRAY);

        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(color);

        p.add(t, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatBoxHistory(String title, JLabel val, Color color) {
        JPanel p = createRoundedPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(Color.GRAY);

        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        val.setForeground(color);

        p.add(t, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void styleSmallButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.DARK_GRAY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btn.setPreferredSize(new Dimension(80, 30));
    }
}