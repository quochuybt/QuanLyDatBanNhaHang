package iuh.fit.gui;

import iuh.fit.core.dto.GiaoCaDTO;
import iuh.fit.core.net.client.GiaoCaRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.dto.giaoca.GiaoCaDashboardResponse;
import iuh.fit.core.net.protocol.EventType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DashboardNhanVienGUI extends BaseEventAwarePanel {

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

    private static final int CHART_DAYS_COUNT = 7;

    private final GiaoCaRemoteService giaoCaRemoteService;

    private final String maNV;
    private final String tenNV;

    private GiaoCaDTO caHienTai;

    private JLabel lblWelcome;
    private JLabel lblShiftStatus;
    private JLabel lblTotalHoursWeek;
    private JLabel lblTotalHoursMonth;
    private JLabel lblRevenueToday;
    private JLabel lblCashInDrawer;
    private JLabel lblCurrentShift;
    private JLabel lblShiftTime;
    private JLabel lblStartMoney;
    private JLabel lblCurrentRevenue;

    private JButton btnStartShift;
    private JButton btnEndShift;

    private JPanel shiftControlInfoPanel;
    private JPanel chartPanel;
    private JPanel upcomingShiftsPanel;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public DashboardNhanVienGUI(String maNV, String tenNV, SocketClientConnection socketConnection) {
        super(socketConnection);
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.giaoCaRemoteService = new GiaoCaRemoteService(socketConnection);

        initComponents();
        loadEmployeeData();
    }

    @Override
    protected void onBusinessEvent(EventType eventType) {
        if (eventType == EventType.GIAOCA_UPDATED) {
            SwingUtilities.invokeLater(this::loadEmployeeData);
        }
    }

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

        welcomePanel.add(lblWelcome);

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

    private JPanel createQuickStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setPreferredSize(new Dimension(0, 130));

        statsPanel.add(createStatCard(
                "Giờ làm tuần",
                "0.0 giờ",
                "/img/icon/calendar_month.png",
                PRIMARY_COLOR,
                l -> lblTotalHoursWeek = l
        ));

        statsPanel.add(createStatCard(
                "Giờ làm tháng",
                "0.0 giờ",
                "/img/icon/date_range.png",
                SUCCESS_COLOR,
                l -> lblTotalHoursMonth = l
        ));

        statsPanel.add(createStatCard(
                "Doanh thu hôm nay",
                "0 ₫",
                "/img/icon/attach_money.png",
                WARNING_COLOR,
                l -> lblRevenueToday = l
        ));

        statsPanel.add(createStatCard(
                "Tiền trong két",
                "0 ₫",
                "/img/icon/account_balance_wallet.png",
                DANGER_COLOR,
                l -> lblCashInDrawer = l
        ));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String iconPath, Color accentColor, StatCardCallback callback) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblIcon = new JLabel("", JLabel.CENTER);
        ImageIcon icon = loadIcon(iconPath, 50, 50);

        if (icon != null) {
            lblIcon.setIcon(icon);
        } else {
            lblIcon.setText("?");
            lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblIcon.setForeground(accentColor);
        }

        lblIcon.setPreferredSize(new Dimension(60, 60));

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

        if (callback != null) {
            callback.accept(lblValue);
        }

        return card;
    }

    @FunctionalInterface
    interface StatCardCallback {
        void accept(JLabel valueLabel);
    }

    private JPanel createShiftControlPanel() {
        JPanel shiftPanel = new JPanel(new BorderLayout(10, 15));
        shiftPanel.setBackground(CARD_COLOR);
        shiftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(30, 20, 30, 20)
        ));

        JLabel lblTitle = new JLabel("Quản lý ca làm & Đồng nghiệp");
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(new Color(44, 62, 80));

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(CARD_COLOR);

        JPanel infoGrid = new JPanel(new GridLayout(4, 2, 10, 12));
        infoGrid.setBackground(CARD_COLOR);
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        shiftControlInfoPanel = createShiftControlInfoPanel();
        shiftControlInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContent.add(infoGrid);
        centerContent.add(Box.createVerticalStrut(20));
        centerContent.add(shiftControlInfoPanel);
        centerContent.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);

        btnStartShift = createActionButton("Bắt đầu ca", SUCCESS_COLOR);
        btnStartShift.addActionListener(e -> handleStartShift());

        btnEndShift = createActionButton("Kết thúc ca", DANGER_COLOR);
        btnEndShift.setEnabled(false);
        btnEndShift.addActionListener(e -> handleEndShift());

        buttonPanel.add(btnStartShift);
        buttonPanel.add(btnEndShift);

        shiftPanel.add(lblTitle, BorderLayout.NORTH);
        shiftPanel.add(centerContent, BorderLayout.CENTER);
        shiftPanel.add(buttonPanel, BorderLayout.SOUTH);

        return shiftPanel;
    }

    private JPanel createShiftControlInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        infoPanel.setBackground(CARD_COLOR);

        infoPanel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Đồng nghiệp làm ca gần nhất",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                BODY_FONT,
                new Color(127, 140, 141)
        ));

        JPanel pnlPrev = new JPanel(new BorderLayout());
        pnlPrev.setBackground(CARD_COLOR);
        pnlPrev.add(createInfoLabel("Ca trước:"), BorderLayout.NORTH);

        JLabel lblPrevShift = new JLabel("<html><center>Đang tải...</center></html>", JLabel.CENTER);
        lblPrevShift.setName("lblPrevShift");
        lblPrevShift.setVerticalAlignment(JLabel.TOP);
        pnlPrev.add(lblPrevShift, BorderLayout.CENTER);

        JPanel pnlNext = new JPanel(new BorderLayout());
        pnlNext.setBackground(CARD_COLOR);
        pnlNext.add(createInfoLabel("Ca sau:"), BorderLayout.NORTH);

        JLabel lblNextShift = new JLabel("<html><center>Đang tải...</center></html>", JLabel.CENTER);
        lblNextShift.setName("lblNextShift");
        lblNextShift.setVerticalAlignment(JLabel.TOP);
        pnlNext.add(lblNextShift, BorderLayout.CENTER);

        infoPanel.add(pnlPrev);
        infoPanel.add(pnlNext);

        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        infoPanel.setPreferredSize(new Dimension(0, 140));

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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 100));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

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

        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_COLOR);

        chartCard.add(title, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        return chartCard;
    }

    private JPanel createWorkHoursBarChart(Map<String, Double> data) {
        Map<String, Double> safeData = data != null ? data : Collections.emptyMap();

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (safeData.isEmpty() || safeData.values().stream().allMatch(v -> v == null || v == 0.0)) {
                    g2.setFont(BODY_FONT);
                    g2.setColor(Color.GRAY);

                    String msg = "Chưa có dữ liệu giờ làm";
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(msg)) / 2;
                    int y = getHeight() / 2;

                    g2.drawString(msg, x, y);
                    return;
                }

                Map<String, Double> filteredData = safeData.entrySet().stream()
                        .skip(Math.max(0, safeData.size() - 7))
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue() != null ? e.getValue() : 0.0,
                                (e1, e2) -> e1,
                                java.util.LinkedHashMap::new
                        ));

                final double REFERENCE_LINE = 8.0;

                int paddingTop = 30;
                int paddingBottom = 60;
                int paddingLeft = 40;
                int paddingRight = 40;

                int barMargin = 20;
                int chartHeight = getHeight() - paddingTop - paddingBottom;
                int totalBars = filteredData.size();

                if (totalBars <= 0 || chartHeight <= 0) {
                    return;
                }

                int barWidth = (getWidth() - paddingLeft - paddingRight - barMargin * (totalBars - 1)) / totalBars;
                if (barWidth < 20) {
                    barWidth = 20;
                }

                double maxValue = Math.max(
                        filteredData.values().stream().mapToDouble(Double::doubleValue).max().orElse(10.0),
                        REFERENCE_LINE + 1.0
                );

                int refY = paddingTop + (int) ((1 - REFERENCE_LINE / maxValue) * chartHeight);

                g2.setPaint(new GradientPaint(
                        paddingLeft, paddingTop, new Color(39, 174, 96, 10),
                        paddingLeft, refY, new Color(39, 174, 96, 20)
                ));
                g2.fillRect(paddingLeft, paddingTop, getWidth() - paddingLeft - paddingRight, refY - paddingTop);

                g2.setPaint(new GradientPaint(
                        paddingLeft, refY, new Color(231, 76, 60, 5),
                        paddingLeft, getHeight() - paddingBottom, new Color(231, 76, 60, 15)
                ));
                g2.fillRect(paddingLeft, refY, getWidth() - paddingLeft - paddingRight, getHeight() - paddingBottom - refY);

                g2.setColor(new Color(41, 128, 185, 180));

                Stroke dashed = new BasicStroke(
                        1.5f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f,
                        new float[]{8.0f, 5.0f},
                        0.0f
                );

                g2.setStroke(dashed);
                g2.drawLine(paddingLeft, refY, getWidth() - paddingRight, refY);
                g2.setStroke(new BasicStroke(1.0f));

                int x = paddingLeft;

                for (Map.Entry<String, Double> entry : filteredData.entrySet()) {
                    double value = entry.getValue() != null ? entry.getValue() : 0.0;
                    int barHeight = (int) ((value / maxValue) * chartHeight);

                    Color barColor;
                    Color barColorDark;

                    if (value >= REFERENCE_LINE) {
                        barColor = new Color(39, 174, 96);
                        barColorDark = new Color(30, 132, 73);
                    } else if (value >= 6.0) {
                        barColor = new Color(243, 156, 18);
                        barColorDark = new Color(211, 84, 0);
                    } else {
                        barColor = new Color(231, 76, 60);
                        barColorDark = new Color(192, 57, 43);
                    }

                    int yPos = getHeight() - paddingBottom - barHeight;

                    g2.setPaint(new GradientPaint(x, yPos, barColor, x, yPos + barHeight, barColorDark));
                    g2.fillRoundRect(x, yPos, barWidth, barHeight, 8, 8);

                    g2.setColor(barColorDark);
                    g2.drawRoundRect(x, yPos, barWidth, barHeight, 8, 8);

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String valueStr = String.format("%.1fh", value);
                    FontMetrics fmVal = g2.getFontMetrics();
                    int txtX = x + (barWidth - fmVal.stringWidth(valueStr)) / 2;

                    g2.setColor(new Color(44, 62, 80));
                    g2.drawString(valueStr, txtX, yPos - 3);

                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    String dateLabel = entry.getKey();
                    int dateX = x + (barWidth - g2.getFontMetrics().stringWidth(dateLabel)) / 2;

                    g2.drawString(dateLabel, dateX, getHeight() - paddingBottom + 18);

                    x += barWidth + barMargin;
                }

                g2.setColor(new Color(189, 195, 199));
                g2.drawLine(
                        paddingLeft,
                        getHeight() - paddingBottom,
                        getWidth() - paddingRight,
                        getHeight() - paddingBottom
                );

                int legendY = getHeight() - 15;
                int centerX = getWidth() / 2;

                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(dashed);
                g2.drawLine(centerX - 40, legendY - 4, centerX - 10, legendY - 4);
                g2.setStroke(new BasicStroke(1.0f));

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(100, 100, 100));
                g2.drawString(": 8h - Chỉ tiêu", centerX, legendY);
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

        JLabel title = new JLabel("Lịch làm việc sắp tới");
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

        JLabel lblInfo = new JLabel(shiftInfo != null ? shiftInfo : "");
        lblInfo.setFont(BODY_FONT);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(lblInfo, BorderLayout.CENTER);

        return item;
    }

    private void loadEmployeeData() {
        setBusy(true);

        new SwingWorker<GiaoCaDashboardResponse, Void>() {
            @Override
            protected GiaoCaDashboardResponse doInBackground() {
                return giaoCaRemoteService.loadDashboard(maNV);
            }

            @Override
            protected void done() {
                try {
                    GiaoCaDashboardResponse response = get();
                    updateDashboard(response);
                } catch (Exception e) {
                    showError(
                            "Lỗi tải dashboard nhân viên: " + getRootMessage(e),
                            "Lỗi"
                    );
                    resetShiftUI();
                    updateWorkHoursChart(Collections.emptyMap());
                    updateUpcomingShifts(Collections.emptyList());
                    updateShiftControlInfo("Không có", "Không có");
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void updateDashboard(GiaoCaDashboardResponse response) {
        if (response == null) {
            resetShiftUI();
            return;
        }

        this.caHienTai = response.getCaHienTai();

        lblTotalHoursWeek.setText(String.format("%.1f giờ", response.getTongGioTuan()));
        lblTotalHoursMonth.setText(String.format("%.1f giờ", response.getTongGioThang()));
        lblRevenueToday.setText(formatMoney(response.getDoanhThuHomNay()));

        if (caHienTai != null) {
            updateShiftWorking(response);
        } else {
            resetShiftUI();
        }

        updateWorkHoursChart(response.getGioLamTheoNgay());
        updateUpcomingShifts(response.getCacCaLamSapToi());
        updateShiftControlInfo(response.getCaTruoc(), response.getCaSau());
    }

    private void updateShiftWorking(GiaoCaDashboardResponse response) {
        lblShiftStatus.setText("Đang làm việc");
        lblShiftStatus.setBackground(SUCCESS_COLOR);

        lblCurrentShift.setText("Ca đang thực hiện");

        if (caHienTai.getThoiGianBatDau() != null) {
            lblShiftTime.setText(caHienTai.getThoiGianBatDau().format(timeFormatter) + " - Hiện tại");
        } else {
            lblShiftTime.setText("--:-- - Hiện tại");
        }

        lblStartMoney.setText(formatMoney(caHienTai.getTienDauCa()));
        lblCurrentRevenue.setText(formatMoney(response.getDoanhThuCaHienTai()));
        lblCashInDrawer.setText(formatMoney(response.getTienMatTrongKet()));

        btnStartShift.setEnabled(false);
        btnEndShift.setEnabled(true);
    }

    private void resetShiftUI() {
        this.caHienTai = null;

        lblShiftStatus.setText("Trạng thái: Chưa bắt đầu ca");
        lblShiftStatus.setBackground(new Color(189, 195, 199));

        lblCurrentShift.setText("Chưa có ca");
        lblShiftTime.setText("--:-- - --:--");
        lblStartMoney.setText("0 ₫");
        lblCurrentRevenue.setText("0 ₫");
        lblCashInDrawer.setText("0 ₫");

        btnStartShift.setEnabled(true);
        btnEndShift.setEnabled(false);
    }

    private void updateWorkHoursChart(Map<String, Double> data) {
        chartPanel.removeAll();
        chartPanel.add(createWorkHoursBarChart(data), BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void updateUpcomingShifts(List<String> shifts) {
        upcomingShiftsPanel.removeAll();

        if (shifts == null || shifts.isEmpty()) {
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
    }

    private void updateShiftControlInfo(String caTruoc, String caSau) {
        JLabel lblPrev = (JLabel) findComponentByName(shiftControlInfoPanel, "lblPrevShift");
        if (lblPrev != null) {
            lblPrev.setText("<html><center>" + safeHtml(caTruoc) + "</center></html>");
        }

        JLabel lblNext = (JLabel) findComponentByName(shiftControlInfoPanel, "lblNextShift");
        if (lblNext != null) {
            lblNext.setText("<html><center>" + safeHtml(caSau) + "</center></html>");
        }
    }

    private Component findComponentByName(Container container, String name) {
        if (container == null || name == null) {
            return null;
        }

        if (name.equals(container.getName())) {
            return container;
        }

        for (Component child : container.getComponents()) {
            if (name.equals(child.getName())) {
                return child;
            }

            if (child instanceof Container) {
                Component found = findComponentByName((Container) child, name);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private void handleStartShift() {
        String input = JOptionPane.showInputDialog(
                this,
                "Nhập số tiền đầu ca:",
                "Bắt đầu ca làm",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        double tienDauCa;

        try {
            tienDauCa = Double.parseDouble(input.replace(",", "").trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số tiền không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (tienDauCa < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tiền đầu ca không được âm!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        GiaoCaDTO newShift = GiaoCaDTO.builder()
                .maNV(maNV)
                .thoiGianBatDau(LocalDateTime.now())
                .tienDauCa(tienDauCa)
                .build();

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return giaoCaRemoteService.batDauCa(newShift);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                DashboardNhanVienGUI.this,
                                "Bắt đầu ca thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadEmployeeData();
                    } else {
                        JOptionPane.showMessageDialog(
                                DashboardNhanVienGUI.this,
                                "Lỗi: Bạn đang có ca chưa kết thúc!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        setBusy(false);
                    }
                } catch (Exception e) {
                    showError(
                            "Lỗi bắt đầu ca: " + getRootMessage(e),
                            "Lỗi"
                    );
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void handleEndShift() {
        if (caHienTai == null || caHienTai.getMaGiaoCa() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy ca đang làm.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String input = JOptionPane.showInputDialog(
                this,
                "Nhập tổng tiền thực tế trong két (Tiền mặt):",
                "Kết thúc ca làm",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        double tienCuoiCa;

        try {
            tienCuoiCa = Double.parseDouble(input.replace(",", "").trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số tiền không hợp lệ!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (tienCuoiCa < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tiền cuối ca không được âm!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String ghiChu = JOptionPane.showInputDialog(this, "Ghi chú (nếu có):");

        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return giaoCaRemoteService.ketThucCa(
                        caHienTai.getMaGiaoCa(),
                        tienCuoiCa,
                        ghiChu
                );
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                DashboardNhanVienGUI.this,
                                "Kết thúc ca thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadEmployeeData();
                    } else {
                        JOptionPane.showMessageDialog(
                                DashboardNhanVienGUI.this,
                                "Lỗi khi kết thúc ca.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        setBusy(false);
                    }
                } catch (Exception e) {
                    showError(
                            "Lỗi kết thúc ca: " + getRootMessage(e),
                            "Lỗi"
                    );
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );

        if (btnStartShift != null) {
            btnStartShift.setEnabled(!busy && caHienTai == null);
        }

        if (btnEndShift != null) {
            btnEndShift.setEnabled(!busy && caHienTai != null);
        }
    }

    private String formatMoney(double value) {
        return String.format("%,.0f ₫", value);
    }

    private String safeHtml(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Không có";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
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

    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);

            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}