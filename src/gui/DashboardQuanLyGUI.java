package gui;

import com.toedter.calendar.JDateChooser;
import dao.BanDAO;
import dao.ChiTietHoaDonDAO;
import dao.GiaoCaDAO;
import dao.HoaDonDAO;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardQuanLyGUI extends JPanel {

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

    private JPanel pnlRevenueChart;
    private JLabel lblTotalRevenue, lblOrderCount, lblAvgOrderValue;

    // [CẬP NHẬT] Thêm label cho bàn đặt trước
    private JLabel lblBanTrong, lblBanPhucVu, lblBanDatTruoc;

    private DefaultListModel<String> modelActiveStaff;
    private JList<String> listActiveStaff;

    private JTabbedPane tabTopItems;
    private DefaultListModel<String> modelBestSellers, modelLeastSellers;
    private JList<String> listBestSellers, listLeastSellers;

    // [CẬP NHẬT] Đổi tên panel
    private JPanel pnlStaffHoursChart;

    // --- DAOs ---
    private final HoaDonDAO hoaDonDAO;
    private final BanDAO banDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final GiaoCaDAO giaoCaDAO;

    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DecimalFormat numberFormatter = new DecimalFormat("#,##0");

    public DashboardQuanLyGUI() {
        this.hoaDonDAO = new HoaDonDAO();
        this.banDAO = new BanDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.giaoCaDAO = new GiaoCaDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Doanh thu
        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 0.6;
        mainContentPanel.add(createRevenueSection(), gbc);

        // 2. Hoạt động hiện tại (Real-time)
        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 0.6;
        mainContentPanel.add(createCurrentActivitySection(), gbc);

        // 3. Hiệu suất (Món ăn & Nhân viên chăm chỉ)
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        mainContentPanel.add(createPerformanceSection(), gbc);

        add(mainContentPanel, BorderLayout.CENTER);

        setDefaultDateRange();
        loadDashboardData();
    }

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
        btnRefreshData.addActionListener(e -> loadDashboardData());
        headerPanel.add(btnRefreshData);

        return headerPanel;
    }

    private JPanel createRevenueSection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(10, 10));
        sectionPanel.setBackground(COLOR_SECTION_BG);
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Thống kê Doanh thu");
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
        statsPanel.add(createStatBox("Số Đơn", lblOrderCount, COLOR_ACCENT_BLUE));
        statsPanel.add(createStatBox("Trung bình / Đơn", lblAvgOrderValue, COLOR_ACCENT_BLUE));

        sectionPanel.add(statsPanel, BorderLayout.SOUTH);
        return sectionPanel;
    }

    private JPanel createCurrentActivitySection() {
        JPanel sectionPanel = new JPanel(new BorderLayout(10, 10));
        sectionPanel.setBackground(COLOR_SECTION_BG);
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Hoạt động hiện tại (Real-time)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        sectionPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        contentPanel.setOpaque(false);

        // [CẬP NHẬT] 1. Trạng thái bàn (Thêm Bàn Đã Đặt)
        JPanel tableStatusPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 3 cột
        tableStatusPanel.setOpaque(false);

        lblBanTrong = new JLabel("0", SwingConstants.CENTER);
        lblBanPhucVu = new JLabel("0", SwingConstants.CENTER);
        lblBanDatTruoc = new JLabel("0", SwingConstants.CENTER);

        tableStatusPanel.add(createStatBox("Trống", lblBanTrong, COLOR_GREEN_STAT));
        tableStatusPanel.add(createStatBox("Phục Vụ", lblBanPhucVu, COLOR_RED_STAT));
        tableStatusPanel.add(createStatBox("Đã Đặt", lblBanDatTruoc, COLOR_YELLOW_STAT));
        contentPanel.add(tableStatusPanel);

        // [CẬP NHẬT] 2. Danh sách nhân viên (Hiển thị chi tiết giờ)
        JPanel staffPanel = new JPanel(new BorderLayout(0, 5));
        staffPanel.setOpaque(false);
        JLabel lblStaffTitle = new JLabel("Nhân viên đang trong ca:");
        lblStaffTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStaffTitle.setForeground(COLOR_TEXT_LIGHT);

        modelActiveStaff = new DefaultListModel<>();
        listActiveStaff = new JList<>(modelActiveStaff);
        listActiveStaff.setFont(FONT_LIST_ITEM);
        listActiveStaff.setOpaque(false);
        listActiveStaff.setBackground(new Color(245, 247, 250));
        listActiveStaff.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollStaff = new JScrollPane(listActiveStaff);
        scrollStaff.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        staffPanel.add(lblStaffTitle, BorderLayout.NORTH);
        staffPanel.add(scrollStaff, BorderLayout.CENTER);
        contentPanel.add(staffPanel);

        sectionPanel.add(contentPanel, BorderLayout.CENTER);
        return sectionPanel;
    }

    private JPanel createPerformanceSection() {
        JPanel sectionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        sectionPanel.setBackground(COLOR_SECTION_BG);
        sectionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        sectionPanel.add(createTopItemsTabbedPane());
        sectionPanel.add(createStaffChartPanel()); // Chart nhân viên chăm chỉ

        return sectionPanel;
    }

    private JPanel createTopItemsTabbedPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Xu hướng món ăn (7 ngày qua)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        panel.add(lblTitle, BorderLayout.NORTH);

        tabTopItems = new JTabbedPane();
        tabTopItems.setFont(new Font("Segoe UI", Font.BOLD, 12));

        modelBestSellers = new DefaultListModel<>();
        listBestSellers = createStyledList(modelBestSellers);
        tabTopItems.addTab("Bán chạy nhất", new JScrollPane(listBestSellers));

        modelLeastSellers = new DefaultListModel<>();
        listLeastSellers = createStyledList(modelLeastSellers);
        tabTopItems.addTab("Ít được gọi nhất", new JScrollPane(listLeastSellers));

        panel.add(tabTopItems, BorderLayout.CENTER);
        return panel;
    }

    private JList<String> createStyledList(DefaultListModel<String> model) {
        JList<String> list = new JList<>(model);
        list.setFont(FONT_LIST_ITEM);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                if (index % 2 == 0) label.setBackground(new Color(250, 250, 250));
                else label.setBackground(Color.WHITE);
                return label;
            }
        });
        return list;
    }

    // [CẬP NHẬT] Đổi tiêu đề cho đúng ý nghĩa mới
    private JPanel createStaffChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Top Nhân viên chăm chỉ (Theo giờ làm)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_DARK);
        panel.add(lblTitle, BorderLayout.NORTH);

        pnlStaffHoursChart = new JPanel(new BorderLayout());
        pnlStaffHoursChart.setOpaque(false);
        pnlStaffHoursChart.setPreferredSize(new Dimension(300, 150));
        panel.add(pnlStaffHoursChart, BorderLayout.CENTER);

        return panel;
    }

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

    private void setDefaultDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate startOfRange = today.minusMonths(1);
        dateChooserStart.setDate(Date.from(startOfRange.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateChooserEnd.setDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private Map<LocalDate, Double> revenueData;
            private int orderCount;
            private Map<String, Integer> tableStatusCounts;
            private Map<String, Integer> topSellingItems;
            private Map<String, Integer> leastSellingItems;
            private Map<String, Double> topStaffHours; // [CẬP NHẬT] Data giờ làm
            private List<String> activeStaffList;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    // 1. Dữ liệu theo bộ lọc ngày (Doanh thu & Top Nhân viên chăm chỉ)
                    Date sDate = dateChooserStart.getDate();
                    Date eDate = dateChooserEnd.getDate();
                    if (sDate == null || eDate == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");

                    LocalDate startDate = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    revenueData = hoaDonDAO.getDailyRevenue(startDate, endDate);
                    orderCount = hoaDonDAO.getOrderCount(startDate, endDate);
                    // [CẬP NHẬT] Gọi hàm lấy Top Giờ Làm
                    topStaffHours = giaoCaDAO.getTopStaffByWorkHours(startDate, endDate, 5);

                    // 2. Dữ liệu Real-time
                    tableStatusCounts = banDAO.getTableStatusCounts();
                    // [CẬP NHẬT] Gọi hàm lấy nhân viên chi tiết (kèm giờ)
                    activeStaffList = giaoCaDAO.getNhanVienDangLamViecChiTiet();

                    // 3. Dữ liệu Top món (7 ngày gần nhất)
                    LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
                    LocalDate today = LocalDate.now();
                    topSellingItems = chiTietHoaDonDAO.getTopSellingItems(sevenDaysAgo, today, 5);
                    leastSellingItems = chiTietHoaDonDAO.getLeastSellingItems(sevenDaysAgo, today, 5);

                } catch (Exception e) {
                    this.error = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (error != null) {
                    error.printStackTrace();
                    JOptionPane.showMessageDialog(DashboardQuanLyGUI.this, "Lỗi tải dữ liệu: " + error.getMessage());
                    return;
                }

                updateRevenueUI(revenueData, orderCount);
                updateRealtimeUI(tableStatusCounts, activeStaffList);
                updateTopItemsUI(topSellingItems, leastSellingItems);
                updateStaffChart(topStaffHours); // Cập nhật biểu đồ nhân viên
            }
        };
        worker.execute();
    }

    private void updateRevenueUI(Map<LocalDate, Double> data, int count) {
        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        double avg = count > 0 ? total / count : 0;

        lblTotalRevenue.setText(currencyFormatter.format(total));
        lblOrderCount.setText(numberFormatter.format(count));
        lblAvgOrderValue.setText(currencyFormatter.format(avg));

        updateRevenueChart(data);
    }

    private void updateRealtimeUI(Map<String, Integer> tables, List<String> staff) {
        // [CẬP NHẬT] Map đúng các key trạng thái từ DB
        lblBanTrong.setText(numberFormatter.format(tables.getOrDefault("Trống", 0)));
        lblBanPhucVu.setText(numberFormatter.format(tables.getOrDefault("Đang có khách", 0)));
        // Lưu ý: Key DB của bạn có thể là "Đã đặt trước" hoặc "Đã đặt", cần kiểm tra BanDAO
        int reserved = tables.getOrDefault("Đã đặt trước", 0) + tables.getOrDefault("Đã đặt", 0);
        lblBanDatTruoc.setText(numberFormatter.format(reserved));

        modelActiveStaff.clear();
        if (staff.isEmpty()) {
            modelActiveStaff.addElement("(Không có nhân viên nào đang trực)");
        } else {
            for (String s : staff) modelActiveStaff.addElement("● " + s);
        }
    }

    private void updateTopItemsUI(Map<String, Integer> best, Map<String, Integer> least) {
        modelBestSellers.clear();
        if (best.isEmpty()) modelBestSellers.addElement("Chưa có dữ liệu tuần này.");
        else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : best.entrySet()) {
                modelBestSellers.addElement(String.format("#%d  %s (%d)", rank++, e.getKey(), e.getValue()));
            }
        }

        modelLeastSellers.clear();
        if (least.isEmpty()) modelLeastSellers.addElement("Chưa có dữ liệu tuần này.");
        else {
            int rank = 1;
            for (Map.Entry<String, Integer> e : least.entrySet()) {
                modelLeastSellers.addElement(String.format("#%d  %s (%d)", rank++, e.getKey(), e.getValue()));
            }
        }
    }

    private void updateRevenueChart(Map<LocalDate, Double> data) {
        pnlRevenueChart.removeAll();
        if (data == null || data.isEmpty()) {
            pnlRevenueChart.add(new JLabel("Không có dữ liệu.", SwingConstants.CENTER));
            pnlRevenueChart.revalidate();
            pnlRevenueChart.repaint();
            return;
        }

        XYChart chart = new XYChartBuilder().width(600).height(400).title("Doanh thu").xAxisTitle("Ngày").yAxisTitle("VNĐ").build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDatePattern("dd/MM");
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setToolTipsEnabled(true);

        List<Date> xData = data.keySet().stream().sorted().map(d -> Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())).collect(Collectors.toList());
        List<Double> yData = data.keySet().stream().sorted().map(data::get).collect(Collectors.toList());

        XYSeries series = chart.addSeries("Doanh thu", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        pnlRevenueChart.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        pnlRevenueChart.revalidate();
        pnlRevenueChart.repaint();
    }

    // [CẬP NHẬT] Biểu đồ Top Nhân viên theo Giờ làm
    private void updateStaffChart(Map<String, Double> data) {
        pnlStaffHoursChart.removeAll();
        if (data == null || data.isEmpty()) {
            pnlStaffHoursChart.add(new JLabel("Không có dữ liệu.", SwingConstants.CENTER));
            pnlStaffHoursChart.revalidate();
            return;
        }

        CategoryChart chart = new CategoryChartBuilder().width(400).height(200).title("").xAxisTitle("Giờ làm").yAxisTitle("Nhân viên").build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setXAxisDecimalPattern("#.0"); // Format 1 số lẻ

        List<String> names = new ArrayList<>(data.keySet());
        List<Double> values = new ArrayList<>(data.values());
        Collections.reverse(names);
        Collections.reverse(values);

        chart.addSeries("Giờ làm", names, values);
        pnlStaffHoursChart.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        pnlStaffHoursChart.revalidate();
    }
}