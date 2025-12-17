package gui;

import com.toedter.calendar.JDateChooser;
import dao.*;
import entity.Ban;
import entity.ChiTietHoaDon;
import entity.DonDatMon;
import entity.HoaDon;
import util.ExcelExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HoaDonGUI extends JPanel {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final MonAnDAO monAnDAO;
    private final NhanVienDAO nhanVienDAO;
    private final DonDatMonDAO donDatMonDAO;
    private final BanDAO banDAO;

    private final JTable tableHoaDon;
    private final DefaultTableModel tableModel;
    private final JTabbedPane tabbedPane;
    private JTextField txtTimKiem;
    private List<HoaDon> dsHoaDonDisplayed;
    private DocumentListener searchListener;
    private Timer searchTimer;

    private JDateChooser dateChooserTuNgay;
    private JDateChooser dateChooserDenNgay;
    private JButton btnLocNgay;
    private JButton btnHomNay;
    private JButton btnXoaLoc;

    private static int printSessionCounter = 0;

    private static final Color COLOR_BG_LIGHT = new Color(244, 247, 252);
    private final String[] columnNames = {"Thời gian thanh toán", "Mã tham chiếu", "Nhân viên", "Ghi chú", "Thanh toán", "Tổng tiền"};
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DateTimeFormatter tableDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter billDateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private static final int ITEMS_PER_PAGE = 15;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentKeyword = "";

    private JPanel paginationPanel;
    private JLabel lblPageInfo;
    private JButton btnFirst, btnPrev, btnNext, btnLast;

    public HoaDonGUI() {
        this.hoaDonDAO = new HoaDonDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.monAnDAO = new MonAnDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.donDatMonDAO = new DonDatMonDAO();
        this.banDAO = new BanDAO();
        this.dsHoaDonDisplayed = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableHoaDon = new JTable(tableModel);
        setupTableAppearance(tableHoaDon);
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        JPanel mainTablePanel = createMainTablePanel(scrollPane);

        tabbedPane = createFilterTabs();
        tabbedPane.addChangeListener(e -> loadDataForSelectedTab());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(tabbedPane, BorderLayout.NORTH);
        centerPanel.add(mainTablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        paginationPanel = createPaginationPanel();
        add(paginationPanel, BorderLayout.SOUTH);

        addTableClickListener();

        SwingUtilities.invokeLater(this::loadFirstPage);
    }

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        btnFirst = new JButton("<< Đầu");
        stylePaginationButton(btnFirst);
        btnFirst.addActionListener(e -> navigateToPage(1));
        panel.add(btnFirst);

        btnPrev = new JButton("< Trước");
        stylePaginationButton(btnPrev);
        btnPrev.addActionListener(e -> navigateToPage(currentPage - 1));
        panel.add(btnPrev);

        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPageInfo);

        btnNext = new JButton("Sau >");
        stylePaginationButton(btnNext);
        btnNext.addActionListener(e -> navigateToPage(currentPage + 1));
        panel.add(btnNext);

        btnLast = new JButton("Cuối >>");
        stylePaginationButton(btnLast);
        btnLast.addActionListener(e -> navigateToPage(totalPages));
        panel.add(btnLast);

        updatePaginationControls();
        return panel;
    }

    private void stylePaginationButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(56, 118, 243));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updatePaginationControls() {
        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages);

        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);

        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);

        if (totalPages <= 1) {
            btnFirst.setEnabled(false);
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            btnLast.setEnabled(false);
        }
    }

    private String getSelectedTrangThaiFilter() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 1: return "Đã thanh toán";
            case 2: return "Chưa thanh toán";
            case 0:
            default: return "Tất cả";
        }
    }

    private LocalDateTime[] getFilterDates() {
        LocalDateTime start = null;
        LocalDateTime end = null;

        Date dateFrom = dateChooserTuNgay.getDate();
        Date dateTo = dateChooserDenNgay.getDate();

        if (dateFrom != null && dateTo != null) {
            start = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().atStartOfDay();
            end = dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);
        } else if (dateFrom != null) {
            start = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().atStartOfDay();
            end = start.plusDays(1).minusNanos(1);
        } else if (dateTo != null) {
            end = dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);
            start = LocalDateTime.MIN;
        } else {
            return new LocalDateTime[]{null, null};
        }

        if (start != LocalDateTime.MIN && start != null && end != null && start.isAfter(end)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.", "Lỗi lọc ngày", JOptionPane.WARNING_MESSAGE);
            return new LocalDateTime[]{null, null};
        }

        return new LocalDateTime[]{start, end};
    }

    private void loadFirstPage() {
        currentPage = 1;
        currentKeyword = "";
        loadDataForCurrentPage();
    }

    private void navigateToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        currentPage = page;
        loadDataForCurrentPage();
    }

    private void loadDataForCurrentPage() {
        String trangThai = getSelectedTrangThaiFilter();
        LocalDateTime[] dates = getFilterDates();
        LocalDateTime tuNgay = dates != null ? dates[0] : null;
        LocalDateTime denNgay = dates != null ? dates[1] : null;

        int totalCount = hoaDonDAO.getTotalHoaDonCount(trangThai, currentKeyword, tuNgay, denNgay);
        totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);

        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        List<HoaDon> list = hoaDonDAO.getHoaDonByPage(currentPage, trangThai, currentKeyword, tuNgay, denNgay);

        loadDataToTable(list);
        updatePaginationControls();
    }

    private void loadDataForSelectedTab() {
        resetSearchFieldIfNeeded();
        loadFirstPage();
    }

    private void loadDataToTable(List<HoaDon> list) {
        SwingUtilities.invokeLater(() -> {
            if (list == null) {
                dsHoaDonDisplayed = new ArrayList<>();
            } else {
                dsHoaDonDisplayed = list;
            }

            tableModel.setRowCount(0);

            for (HoaDon hd : dsHoaDonDisplayed) {
                if (hd == null) continue;

                String maThamChieu = hd.getMaHD() != null ? hd.getMaHD() : "N/A";
                String tenNV_Thuc = nhanVienDAO.getTenNhanVienByMa(hd.getMaNV());

                String ghiChu = "";
                if (hd.getMaDon() != null) {
                    DonDatMon ddm = donDatMonDAO.getDonDatMonByMa(hd.getMaDon());

                    if (ddm != null && ddm.getGhiChu() != null) {
                        ghiChu = ddm.getGhiChu().trim();

                        if (ghiChu.contains("LINKED:")) {
                            String cleanNote = ghiChu.substring(0, ghiChu.indexOf("LINKED:")).trim();
                            if (cleanNote.isEmpty()) {
                                ghiChu = "Gộp bàn";
                            } else {
                                ghiChu = cleanNote + " (Gộp)";
                            }
                        } else if (ghiChu.isEmpty()) {
                            ghiChu = "Không";
                        }
                    }
                }

                if (ghiChu == null || ghiChu.isEmpty()) {
                    ghiChu = "Không";
                } else if (ghiChu.equalsIgnoreCase("Không")) {
                    ghiChu = "Không";
                }

                try {
                    tableModel.addRow(new Object[]{
                            (hd.getNgayLap() != null ? hd.getNgayLap().format(tableDateFormatter) : "N/A"),
                            maThamChieu,
                            tenNV_Thuc,
                            ghiChu,
                            hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "N/A",
                            currencyFormatter.format(hd.getTongThanhToan())
                    });
                } catch (Exception e) {
                }
            }
        });
    }

    private void searchHoaDonRealTime() {
        final String currentText = txtTimKiem.getText();
        final String placeholder = " Tìm kiếm qua mã hóa đơn";

        if (currentText == null) return;

        String query = currentText.trim();

        if (query.isEmpty() || query.equalsIgnoreCase(placeholder.trim())) {
            currentKeyword = "";
        } else {
            currentKeyword = query;
        }

        currentPage = 1;
        loadDataForCurrentPage();
    }

    private void resetSearchFieldIfNeeded() {
        final String placeholder = " Tìm kiếm qua mã hóa đơn";
        if (!txtTimKiem.getText().equals(placeholder)) {
            SwingUtilities.invokeLater(() -> {
                txtTimKiem.getDocument().removeDocumentListener(searchListener);
                txtTimKiem.setForeground(Color.GRAY);
                txtTimKiem.setText(placeholder);
                txtTimKiem.getDocument().addDocumentListener(searchListener);
            });
        }
    }

    private JPanel createMainTablePanel(JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel topFilterPanel = new JPanel(new BorderLayout(10, 0));
        topFilterPanel.setOpaque(false);
        topFilterPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        txtTimKiem = new JTextField(" Tìm kiếm qua mã hóa đơn");
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY);
        txtTimKiem.setPreferredSize(new Dimension(250, 35));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        addPlaceholderFocusHandler(txtTimKiem, " Tìm kiếm qua mã hóa đơn");
        setupRealTimeSearch();

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        JPanel inputWrapper = new JPanel(new BorderLayout(5, 0));
        inputWrapper.setOpaque(false);
        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(txtTimKiem, BorderLayout.CENTER);
        searchPanel.add(inputWrapper, BorderLayout.CENTER);

        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dateFilterPanel.setOpaque(false);

        dateChooserTuNgay = new JDateChooser();
        dateChooserTuNgay.setDateFormatString("dd/MM/yyyy");
        dateChooserTuNgay.setPreferredSize(new Dimension(130, 35));

        dateChooserDenNgay = new JDateChooser();
        dateChooserDenNgay.setDateFormatString("dd/MM/yyyy");
        dateChooserDenNgay.setPreferredSize(new Dimension(130, 35));

        btnLocNgay = new JButton("Lọc");
        btnLocNgay.setFont(new Font("Arial", Font.BOLD, 14));
        btnLocNgay.setPreferredSize(new Dimension(80, 35));
        btnLocNgay.setBackground(new Color(50, 150, 200));
        btnLocNgay.setForeground(Color.WHITE);

        btnLocNgay.addActionListener(e -> {
            btnLocNgay.requestFocusInWindow();
            currentPage = 1;
            loadDataForCurrentPage();
        });

        btnHomNay = new JButton("Hôm nay");
        btnHomNay.setFont(new Font("Arial", Font.BOLD, 14));
        btnHomNay.setPreferredSize(new Dimension(100, 35));
        btnHomNay.setBackground(new Color(255, 165, 0));
        btnHomNay.setForeground(Color.WHITE);
        btnHomNay.addActionListener(e -> {
            Date today = new Date();
            dateChooserTuNgay.setDate(today);
            dateChooserDenNgay.setDate(today);
            currentPage = 1;
            loadDataForCurrentPage();
        });

        btnXoaLoc = new JButton("Xóa lọc");
        btnXoaLoc.setFont(new Font("Arial", Font.PLAIN, 14));
        btnXoaLoc.setPreferredSize(new Dimension(80, 35));
        btnXoaLoc.setForeground(Color.WHITE);
        btnXoaLoc.addActionListener(e -> {
            dateChooserTuNgay.setDate(null);
            dateChooserDenNgay.setDate(null);
            currentPage = 1;
            loadDataForCurrentPage();
        });

        dateFilterPanel.add(new JLabel("Từ ngày:"));
        dateFilterPanel.add(dateChooserTuNgay);
        dateFilterPanel.add(new JLabel("Đến ngày:"));
        dateFilterPanel.add(dateChooserDenNgay);
        dateFilterPanel.add(btnLocNgay);
        dateFilterPanel.add(btnHomNay);
        dateFilterPanel.add(btnXoaLoc);

        topFilterPanel.add(searchPanel, BorderLayout.WEST);
        topFilterPanel.add(dateFilterPanel, BorderLayout.EAST);

        panel.add(topFilterPanel, BorderLayout.NORTH);

        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Hóa đơn");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.WEST);

        JButton btnExport = new JButton("Xuất hóa đơn");
        styleExportButton(btnExport);
        btnExport.addActionListener(e -> exportDataToExcel());
        panel.add(btnExport, BorderLayout.EAST);

        return panel;
    }

    private void styleExportButton(JButton btnExport) {
        ImageIcon originalIcon = null;
        try {
            java.net.URL iconURL = getClass().getResource("/img/icon/excel.png");
            if (iconURL != null) {
                originalIcon = new ImageIcon(iconURL);
            }
        } catch (Exception e) {
            originalIcon = null;
        }

        if (originalIcon != null) {
            Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            btnExport.setIcon(new ImageIcon(scaledImage));
            btnExport.setHorizontalTextPosition(SwingConstants.RIGHT);
            btnExport.setIconTextGap(8);
        } else {
            btnExport.setText("Xuất Excel");
        }

        btnExport.setBackground(new Color(0, 150, 60));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 80), 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        btnExport.setContentAreaFilled(true);
    }

    private JTabbedPane createFilterTabs() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabPane.setBackground(Color.WHITE);
        tabPane.addTab("Tất cả hóa đơn", null);
        tabPane.addTab("Đã thanh toán", null);
        tabPane.addTab("Chưa thanh toán", null);
        return tabPane;
    }

    private void addPlaceholderFocusHandler(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    private void setupRealTimeSearch() {
        searchTimer = new Timer(300, e -> performSearch());
        searchTimer.setRepeats(false);

        searchListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        };
        txtTimKiem.getDocument().addDocumentListener(searchListener);
    }

    private void performSearch() {
        SwingUtilities.invokeLater(this::searchHoaDonRealTime);
    }

    private void setupTableAppearance(JTable table) {
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(150);
        tcm.getColumn(1).setPreferredWidth(100);
        tcm.getColumn(2).setPreferredWidth(100);
        tcm.getColumn(3).setPreferredWidth(200);
        tcm.getColumn(4).setPreferredWidth(100);
        tcm.getColumn(5).setPreferredWidth(100);
    }

    private void addTableClickListener() {
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = tableHoaDon.getSelectedRow();
                    if (selectedRow == -1) return;

                    if (dsHoaDonDisplayed == null || selectedRow >= dsHoaDonDisplayed.size()) {
                        return;
                    }

                    HoaDon selectedHoaDon = dsHoaDonDisplayed.get(selectedRow);
                    if (selectedHoaDon == null) {
                        return;
                    }

                    String maDon = selectedHoaDon.getMaDon();
                    if (maDon == null || maDon.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(HoaDonGUI.this,
                                "Hóa đơn [" + selectedHoaDon.getMaHD() + "] không có Mã Đơn Đặt liên kết.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    List<ChiTietHoaDon> chiTietList = chiTietHoaDonDAO.getChiTietTheoMaDon(maDon);
                    showChiTietDialog(selectedHoaDon, chiTietList);
                }
            }
        });
    }

    private String getTenBanVaKhuVuc(String maDon) {
        String maBan = donDatMonDAO.getMaBanByMaDon(maDon);
        if (maBan == null) return "N/A";

        Ban ban = banDAO.getBanByMa(maBan);
        if (ban != null) {
            return ban.getTenBan() + " - " + ban.getKhuVuc();
        }
        return maBan;
    }

    private void showChiTietDialog(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList) {
        if (chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy chi tiết món ăn cho Mã Đơn Đặt: " + hoaDon.getMaDon(),
                    "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String tenKhachHang = "Khách lẻ";
        String tenBan = "Mang về / Không rõ";
        String maKHTimDuoc = hoaDon.getMaKH();
        if (maKHTimDuoc == null || maKHTimDuoc.isEmpty()) {
            try {
                dao.DonDatMonDAO ddmDAO = new dao.DonDatMonDAO();
                entity.DonDatMon ddm = ddmDAO.getDonDatMonByMa(hoaDon.getMaDon());
                if (ddm != null) {
                    maKHTimDuoc = ddm.getMaKH();
                }
            } catch (Exception e) {
            }
        }

        if (maKHTimDuoc != null && !maKHTimDuoc.isEmpty()) {
            KhachHangDAO khDAO = new KhachHangDAO();
            entity.KhachHang kh = khDAO.timTheoMaKH(maKHTimDuoc);
            if (kh != null) {
                tenKhachHang = kh.getTenKH();
            }
        }

        if (hoaDon.getTenBan() != null && !hoaDon.getTenBan().isEmpty()) {
            tenBan = hoaDon.getTenBan();
        } else {
            try {
                dao.DonDatMonDAO ddmDAO = new dao.DonDatMonDAO();
                dao.BanDAO banDAO = new dao.BanDAO();
                entity.DonDatMon ddm = ddmDAO.getDonDatMonByMa(hoaDon.getMaDon());

                if (ddm != null) {
                    tenBan = banDAO.getTenBanByMa(ddm.getMaBan());
                }
            } catch (Exception e) {
            }
        }

        StringBuilder detailsText = new StringBuilder();
        detailsText.append("<html><body style='font-family: Arial; font-size: 11pt;'>");
        detailsText.append("<h2>Chi Tiết Hóa Đơn: ").append(hoaDon.getMaHD()).append("</h2>");

        detailsText.append("<b>Ngày lập:</b> ").append(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(tableDateFormatter) : "N/A").append("<br>");
        detailsText.append("<b>Mã Đơn Đặt:</b> ").append(hoaDon.getMaDon()).append("<br>");

        detailsText.append("<b>Bàn:</b> ").append(tenBan).append("<br>");
        detailsText.append("<b>Khách hàng:</b> ").append(tenKhachHang).append("<br>");

        String tenNV = nhanVienDAO.getTenNhanVienByMa(hoaDon.getMaNV());
        detailsText.append("<b>Nhân viên:</b> ").append(tenNV).append(" (").append(hoaDon.getMaNV()).append(")<br>");
        detailsText.append("<br>");

        detailsText.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse; width:100%; font-size: 10pt;'>");
        detailsText.append("<tr style='background-color:#f0f0f0;'><th>Mã Món</th><th>Tên Món</th><th>Số Lượng</th><th>Đơn Giá</th><th>Thành Tiền</th></tr>");

        float tongTienChiTiet = 0;
        for (ChiTietHoaDon ct : chiTietList) {
            if (ct == null) continue;
            String maMon = ct.getMaMon() != null ? ct.getMaMon() : "N/A";
            String tenMon = ct.getTenMon() != null ? ct.getTenMon() : monAnDAO.getTenMonByMa(maMon);
            float thanhTien = ct.getThanhtien();
            tongTienChiTiet += thanhTien;

            detailsText.append("<tr>");
            detailsText.append("<td>").append(maMon).append("</td>");
            detailsText.append("<td>").append(tenMon).append("</td>");
            detailsText.append("<td align='right'>").append(ct.getSoluong()).append("</td>");
            detailsText.append("<td align='right'>").append(currencyFormatter.format(ct.getDongia())).append("</td>");
            detailsText.append("<td align='right'>").append(currencyFormatter.format(thanhTien)).append("</td>");
            detailsText.append("</tr>");
        }
        detailsText.append("</table><br>");

        detailsText.append("<b>Tổng tiền (từ chi tiết): ").append(currencyFormatter.format(tongTienChiTiet)).append("</b><br>");

        if (Math.abs(tongTienChiTiet - hoaDon.getTongTien()) > 1) {
            detailsText.append("<b style='color:red;'>Lưu ý: Tổng tiền chi tiết khác tổng tiền hóa đơn (")
                    .append(currencyFormatter.format(hoaDon.getTongTien())).append(")</b><br>");
        }
        detailsText.append("<b>Giảm giá:</b> ").append(currencyFormatter.format(hoaDon.getGiamGia())).append("<br>");
        detailsText.append("<b>Tổng thanh toán:</b> ").append(currencyFormatter.format(hoaDon.getTongThanhToan())).append("<br><br>");

        detailsText.append("<b>Trạng thái HĐ:</b> ").append(hoaDon.getTrangThai() != null ? hoaDon.getTrangThai() : "N/A").append("<br>");
        detailsText.append("<b>Hình thức TT:</b> ").append(hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "N/A").append("<br>");
        if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrangThai())) {
            detailsText.append("<b>Tiền khách đưa:</b> ").append(currencyFormatter.format(hoaDon.getTienKhachDua())).append("<br>");
            detailsText.append("<b>Tiền thối:</b> ").append(currencyFormatter.format(hoaDon.getTienThoi())).append("<br>");
        }
        detailsText.append("</body></html>");

        JEditorPane editorPane = new JEditorPane("text/html", detailsText.toString());
        editorPane.setEditable(false);
        editorPane.setBackground(COLOR_BG_LIGHT);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(650, 450));

        JDialog detailDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn " + hoaDon.getMaHD(), Dialog.ModalityType.APPLICATION_MODAL);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.addActionListener(e -> detailDialog.dispose());

        JButton btnPrint = new JButton("In Hóa Đơn");
        btnPrint.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrint.addActionListener(e -> {
            printSessionCounter++;
            showPrintPreviewDialog(
                    "PHIẾU IN (PHIÊN " + printSessionCounter + ")",
                    hoaDon,
                    chiTietList
            );
        });

        buttonPanel.add(btnPrint);
        buttonPanel.add(btnClose);

        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.pack();
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setVisible(true);
    }

    private void showPrintPreviewDialog(String title, HoaDon hoaDon, List<ChiTietHoaDon> dsMon) {
        if (hoaDon == null || dsMon == null || dsMon.isEmpty()) return;

        String tongTienGoc = currencyFormatter.format(hoaDon.getTongTien());
        String giamGia = currencyFormatter.format(hoaDon.getGiamGia());
        String tongThanhToan = currencyFormatter.format(hoaDon.getTongThanhToan());

        String tenNV = nhanVienDAO.getTenNhanVienByMa(hoaDon.getMaNV());
        boolean daThanhToan = "Đã thanh toán".equalsIgnoreCase(hoaDon.getTrangThai());
        String tenBanKhuVuc = getTenBanVaKhuVuc(hoaDon.getMaDon());

        StringBuilder billText = new StringBuilder();

        billText.append("===================================================\n");
        billText.append("                   PHIẾU HÓA ĐƠN\n");
        billText.append("               ").append(title).append("\n");
        billText.append("===================================================\n");
        billText.append("Mã HĐ: ").append(hoaDon.getMaHD()).append("\n");
        billText.append("Ngày:  ").append(hoaDon.getNgayLap().format(billDateFormatter)).append("\n");
        billText.append("Nhân viên: ").append(tenNV).append("\n");
        billText.append("Bàn:   ").append(tenBanKhuVuc).append("\n");
        billText.append("---------------------------------------------------\n");

        billText.append(String.format("%-20s %5s %10s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));
        billText.append("---------------------------------------------------\n");

        for (ChiTietHoaDon ct : dsMon) {
            String maMon = ct.getMaMon() != null ? ct.getMaMon() : "N/A";
            String tenMon = ct.getTenMon() != null ? ct.getTenMon() : monAnDAO.getTenMonByMa(maMon);
            String tenMonDisplay = tenMon.length() > 18 ? tenMon.substring(0, 17) + "." : tenMon;

            billText.append(String.format("%-20s %5d %10s %12s\n",
                    tenMonDisplay,
                    ct.getSoluong(),
                    currencyFormatter.format(ct.getDongia()),
                    currencyFormatter.format(ct.getThanhtien())));
        }
        billText.append("---------------------------------------------------\n");

        billText.append(String.format("%-28s %20s\n", "Tổng cộng (Gốc):", tongTienGoc));
        if (hoaDon.getGiamGia() > 0) {
            billText.append(String.format("%-28s %20s\n", "Giảm giá:", giamGia));
        }

        billText.append("===================================================\n");
        billText.append(String.format("%-28s %20s\n", "TỔNG THANH TOÁN:", tongThanhToan));

        if (daThanhToan) {
            String tienKhachDua = currencyFormatter.format(hoaDon.getTienKhachDua());
            String tienThoi = currencyFormatter.format(hoaDon.getTienThoi());

            billText.append(String.format("%-28s %20s\n", "Hình thức:", hoaDon.getHinhThucThanhToan()));
            billText.append(String.format("%-28s %20s\n", "Tiền khách đưa:", tienKhachDua));
            billText.append(String.format("%-28s %20s\n", "Tiền thối lại:", tienThoi));
            billText.append("---------------------------------------------------\n");
            billText.append("               XIN CẢM ƠN VÀ HẸN GẶP LẠI!       \n");
        } else {
            billText.append("\n(Phiếu này chỉ để kiểm tra, đã thanh toán)\n");
        }
        billText.append("===================================================\n");

        JDialog previewDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Xem trước in: " + hoaDon.getMaHD(), Dialog.ModalityType.APPLICATION_MODAL);
        previewDialog.setSize(420, 600);
        previewDialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea(billText.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> previewDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnClose);

        previewDialog.add(scrollPane, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }

    private void exportDataToExcel() {
        String trangThai = getSelectedTrangThaiFilter();
        String keyword = currentKeyword;
        LocalDateTime[] dates = getFilterDates();
        LocalDateTime tuNgay = dates != null ? dates[0] : null;
        LocalDateTime denNgay = dates != null ? dates[1] : null;

        List<HoaDon> listToExport = hoaDonDAO.getAllHoaDonFiltered(trangThai, keyword, tuNgay, denNgay);

        if (listToExport == null || listToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu hóa đơn thỏa mãn điều kiện lọc để xuất.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");

        String dateSuffix = "";
        if (tuNgay != null && denNgay != null) {
            if (tuNgay.toLocalDate().isEqual(denNgay.toLocalDate())) {
                dateSuffix = "_Ngay_" + tuNgay.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            } else {
                dateSuffix = "_Tu_" + tuNgay.format(DateTimeFormatter.ofPattern("ddMMyy")) +
                        "_Den_" + denNgay.toLocalDate().format(DateTimeFormatter.ofPattern("ddMMyy"));
            }
        }

        DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String defaultFileName = "HoaDon_Loc" + dateSuffix + "_" + LocalDateTime.now().format(fileNameFormat) + ".xlsx";
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            ExcelExporter exporter = new ExcelExporter();
            boolean success = exporter.exportToExcel(listToExport, filePath);

            if (success) {
                JOptionPane.showMessageDialog(this, "Xuất hóa đơn thành công tại:\n" + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}