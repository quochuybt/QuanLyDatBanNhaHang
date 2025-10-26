package gui;

import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.MonAnDAO; // <-- Bỏ comment nếu bạn đã tạo và muốn dùng MonAnDAO
import entity.ChiTietHoaDon;
import entity.HoaDon;

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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import util.ExcelExporter;

public class HoaDonGUI extends JPanel {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final MonAnDAO monAnDAO; // <-- Bỏ comment nếu dùng
    private final JTable tableHoaDon;
    private final DefaultTableModel tableModel;
    private final JTabbedPane tabbedPane;
    private JTextField txtTimKiem;
    private List<HoaDon> dsHoaDonDisplayed; // Danh sách đang hiển thị trên bảng
    private DocumentListener searchListener; // Listener cho ô tìm kiếm
    private Timer searchTimer; // Timer để trì hoãn tìm kiếm

    private static final Color COLOR_BG_LIGHT = new Color(244, 247, 252);
    private final String[] columnNames = {"Thời gian thanh toán", "Mã tham chiếu", "Nhân viên", "Ghi chú", "Thanh toán", "Tổng tiền"};
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫");
    private final DateTimeFormatter tableDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HoaDonGUI() {
        this.hoaDonDAO = new HoaDonDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.monAnDAO = new MonAnDAO(); // <-- Bỏ comment nếu dùng
        this.dsHoaDonDisplayed = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Header ---
        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- Components Chính (Tạo một lần) ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableHoaDon = new JTable(tableModel);
        setupTable(tableHoaDon);
        JScrollPane scrollPane = new JScrollPane(tableHoaDon); // ScrollPane chính
        JPanel mainTablePanel = createTablePanel(scrollPane); // Panel chính chứa search và scrollPane

        // --- JTabbedPane để Lọc ---
        tabbedPane = createTabbedPane();
        tabbedPane.addTab("Tất cả hóa đơn", null);
        tabbedPane.addTab("Đã thanh toán", null);
        tabbedPane.addTab("Chờ xác nhận thanh toán", null);
        tabbedPane.addChangeListener(e -> loadDataForSelectedTab());

        // --- Bố Cục Chính ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(tabbedPane, BorderLayout.NORTH); // Tabs ở trên
        centerPanel.add(mainTablePanel, BorderLayout.CENTER); // Panel bảng ở giữa

        add(centerPanel, BorderLayout.CENTER); // Add panel trung tâm vào frame

        // --- Listeners ---
        addTableClickListener();

        // --- Load dữ liệu lần đầu ---
        SwingUtilities.invokeLater(() -> loadDataToTable(hoaDonDAO.getAllHoaDon()));
    }

    // --- Create Header Panel ---
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Hóa đơn");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.WEST);

        // --- Nút Xuất Excel ---
        JButton btnExport = new JButton("Xuất hóa đơn");
        ImageIcon originalIcon = null;
        try {
            originalIcon = new ImageIcon(getClass().getResource("/img/icon_excel/excel.png"));
            if (originalIcon.getImageLoadStatus() != MediaTracker.COMPLETE) originalIcon = null;
        } catch (Exception e) { originalIcon = null; }
        if (originalIcon != null) {
             Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
             btnExport.setIcon(new ImageIcon(scaledImage));
             btnExport.setHorizontalTextPosition(SwingConstants.RIGHT);
             btnExport.setIconTextGap(8);
        } else { btnExport.setText("Xuất hóa đơn (Lỗi icon)"); }
        btnExport.setBackground(new Color(0, 150, 60));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 80), 1), new EmptyBorder(8, 15, 8, 15) ));
        btnExport.setContentAreaFilled(true);

        btnExport.addActionListener(e -> exportDataToExcel());
        panel.add(btnExport, BorderLayout.EAST);
        return panel;
    }

    // --- Create Tabbed Pane ---
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabPane.setBackground(Color.WHITE);
        return tabPane;
    }

    // --- Create Main Table Panel ---
    private JPanel createTablePanel(JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // --- Panel Tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        searchPanel.setOpaque(false);

        txtTimKiem = new JTextField(" Tìm kiếm qua mã hóa đơn");
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY);
        txtTimKiem.setPreferredSize(new Dimension(0, 35));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Placeholder Handling (Focus Listener)
        txtTimKiem.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiem.getText().trim().equals(" Tìm kiếm qua mã hóa đơn")) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiem.getText().trim().isEmpty()) {
                    txtTimKiem.setForeground(Color.GRAY);
                    txtTimKiem.setText(" Tìm kiếm qua mã hóa đơn");
                }
            }
        });

        // Real-time Search (Document Listener with Timer)
        searchTimer = new Timer(300, e -> performSearch());
        searchTimer.setRepeats(false);

        searchListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { /* Not used */ }
        };
        txtTimKiem.getDocument().addDocumentListener(searchListener);

        // Search Icon
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        JPanel inputWrapper = new JPanel(new BorderLayout(5, 0));
        inputWrapper.setOpaque(false);
        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(txtTimKiem, BorderLayout.CENTER);
        searchPanel.add(inputWrapper, BorderLayout.CENTER);

        panel.add(searchPanel, BorderLayout.NORTH);

        // --- Bảng Hóa Đơn ---
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // --- Setup Table Appearance ---
    private void setupTable(JTable table) {
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

    // --- Load Data to Table ---
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
                 String tenNV_Moc = (maThamChieu.hashCode() % 2 == 0) ? "Huỳnh Quốc Huy" : "Nguyễn Văn A";
                 String ghiChu = "Không";
                 if (hd.getTongTien() > 1000000) ghiChu = "Yêu cầu xuất VAT";
                 else if (hd.getHinhThucThanhToan() != null && hd.getHinhThucThanhToan().equals("Chuyển khoản")) ghiChu = "Đã xác nhận";

                 try {
                     tableModel.addRow(new Object[]{
                         (hd.getNgayLap() != null ? hd.getNgayLap().format(tableDateFormatter) : "N/A"),
                         maThamChieu,
                         tenNV_Moc,
                         ghiChu,
                         hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "N/A",
                         currencyFormatter.format(hd.getTongTien())
                     });
                 } catch (Exception e) {
                     // Optionally log the error more formally
                     System.err.println("Error adding row for HD " + maThamChieu + ": " + e.getMessage());
                 }
            }
        });
    }

    // --- Load Data Based on Selected Tab ---
    private void loadDataForSelectedTab() {
        List<HoaDon> allList = hoaDonDAO.getAllHoaDon();
        List<HoaDon> filteredList;

        if (allList == null) allList = new ArrayList<>();

        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 1: // Đã thanh toán
                filteredList = allList.stream()
                        .filter(hd -> hd != null && "Đã thanh toán".equals(hd.getTrangThai()))
                        .collect(Collectors.toList());
                break;
            case 2: // Chờ xác nhận thanh toán (ĐÃ CẬP NHẬT)
                filteredList = allList.stream()
                        .filter(hd -> hd != null && "Chưa thanh toán".equals(hd.getTrangThai())) // <--- Đã loại bỏ điều kiện hinhThucThanhToan
                        .collect(Collectors.toList());
                break;
            case 0: // Tất cả hóa đơn
            default:
                filteredList = allList;
        }

        loadDataToTable(filteredList);
        resetSearchFieldIfNeeded();
    }

     // --- Reset Search Field Safely ---
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


    // --- Perform Search (Called by Timer) ---
    private void performSearch() {
        SwingUtilities.invokeLater(this::searchHoaDonRealTime);
    }

    // --- Real-time Search Logic ---
    private void searchHoaDonRealTime() {
        final String currentText = txtTimKiem.getText();
        final String placeholder = " Tìm kiếm qua mã hóa đơn";

        if (currentText == null) return;

        String query = currentText.trim().toLowerCase();

        if (query.isEmpty() || query.equals(placeholder.trim().toLowerCase())) {
            loadDataForSelectedTab();
            return;
        }

        List<HoaDon> allListForTab = getCurrentTabList();
        List<HoaDon> searchResult = allListForTab.stream()
                .filter(hd -> hd != null && hd.getMaHD() != null && hd.getMaHD().toLowerCase().contains(query))
                .collect(Collectors.toList());

        loadDataToTable(searchResult);
    }

    // --- Helper: Get Base List for Current Tab ---
    private List<HoaDon> getCurrentTabList() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        List<HoaDon> allList = hoaDonDAO.getAllHoaDon();
        if (allList == null) allList = new ArrayList<>();

        switch (selectedIndex) {
            case 1:
                return allList.stream().filter(hd -> hd != null && "Đã thanh toán".equals(hd.getTrangThai())).collect(Collectors.toList());
            case 2:
                return allList.stream().filter(hd -> hd != null && "Chưa thanh toán".equals(hd.getTrangThai()) && "Chuyển khoản".equals(hd.getHinhThucThanhToan())).collect(Collectors.toList());
            case 0:
            default:
                return allList;
        }
    }

    // --- Add Table Click Listener (for details) ---
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
                        JOptionPane.showMessageDialog(HoaDonGUI.this, "Hóa đơn [" + selectedHoaDon.getMaHD() + "] không có Mã Đơn liên kết.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    List<ChiTietHoaDon> chiTietList = chiTietHoaDonDAO.getChiTietTheoMaDon(maDon);
                    showChiTietDialog(selectedHoaDon, chiTietList);
                }
            }
        });
    }

    // --- Show Details Dialog ---
    private void showChiTietDialog(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList) {
        if (chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết món ăn cho Mã Đơn: " + hoaDon.getMaDon(), "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder detailsText = new StringBuilder();
        detailsText.append("<html><h2>Chi Tiết Hóa Đơn: ").append(hoaDon.getMaHD()).append("</h2>");
        detailsText.append("<b>Ngày lập:</b> ").append(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(tableDateFormatter) : "N/A").append("<br>");
        detailsText.append("<b>Mã Đơn Đặt:</b> ").append(hoaDon.getMaDon()).append("<br><br>");
        detailsText.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse; width:100%;'>");
        detailsText.append("<tr style='background-color:#f0f0f0;'><th>Mã Món</th><th>Tên Món</th><th>Số Lượng</th><th>Đơn Giá</th><th>Thành Tiền</th></tr>"); // Thêm cột Tên Món

        float tongTienChiTiet = 0;
        for (ChiTietHoaDon ct : chiTietList) {
             if (ct == null) continue;
             String maMon = ct.getMaMon() != null ? ct.getMaMon() : "N/A";
             String tenMon = monAnDAO.getTenMonByMa(maMon); // Lấy tên món từ DAO
             float thanhTien = ct.getThanhtien();
             tongTienChiTiet += thanhTien;

             detailsText.append("<tr>");
             detailsText.append("<td>").append(maMon).append("</td>");
             detailsText.append("<td>").append(tenMon).append("</td>"); // Hiển thị tên món
             detailsText.append("<td align='right'>").append(ct.getSoluong()).append("</td>");
             detailsText.append("<td align='right'>").append(currencyFormatter.format(ct.getDongia())).append("</td>");
             detailsText.append("<td align='right'>").append(currencyFormatter.format(thanhTien)).append("</td>");
             detailsText.append("</tr>");
        }
        detailsText.append("</table><br>");
        detailsText.append("<b>Tổng tiền chi tiết: ").append(currencyFormatter.format(tongTienChiTiet)).append("</b><br>");
        // So sánh tổng tiền
        if (Math.abs(tongTienChiTiet - hoaDon.getTongTien()) > 1) {
             detailsText.append("<b style='color:red;'>Lưu ý: Tổng tiền chi tiết khác tổng tiền hóa đơn (")
                        .append(currencyFormatter.format(hoaDon.getTongTien())).append(")</b><br>");
         }
        // Thông tin thanh toán
        detailsText.append("<b>Trạng thái HĐ:</b> ").append(hoaDon.getTrangThai() != null ? hoaDon.getTrangThai() : "N/A").append("<br>");
        detailsText.append("<b>Hình thức TT:</b> ").append(hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "N/A").append("<br>");
        if ("Đã thanh toán".equals(hoaDon.getTrangThai())) {
             detailsText.append("<b>Tiền khách đưa:</b> ").append(currencyFormatter.format(hoaDon.getTienKhachDua())).append("<br>");
             detailsText.append("<b>Tiền thối:</b> ").append(currencyFormatter.format(hoaDon.getTienThoi())).append("<br>");
        }
        detailsText.append("</html>");

        JEditorPane editorPane = new JEditorPane("text/html", detailsText.toString());
        editorPane.setEditable(false);
        editorPane.setBackground(COLOR_BG_LIGHT);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(550, 400)); // Tăng chiều rộng

        JOptionPane.showMessageDialog(this, scrollPane, "Chi tiết hóa đơn " + hoaDon.getMaHD(), JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Hàm Xuất Excel ---
    private void exportDataToExcel() {
        List<HoaDon> listToExport = this.dsHoaDonDisplayed;
        if (listToExport == null || listToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu hóa đơn để xuất.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        DateTimeFormatter fileNameFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String defaultFileName = "HoaDon_" + LocalDateTime.now().format(fileNameFormat) + ".xlsx";
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