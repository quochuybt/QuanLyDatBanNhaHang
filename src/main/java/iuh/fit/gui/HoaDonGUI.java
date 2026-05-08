package iuh.fit.gui;

import com.toedter.calendar.JDateChooser;
import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.dto.DonDatMonDTO;
import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.entity.HoaDon; // Chỉ dùng để xuất Excel tạm thời
import iuh.fit.core.net.client.HoaDonRemoteService;
import iuh.fit.core.net.client.NetClientContext;
import iuh.fit.core.net.dto.hoadon.HoaDonDetailRequestDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonPageRequestDTO;
import iuh.fit.core.net.dto.hoadon.HoaDonTotalRequestDTO;
import iuh.fit.core.service.*;
import iuh.fit.core.util.ExcelExporter;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HoaDonGUI extends JPanel {

    // --- Services ---
    private final HoaDonService hoaDonService = new HoaDonService();
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final DonDatMonService donDatMonService = new DonDatMonService();
    private final NhanVienService nhanVienService = new NhanVienService();
    private final BanService banService = new BanService();
    private final HoaDonRemoteService hoaDonRemoteService;

    // --- UI Components ---
    private final JTable tableHoaDon;
    private final DefaultTableModel tableModel;
    private final JTabbedPane tabbedPane;
    private JTextField txtTimKiem;
    private JDateChooser dateChooserTuNgay, dateChooserDenNgay;
    private JButton btnLocNgay, btnHomNay, btnXoaLoc;

    // Phân trang
    private JPanel paginationPanel;
    private JLabel lblPageInfo;
    private JButton btnFirst, btnPrev, btnNext, btnLast;

    // --- Formatters & Constants ---
    private static final Color COLOR_BG_LIGHT = new Color(244, 247, 252);
    private final String[] columnNames = { "Thời gian thanh toán", "Mã tham chiếu", "Nhân viên", "Ghi chú",
            "Thanh toán", "Tổng tiền" };
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DateTimeFormatter tableDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter billDateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final int ITEMS_PER_PAGE = 15;

    // --- State Variables ---
    private List<HoaDonDTO> dsHoaDonDisplayed = new ArrayList<>();
    private DocumentListener searchListener;
    private Timer searchTimer;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentKeyword = "";
    private static int printSessionCounter = 0;

    public HoaDonGUI() {
        if (NetClientContext.isReady()) {
            hoaDonRemoteService = new HoaDonRemoteService(NetClientContext.getConnection());
        } else {
            hoaDonRemoteService = null;
        }

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

        // Nạp dữ liệu ban đầu
        SwingUtilities.invokeLater(this::loadFirstPage);
    }

    // =================================================================================
    // UI LAYOUT SETUP (Giữ nguyên cấu trúc của bạn)
    // =================================================================================

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
        try {
            java.net.URL iconURL = getClass().getResource("/img/icon/excel.png");
            if (iconURL != null) {
                Image scaledImage = new ImageIcon(iconURL).getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                btnExport.setIcon(new ImageIcon(scaledImage));
                btnExport.setHorizontalTextPosition(SwingConstants.RIGHT);
                btnExport.setIconTextGap(8);
            }
        } catch (Exception ignored) {
        }

        btnExport.setBackground(new Color(0, 150, 60));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 80), 1),
                new EmptyBorder(8, 15, 8, 15)));
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

    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        btnFirst = new JButton("<< Đầu");
        btnPrev = new JButton("< Trước");
        lblPageInfo = new JLabel("Trang 1/1");
        btnNext = new JButton("Sau >");
        btnLast = new JButton("Cuối >>");

        JButton[] btns = { btnFirst, btnPrev, btnNext, btnLast };
        for (JButton btn : btns) {
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(56, 118, 243));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.add(btn);
        }

        panel.removeAll();
        panel.add(btnFirst);
        panel.add(btnPrev);
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPageInfo);
        panel.add(btnNext);
        panel.add(btnLast);

        btnFirst.addActionListener(e -> navigateToPage(1));
        btnPrev.addActionListener(e -> navigateToPage(currentPage - 1));
        btnNext.addActionListener(e -> navigateToPage(currentPage + 1));
        btnLast.addActionListener(e -> navigateToPage(totalPages));

        return panel;
    }

    private JPanel createMainTablePanel(JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel topFilterPanel = new JPanel(new BorderLayout(10, 0));
        topFilterPanel.setOpaque(false);
        topFilterPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        txtTimKiem = new JTextField(" Tìm kiếm qua mã hóa đơn");
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY);
        txtTimKiem.setPreferredSize(new Dimension(250, 35));
        addPlaceholderFocusHandler(txtTimKiem, " Tìm kiếm qua mã hóa đơn");
        setupRealTimeSearch();

        JPanel inputWrapper = new JPanel(new BorderLayout(5, 0));
        inputWrapper.setOpaque(false);
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(txtTimKiem, BorderLayout.CENTER);
        searchPanel.add(inputWrapper, BorderLayout.CENTER);

        // Date Filter Panel
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dateFilterPanel.setOpaque(false);

        dateChooserTuNgay = new JDateChooser();
        dateChooserTuNgay.setDateFormatString("dd/MM/yyyy");
        dateChooserTuNgay.setPreferredSize(new Dimension(130, 35));

        dateChooserDenNgay = new JDateChooser();
        dateChooserDenNgay.setDateFormatString("dd/MM/yyyy");
        dateChooserDenNgay.setPreferredSize(new Dimension(130, 35));

        btnLocNgay = new JButton("Lọc");
        btnLocNgay.setPreferredSize(new Dimension(80, 35));
        btnLocNgay.setBackground(new Color(50, 150, 200));
        btnLocNgay.setForeground(Color.WHITE);
        btnLocNgay.addActionListener(e -> {
            currentPage = 1;
            loadDataForCurrentPage();
        });

        btnHomNay = new JButton("Hôm nay");
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
        btnXoaLoc.setPreferredSize(new Dimension(80, 35));
        btnXoaLoc.addActionListener(e -> {
            dateChooserTuNgay.setDate(null);
            dateChooserDenNgay.setDate(null);
            btnXoaLoc.requestFocus();
            resetSearchFieldIfNeeded();
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
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupTableAppearance(JTable table) {
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(150);
        tcm.getColumn(1).setPreferredWidth(100);
        tcm.getColumn(2).setPreferredWidth(150);
        tcm.getColumn(3).setPreferredWidth(200);
        tcm.getColumn(4).setPreferredWidth(100);
        tcm.getColumn(5).setPreferredWidth(100);
    }

    // =================================================================================
    // LOGIC NGHIỆP VỤ & TƯƠNG TÁC SERVICE
    // =================================================================================

    private void updatePaginationControls() {
        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages);
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);
    }

    private void loadFirstPage() {
        currentPage = 1;
        currentKeyword = "";
        loadDataForCurrentPage();
    }

    private void navigateToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage)
            return;
        currentPage = page;
        loadDataForCurrentPage();
    }

    private void loadDataForSelectedTab() {
        resetSearchFieldIfNeeded();
        loadFirstPage();
    }

    private String getSelectedTrangThaiFilter() {
        int idx = tabbedPane.getSelectedIndex();
        return switch (idx) {
            case 1 -> "Đã thanh toán";
            case 2 -> "Chưa thanh toán";
            default -> "Tất cả";
        };
    }

    private LocalDateTime[] getFilterDates() {
        LocalDateTime start = null, end = null;
        Date dFrom = dateChooserTuNgay.getDate();
        Date dTo = dateChooserDenNgay.getDate();

        if (dFrom != null)
            start = dFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        if (dTo != null)
            end = dTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);

        if (dFrom != null && dTo == null)
            end = start.plusDays(1).minusNanos(1);
        if (dTo != null && dFrom == null)
            start = LocalDateTime.MIN;

        if (start != null && end != null && start.isAfter(end) && start != LocalDateTime.MIN) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.");
            return new LocalDateTime[] { null, null };
        }
        return new LocalDateTime[] { start, end };
    }

    private void loadDataForCurrentPage() {
        new SwingWorker<Void, Void>() {
            private List<HoaDonDTO> resultList;

            @Override
            protected Void doInBackground() {
                String trangThai = getSelectedTrangThaiFilter();
                LocalDateTime[] dates = getFilterDates();

                long totalCount;
                if (hoaDonRemoteService != null) {
                    totalCount = hoaDonRemoteService.getTotalHoaDonCount(HoaDonTotalRequestDTO.builder()
                            .trangThai(trangThai)
                            .keyword(currentKeyword)
                            .tuNgay(dates[0])
                            .denNgay(dates[1])
                            .build());
                } else {
                    totalCount = hoaDonService.getTotalHoaDonCount(trangThai, currentKeyword, dates[0], dates[1]);
                }

                totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
                if (totalPages < 1)
                    totalPages = 1;
                if (currentPage > totalPages)
                    currentPage = totalPages;

                if (hoaDonRemoteService != null) {
                    resultList = hoaDonRemoteService.getHoaDonByPage(HoaDonPageRequestDTO.builder()
                            .page(currentPage)
                            .itemsPerPage(ITEMS_PER_PAGE)
                            .trangThai(trangThai)
                            .keyword(currentKeyword)
                            .tuNgay(dates[0])
                            .denNgay(dates[1])
                            .build());
                } else {
                    resultList = hoaDonService.getHoaDonByPage(currentPage, ITEMS_PER_PAGE, trangThai, currentKeyword,
                            dates[0], dates[1]);
                }
                return null;
            }

            @Override
            protected void done() {
                loadDataToTable(resultList);
                updatePaginationControls();
            }
        }.execute();
    }

    private void loadDataToTable(List<HoaDonDTO> list) {
        dsHoaDonDisplayed = (list == null) ? new ArrayList<>() : list;
        tableModel.setRowCount(0);

        for (HoaDonDTO hd : dsHoaDonDisplayed) {
            String tenNV = "N/A";
            try {
                if (hd.getMaNV() != null) {
                    var nv = nhanVienService.findById(hd.getMaNV());
                    if (nv != null)
                        tenNV = nv.getHoten();
                }
            } catch (Exception ignored) {
            }

            String ghiChu = "Không";
            if (hd.getMaDon() != null) {
                try {
                    DonDatMonDTO ddm = donDatMonService.findById(hd.getMaDon());
                    if (ddm != null && ddm.getGhiChu() != null && !ddm.getGhiChu().trim().isEmpty()) {
                        ghiChu = ddm.getGhiChu().trim();
                        if (ghiChu.contains("LINKED:")) {
                            int linkedIndex = ghiChu.indexOf("LINKED:");
                            String clean = ghiChu.substring(0, linkedIndex).trim();
                            ghiChu = clean.isEmpty() ? "Gộp bàn" : clean + " (Gộp)";
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            tableModel.addRow(new Object[] {
                    hd.getNgayLap() != null ? hd.getNgayLap().format(tableDateFormatter) : "N/A",
                    hd.getMaHD() != null ? hd.getMaHD() : "N/A",
                    tenNV,
                    ghiChu,
                    hd.getHinhThucThanhToan(),
                    currencyFormatter.format(hd.getTongThanhToan())
            });
        }
    }

    // =================================================================================
    // SỰ KIỆN CLICK VÀ XEM CHI TIẾT
    // =================================================================================

    private void addTableClickListener() {
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableHoaDon.getSelectedRow();
                    if (row == -1 || row >= dsHoaDonDisplayed.size())
                        return;

                    HoaDonDTO hd = dsHoaDonDisplayed.get(row);
                    if (hd.getMaDon() == null || hd.getMaDon().isEmpty()) {
                        JOptionPane.showMessageDialog(
                                HoaDonGUI.this,
                                "Hóa đơn này thiếu mã đơn liên kết nên không thể tải chi tiết.",
                                "Thiếu dữ liệu",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    // Tạo DTO giả lập để gọi hàm Service theo đúng chữ ký của bạn
                    List<ChiTietHoaDonDTO> chiTietList;
                    if (hoaDonRemoteService != null) {
                        chiTietList = hoaDonRemoteService.getChiTietHoaDon(HoaDonDetailRequestDTO.builder()
                                .maDon(hd.getMaDon())
                                .build());
                    } else {
                        ChiTietHoaDonDTO filterDTO = ChiTietHoaDonDTO.builder().maDon(hd.getMaDon()).build();
                        chiTietList = chiTietHoaDonService.getChiTietTheoMaDon(filterDTO);
                    }

                    if (chiTietList == null || chiTietList.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                HoaDonGUI.this,
                                "Không tìm thấy chi tiết món ăn cho hóa đơn này.",
                                "Không có dữ liệu",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    showChiTietDialog(hd, chiTietList);
                }
            }
        });
    }

    private void showChiTietDialog(HoaDonDTO hoaDon, List<ChiTietHoaDonDTO> chiTietList) {
        if (chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết món ăn.");
            return;
        }

        String tenBan = "Không rõ";
        if (hoaDon.getMaDon() != null) {
            DonDatMonDTO ddm = donDatMonService.findById(hoaDon.getMaDon());
            if (ddm != null && ddm.getMaBan() != null) {
                tenBan = banService.getTenBanByMa(iuh.fit.core.dto.BanDTO.builder().maBan(ddm.getMaBan()).build());
            }
        }

        StringBuilder html = new StringBuilder("<html><body style='font-family: Arial; font-size: 11pt;'>");
        html.append("<h2>Chi Tiết Hóa Đơn: ").append(hoaDon.getMaHD()).append("</h2>");
        html.append("<b>Ngày lập:</b> ")
                .append(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(tableDateFormatter) : "N/A")
                .append("<br>");
        html.append("<b>Bàn:</b> ").append(tenBan).append("<br><br>");

        html.append(
                "<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse; width:100%; font-size: 10pt;'>");
        html.append(
                "<tr style='background-color:#f0f0f0;'><th>Tên Món</th><th>SL</th><th>Đơn Giá</th><th>Thành Tiền</th></tr>");

        float tongTienMon = 0;
        for (ChiTietHoaDonDTO ct : chiTietList) {
            tongTienMon += ct.getThanhTien();
            html.append("<tr>")
                    .append("<td>").append(ct.getTenMon() != null ? ct.getTenMon() : ct.getMaMonAn()).append("</td>")
                    .append("<td align='right'>").append(ct.getSoLuong()).append("</td>")
                    .append("<td align='right'>").append(currencyFormatter.format(ct.getDonGia())).append("</td>")
                    .append("<td align='right'>").append(currencyFormatter.format(ct.getThanhTien())).append("</td>")
                    .append("</tr>");
        }
        html.append("</table><br>");

        html.append("<b>Tổng tiền món:</b> ").append(currencyFormatter.format(tongTienMon)).append("<br>");
        html.append("<b>Giảm giá:</b> ").append(currencyFormatter.format(hoaDon.getGiamGia())).append("<br>");
        html.append("<b>TỔNG THANH TOÁN:</b> <b style='color:blue;font-size:14pt'>")
                .append(currencyFormatter.format(hoaDon.getTongThanhToan())).append("</b><br>");

        html.append("</body></html>");

        JEditorPane editorPane = new JEditorPane("text/html", html.toString());
        editorPane.setEditable(false);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(editorPane), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("In Hóa Đơn");
        final String finalTenBan = tenBan;
        btnPrint.addActionListener(e -> showPrintPreviewDialog(hoaDon, chiTietList, finalTenBan));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showPrintPreviewDialog(HoaDonDTO hoaDon, List<ChiTietHoaDonDTO> chiTietList, String tenBan) {
        printSessionCounter++;
        StringBuilder billText = new StringBuilder();
        billText.append("===================================================\n");
        billText.append("                   PHIẾU HÓA ĐƠN\n");
        billText.append("               PHIÊN IN COPY LẦN ").append(printSessionCounter).append("\n");
        billText.append("===================================================\n");
        billText.append("Mã HĐ: ").append(hoaDon.getMaHD()).append("\n");
        billText.append("Ngày:  ").append(hoaDon.getNgayLap().format(billDateFormatter)).append("\n");
        billText.append("Bàn:   ").append(tenBan).append("\n");
        billText.append("---------------------------------------------------\n");
        billText.append(String.format("%-20s %5s %10s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));

        for (ChiTietHoaDonDTO ct : chiTietList) {
            String ten = ct.getTenMon() != null ? ct.getTenMon() : ct.getMaMonAn();
            if (ten.length() > 18)
                ten = ten.substring(0, 17) + ".";
            billText.append(String.format("%-20s %5d %10s %12s\n",
                    ten, ct.getSoLuong(),
                    currencyFormatter.format(ct.getDonGia()),
                    currencyFormatter.format(ct.getThanhTien())));
        }
        billText.append("---------------------------------------------------\n");
        billText.append(String.format("%-28s %20s\n", "Giảm giá:", currencyFormatter.format(hoaDon.getGiamGia())));
        billText.append("===================================================\n");
        billText.append(
                String.format("%-28s %20s\n", "TỔNG CỘNG:", currencyFormatter.format(hoaDon.getTongThanhToan())));

        JDialog previewDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Xem trước in",
                Dialog.ModalityType.APPLICATION_MODAL);
        JTextArea textArea = new JTextArea(billText.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        previewDialog.add(new JScrollPane(textArea));
        previewDialog.setSize(420, 600);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setVisible(true);
    }

    // =================================================================================
    // TÌM KIẾM & XUẤT EXCEL
    // =================================================================================

    private void addPlaceholderFocusHandler(JTextField tf, String placeholder) {
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().trim().isEmpty()) {
                    tf.setForeground(Color.GRAY);
                    tf.setText(placeholder);
                }
            }
        });
    }

    private void setupRealTimeSearch() {
        searchTimer = new Timer(300, e -> {
            String query = txtTimKiem.getText().trim();
            currentKeyword = (query.isEmpty() || query.equals("Tìm kiếm qua mã hóa đơn")) ? "" : query;
            currentPage = 1;
            loadDataForCurrentPage();
        });
        searchTimer.setRepeats(false);
        searchListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                searchTimer.restart();
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        txtTimKiem.getDocument().addDocumentListener(searchListener);
    }

    private void resetSearchFieldIfNeeded() {
        String placeholder = " Tìm kiếm qua mã hóa đơn";
        if (!txtTimKiem.getText().equals(placeholder)) {
            txtTimKiem.getDocument().removeDocumentListener(searchListener);
            if (txtTimKiem.hasFocus()) {
                txtTimKiem.setText("");
                txtTimKiem.setForeground(Color.BLACK);
            } else {
                txtTimKiem.setText(placeholder);
                txtTimKiem.setForeground(Color.GRAY);
            }
            txtTimKiem.getDocument().addDocumentListener(searchListener);
            currentKeyword = "";
        }
    }

    private void exportDataToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));
        fileChooser.setSelectedFile(new java.io.File("DanhSachHoaDon.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            final String finalPath = filePath;
            final String trangThai = getSelectedTrangThaiFilter();
            final String keyword = currentKeyword;
            final LocalDateTime[] dates = getFilterDates();

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        // Lấy toàn bộ danh sách theo bộ lọc, không phân trang khi xuất Excel
                        long total;
                        if (hoaDonRemoteService != null) {
                            total = hoaDonRemoteService.getTotalHoaDonCount(HoaDonTotalRequestDTO.builder()
                                    .trangThai(trangThai)
                                    .keyword(keyword)
                                    .tuNgay(dates[0])
                                    .denNgay(dates[1])
                                    .build());
                        } else {
                            total = hoaDonService.getTotalHoaDonCount(
                                    trangThai,
                                    keyword,
                                    dates[0],
                                    dates[1]
                            );
                        }

                        if (total == 0) {
                            return false;
                        }

                        List<HoaDonDTO> dtos;
                        if (hoaDonRemoteService != null) {
                            dtos = hoaDonRemoteService.getHoaDonByPage(HoaDonPageRequestDTO.builder()
                                    .page(1)
                                    .itemsPerPage((int) total)
                                    .trangThai(trangThai)
                                    .keyword(keyword)
                                    .tuNgay(dates[0])
                                    .denNgay(dates[1])
                                    .build());
                        } else {
                            dtos = hoaDonService.getHoaDonByPage(
                                    1,
                                    (int) total,
                                    trangThai,
                                    keyword,
                                    dates[0],
                                    dates[1]
                            );
                        }

                        ExcelExporter reporter = new ExcelExporter();
                        return reporter.exportHoaDonReport(dtos, finalPath);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                    HoaDonGUI.this,
                                    "Xuất file Excel thành công!",
                                    "Thông báo",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    HoaDonGUI.this,
                                    "Xuất file thất bại hoặc không có dữ liệu.",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(
                                HoaDonGUI.this,
                                "Có lỗi xảy ra khi xuất Excel.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }.execute();
        }
    }
}
