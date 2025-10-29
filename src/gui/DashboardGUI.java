package gui;

import com.toedter.calendar.JDateChooser;
import dao.BanDAO;
import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.MonAnDAO;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.*;

public class DashboardGUI extends JPanel {

    // --- Màu sắc và Fonts ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_SECTION_BG = Color.WHITE;
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    private static final Color COLOR_TEXT_DARK = new Color(51, 51, 51);
    private static final Color COLOR_TEXT_LIGHT = new Color(102, 102, 102);
    private static final Color COLOR_GREEN_STAT = new Color(0, 150, 50);
    private static final Color COLOR_RED_STAT = new Color(220, 53, 69);
    private static final Color COLOR_YELLOW_STAT = new Color(255, 193, 7);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LIST_ITEM = new Font("Segoe UI", Font.PLAIN, 14);

    // --- Components ---
    private JDateChooser dateChooserStart;
    private JDateChooser dateChooserEnd;
    private JButton btnRefreshData;

    // Revenue components (Doanh thu)
    private JPanel pnlRevenueChart;
    private JLabel lblTotalRevenue;
    private JLabel lblOrderCount;
    private JLabel lblAvgOrderValue;

    // Table Status components (Tình trạng bàn)
    private JLabel lblBanTrong;
    private JLabel lblBanPhucVu;
    private JLabel lblBanDatTruoc;

    // Performance components (Hiệu suất)
    private DefaultListModel<String> modelTopMonAn;
    private JList<String> listTopMonAn;
    private JPanel pnlStaffPerformanceChart; // <<< SỬA: Đổi tên từ pnlPaymentChart

    // --- DAOs ---
    private final HoaDonDAO hoaDonDAO;
    private final BanDAO banDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;

    // --- Formatters ---
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DecimalFormat numberFormatter = new DecimalFormat("#,##0");

    public DashboardGUI() {
        this.hoaDonDAO = new HoaDonDAO();
        this.banDAO = new BanDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // --- Revenue Section ---
        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 0.6;
        mainContentPanel.add(createRevenueSection(), gbc);

        // --- Real-time Status Section ---
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 0.6;
        mainContentPanel.add(createRealtimeStatusSection(), gbc);

        // --- Performance Section ---
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        mainContentPanel.add(createPerformanceSection(), gbc); // <<< SỬA: Hàm này đã được cập nhật

        add(mainContentPanel, BorderLayout.CENTER);

        setDefaultDateRange();
        loadDashboardData();
    }

    // --- Create Header Panel (Không đổi) ---
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        headerPanel.add(new JLabel("Từ ngày:"));
        dateChooserStart = new JDateChooser();
        dateChooserStart.setDateFormatString("dd/MM/yyyy");
        dateChooserStart.setPreferredSize(new Dimension(130, 30));
        headerPanel.add(dateChooserStart);

        headerPanel.add(new JLabel("Đến ngày:"));
        dateChooserEnd = new JDateChooser();
        dateChooserEnd.setDateFormatString("dd/MM/yyyy");
        dateChooserEnd.setPreferredSize(new Dimension(130, 30));
        headerPanel.add(dateChooserEnd);

        btnRefreshData = new JButton("Xem thống kê");
        btnRefreshData.setFont(FONT_BUTTON);
        btnRefreshData.setBackground(COLOR_ACCENT_BLUE);
        btnRefreshData.setForeground(Color.WHITE);
        btnRefreshData.setFocusPainted(false);
        btnRefreshData.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefreshData.setIcon(createIcon("/img/icon_common/refresh.png", 16, 16));
        btnRefreshData.addActionListener(e -> loadDashboardData());
        headerPanel.add(btnRefreshData);

        return headerPanel;
    }

    // --- Create Revenue Section (Không đổi) ---
    private JPanel createRevenueSection() {
        RoundedPanel sectionPanel = new RoundedPanel(15, COLOR_SECTION_BG);
        sectionPanel.setLayout(new BorderLayout(10, 10));
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("📊 Thống kê Doanh thu");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        sectionPanel.add(lblTitle, BorderLayout.NORTH);

        pnlRevenueChart = new JPanel(new BorderLayout());
        pnlRevenueChart.setOpaque(false);
        pnlRevenueChart.setPreferredSize(new Dimension(500, 300));
        sectionPanel.add(pnlRevenueChart, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);

        lblTotalRevenue = new JLabel("0 ₫", SwingConstants.CENTER);
        lblOrderCount = new JLabel("0", SwingConstants.CENTER);
        lblAvgOrderValue = new JLabel("0 ₫", SwingConstants.CENTER);

        statsPanel.add(createStatBox("Tổng Doanh Thu", lblTotalRevenue, COLOR_ACCENT_BLUE));
        statsPanel.add(createStatBox("Số Đơn Hàng", lblOrderCount, COLOR_ACCENT_BLUE));
        statsPanel.add(createStatBox("TB / Đơn Hàng", lblAvgOrderValue, COLOR_ACCENT_BLUE));

        sectionPanel.add(statsPanel, BorderLayout.SOUTH);
        return sectionPanel;
    }

    // --- Create Real-time Status Section (Không đổi) ---
    private JPanel createRealtimeStatusSection() {
        RoundedPanel sectionPanel = new RoundedPanel(15, COLOR_SECTION_BG);
        sectionPanel.setLayout(new BorderLayout(10, 10));
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("🕒 Tình trạng Bàn (Hiện tại)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        sectionPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.add(Box.createVerticalGlue());

        lblBanTrong = new JLabel("0", SwingConstants.CENTER);
        lblBanPhucVu = new JLabel("0", SwingConstants.CENTER);
        lblBanDatTruoc = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(createStatBox("Bàn Trống", lblBanTrong, COLOR_GREEN_STAT));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        statsPanel.add(createStatBox("Đang Phục Vụ", lblBanPhucVu, COLOR_RED_STAT));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        statsPanel.add(createStatBox("Đã Đặt Trước", lblBanDatTruoc, COLOR_YELLOW_STAT));

        statsPanel.add(Box.createVerticalGlue());
        sectionPanel.add(statsPanel, BorderLayout.CENTER);

        return sectionPanel;
    }

    // --- (SỬA) Create Performance Section (Thay thế Payment bằng Staff) ---
    private JPanel createPerformanceSection() {
        RoundedPanel sectionPanel = new RoundedPanel(15, COLOR_SECTION_BG);
        sectionPanel.setLayout(new GridLayout(1, 2, 20, 0)); // Chia 2 cột, khoảng cách 20
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        sectionPanel.add(createTopItemsPanel());    // Cột Top Món
        sectionPanel.add(createStaffChartPanel()); // <<< SỬA: Cột Top Nhân viên

        return sectionPanel;
    }

    // --- (MỚI) Helper cho Performance Section: Panel Top Nhân viên ---
    private JPanel createStaffChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("🏆 Top Nhân viên (theo thời gian đã chọn)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        panel.add(lblTitle, BorderLayout.NORTH);

        pnlStaffPerformanceChart = new JPanel(new BorderLayout()); // Panel này sẽ chứa biểu đồ
        pnlStaffPerformanceChart.setOpaque(false);
        pnlStaffPerformanceChart.setPreferredSize(new Dimension(300, 150)); // Kích thước
        panel.add(pnlStaffPerformanceChart, BorderLayout.CENTER);

        return panel;
    }

    // --- Create Top Items Panel (Không đổi) ---
    private JPanel createTopItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("⭐ Top 5 Món Bán Chạy (theo thời gian đã chọn)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        panel.add(lblTitle, BorderLayout.NORTH);

        modelTopMonAn = new DefaultListModel<>();
        listTopMonAn = new JList<>(modelTopMonAn);
        listTopMonAn.setFont(FONT_LIST_ITEM);
        listTopMonAn.setOpaque(false);
        listTopMonAn.setBackground(COLOR_SECTION_BG);

        listTopMonAn.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(COLOR_ACCENT_BLUE);
                    label.setForeground(Color.WHITE);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listTopMonAn);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // --- Create Stat Box (Không đổi) ---
    private JPanel createStatBox(String label, JLabel valueLabel, Color valueColor) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblStatLabel = new JLabel(label);
        lblStatLabel.setFont(FONT_LABEL);
        lblStatLabel.setForeground(COLOR_TEXT_LIGHT);
        lblStatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(lblStatLabel);
        box.add(Box.createRigidArea(new Dimension(0, 5)));
        box.add(valueLabel);
        return box;
    }

    // --- Set Default Date Range (Không đổi) ---
    private void setDefaultDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        dateChooserStart.setDate(Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateChooserEnd.setDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    // --- (SỬA) Load Dashboard Data (Gọi DAO Top Staff) ---
    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Map<LocalDate, Double> revenueData = null;
            private int orderCount = 0;
            private Map<String, Integer> tableStatusCounts = null;
            private Map<String, Integer> topSellingItems = null;
            private Map<String, Double> topStaffData = null; // <<< SỬA: Đổi tên
            private Exception error = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Date startDateUtil = dateChooserStart.getDate();
                    Date endDateUtil = dateChooserEnd.getDate();

                    if (startDateUtil == null || endDateUtil == null) {
                        throw new IllegalArgumentException("Ngày bắt đầu hoặc kết thúc không được để trống.");
                    }
                    if (endDateUtil.before(startDateUtil)) {
                        throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
                    }

                    LocalDate startDate = startDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = endDateUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    // --- Load dữ liệu từ các DAO ---
                    revenueData = hoaDonDAO.getDailyRevenue(startDate, endDate);
                    orderCount = hoaDonDAO.getOrderCount(startDate, endDate);
                    tableStatusCounts = banDAO.getTableStatusCounts();
                    topSellingItems = chiTietHoaDonDAO.getTopSellingItems(startDate, endDate, 5);
                    topStaffData = hoaDonDAO.getTopStaffByRevenue(startDate, endDate, 5); // <<< SỬA: Gọi hàm mới

                } catch (Exception e) {
                    this.error = e;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (error != null) {
                        // Xử lý lỗi
                        System.err.println("Lỗi khi tải dữ liệu dashboard: " + error.getMessage());
                        error.printStackTrace();
                        // Cập nhật UI báo lỗi
                        lblTotalRevenue.setText("Lỗi");
                        lblOrderCount.setText("Lỗi");
                        lblAvgOrderValue.setText("Lỗi");
                        pnlRevenueChart.removeAll();
                        pnlRevenueChart.add(new JLabel("Lỗi tải dữ liệu doanh thu.", SwingConstants.CENTER));

                        lblBanTrong.setText("Lỗi");
                        lblBanPhucVu.setText("Lỗi");
                        lblBanDatTruoc.setText("Lỗi");

                        modelTopMonAn.clear();
                        modelTopMonAn.addElement("Lỗi tải dữ liệu...");
                        pnlStaffPerformanceChart.removeAll(); // <<< SỬA
                        pnlStaffPerformanceChart.add(new JLabel("Lỗi tải dữ liệu.", SwingConstants.CENTER)); // <<< SỬA

                        if (error instanceof IllegalArgumentException) {
                            JOptionPane.showMessageDialog(DashboardGUI.this, error.getMessage(), "Lỗi nhập ngày", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(DashboardGUI.this, "Không thể tải dữ liệu dashboard. Kiểm tra kết nối CSDL.", "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                        }

                    } else {
                        // --- Cập nhật UI thành công ---

                        // 1. Cập nhật Doanh thu
                        double totalRevenue = revenueData.values().stream().mapToDouble(Double::doubleValue).sum();
                        double avgOrderValue = (orderCount > 0) ? totalRevenue / orderCount : 0;
                        lblTotalRevenue.setText(currencyFormatter.format(totalRevenue));
                        lblOrderCount.setText(numberFormatter.format(orderCount));
                        lblAvgOrderValue.setText(currencyFormatter.format(avgOrderValue));
                        updateRevenueChart(revenueData);

                        // 2. Cập nhật Tình trạng bàn
                        lblBanTrong.setText(numberFormatter.format(tableStatusCounts.getOrDefault("Trống", 0)));
                        lblBanPhucVu.setText(numberFormatter.format(tableStatusCounts.getOrDefault("Đang có khách", 0)));
                        lblBanDatTruoc.setText(numberFormatter.format(tableStatusCounts.getOrDefault("Đã đặt trước", 0)));

                        // 3. Cập nhật Top Món Bán Chạy
                        updateTopSellingItemsList(topSellingItems);

                        // 4. Cập nhật Biểu đồ Top Nhân viên
                        updateStaffPerformanceChart(topStaffData); // <<< SỬA

                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi cập nhật UI dashboard: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DashboardGUI.this, "Lỗi khi hiển thị dữ liệu dashboard.", "Lỗi giao diện", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Vẽ lại các panel đã thay đổi
                    pnlRevenueChart.revalidate();
                    pnlRevenueChart.repaint();
                    pnlStaffPerformanceChart.revalidate(); // <<< SỬA
                    pnlStaffPerformanceChart.repaint();    // <<< SỬA
                }
            }
        };
        worker.execute();
    }

    // --- Update Revenue Chart (Không đổi) ---
    private void updateRevenueChart(Map<LocalDate, Double> data) {
        pnlRevenueChart.removeAll();

        if (data == null || data.isEmpty()) {
            pnlRevenueChart.add(new JLabel("Không có dữ liệu doanh thu cho khoảng thời gian này.", SwingConstants.CENTER));
            return;
        }

        XYChart chart = new XYChartBuilder()
                .width(600).height(400)
                .title("Doanh thu theo ngày")
                .xAxisTitle("Ngày")
                .yAxisTitle("Doanh thu (₫)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDatePattern("dd/MM");
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setMarkerSize(5);
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setChartFontColor(COLOR_TEXT_DARK);
        chart.getStyler().setAxisTickLabelsColor(COLOR_TEXT_LIGHT);
        chart.getStyler().setPlotGridLinesColor(new Color(230, 230, 230));
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setXAxisTicksVisible(true);
        chart.getStyler().setXAxisDecimalPattern("#");
        chart.getStyler().setYAxisDecimalPattern("#,##0");

        java.util.List<Date> xData = data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> Date.from(entry.getKey().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());

        java.util.List<Double> yData = data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> (Double) entry.getValue())
                .collect(Collectors.toList());

        XYSeries series = chart.addSeries("Doanh thu", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        JPanel chartPanelWrapper = new XChartPanel<>(chart);
        chartPanelWrapper.setOpaque(false);

        pnlRevenueChart.add(chartPanelWrapper, BorderLayout.CENTER);
    }

    // --- Update Top Selling Items List (Không đổi) ---
    private void updateTopSellingItemsList(Map<String, Integer> data) {
        modelTopMonAn.clear();
        if (data == null || data.isEmpty()) {
            modelTopMonAn.addElement("Không có dữ liệu món bán chạy.");
            return;
        }

        int rank = 1;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            modelTopMonAn.addElement(String.format("%d. %s (%d lượt)",
                    rank++,
                    entry.getKey(),
                    entry.getValue()
            ));
        }
    }

    // --- (MỚI) Update Staff Performance Chart (Thay thế Payment Chart) ---
    private void updateStaffPerformanceChart(Map<String, Double> data) {
        pnlStaffPerformanceChart.removeAll();

        if (data == null || data.isEmpty()) {
            pnlStaffPerformanceChart.add(new JLabel("Không có dữ liệu hiệu suất nhân viên.", SwingConstants.CENTER));
            return;
        }

        // Tạo Biểu đồ Cột Ngang (Horizontal Bar Chart)
        CategoryChart chart = new CategoryChartBuilder()
                .width(400).height(200) // Kích thước
                .title("") // Không cần tiêu đề phụ
                .xAxisTitle("Doanh thu (₫)")
                .yAxisTitle("Nhân viên")
                .build();

        // --- Tùy chỉnh ---
        chart.getStyler().setLegendVisible(false); // Ẩn chú thích (vì chỉ có 1 series)
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setChartFontColor(COLOR_TEXT_DARK);
        chart.getStyler().setAxisTickLabelsColor(COLOR_TEXT_LIGHT);
        chart.getStyler().setPlotGridLinesVisible(false); // Ẩn lưới
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setXAxisDecimalPattern("#,##0"); // Format số trục X
        chart.getStyler().setAvailableSpaceFill(.6); // Độ dày của cột
//        chart.getStyler().setOrientation(Styler.Orientation.Horizontal); // <<< ĐẶT BIỂU ĐỒ NẰM NGANG
        chart.getStyler().setToolTipsEnabled(true); // Bật tooltip

        // Chuẩn bị dữ liệu (cần đảo ngược thứ tự để top 1 ở trên cùng)
        java.util.List<String> staffNames = new ArrayList<>(data.keySet());
        java.util.List<Double> revenues = new ArrayList<>(data.values());

        // Đảo ngược 2 danh sách để hiển thị Top 1 ở trên
        Collections.reverse(staffNames);
        Collections.reverse(revenues);

        // Thêm dữ liệu
        chart.addSeries("Doanh thu", staffNames, revenues);

        JPanel chartPanelWrapper = new XChartPanel<>(chart);
        chartPanelWrapper.setOpaque(false);

        pnlStaffPerformanceChart.add(chartPanelWrapper, BorderLayout.CENTER);
    }


    // --- Helper tạo icon (Không đổi) ---
    private ImageIcon createIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("Không tìm thấy icon tại: " + path);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải icon: " + path + " - " + e.getMessage());
        }
        return null;
    }

} // Kết thúc class DashboardGUI