package gui;

import dao.*;
import entity.Ban;
import entity.ChiTietHoaDon;
import entity.DonDatMon;
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
import java.util.Date;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import util.ExcelExporter;
import java.text.ParseException; // Thêm thư viện để xử lý lỗi parse Date

// import com.toedter.calendar.JDateChooser; // Giả định đã import

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
    private List<HoaDon> dsHoaDonDisplayed; // Danh sách hóa đơn đang hiển thị trên bảng
    private DocumentListener searchListener;
    private Timer searchTimer; // Timer để trì hoãn tìm kiếm khi gõ

    // ⭐ GIỮ NGUYÊN: Dùng JTextField cho mục đích biên dịch ⭐
    private JTextField dateChooserTuNgay;
    private JTextField dateChooserDenNgay;
    private JButton btnLocNgay;
    private JButton btnHomNay;
    private JButton btnXoaLoc;

    // ⭐ THÊM: Biến Phiên In ⭐
    private static int printSessionCounter = 0;

    // --- Constants ---
    private static final Color COLOR_BG_LIGHT = new Color(244, 247, 252);
    private final String[] columnNames = {"Thời gian thanh toán", "Mã tham chiếu", "Nhân viên", "Ghi chú", "Thanh toán", "Tổng tiền"};
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,##0 ₫"); // Format tiền tệ VNĐ
    private final DateTimeFormatter tableDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); // Format ngày giờ cho bảng
    private final DateTimeFormatter displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // ⭐ THÊM: Format ngày hiển thị/nhập

    // ⭐ THÊM: Formatter cho Phiếu in (để khớp BillPanel) ⭐
    private final DateTimeFormatter billDateFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    // ===============================================
    // ⭐ CÁC BIẾN PHÂN TRANG ⭐
    // ===============================================
    private static final int ITEMS_PER_PAGE = 15; // Số dòng trên mỗi trang
    private int currentPage = 1;                   // Trang hiện tại (bắt đầu từ 1)
    private int totalPages = 1;                    // Tổng số trang
    private String currentKeyword = "";            // Từ khóa tìm kiếm hiện tại (rỗng nếu không tìm kiếm)

    // Thêm các thành phần GUI cho phân trang
    private JPanel paginationPanel;
    private JLabel lblPageInfo; // Hiển thị "Trang X/Y"
    private JButton btnFirst, btnPrev, btnNext, btnLast;


    public HoaDonGUI() {
        // --- Khởi tạo DAO ---
        this.hoaDonDAO = new HoaDonDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.monAnDAO = new MonAnDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.donDatMonDAO = new DonDatMonDAO(); // Giữ nguyên khởi tạo DAO gốc
        this.banDAO = new BanDAO();             // Giữ nguyên khởi tạo DAO gốc
        this.dsHoaDonDisplayed = new ArrayList<>(); // Khởi tạo danh sách trống

        // --- Cài đặt Layout và Giao diện cơ bản ---
        setLayout(new BorderLayout(10, 10)); // Khoảng cách ngang dọc 10px
        setBackground(COLOR_BG_LIGHT);       // Màu nền nhạt
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding xung quanh

        // --- Header (Tiêu đề và nút Xuất Excel) ---
        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- Bảng Hóa Đơn (Tạo một lần) ---
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        tableHoaDon = new JTable(tableModel);
        setupTableAppearance(tableHoaDon); // Cấu hình giao diện bảng
        JScrollPane scrollPane = new JScrollPane(tableHoaDon); // Bọc bảng trong thanh cuộn
        JPanel mainTablePanel = createMainTablePanel(scrollPane); // Panel chứa ô tìm kiếm và bảng

        // --- Tab Lọc ---
        tabbedPane = createFilterTabs(); // Tạo các tab lọc
        // Gắn sự kiện khi chuyển tab: Bắt đầu lại từ trang 1
        tabbedPane.addChangeListener(e -> loadDataForSelectedTab());

        // --- Bố cục chính ---
        JPanel centerPanel = new JPanel(new BorderLayout()); // Panel trung tâm chứa tab và bảng
        centerPanel.setOpaque(false); // Nền trong suốt
        centerPanel.add(tabbedPane, BorderLayout.NORTH);     // Tab ở trên
        centerPanel.add(mainTablePanel, BorderLayout.CENTER); // Bảng ở giữa

        add(centerPanel, BorderLayout.CENTER); // Thêm panel trung tâm vào layout chính

        // ⭐ THÊM PANEL PHÂN TRANG VÀO PHÍA DƯỚI ⭐
        paginationPanel = createPaginationPanel(); // Tạo panel phân trang
        add(paginationPanel, BorderLayout.SOUTH); // Thêm vào BOTTOM của layout chính

        // --- Gắn Listener cho bảng ---
        addTableClickListener(); // Xử lý double-click để xem chi tiết

        // --- Tải dữ liệu lần đầu ---
        // Sử dụng invokeLater để đảm bảo giao diện được vẽ xong trước khi tải dữ liệu nặng
        SwingUtilities.invokeLater(this::loadFirstPage); // ⭐ THAY THẾ: Gọi hàm tải trang đầu tiên ⭐
    }

    // ===============================================
    // ⭐ PHƯƠNG THỨC HỖ TRỢ PHÂN TRANG & LỌC NGÀY ⭐
    // ===============================================

    /**
     * Tạo panel chứa các nút điều hướng và thông tin phân trang.
     */
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5)); // Căn giữa, khoảng cách 15
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Nút Đầu (<<)
        btnFirst = new JButton("<< Đầu");
        btnFirst.setFont(new Font("Arial", Font.BOLD, 12));
        btnFirst.setForeground(Color.WHITE);
        btnFirst.addActionListener(e -> navigateToPage(1));
        panel.add(btnFirst);

        // Nút Trước (<)
        btnPrev = new JButton("< Trước");
        btnPrev.setFont(new Font("Arial", Font.BOLD, 12));
        btnPrev.setForeground(Color.WHITE);
        btnPrev.addActionListener(e -> navigateToPage(currentPage - 1));
        panel.add(btnPrev);

        // Thông tin trang (Trang X/Y)
        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblPageInfo);

        // Nút Sau (>)
        btnNext = new JButton("Sau >");
        btnNext.setFont(new Font("Arial", Font.BOLD, 12));
        btnNext.setForeground(Color.WHITE);
        btnNext.addActionListener(e -> navigateToPage(currentPage + 1));
        panel.add(btnNext);

        // Nút Cuối (>>)
        btnLast = new JButton("Cuối >>");
        btnLast.setFont(new Font("Arial", Font.BOLD, 12));
        btnLast.setForeground(Color.WHITE);
        btnLast.addActionListener(e -> navigateToPage(totalPages));
        panel.add(btnLast);

        updatePaginationControls();
        return panel;
    }

    /**
     * Cập nhật trạng thái hiển thị của các nút phân trang.
     */
    private void updatePaginationControls() {
        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages);

        // Vô hiệu hóa nút Đầu/Trước nếu đang ở trang 1
        btnFirst.setEnabled(currentPage > 1);
        btnPrev.setEnabled(currentPage > 1);

        // Vô hiệu hóa nút Sau/Cuối nếu đang ở trang cuối
        btnNext.setEnabled(currentPage < totalPages);
        btnLast.setEnabled(currentPage < totalPages);

        // Xử lý trường hợp không có dữ liệu (totalPages <= 1)
        if (totalPages <= 1) {
            btnFirst.setEnabled(false);
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            btnLast.setEnabled(false);
        }
    }

    /**
     * Helper: Lấy trạng thái lọc từ tab đang chọn.
     */
    private String getSelectedTrangThaiFilter() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 1: return "Đã thanh toán";
            case 2: return "Chưa thanh toán";
            case 0:
            default: return "Tất cả";
        }
    }

    /**
     * Helper: Chuyển đổi JTextField (Giả định là JDateChooser) thành Date.
     */
    private Date getDateFromJComponent(JTextField component) {
        String text = component.getText();
        if (text.equals("DD/MM/YYYY") || text.trim().isEmpty()) {
            return null;
        }
        try {
            // Giả lập Parse date từ JTextField
            return new java.text.SimpleDateFormat("dd/MM/yyyy").parse(text);
        } catch (ParseException e) {
            // Nếu parse lỗi, trả về null và hiển thị cảnh báo (nếu cần)
            return null;
        }
    }

    /**
     * ⭐ SỬA LỖI: Lấy ngày bắt đầu/kết thúc từ DateChooser, kiểm tra tính hợp lệ.
     * @return Một mảng [startDateTime, endDateTime]. Nếu null, tức là không lọc ngày.
     */
    private LocalDateTime[] getFilterDates() {
        LocalDateTime start = null;
        LocalDateTime end = null;

        // 1. Lấy Date từ các trường nhập liệu giả lập
        Date dateFrom = getDateFromJComponent(dateChooserTuNgay);
        Date dateTo = getDateFromJComponent(dateChooserDenNgay);

        // 2. Xử lý logic ngày
        if (dateFrom != null && dateTo != null) {
            // Trường hợp 1: Có cả ngày Bắt đầu và Kết thúc (lọc phạm vi)
            start = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().atStartOfDay();
            end = dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);
        } else if (dateFrom != null) {
            // Trường hợp 2: Chỉ có ngày Bắt đầu (lọc chính xác 1 ngày)
            start = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().atStartOfDay();
            end = start.plusDays(1).minusNanos(1);
        } else if (dateTo != null) {
            // Trường hợp 3: Chỉ có ngày Kết thúc (lọc từ trước đến hết ngày đó)
            end = dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate().plusDays(1).atStartOfDay().minusNanos(1);
            start = LocalDateTime.MIN; // Từ rất lâu
        } else {
            // Trường hợp 4: Không có ngày nào (Xóa bộ lọc ngày)
            return new LocalDateTime[]{null, null};
        }

        // 3. Kiểm tra logic: Ngày bắt đầu không được sau ngày kết thúc (trừ trường hợp MIN/MAX)
        if (start != LocalDateTime.MIN && start != null && end != null && start.isAfter(end)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được sau ngày kết thúc.", "Lỗi lọc ngày", JOptionPane.WARNING_MESSAGE);
            return new LocalDateTime[]{null, null};
        }

        return new LocalDateTime[]{start, end};
    }

    /**
     * Tải trang đầu tiên khi khởi động hoặc khi chuyển tab/reset tìm kiếm/lọc.
     */
    private void loadFirstPage() {
        currentPage = 1;
        currentKeyword = ""; // Reset từ khóa tìm kiếm
        loadDataForCurrentPage();
    }

    /**
     * Điều hướng trang: Gọi DAO để lấy dữ liệu mới từ CSDL.
     */
    private void navigateToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return; // Trang không hợp lệ hoặc đang ở trang đó
        }
        currentPage = page;
        // ⭐ GỌI DAO TẢI DỮ LIỆU MỚI TỪ CSDL ⭐
        loadDataForCurrentPage();
    }

    /**
     * ⭐ QUAN TRỌNG: Tải dữ liệu cho trang hiện tại (Áp dụng lọc, tìm kiếm và LỌC NGÀY).
     * Hàm này gọi DAO với OFFSET/LIMIT và là cốt lõi của lazy loading.
     */
    private void loadDataForCurrentPage() {
        String trangThai = getSelectedTrangThaiFilter();

        // ⭐ LẤY THAM SỐ NGÀY HỢP LỆ ⭐
        LocalDateTime[] dates = getFilterDates();
        LocalDateTime tuNgay = dates != null ? dates[0] : null;
        LocalDateTime denNgay = dates != null ? dates[1] : null;

        // 1. Lấy tổng số lượng và tính tổng số trang
        int totalCount = hoaDonDAO.getTotalHoaDonCount(trangThai, currentKeyword, tuNgay, denNgay);
        totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);

        // Đảm bảo totalPages ít nhất là 1, và currentPage không vượt quá totalPages
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        // 2. Lấy danh sách hóa đơn theo trang từ DAO
        List<HoaDon> list = hoaDonDAO.getHoaDonByPage(currentPage, trangThai, currentKeyword, tuNgay, denNgay);

        // 3. Hiển thị lên bảng và cập nhật điều khiển
        loadDataToTable(list);
        updatePaginationControls();
    }

    // ===============================================
    // ⭐ PHƯƠNG THỨC CHÍNH (CÓ THAY ĐỔI LOGIC TẢI DỮ LIỆU) ⭐
    // ===============================================

    /**
     * Tải lại dữ liệu cho tab đang được chọn (Quay về trang 1).
     */
    private void loadDataForSelectedTab() {
        resetSearchFieldIfNeeded(); // Xóa tìm kiếm nếu cần
        loadFirstPage(); // Tải lại dữ liệu (từ trang 1) của tab mới
    }

    /**
     * Tải dữ liệu từ danh sách HoaDon vào JTable.
     * (GIỮ NGUYÊN)
     */
    private void loadDataToTable(List<HoaDon> list) {
        // Chạy trên luồng EDT để đảm bảo an toàn cho Swing
        SwingUtilities.invokeLater(() -> {
            // Cập nhật danh sách đang hiển thị
            if (list == null) {
                dsHoaDonDisplayed = new ArrayList<>(); // Tạo list rỗng nếu null
            } else {
                dsHoaDonDisplayed = list;
            }

            tableModel.setRowCount(0); // Xóa hết dữ liệu cũ trên bảng

            // Lặp qua danh sách hóa đơn và thêm vào bảng
            for (HoaDon hd : dsHoaDonDisplayed) {
                if (hd == null) continue; // Bỏ qua nếu hóa đơn bị null

                String maThamChieu = hd.getMaHD() != null ? hd.getMaHD() : "N/A";

                // Lấy tên nhân viên từ mã NV
                String maNV = hd.getMaNV();
                String tenNV_Thuc = nhanVienDAO.getTenNhanVienByMa(maNV); // Dùng DAO để lấy tên

                // Xác định ghi chú dựa trên logic nghiệp vụ
                String ghiChu = "Không";
                try {
                    // Thêm dòng mới vào tableModel
                    tableModel.addRow(new Object[]{
                            (hd.getNgayLap() != null ? hd.getNgayLap().format(tableDateFormatter) : "N/A"), // Format ngày giờ
                            maThamChieu,
                            tenNV_Thuc, // Hiển thị tên NV
                            ghiChu,
                            hd.getHinhThucThanhToan() != null ? hd.getHinhThucThanhToan() : "N/A",
                            currencyFormatter.format(hd.getTongThanhToan()) // Sửa để hiển thị tổng thanh toán
                    });
                } catch (Exception e) {
                    // Ghi log lỗi nếu có vấn đề khi thêm dòng (ví dụ dữ liệu không hợp lệ)
                    System.err.println("Lỗi khi thêm dòng cho HĐ " + maThamChieu + ": " + e.getMessage());
                }
            }
        });
    }

    /**
     * Logic tìm kiếm hóa đơn dựa trên từ khóa nhập vào ô tìm kiếm.
     * Cập nhật currentKeyword và tải lại trang 1.
     */
    private void searchHoaDonRealTime() {
        final String currentText = txtTimKiem.getText();
        final String placeholder = " Tìm kiếm qua mã hóa đơn";

        if (currentText == null) return;

        String query = currentText.trim();

        // Nếu ô tìm kiếm trống hoặc là placeholder, reset và tải trang đầu tiên
        if (query.isEmpty() || query.equalsIgnoreCase(placeholder.trim())) {
            currentKeyword = ""; // Đặt từ khóa tìm kiếm là rỗng
        } else {
            currentKeyword = query; // Cập nhật từ khóa tìm kiếm
        }

        // ⭐ ĐẢM BẢO GỌI LẠI loadDataForCurrentPage() ĐỂ CẬP NHẬT DỮ LIỆU VÀ BẢNG ⭐
        currentPage = 1;
        loadDataForCurrentPage();
    }

    /**
     * Reset ô tìm kiếm về trạng thái placeholder nếu nó không chứa placeholder.
     */
    private void resetSearchFieldIfNeeded() {
        final String placeholder = " Tìm kiếm qua mã hóa đơn";
        // Chỉ reset nếu nội dung hiện tại khác placeholder
        if (!txtTimKiem.getText().equals(placeholder)) {
            // Dùng invokeLater để tránh xung đột luồng khi thay đổi DocumentListener
            SwingUtilities.invokeLater(() -> {
                txtTimKiem.getDocument().removeDocumentListener(searchListener); // Tạm gỡ listener
                txtTimKiem.setForeground(Color.GRAY); // Đặt màu placeholder
                txtTimKiem.setText(placeholder);      // Đặt text placeholder
                txtTimKiem.getDocument().addDocumentListener(searchListener); // Gắn lại listener
            });
        }
    }


    /**
     * Tạo panel chính chứa ô tìm kiếm và bảng hóa đơn.
     * @param scrollPane JScrollPane chứa bảng hóa đơn.
     */
    private JPanel createMainTablePanel(JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); // Khoảng cách dọc 10px
        panel.setOpaque(false); // Nền trong suốt

        // --- Panel Tìm kiếm & Lọc Ngày ---
        JPanel topFilterPanel = new JPanel(new BorderLayout(10, 0)); // Panel cha chứa tìm kiếm và lọc ngày
        topFilterPanel.setOpaque(false);
        topFilterPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Lề trên dưới 10px


        // 1. Panel Tìm kiếm (Bên trái)
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);

        // Ô nhập liệu tìm kiếm
        txtTimKiem = new JTextField(" Tìm kiếm qua mã hóa đơn"); // Placeholder ban đầu
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY); // Màu chữ placeholder
        txtTimKiem.setPreferredSize(new Dimension(250, 35)); // Chiều cao 35px
        // Viền kết hợp padding
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), // Viền xám nhạt
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding
        ));

        // Xử lý Placeholder khi focus/mất focus
        addPlaceholderFocusHandler(txtTimKiem, " Tìm kiếm qua mã hóa đơn");

        // Tìm kiếm real-time (khi gõ) với độ trễ (timer)
        setupRealTimeSearch();

        // Icon tìm kiếm
        JLabel searchIcon = new JLabel("🔎"); // Ký tự kính lúp
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        JPanel inputWrapper = new JPanel(new BorderLayout(5, 0)); // Bọc icon và ô nhập
        inputWrapper.setOpaque(false);
        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(txtTimKiem, BorderLayout.CENTER);
        searchPanel.add(inputWrapper, BorderLayout.CENTER); // Thêm vào panel tìm kiếm

        // 2. Panel Lọc Ngày (Bên phải)
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dateFilterPanel.setOpaque(false);

        // --- Khởi tạo JDateChooser (Sử dụng JTextField để tránh lỗi biên dịch) ---
        dateChooserTuNgay = new JTextField("");
        dateChooserDenNgay = new JTextField("");

        // Cấu hình
        dateChooserTuNgay.setPreferredSize(new Dimension(100, 35));
        dateChooserDenNgay.setPreferredSize(new Dimension(100, 35));
        dateChooserTuNgay.setEditable(true);
        dateChooserDenNgay.setEditable(true);


        // Nút Lọc
        btnLocNgay = new JButton("Lọc");
        btnLocNgay.setFont(new Font("Arial", Font.BOLD, 14));
        btnLocNgay.setPreferredSize(new Dimension(80, 35));
        btnLocNgay.setBackground(new Color(50, 150, 200));
        btnLocNgay.setForeground(Color.WHITE);

        // ⭐ LOGIC: Gắn sự kiện cho nút Lọc ⭐
        btnLocNgay.addActionListener(e -> {
            // Tạm thời ẩn focus để kích hoạt kiểm tra
            btnLocNgay.requestFocusInWindow();
            currentPage = 1; // Luôn quay về trang 1 khi lọc ngày
            loadDataForCurrentPage(); // ⭐ GỌI HÀM CẬP NHẬT DỮ LIỆU VÀ BẢNG ⭐
        });

        // ⭐ Nút Hôm nay ⭐
        btnHomNay = new JButton("Hôm nay");
        btnHomNay.setFont(new Font("Arial", Font.BOLD, 14));
        btnHomNay.setPreferredSize(new Dimension(100, 35));
        btnHomNay.setBackground(new Color(255, 165, 0));
        btnHomNay.setForeground(Color.WHITE);
        btnHomNay.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            String todayStr = today.format(displayDateFormatter);
            dateChooserTuNgay.setText(todayStr);
            dateChooserDenNgay.setText(todayStr);
            currentPage = 1;
            loadDataForCurrentPage(); // ⭐ GỌI HÀM CẬP NHẬT DỮ LIỆU VÀ BẢNG ⭐
        });

        // ⭐ Nút Xóa lọc ⭐
        btnXoaLoc = new JButton("Xóa lọc");
        btnXoaLoc.setFont(new Font("Arial", Font.PLAIN, 14));
        btnXoaLoc.setPreferredSize(new Dimension(80, 35));
        btnXoaLoc.setForeground(Color.WHITE);
        btnXoaLoc.addActionListener(e -> {
            dateChooserTuNgay.setText("");
            dateChooserDenNgay.setText("");
            currentPage = 1;
            loadDataForCurrentPage(); // ⭐ GỌI HÀM CẬP NHẬT DỮ LIỆU VÀ BẢNG ⭐
        });

        dateFilterPanel.add(new JLabel("Từ ngày:"));
        dateFilterPanel.add(dateChooserTuNgay);
        dateFilterPanel.add(new JLabel("Đến ngày:"));
        dateFilterPanel.add(dateChooserDenNgay);
        dateFilterPanel.add(btnLocNgay);
        dateFilterPanel.add(btnHomNay); // ⭐ THÊM NÚT HÔM NAY ⭐
        dateFilterPanel.add(btnXoaLoc); // ⭐ THÊM NÚT XÓA LỌC ⭐

        // Bố cục tổng thể của topFilterPanel
        topFilterPanel.add(searchPanel, BorderLayout.WEST);
        topFilterPanel.add(dateFilterPanel, BorderLayout.EAST);

        panel.add(topFilterPanel, BorderLayout.NORTH); // Panel tìm kiếm và lọc ngày ở trên

        // --- Bảng Hóa Đơn ---
        scrollPane.getViewport().setBackground(Color.WHITE); // Nền trắng cho vùng chứa bảng
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // Viền xám nhạt
        panel.add(scrollPane, BorderLayout.CENTER); // Bảng ở giữa

        return panel;
    }

    // --- CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN ---

    /**
     * Tạo panel header chứa tiêu đề "Hóa đơn" và nút "Xuất hóa đơn".
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Nền trong suốt
        panel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Lề dưới 10px

        JLabel titleLabel = new JLabel("Hóa đơn");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.WEST); // Tiêu đề bên trái

        // --- Nút Xuất Excel ---
        JButton btnExport = new JButton("Xuất hóa đơn");
        styleExportButton(btnExport); // Áp dụng style cho nút
        btnExport.addActionListener(e -> exportDataToExcel()); // Gắn sự kiện xuất Excel
        panel.add(btnExport, BorderLayout.EAST); // Nút bên phải

        return panel;
    }

    /**
     * Áp dụng style cho nút Xuất Excel (icon, màu sắc, font chữ).
     * (GIỮ NGUYÊN CODE GỐC)
     */
    private void styleExportButton(JButton btnExport) {
        ImageIcon originalIcon = null;
        try {
            // Cố gắng tải icon từ resources
            java.net.URL iconURL = getClass().getResource("/img/icon/excel.png");
            if (iconURL != null) {
                originalIcon = new ImageIcon(iconURL);
            } else {
                System.err.println("Không tìm thấy icon excel.png");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải icon excel: " + e.getMessage());
            originalIcon = null;
        }

        if (originalIcon != null) {
            // Thay đổi kích thước icon nếu tải thành công
            Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            btnExport.setIcon(new ImageIcon(scaledImage));
            btnExport.setHorizontalTextPosition(SwingConstants.RIGHT); // Chữ bên phải icon
            btnExport.setIconTextGap(8); // Khoảng cách giữa icon và chữ
        } else {
            btnExport.setText("Xuất Excel (icon lỗi)"); // Thông báo nếu icon lỗi
        }

        btnExport.setBackground(new Color(0, 150, 60)); // Màu nền xanh lá
        btnExport.setForeground(Color.WHITE);          // Chữ màu trắng
        btnExport.setFont(new Font("Arial", Font.BOLD, 14));
        btnExport.setFocusPainted(false);             // Bỏ viền focus
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi con trỏ khi rê chuột
        // Viền kết hợp padding
        btnExport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 80), 1), // Viền xanh lá đậm hơn
                new EmptyBorder(8, 15, 8, 15) // Padding
        ));
        btnExport.setContentAreaFilled(true); // Đảm bảo nền được vẽ
    }


    /**
     * Tạo JTabbedPane chứa các tab lọc hóa đơn.
     */
    private JTabbedPane createFilterTabs() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabPane.setBackground(Color.WHITE); // Nền trắng cho các tab
        // Thêm các tab
        tabPane.addTab("Tất cả hóa đơn", null);
        tabPane.addTab("Đã thanh toán", null);
        tabPane.addTab("Chưa thanh toán", null); // Đổi tên tab cho rõ ràng
        return tabPane;
    }

    /**
     * Xử lý hiển thị placeholder cho JTextField.
     */
    private void addPlaceholderFocusHandler(JTextField textField, String placeholder) {
        // Thiết lập ban đầu (chắc chắn màu xám)
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Khi focus vào, nếu đang là placeholder, xóa text và đổi màu chữ
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK); // Đổi màu chữ nhập liệu
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                // Khi mất focus, nếu ô trống (hoặc chỉ chứa khoảng trắng) thì đặt lại placeholder
                if (textField.getText().trim().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    /**
     * Cài đặt tìm kiếm real-time sử dụng Timer và DocumentListener.
     */
    private void setupRealTimeSearch() {
        // Timer để trì hoãn việc tìm kiếm 300ms sau khi người dùng ngừng gõ
        searchTimer = new Timer(300, e -> performSearch());
        searchTimer.setRepeats(false); // Chỉ chạy 1 lần sau khi ngừng gõ

        // Listener theo dõi thay đổi trong ô tìm kiếm
        searchListener = new DocumentListener() {
            // Khi gõ, set lại trang 1
            @Override public void insertUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void removeUpdate(DocumentEvent e) { searchTimer.restart(); }
            @Override public void changedUpdate(DocumentEvent e) { /* Không dùng cho plain text */ }
        };
        txtTimKiem.getDocument().addDocumentListener(searchListener); // Gắn listener vào ô tìm kiếm
    }

    private void performSearch() {
        // Chạy tìm kiếm trên luồng EDT
        SwingUtilities.invokeLater(this::searchHoaDonRealTime);
    }
    /**
     * Cấu hình giao diện cho bảng (font, màu sắc, chiều cao dòng, độ rộng cột).
     */
    private void setupTableAppearance(JTable table) {
        // Header
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230)); // Màu nền header xám nhạt
        table.getTableHeader().setReorderingAllowed(false); // Không cho kéo thả cột
        // Dòng dữ liệu
        table.setRowHeight(30); // Chiều cao dòng
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setShowGrid(true); // Hiển thị đường kẻ lưới
        table.setGridColor(new Color(230, 230, 230)); // Màu đường kẻ lưới
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 dòng

        // Thiết lập độ rộng ưu tiên cho các cột
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(150); // Thời gian
        tcm.getColumn(1).setPreferredWidth(100); // Mã tham chiếu
        tcm.getColumn(2).setPreferredWidth(100); // Nhân viên
        tcm.getColumn(3).setPreferredWidth(200); // Ghi chú
        tcm.getColumn(4).setPreferredWidth(100); // Thanh toán
        tcm.getColumn(5).setPreferredWidth(100); // Tổng tiền
    }


    /**
     * Gắn sự kiện double-click vào bảng để hiển thị chi tiết hóa đơn.
     */
    private void addTableClickListener() {
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Chỉ xử lý khi double-click
                if (e.getClickCount() == 2) {
                    int selectedRow = tableHoaDon.getSelectedRow(); // Lấy dòng đang chọn
                    if (selectedRow == -1) return; // Nếu không có dòng nào được chọn thì thoát

                    // Kiểm tra index hợp lệ với danh sách đang hiển thị
                    if (dsHoaDonDisplayed == null || selectedRow >= dsHoaDonDisplayed.size()) {
                        System.err.println("Lỗi: Index dòng chọn không hợp lệ hoặc danh sách hiển thị null.");
                        return;
                    }

                    HoaDon selectedHoaDon = dsHoaDonDisplayed.get(selectedRow); // Lấy hóa đơn tương ứng
                    if (selectedHoaDon == null) {
                        System.err.println("Lỗi: Hóa đơn tại dòng " + selectedRow + " bị null.");
                        return;
                    }

                    // Lấy mã đơn đặt hàng để truy vấn chi tiết
                    String maDon = selectedHoaDon.getMaDon();
                    if (maDon == null || maDon.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(HoaDonGUI.this,
                                "Hóa đơn [" + selectedHoaDon.getMaHD() + "] không có Mã Đơn Đặt liên kết.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Lấy danh sách chi tiết từ DAO
                    List<ChiTietHoaDon> chiTietList = chiTietHoaDonDAO.getChiTietTheoMaDon(maDon);
                    // Hiển thị dialog chi tiết
                    showChiTietDialog(selectedHoaDon, chiTietList);
                }
            }
        });
    }

    /**
     * Helper: Truy vấn tên bàn và khu vực từ CSDL.
     */
    private String getTenBanVaKhuVuc(String maDon) {
        String maBan = donDatMonDAO.getMaBanByMaDon(maDon);
        if (maBan == null) return "N/A";

        // Sử dụng getBanByMa(maBan) từ BanDAO bạn đã cung cấp
        Ban ban = banDAO.getBanByMa(maBan);
        if (ban != null) {
            return ban.getTenBan() + " - " + ban.getKhuVuc();
        }
        return maBan;
    }


    /**
     * Hiển thị JDialog chi tiết và thêm nút In (có logic Phiên In).
     * @param hoaDon Hóa đơn cần hiển thị.
     * @param chiTietList Danh sách chi tiết món ăn của hóa đơn đó.
     */
    private void showChiTietDialog(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList) {
        // Kiểm tra nếu không có chi tiết
        if (chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy chi tiết món ăn cho Mã Đơn Đặt: " + hoaDon.getMaDon(),
                    "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String tenKhachHang = "Khách lẻ"; // Mặc định
        String tenBan = "Mang về / Không rõ"; // Mặc định
        String maKHTimDuoc = hoaDon.getMaKH();
        if (maKHTimDuoc == null || maKHTimDuoc.isEmpty()) {
            try {
                dao.DonDatMonDAO ddmDAO = new dao.DonDatMonDAO();
                entity.DonDatMon ddm = ddmDAO.getDonDatMonByMa(hoaDon.getMaDon());
                if (ddm != null) {
                    maKHTimDuoc = ddm.getMaKH();
                }
            } catch (Exception e) {
                System.err.println("Lỗi tìm mã khách hàng từ đơn: " + e.getMessage());
            }
        }

        // 2. Nếu đã có mã khách (từ HĐ hoặc từ Đơn), đi tìm Tên
        if (maKHTimDuoc != null && !maKHTimDuoc.isEmpty()) {
            KhachHangDAO khDAO = new KhachHangDAO();
            entity.KhachHang kh = khDAO.timTheoMaKH(maKHTimDuoc);
            if (kh != null) {
                tenKhachHang = kh.getTenKH();
                // (Tùy chọn) Thêm SĐT nếu muốn chi tiết hơn
                // tenKhachHang += " (" + kh.getSdt() + ")";
            }
        }

        if (hoaDon.getTenBan() != null && !hoaDon.getTenBan().isEmpty()) {
            tenBan = hoaDon.getTenBan();
        }else {
            // B. Lấy tên Bàn (Phức tạp hơn xíu: HoaDon -> DonDatMon -> Ban)
            try {
                dao.DonDatMonDAO ddmDAO = new dao.DonDatMonDAO();
                dao.BanDAO banDAO = new dao.BanDAO();

                // Bạn cần đảm bảo DonDatMonDAO có hàm lấy đơn theo mã (ví dụ: getDonDatMonByMa hoặc timDonDatMon)
                // Nếu chưa có, bạn có thể viết tạm logic lấy mã bàn trực tiếp từ DB ở đây hoặc thêm hàm vào DAO
                entity.DonDatMon ddm = ddmDAO.getDonDatMonByMa(hoaDon.getMaDon());

                if (ddm != null) {
                    tenBan = banDAO.getTenBanByMa(ddm.getMaBan());
                }
            } catch (Exception e) {
                System.err.println("Lỗi lấy thông tin bàn: " + e.getMessage());
            }
        }

        // --- 2. TẠO NỘI DUNG HTML ---
        StringBuilder detailsText = new StringBuilder();
        detailsText.append("<html><body style='font-family: Arial; font-size: 11pt;'>");
        detailsText.append("<h2>Chi Tiết Hóa Đơn: ").append(hoaDon.getMaHD()).append("</h2>");

        detailsText.append("<b>Ngày lập:</b> ").append(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(tableDateFormatter) : "N/A").append("<br>");
        detailsText.append("<b>Mã Đơn Đặt:</b> ").append(hoaDon.getMaDon()).append("<br>");

        // --- CẬP NHẬT: THÊM BÀN VÀ KHÁCH ---
        detailsText.append("<b>Bàn:</b> ").append(tenBan).append("<br>");
        detailsText.append("<b>Khách hàng:</b> ").append(tenKhachHang).append("<br>");
        // ------------------------------------

        // Lấy tên NV từ mã NV
        String tenNV = nhanVienDAO.getTenNhanVienByMa(hoaDon.getMaNV());
        detailsText.append("<b>Nhân viên:</b> ").append(tenNV).append(" (").append(hoaDon.getMaNV()).append(")<br>");
        detailsText.append("<br>");

        // Bảng chi tiết món ăn (HTML Table)
        detailsText.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse:collapse; width:100%; font-size: 10pt;'>");
        detailsText.append("<tr style='background-color:#f0f0f0;'><th>Mã Món</th><th>Tên Món</th><th>Số Lượng</th><th>Đơn Giá</th><th>Thành Tiền</th></tr>");

        float tongTienChiTiet = 0;
        for (ChiTietHoaDon ct : chiTietList) {
            if (ct == null) continue;
            String maMon = ct.getMaMon() != null ? ct.getMaMon() : "N/A";
            String tenMon = ct.getTenMon() != null ? ct.getTenMon() : monAnDAO.getTenMonByMa(maMon); // Ưu tiên tên từ chi tiết, nếu không có mới lấy từ DAO
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

        detailsText.append("<b>Tổng tiền (từ chi tiết): ").append(currencyFormatter.format(tongTienChiTiet)).append("</b><br>");

        // So sánh tổng tiền chi tiết với tổng tiền trên hóa đơn (nếu khác biệt)
        if (Math.abs(tongTienChiTiet - hoaDon.getTongTien()) > 1) { // Cho phép sai số nhỏ
            detailsText.append("<b style='color:red;'>Lưu ý: Tổng tiền chi tiết khác tổng tiền hóa đơn (")
                    .append(currencyFormatter.format(hoaDon.getTongTien())).append(")</b><br>");
        }
        // Hiển thị giảm giá và tổng thanh toán
        detailsText.append("<b>Giảm giá:</b> ").append(currencyFormatter.format(hoaDon.getGiamGia())).append("<br>");
        detailsText.append("<b>Tổng thanh toán:</b> ").append(currencyFormatter.format(hoaDon.getTongThanhToan())).append("<br><br>");


        // Thông tin thanh toán
        detailsText.append("<b>Trạng thái HĐ:</b> ").append(hoaDon.getTrangThai() != null ? hoaDon.getTrangThai() : "N/A").append("<br>");
        detailsText.append("<b>Hình thức TT:</b> ").append(hoaDon.getHinhThucThanhToan() != null ? hoaDon.getHinhThucThanhToan() : "N/A").append("<br>");
        // Chỉ hiển thị tiền khách đưa/thối nếu đã thanh toán
        if ("Đã thanh toán".equalsIgnoreCase(hoaDon.getTrangThai())) {
            detailsText.append("<b>Tiền khách đưa:</b> ").append(currencyFormatter.format(hoaDon.getTienKhachDua())).append("<br>");
            detailsText.append("<b>Tiền thối:</b> ").append(currencyFormatter.format(hoaDon.getTienThoi())).append("<br>");
        }
        detailsText.append("</body></html>");

        // --- Tạo JDialog Tùy chỉnh ---
        JEditorPane editorPane = new JEditorPane("text/html", detailsText.toString());
        editorPane.setEditable(false); // Không cho sửa
        editorPane.setBackground(COLOR_BG_LIGHT); // Màu nền nhạt

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(650, 450)); // Kích thước dialog chi tiết

        JDialog detailDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn " + hoaDon.getMaHD(), Dialog.ModalityType.APPLICATION_MODAL);
        detailDialog.setLayout(new BorderLayout());
        detailDialog.add(scrollPane, BorderLayout.CENTER);

        // Panel Nút Bấm
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.addActionListener(e -> detailDialog.dispose());

        // ⭐ THÊM NÚT IN (ÁP DỤNG PHIÊN IN) ⭐
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


    /**
     * Hiển thị JDialog mô phỏng phiếu in theo cấu trúc BillPanel/xuatPhieuIn.
     */
    private void showPrintPreviewDialog(String title, HoaDon hoaDon, List<ChiTietHoaDon> dsMon) {
        if (hoaDon == null || dsMon == null || dsMon.isEmpty()) return;

        // --- 1. Lấy và định dạng các giá trị tiền tệ từ đối tượng HoaDon
        String tongTienGoc = currencyFormatter.format(hoaDon.getTongTien());
        String giamGia = currencyFormatter.format(hoaDon.getGiamGia());
        String tongThanhToan = currencyFormatter.format(hoaDon.getTongThanhToan());

        // Lấy các giá trị phụ
        String tenNV = nhanVienDAO.getTenNhanVienByMa(hoaDon.getMaNV());
        boolean daThanhToan = "Đã thanh toán".equalsIgnoreCase(hoaDon.getTrangThai());
        String tenBanKhuVuc = getTenBanVaKhuVuc(hoaDon.getMaDon());

        // --- 2. Xây dựng nội dung phiếu in (SỬ DỤNG CẤU TRÚC STRING.FORMAT) ---
        StringBuilder billText = new StringBuilder();

        // --- Header ---
        billText.append("===================================================\n");
        billText.append("                   PHIẾU HÓA ĐƠN\n");
        billText.append("               ").append(title).append("\n");
        billText.append("===================================================\n");
        billText.append("Mã HĐ: ").append(hoaDon.getMaHD()).append("\n");
        billText.append("Ngày:  ").append(hoaDon.getNgayLap().format(billDateFormatter)).append("\n");
        billText.append("Nhân viên: ").append(tenNV).append("\n");
        billText.append("Bàn:   ").append(tenBanKhuVuc).append("\n");
        billText.append("---------------------------------------------------\n");

        // --- Danh sách món ---
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

        // --- Tổng kết ---
        billText.append(String.format("%-28s %20s\n", "Tổng cộng (Gốc):", tongTienGoc));
        if (hoaDon.getGiamGia() > 0) {
            billText.append(String.format("%-28s %20s\n", "Giảm giá:", giamGia));
        }

        billText.append("===================================================\n");
        billText.append(String.format("%-28s %20s\n", "TỔNG THANH TOÁN:", tongThanhToan));

        // --- Phần thêm cho Hóa đơn đã thanh toán ---
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

        // --- 2. Hiển thị JDialog ---
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

    /**
     * ⭐ SỬA LỖI QUAN TRỌNG: Hàm xuất Excel lấy TẤT CẢ dữ liệu đã được lọc (không phân trang).
     */
    private void exportDataToExcel() {
        // 1. Lấy các điều kiện lọc hiện tại
        String trangThai = getSelectedTrangThaiFilter();
        String keyword = currentKeyword;
        LocalDateTime[] dates = getFilterDates();
        LocalDateTime tuNgay = dates != null ? dates[0] : null;
        LocalDateTime denNgay = dates != null ? dates[1] : null;

        // 2. ⭐ GỌI DAO ĐỂ LẤY TOÀN BỘ DANH SÁCH KHÔNG PHÂN TRANG ⭐
        // *** TẠI ĐÂY, TA GIẢ ĐỊNH HOA DON DAO CÓ HÀM `getAllHoaDonFiltered` ***
        // Nếu không có, bạn cần tự thêm hàm này vào HoaDonDAO (tham khảo code ở bước 2)
        List<HoaDon> listToExport = hoaDonDAO.getAllHoaDonFiltered(trangThai, keyword, tuNgay, denNgay);

        if (listToExport == null || listToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu hóa đơn thỏa mãn điều kiện lọc để xuất.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Mở hộp thoại chọn nơi lưu file ---
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");

        // ⭐ ĐẶT TÊN FILE DỰA TRÊN BỘ LỌC NGÀY ⭐
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

        int userSelection = fileChooser.showSaveDialog(this); // Hiển thị hộp thoại lưu

        // Nếu người dùng chọn "Save"
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            // Đảm bảo file có đuôi .xlsx
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            // --- Gọi lớp ExcelExporter để thực hiện xuất ---
            ExcelExporter exporter = new ExcelExporter();
            boolean success = exporter.exportToExcel(listToExport, filePath); // Gọi hàm xuất

            // Thông báo kết quả
            if (success) {
                JOptionPane.showMessageDialog(this, "Xuất hóa đơn thành công tại:\n" + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}