package gui;

import dao.*;
import entity.CaLam;
import entity.GiaoCa;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*; // [QUAN TRỌNG] Thêm thư viện này để vẽ biểu đồ
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Nhân Viên - Version 2.2 Final
 */
public class DashboardNhanVienGUI extends JPanel {

    // =============== CONSTANTS ===============
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font METRIC_FONT = new Font("Segoe UI", Font.BOLD, 32);

    // Cấu hình biểu đồ
    private static final int CHART_DAYS_COUNT = 7;
    private static final Color CHART_BAR_COLOR = PRIMARY_COLOR;
    private static final Color CHART_AVG_COLOR = new Color(149, 165, 166);

    // =============== DAOs ===============
    private final GiaoCaDAO giaoCaDAO;
    private final PhanCongDAO phanCongDAO;
    private final HoaDonDAO hoaDonDAO;

    // =============== DATA ===============
    private final String maNV;
    private final String tenNV;
    private NhanVien nhanVienInfo;

    // =============== UI COMPONENTS ===============
    // Header
    private JLabel lblWelcome;
    private JLabel lblCurrentTime;
    private JLabel lblShiftStatus;

    // Stats
    private JLabel lblTotalHoursWeek;
    private JLabel lblTotalHoursMonth;
    private JLabel lblRevenueToday;
    private JLabel lblCashInDrawer;

    // Shift Control
    private JLabel lblCurrentShift;
    private JLabel lblShiftTime;
    private JLabel lblStartMoney;
    private JLabel lblCurrentRevenue;
    private JButton btnStartShift;
    private JButton btnEndShift;
    private JPanel shiftControlInfoPanel;

    // Charts Container
    private JPanel chartPanel;
    private JPanel upcomingShiftsPanel;

    // Timers
    private Timer clockTimer;
    private Timer dataRefreshTimer;

    // =============== CONSTRUCTOR ===============
    public DashboardNhanVienGUI(String maNV, String tenNV) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.giaoCaDAO = new GiaoCaDAO();
        this.phanCongDAO = new PhanCongDAO();
        this.hoaDonDAO = new HoaDonDAO();

        initComponents();
        loadEmployeeData();
        startTimers();
    }

    // =============== INIT UI ===============
    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);

        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainContainer.add(createHeaderSection(), BorderLayout.NORTH);
        mainContainer.add(createCenterSection(), BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(BACKGROUND_COLOR);

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        welcomePanel.setBackground(BACKGROUND_COLOR);

        lblWelcome = new JLabel("Xin chào, " + tenNV);
        lblWelcome.setFont(TITLE_FONT);
        lblWelcome.setForeground(new Color(44, 62, 80));

        lblCurrentTime = new JLabel();
        lblCurrentTime.setFont(BODY_FONT);
        lblCurrentTime.setForeground(new Color(127, 140, 141));
        updateClock();

        welcomePanel.add(lblWelcome);
        welcomePanel.add(lblCurrentTime);

        lblShiftStatus = new JLabel("Chưa bắt đầu ca", JLabel.CENTER);
        lblShiftStatus.setFont(HEADER_FONT);
        lblShiftStatus.setOpaque(true);
        lblShiftStatus.setBackground(new Color(189, 195, 199));
        lblShiftStatus.setForeground(Color.WHITE);
        lblShiftStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(127, 140, 141), 2, true),
                new EmptyBorder(10, 20, 10, 20)
        ));

        headerPanel.add(welcomePanel, BorderLayout.WEST);
        headerPanel.add(lblShiftStatus, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCenterSection() {
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(BACKGROUND_COLOR);

        centerPanel.add(createQuickStatsPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createShiftControlPanel());
        splitPane.setRightComponent(createChartsPanel());

        centerPanel.add(splitPane, BorderLayout.CENTER);

        return centerPanel;
    }

    // =============== QUICK STATS ===============
    private JPanel createQuickStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setPreferredSize(new Dimension(0, 130));

        // Card 1: Giờ làm tuần
        statsPanel.add(createStatCard(
                "Giờ làm tuần",
                "0.0 giờ",
                "/img/icon/calendar_month.png", // Đường dẫn ảnh của bạn
                PRIMARY_COLOR,
                l -> lblTotalHoursWeek = l
        ));

        // Card 2: Giờ làm tháng
        statsPanel.add(createStatCard(
                "Giờ làm tháng",
                "0.0 giờ",
                "/img/icon/date_range.png", // Đường dẫn ảnh của bạn
                SUCCESS_COLOR,
                l -> lblTotalHoursMonth = l
        ));

        // Card 3: Doanh thu hôm nay
        statsPanel.add(createStatCard(
                "Doanh thu hôm nay",
                "0 ₫",
                "/img/icon/attach_money.png", // Đường dẫn ảnh của bạn
                WARNING_COLOR,
                l -> lblRevenueToday = l
        ));

        // Card 4: Tiền trong két
        statsPanel.add(createStatCard(
                "Tiền trong két",
                "0 ₫",
                "/img/icon/account_balance_wallet.png", // Đường dẫn ảnh của bạn
                DANGER_COLOR,
                l -> lblCashInDrawer = l
        ));

        return statsPanel;
    }

    // Sửa tham số thứ 3 từ 'String icon' (emoji) thành 'String iconPath' (đường dẫn ảnh)
    private JPanel createStatCard(String title, String value, String iconPath, Color accentColor, StatCardCallback callback) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // --- PHẦN THAY ĐỔI: Xử lý Icon ảnh ---
        JLabel lblIcon = new JLabel("", JLabel.CENTER);
        // Load ảnh kích thước 50x50 pixel
        ImageIcon icon = loadIcon(iconPath, 50, 50);

        if (icon != null) {
            lblIcon.setIcon(icon);
        } else {
            // Fallback: Nếu không thấy ảnh thì hiện tạm chữ cái đầu hoặc dấu hỏi
            lblIcon.setText("?");
            lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblIcon.setForeground(accentColor);
        }
        lblIcon.setPreferredSize(new Dimension(60, 60)); // Tăng size container chứa icon
        // -------------------------------------

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(CARD_COLOR);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(BODY_FONT);
        lblTitle.setForeground(new Color(127, 140, 141));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(METRIC_FONT);
        lblValue.setForeground(accentColor);

        textPanel.add(lblTitle);
        textPanel.add(lblValue);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        if (callback != null) callback.accept(lblValue);

        return card;
    }

    @FunctionalInterface
    interface StatCardCallback {
        void accept(JLabel valueLabel);
    }

    // =============== SHIFT CONTROL PANEL ===============
    private JPanel createShiftControlPanel() {
        JPanel shiftPanel = new JPanel(new BorderLayout(0, 15));
        shiftPanel.setBackground(CARD_COLOR);
        shiftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel("Quản lý ca làm & Đồng nghiệp");
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(new Color(44, 62, 80));

        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 10, 15));
        infoGrid.setBackground(CARD_COLOR);

        infoGrid.add(createInfoLabel("Ca hiện tại:"));
        lblCurrentShift = createInfoValue("Chưa có ca");
        infoGrid.add(lblCurrentShift);

        infoGrid.add(createInfoLabel("Thời gian:"));
        lblShiftTime = createInfoValue("--:-- - --:--");
        infoGrid.add(lblShiftTime);

        infoGrid.add(createInfoLabel("Tiền đầu ca:"));
        lblStartMoney = createInfoValue("0 ₫");
        infoGrid.add(lblStartMoney);

        infoGrid.add(createInfoLabel("Doanh thu ca:"));
        lblCurrentRevenue = createInfoValue("0 ₫");
        infoGrid.add(lblCurrentRevenue);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        btnStartShift = createActionButton("Bắt đầu ca", SUCCESS_COLOR);
        btnStartShift.addActionListener(e -> handleStartShift());

        btnEndShift = createActionButton("Kết thúc ca", DANGER_COLOR);
        btnEndShift.setEnabled(false);
        btnEndShift.addActionListener(e -> handleEndShift());

        buttonPanel.add(btnStartShift);
        buttonPanel.add(btnEndShift);

        shiftControlInfoPanel = createShiftControlInfoPanel();

        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.add(infoGrid, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);
        contentPanel.add(shiftControlInfoPanel, BorderLayout.SOUTH);

        shiftPanel.add(lblTitle, BorderLayout.NORTH);
        shiftPanel.add(contentPanel, BorderLayout.CENTER);

        return shiftPanel;
    }

    private JPanel createShiftControlInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBackground(CARD_COLOR);
        infoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Đồng nghiệp làm ca gần nhất", TitledBorder.LEFT, TitledBorder.TOP,
                BODY_FONT, new Color(127, 140, 141)));

        infoPanel.add(createInfoLabel("Ca trước:"));
        JLabel lblPrevShift = createInfoValue("-- Trống --");
        lblPrevShift.setName("lblPrevShift");
        infoPanel.add(lblPrevShift);

        infoPanel.add(createInfoLabel("Ca sau:"));
        JLabel lblNextShift = createInfoValue("-- Trống --");
        lblNextShift.setName("lblNextShift");
        infoPanel.add(lblNextShift);

        return infoPanel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY_FONT);
        label.setForeground(new Color(127, 140, 141));
        return label;
    }

    private JLabel createInfoValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(BODY_FONT);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    // =============== CHARTS & SCHEDULE ===============
    private JPanel createChartsPanel() {
        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        chartsContainer.setBackground(BACKGROUND_COLOR);

        chartsContainer.add(createWorkHoursChart());
        chartsContainer.add(createWorkSchedulePanel());

        return chartsContainer;
    }

    private JPanel createWorkHoursChart() {
        JPanel chartCard = new JPanel(new BorderLayout(10, 10));
        chartCard.setBackground(CARD_COLOR);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Giờ làm " + CHART_DAYS_COUNT + " ngày gần nhất");
        title.setFont(HEADER_FONT);
        title.setForeground(new Color(44, 62, 80));

        chartPanel = new JPanel();
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setLayout(new BorderLayout());

        chartCard.add(title, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        return chartCard;
    }

    // [QUAN TRỌNG] Phương thức vẽ biểu đồ tùy chỉnh (Code bạn cung cấp)
    // File: gui/DashboardNhanVienGUI.java

    /**
     * [ĐÃ SỬA] Biểu đồ giờ làm 7 ngày gần nhất với đường tham chiếu 8 giờ
     * Fix lỗi: Cột chồng lên nhau, hiển thị sai số lượng ngày
     */
    private JPanel createWorkHoursBarChart(Map<String, Double> data) {
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (data.isEmpty() || data.values().stream().allMatch(v -> v == 0.0)) {
                    g2.setFont(BODY_FONT);
                    g2.setColor(Color.GRAY);
                    String msg = "Chưa có dữ liệu giờ làm";
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(msg)) / 2;
                    int y = getHeight() / 2;
                    g2.drawString(msg, x, y);
                    return;
                }

                // === 1. LỌC DỮ LIỆU (CHỈ LẤY 7 NGÀY CUỐI) ===
                // Dù DAO đã lọc, ta lọc lại ở đây để đảm bảo hiển thị không bị vỡ layout
                Map<String, Double> filteredData = data.entrySet().stream()
                        .skip(Math.max(0, data.size() - 7)) // Bỏ qua các ngày cũ, chỉ lấy 7 ngày cuối
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                java.util.LinkedHashMap::new
                        ));

                // === 2. CẤU HÌNH KÍCH THƯỚC ===
                final double REFERENCE_LINE = 8.0; // Đường tham chiếu 8 giờ
                int padding = 40;
                int barMargin = 20; // Khoảng cách rộng hơn để không bị dính
                int chartHeight = getHeight() - 2 * padding;
                int totalBars = filteredData.size();

                // Tính chiều rộng cột động, nhưng giới hạn tối thiểu để dễ nhìn
                int barWidth = (getWidth() - 2 * padding - barMargin * (totalBars - 1)) / totalBars;
                // Nếu cột quá nhỏ (do màn hình bé), set cố định (có thể bị tràn, nhưng đẹp hơn dính)
                if (barWidth < 20) barWidth = 20;

                // Tính giá trị max (Trục Y)
                double maxValue = Math.max(
                        filteredData.values().stream().mapToDouble(Double::doubleValue).max().orElse(10.0),
                        REFERENCE_LINE + 1.0
                );

                // Tính trung bình
                double averageValue = filteredData.values().stream().mapToDouble(Double::doubleValue).sum() / totalBars;

                // === 3. VẼ NỀN THAM CHIẾU ===
                int refY = getHeight() - padding - (int) ((REFERENCE_LINE / maxValue) * chartHeight);

                // Vùng xanh (Đạt chuẩn)
                g2.setColor(new Color(39, 174, 96, 20));
                g2.fillRect(padding, padding, getWidth() - 2 * padding, refY - padding);

                // Vùng đỏ (Chưa đạt)
                g2.setColor(new Color(231, 76, 60, 15));
                g2.fillRect(padding, refY, getWidth() - 2 * padding, getHeight() - padding - refY);

                // Đường kẻ 8h
                g2.setColor(new Color(52, 73, 94));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f));
                g2.drawLine(padding, refY, getWidth() - padding, refY);

                g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                g2.drawString("Mục tiêu: 8h", getWidth() - padding - 60, refY - 5);
                g2.setStroke(new BasicStroke(1.0f));

                // === 4. VẼ ĐƯỜNG TRUNG BÌNH ===
                int avgY = getHeight() - padding - (int) ((averageValue / maxValue) * chartHeight);
                g2.setColor(CHART_AVG_COLOR);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5.0f, 5.0f}, 0.0f));
                g2.drawLine(padding, avgY, getWidth() - padding, avgY);
                g2.setStroke(new BasicStroke(1.0f));

                // === 5. VẼ CỘT ===
                int x = padding;
                for (Map.Entry<String, Double> entry : filteredData.entrySet()) {
                    double value = entry.getValue();
                    int barHeight = (int) ((value / maxValue) * chartHeight);

                    // Màu sắc: Xanh (>=8h), Vàng (>=6h), Đỏ (<6h)
                    Color barColor;
                    if (value >= REFERENCE_LINE) barColor = SUCCESS_COLOR;
                    else if (value >= 6.0) barColor = WARNING_COLOR;
                    else barColor = DANGER_COLOR;

                    // Vẽ cột
                    g2.setColor(barColor);
                    g2.fillRoundRect(x, getHeight() - padding - barHeight, barWidth, barHeight, 8, 8);

                    // Giá trị trên cột
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String valueStr = String.format("%.1fh", value);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = x + (barWidth - fm.stringWidth(valueStr)) / 2;
                    g2.drawString(valueStr, textX, getHeight() - padding - barHeight - 5);

                    // Nhãn ngày (Trục X)
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.setColor(new Color(44, 62, 80));
                    String label = entry.getKey();
                    int labelX = x + (barWidth - g2.getFontMetrics().stringWidth(label)) / 2;
                    g2.drawString(label, labelX, getHeight() - padding + 20);

                    x += barWidth + barMargin;
                }

                // Vẽ trục Y đơn giản
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(padding, padding, padding, getHeight() - padding); // Trục dọc
                g2.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding); // Trục ngang
            }
        };

        chart.setBackground(CARD_COLOR);
        chart.setPreferredSize(new Dimension(0, 250));
        return chart;
    }

    private JPanel createWorkSchedulePanel() {
        JPanel shiftsCard = new JPanel(new BorderLayout(10, 10));
        shiftsCard.setBackground(CARD_COLOR);
        shiftsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Lịch làm việc 3 ngày sắp tới");
        title.setFont(HEADER_FONT);
        title.setForeground(new Color(44, 62, 80));

        upcomingShiftsPanel = new JPanel();
        upcomingShiftsPanel.setLayout(new BoxLayout(upcomingShiftsPanel, BoxLayout.Y_AXIS));
        upcomingShiftsPanel.setBackground(CARD_COLOR);

        JScrollPane scrollPane = new JScrollPane(upcomingShiftsPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_COLOR);

        shiftsCard.add(title, BorderLayout.NORTH);
        shiftsCard.add(scrollPane, BorderLayout.CENTER);

        return shiftsCard;
    }

    private JPanel createShiftItem(String shiftInfo) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(new Color(245, 247, 250));
        item.setBorder(new EmptyBorder(10, 15, 10, 15));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lblIcon = new JLabel("");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel lblInfo = new JLabel(shiftInfo);
        lblInfo.setFont(BODY_FONT);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(lblInfo, BorderLayout.CENTER);

        return item;
    }

    // =============== DATA LOADING & LOGIC ===============
    private void loadEmployeeData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    NhanVienDAO nvDAO = new NhanVienDAO();
                    nhanVienInfo = nvDAO.getChiTietNhanVien(maNV);

                    loadShiftStatus();
                    loadStatistics();
                    loadWorkHoursChart();
                    loadUpcomingShifts();
                    loadShiftControlInfo();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }

    private void loadShiftStatus() {
        int maGiaoCa = giaoCaDAO.getMaCaDangLamViec(maNV);
        GiaoCa caHienTai = (maGiaoCa > 0) ? giaoCaDAO.getThongTinCaDangLam(maNV) : null;

        SwingUtilities.invokeLater(() -> {
            if (maGiaoCa > 0 && caHienTai != null) {
                lblShiftStatus.setText("Đang làm việc");
                lblShiftStatus.setBackground(SUCCESS_COLOR);

                CaLam caLam = phanCongDAO.getCaLamViecCuaNhanVien(maNV, LocalDate.now());
                if (caLam != null) {
                    lblCurrentShift.setText(caLam.getTenCa());
                    lblShiftTime.setText(caLam.getGioBatDau().toString().substring(0, 5) + " - " + caLam.getGioKetThuc().toString().substring(0, 5));
                } else {
                    lblCurrentShift.setText("Không rõ");
                    lblShiftTime.setText("--:-- - --:--");
                }

                lblStartMoney.setText(String.format("%,.0f ₫", caHienTai.getTienDauCa()));

                // Tính tiền trong két
                double revenueAll = hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, caHienTai.getThoiGianBatDau(), "Tiền mặt") +
                        hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, caHienTai.getThoiGianBatDau(), "Chuyển khoản") +
                        hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, caHienTai.getThoiGianBatDau(), "Thẻ");

                lblCurrentRevenue.setText(String.format("%,.0f ₫", revenueAll));

                double revenueCash = hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, caHienTai.getThoiGianBatDau(), "Tiền mặt");
                double cashInDrawer = caHienTai.getTienDauCa() + revenueCash;
                lblCashInDrawer.setText(String.format("%,.0f ₫", cashInDrawer));

                btnStartShift.setEnabled(false);
                btnEndShift.setEnabled(true);
            } else {
                lblShiftStatus.setText("⏸️ Chưa bắt đầu ca");
                lblShiftStatus.setBackground(new Color(189, 195, 199));
                lblCurrentShift.setText("Chưa có ca");
                lblShiftTime.setText("--:-- - --:--");
                lblStartMoney.setText("0 ₫");
                lblCurrentRevenue.setText("0 ₫");
                lblCashInDrawer.setText("0 ₫");

                btnStartShift.setEnabled(true);
                btnEndShift.setEnabled(false);
            }
        });
    }

    private void loadStatistics() {
        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        double hoursWeek = giaoCaDAO.getTongGioLamTheoTuan(maNV, startOfWeek);

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        double hoursMonth = giaoCaDAO.getTongGioLamTheoThang(maNV, startOfMonth);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double revenueToday = hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, startOfDay, "Tiền mặt")
                + hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, startOfDay, "Chuyển khoản")
                + hoaDonDAO.getDoanhThuTheoHinhThuc(maNV, startOfDay, "Thẻ");

        SwingUtilities.invokeLater(() -> {
            lblTotalHoursWeek.setText(String.format("%.1f giờ", hoursWeek));
            lblTotalHoursMonth.setText(String.format("%.1f giờ", hoursMonth));
            lblRevenueToday.setText(String.format("%,.0f ₫", revenueToday));
        });
    }

    private void loadWorkHoursChart() {
        Map<String, Double> data = giaoCaDAO.getGioLamTheoNgay(maNV, CHART_DAYS_COUNT);
        SwingUtilities.invokeLater(() -> {
            chartPanel.removeAll();
            chartPanel.add(createWorkHoursBarChart(data), BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private void loadUpcomingShifts() {
        List<String> shifts = giaoCaDAO.getCacCaLamSapToi(maNV);
        SwingUtilities.invokeLater(() -> {
            upcomingShiftsPanel.removeAll();
            if (shifts.isEmpty()) {
                JLabel lblEmpty = new JLabel("Không có ca làm sắp tới", JLabel.CENTER);
                lblEmpty.setFont(BODY_FONT);
                lblEmpty.setForeground(Color.GRAY);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
                upcomingShiftsPanel.add(Box.createVerticalGlue());
                upcomingShiftsPanel.add(lblEmpty);
                upcomingShiftsPanel.add(Box.createVerticalGlue());
            } else {
                for (String shift : shifts) {
                    upcomingShiftsPanel.add(createShiftItem(shift));
                    upcomingShiftsPanel.add(Box.createVerticalStrut(10));
                }
            }
            upcomingShiftsPanel.revalidate();
            upcomingShiftsPanel.repaint();
        });
    }

    private void loadShiftControlInfo() {
        String[] shiftInfo = phanCongDAO.getThongTinCaTruocSau(maNV, LocalDate.now());
        SwingUtilities.invokeLater(() -> {
            JLabel lblPrev = (JLabel) findComponentByName(shiftControlInfoPanel, "lblPrevShift");
            if (lblPrev != null) lblPrev.setText(shiftInfo[0]);

            JLabel lblNext = (JLabel) findComponentByName(shiftControlInfoPanel, "lblNextShift");
            if (lblNext != null) lblNext.setText(shiftInfo[1]);
        });
    }

    private Component findComponentByName(Container container, String name) {
        if (name.equals(container.getName())) return container;
        for (Component child : container.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container) {
                Component found = findComponentByName((Container) child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    // =============== ACTIONS ===============
    private void handleStartShift() {
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền đầu ca:", "Bắt đầu ca làm", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            double tien = Double.parseDouble(input.replace(",", ""));
            if (giaoCaDAO.batDauCa(maNV, tien)) {
                JOptionPane.showMessageDialog(this, "Bắt đầu ca thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeData();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể bắt đầu ca. Có thể bạn chưa kết thúc ca trước.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEndShift() {
        GiaoCa ca = giaoCaDAO.getThongTinCaDangLam(maNV);
        if (ca == null) return;

        String input = JOptionPane.showInputDialog(this, "Nhập tổng tiền thực tế trong két (Tiền mặt):", "Kết thúc ca làm", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;

        try {
            double tienCuoi = Double.parseDouble(input.replace(",", ""));
            String ghiChu = JOptionPane.showInputDialog(this, "Ghi chú (nếu có):");
            if (giaoCaDAO.ketThucCa(ca.getMaGiaoCa(), tienCuoi, ghiChu)) {
                JOptionPane.showMessageDialog(this, "Kết thúc ca thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi kết thúc ca.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClock() {
        lblCurrentTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy - HH:mm:ss")));
    }

    private void startTimers() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        dataRefreshTimer = new Timer(30000, e -> loadEmployeeData());
        dataRefreshTimer.start();
    }

    public void stopTimers() {
        if (clockTimer != null) clockTimer.stop();
        if (dataRefreshTimer != null) dataRefreshTimer.stop();
    }

    // Hàm hỗ trợ tải và resize icon
    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            // Tải ảnh từ đường dẫn resource
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                // Resize ảnh cho mượt (Smooth)
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("Không tìm thấy icon: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}