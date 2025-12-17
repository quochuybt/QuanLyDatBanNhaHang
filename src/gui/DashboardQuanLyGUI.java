package gui;

import com.toedter.calendar.JDateChooser;
import dao.BanDAO;
import dao.ChiTietHoaDonDAO;
import dao.GiaoCaDAO;
import dao.HoaDonDAO;
import entity.LichSuGiaoCa;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardQuanLyGUI extends JPanel {

    private static final Color COLOR_LEAST_SOLD = new Color(217, 83, 79);
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

    private JDateChooser dateChooserStart;
    private JDateChooser dateChooserEnd;
    private JButton btnRefreshData;
    private JButton btnLichSuGiaoCa; // Nút mới

    private JPanel pnlRevenueChart;
    private JLabel lblTotalRevenue, lblOrderCount, lblAvgOrderValue;
    private JLabel lblBanTrong, lblBanPhucVu, lblBanDatTruoc;

    private DefaultListModel<String> modelActiveStaff;
    private JList<String> listActiveStaff;

    private JTabbedPane tabTopItems;
    private DefaultListModel<String> modelBestSellers, modelLeastSellers;
    private JList<String> listBestSellers, listLeastSellers;

    private JPanel pnlStaffHoursChart;

    private final HoaDonDAO hoaDonDAO;
    private final BanDAO banDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final GiaoCaDAO giaoCaDAO;

    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DecimalFormat numberFormatter = new DecimalFormat("#,##0");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

        gbc.insets = new Insets(0, 0, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 0.6;
        mainContentPanel.add(createRevenueSection(), gbc);

        gbc.insets = new Insets(0, 0, 15, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 0.6;
        mainContentPanel.add(createCurrentActivitySection(), gbc);

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
        styleButton(btnRefreshData, COLOR_ACCENT_BLUE);
        btnRefreshData.addActionListener(e -> loadDashboardData());
        headerPanel.add(btnRefreshData);

        btnLichSuGiaoCa = new JButton("Lịch sử giao ca");
        styleButton(btnLichSuGiaoCa, new Color(40, 167, 69)); // Màu xanh lá
        btnLichSuGiaoCa.addActionListener(e -> showLichSuGiaoCaDialog());
        headerPanel.add(btnLichSuGiaoCa);

        return headerPanel;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(FONT_BUTTON);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void showLichSuGiaoCaDialog() {
        Date sDate = dateChooserStart.getDate();
        Date eDate = dateChooserEnd.getDate();

        if (sDate == null || eDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian trước.");
            return;
        }

        LocalDate from = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        List<LichSuGiaoCa> historyList = giaoCaDAO.getLichSuGiaoCa(from, to);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Lịch sử giao ca", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Mã GC", "Mã NV", "Bắt đầu", "Kết thúc", "Tiền đầu ca", "Tiền cuối ca", "Doanh thu ca", "Chênh lệch", "Ghi chú"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        for (LichSuGiaoCa ls : historyList) {
            model.addRow(new Object[]{
                    ls.getMaGiaoCa(),
                    ls.getMaNV(),
                    ls.getThoiGianBatDau().format(dateTimeFormatter),
                    ls.getThoiGianKetThuc() != null ? ls.getThoiGianKetThuc().format(dateTimeFormatter) : "Chưa kết thúc",
                    currencyFormatter.format(ls.getTienDauCa()),
                    currencyFormatter.format(ls.getTienCuoiCa()),
                    currencyFormatter.format(ls.getTienHeThongTinh()),
                    currencyFormatter.format(ls.getChenhLech()),
                    ls.getGhiChu()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        for(int i=4; i<=7; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    try {
                        String s = value.toString().replaceAll("[^\\d-]", "");
                        double val = Double.parseDouble(s);
                        if (val < 0) c.setForeground(Color.RED);
                        else if (val > 0) c.setForeground(new Color(0, 150, 0));
                        else c.setForeground(Color.BLACK);
                    } catch (Exception e) { c.setForeground(Color.BLACK); }
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50); // Mã GC
        table.getColumnModel().getColumn(2).setPreferredWidth(130); // Bắt đầu
        table.getColumnModel().getColumn(3).setPreferredWidth(130); // Kết thúc
        table.getColumnModel().getColumn(8).setPreferredWidth(200); // Ghi chú

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBorder(new EmptyBorder(10,10,10,10));
        JLabel lblInfo = new JLabel("Lịch sử từ " + from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " đến " + to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblInfo.setFont(FONT_TITLE);
        header.add(lblInfo);
        dialog.add(header, BorderLayout.NORTH);

        dialog.setVisible(true);
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

        JPanel tableStatusPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        tableStatusPanel.setOpaque(false);

        lblBanTrong = new JLabel("0", SwingConstants.CENTER);
        lblBanPhucVu = new JLabel("0", SwingConstants.CENTER);
        lblBanDatTruoc = new JLabel("0", SwingConstants.CENTER);

        tableStatusPanel.add(createStatBox("Trống", lblBanTrong, COLOR_GREEN_STAT));
        tableStatusPanel.add(createStatBox("Phục Vụ", lblBanPhucVu, COLOR_RED_STAT));
        tableStatusPanel.add(createStatBox("Đã Đặt", lblBanDatTruoc, COLOR_YELLOW_STAT));
        contentPanel.add(tableStatusPanel);

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
        listBestSellers = createStyledList(modelBestSellers, COLOR_ACCENT_BLUE);
        tabTopItems.addTab("Bán chạy nhất", new JScrollPane(listBestSellers));

        modelLeastSellers = new DefaultListModel<>();
        listLeastSellers = createStyledList(modelLeastSellers, COLOR_LEAST_SOLD);
        tabTopItems.addTab("Ít được gọi nhất", new JScrollPane(listLeastSellers));

        panel.add(tabTopItems, BorderLayout.CENTER);
        return panel;
    }

    private JList<String> createStyledList(DefaultListModel<String> model, Color barColor) {
        JList<String> list = new JList<>(model);
        list.setCellRenderer(new ItemRenderer(barColor));
        list.setBackground(COLOR_SECTION_BG);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return list;
    }

    private JPanel createStaffChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Top Nhân viên chăm chỉ");
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
            private Map<String, Double> topStaffHours;
            private List<String> activeStaffList;
            private Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    Date sDate = dateChooserStart.getDate();
                    Date eDate = dateChooserEnd.getDate();
                    if (sDate == null || eDate == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");

                    LocalDate startDate = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    revenueData = hoaDonDAO.getDailyRevenue(startDate, endDate);
                    orderCount = hoaDonDAO.getOrderCount(startDate, endDate);

                    LocalDate today = LocalDate.now();
                    LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                    LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                    topStaffHours = giaoCaDAO.getTopStaffByWorkHours(startOfWeek, endOfWeek, 5);
                    tableStatusCounts = banDAO.getTableStatusCounts();
                    activeStaffList = giaoCaDAO.getNhanVienDangLamViecChiTiet();

                    LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
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
                updateStaffChart(topStaffHours);
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
        lblBanTrong.setText(numberFormatter.format(tables.getOrDefault("Trống", 0)));
        lblBanPhucVu.setText(numberFormatter.format(tables.getOrDefault("Đang có khách", 0)));
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
        if (best.isEmpty()) {
            modelBestSellers.addElement("Chưa có dữ liệu tuần này.");
            ((ItemRenderer) listBestSellers.getCellRenderer()).setMaxCount(1);
        } else {
            int maxBest = best.values().stream().max(Integer::compare).orElse(1);
            ((ItemRenderer) listBestSellers.getCellRenderer()).setMaxCount(maxBest);
            int rank = 1;
            for (Map.Entry<String, Integer> e : best.entrySet()) {
                modelBestSellers.addElement(String.format("#%d  %s (%d)", rank++, e.getKey(), e.getValue()));
            }
        }
        listBestSellers.repaint();

        modelLeastSellers.clear();
        if (least.isEmpty()) {
            modelLeastSellers.addElement("Chưa có dữ liệu tuần này.");
            ((ItemRenderer) listLeastSellers.getCellRenderer()).setMaxCount(1);
        } else {
            int maxLeast = least.values().stream().max(Integer::compare).orElse(1);
            ((ItemRenderer) listLeastSellers.getCellRenderer()).setMaxCount(maxLeast);

            int rank = 1;
            for (Map.Entry<String, Integer> e : least.entrySet()) {
                modelLeastSellers.addElement(String.format("#%d  %s (%d)", rank++, e.getKey(), e.getValue()));
            }
        }
        listLeastSellers.repaint();
    }

    private void updateRevenueChart(Map<LocalDate, Double> data) {
        pnlRevenueChart.removeAll();
        if (data == null || data.isEmpty()) {
            pnlRevenueChart.add(new JLabel("Không có dữ liệu.", SwingConstants.CENTER));
            pnlRevenueChart.revalidate();
            pnlRevenueChart.repaint();
            return;
        }

        XYChart chart = new XYChartBuilder()
                .width(600)
                .height(400)
                .title("Doanh thu")
                .xAxisTitle("Ngày")
                .yAxisTitle("VNĐ")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDatePattern("dd/MM");
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setYAxisDecimalPattern("#,###");

        List<Date> xData = data.keySet().stream().sorted()
                .map(d -> Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());
        List<Double> yData = data.keySet().stream().sorted()
                .map(data::get)
                .collect(Collectors.toList());

        XYSeries series = chart.addSeries("Doanh thu", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);

        pnlRevenueChart.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        pnlRevenueChart.revalidate();
        pnlRevenueChart.repaint();
    }

    private void updateStaffChart(Map<String, Double> data) {
        pnlStaffHoursChart.removeAll();
        if (data == null || data.isEmpty()) {
            pnlStaffHoursChart.add(new JLabel("Chưa có dữ liệu chấm công tuần này.", SwingConstants.CENTER));
            pnlStaffHoursChart.revalidate();
            pnlStaffHoursChart.repaint();
            return;
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(400)
                .height(200)
                .title("Tuần này (" + LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ")")
                .xAxisTitle("Nhân viên")
                .yAxisTitle("Số giờ làm (h)")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setChartBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotBackgroundColor(COLOR_SECTION_BG);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setSeriesColors(new Color[]{COLOR_ACCENT_BLUE});
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setYAxisDecimalPattern("#0.0");

        List<String> names = new ArrayList<>(data.keySet());
        List<Double> values = new ArrayList<>(data.values());

        chart.addSeries("Giờ làm", names, values);
        pnlStaffHoursChart.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        pnlStaffHoursChart.revalidate();
        pnlStaffHoursChart.repaint();
    }

    private class ItemRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel lblRank;
        private JLabel lblName;
        private JLabel lblCount;
        private JProgressBar progressBar;
        private Color barColor;
        private int maxCount = 1;

        public ItemRenderer(Color barColor) {
            this.barColor = barColor;
            setLayout(new BorderLayout(10, 5));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            lblRank = new JLabel();
            lblRank.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblRank.setHorizontalAlignment(SwingConstants.CENTER);
            lblRank.setPreferredSize(new Dimension(30, 30));
            lblRank.setOpaque(true);

            JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            centerPanel.setOpaque(false);

            lblName = new JLabel();
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblName.setForeground(COLOR_TEXT_DARK);

            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(100, 4));
            progressBar.setBorderPainted(false);
            progressBar.setBackground(new Color(230, 230, 230));
            progressBar.setForeground(this.barColor);

            centerPanel.add(lblName);
            centerPanel.add(progressBar);

            lblCount = new JLabel();
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblCount.setForeground(COLOR_TEXT_LIGHT);

            add(lblRank, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
            add(lblCount, BorderLayout.EAST);
        }

        public void setMaxCount(int max) {
            this.maxCount = Math.max(1, max); // Tránh chia cho 0
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(new Color(220, 235, 255));
            } else {
                setBackground(index % 2 == 0 ? new Color(250, 250, 252) : Color.WHITE);
            }

            if (value != null && value.startsWith("#")) {
                try {
                    int firstSpace = value.indexOf("  ");
                    String rankStr = value.substring(1, firstSpace);
                    int rank = Integer.parseInt(rankStr);

                    int lastOpenParen = value.lastIndexOf("(");
                    int lastCloseParen = value.lastIndexOf(")");
                    String countStr = value.substring(lastOpenParen + 1, lastCloseParen);
                    int count = Integer.parseInt(countStr);
                    String name = value.substring(firstSpace + 2, lastOpenParen).trim();

                    lblRank.setText(rankStr);
                    lblName.setText(name);
                    lblCount.setText(count + " suất");

                    lblRank.setForeground(Color.WHITE);
                    if (rank == 1) lblRank.setBackground(new Color(255, 193, 7));
                    else if (rank == 2) lblRank.setBackground(new Color(192, 192, 192));
                    else if (rank == 3) lblRank.setBackground(new Color(205, 127, 50));
                    else {
                        lblRank.setBackground(new Color(230, 230, 230));
                        lblRank.setForeground(COLOR_TEXT_DARK);
                    }

                    progressBar.setVisible(true);
                    progressBar.setValue((int) ((double) count / maxCount * 100));

                } catch (Exception e) {
                    lblRank.setText("-");
                    lblRank.setBackground(Color.LIGHT_GRAY);
                    lblName.setText(value);
                    lblCount.setText("");
                    progressBar.setVisible(false);
                }
            } else {
                lblRank.setText("");
                lblRank.setBackground(new Color(0,0,0,0));
                lblName.setText(value);
                lblCount.setText("");
                progressBar.setVisible(false);
            }

            return this;
        }
    }
}