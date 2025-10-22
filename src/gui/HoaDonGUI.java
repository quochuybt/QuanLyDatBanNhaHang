package gui;

import dao.HoaDonDAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HoaDonGUI extends JPanel {
    private final HoaDonDAO hoaDonDAO;
    private final JTable tableHoaDon;
    private final DefaultTableModel tableModel;
    private final JTabbedPane tabbedPane;
    private JTextField txtTimKiem;

    private static final Color COLOR_BG_LIGHT = new Color(244, 247, 252);
    // Vẫn giữ các cột này để khớp với thiết kế giao diện (cột NV, Ghi chú sẽ là dữ liệu mô phỏng/tổng hợp)
    private final String[] columnNames = {"Thời gian thanh toán", "Mã tham chiếu", "Nhân viên", "Ghi chú", "Thanh toán", "Tổng tiền"};

    public HoaDonGUI() {
        // Khởi tạo DAO để lấy dữ liệu từ CSDL
        this.hoaDonDAO = new HoaDonDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- 1. Header (Tiêu đề + Nút Export) ---
        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- 2. Nội dung chính (Tab và Bảng) ---
        tabbedPane = createTabbedPane();

        // Khởi tạo model và bảng
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableHoaDon = new JTable(tableModel);
        setupTable(tableHoaDon);

        // Thêm các tab
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        tabbedPane.addTab("Tất cả hóa đơn", createTablePanel(scrollPane));
        // Sử dụng lại scrollPane vì dữ liệu được load dynamically vào cùng một tableModel
        tabbedPane.addTab("Đã thanh toán", createTablePanel(scrollPane));
        tabbedPane.addTab("Chờ xác nhận thanh toán", createTablePanel(scrollPane));

        // Thêm sự kiện chuyển tab
        tabbedPane.addChangeListener(e -> loadDataForSelectedTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Load dữ liệu lần đầu tiên (Tab "Tất cả hóa đơn")
        loadDataToTable(hoaDonDAO.getAllHoaDon());
    }

    /**
     * Tạo Panel chứa tiêu đề và nút Xuất hóa đơn.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // 1. Tiêu đề
        JLabel titleLabel = new JLabel("Hóa đơn");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.WEST);

        // 2. Nút Xuất hóa đơn (Với Icon Image và Text)

        // 💡 BƯỚC 1: Tải Icon Excel
        // Đảm bảo file "excel_icon.png" nằm trong thư mục resources hoặc cùng cấp với class file
        // Ví dụ: new ImageIcon(getClass().getResource("/images/excel_icon.png"));
        // Tôi sẽ dùng đường dẫn tương đối, bạn cần đặt file ảnh phù hợp.
        ImageIcon originalIcon = null;
        try {
            originalIcon = new ImageIcon(getClass().getResource("/img/icon_excel/excel.png")); // Đổi đường dẫn nếu cần
            if (originalIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.err.println("Lỗi tải ảnh Excel: Đảm bảo đường dẫn và file ảnh đúng.");
                // Fallback nếu ảnh không tải được
                originalIcon = null;
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy file icon Excel: " + e.getMessage());
            originalIcon = null; // Đặt null để xử lý nếu không có icon
        }

        // 💡 BƯỚC 2: Tạo JButton và tùy chỉnh
        JButton btnExport = new JButton("Xuất hóa đơn");

        if (originalIcon != null) {
            // Thay đổi kích thước icon để phù hợp (ví dụ: cao 24px)
            // Kích thước nút trong ảnh trông khá nhỏ, icon có thể cần được scale
            Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            btnExport.setIcon(scaledIcon);

            // Đặt vị trí icon ở bên trái của text
            btnExport.setHorizontalTextPosition(SwingConstants.RIGHT);
            btnExport.setVerticalTextPosition(SwingConstants.CENTER);
            btnExport.setIconTextGap(8); // Khoảng cách giữa icon và text
        } else {
            // Nếu không tải được icon, chỉ hiển thị text
            btnExport.setText("Xuất hóa đơn (Lỗi tải icon)");
        }

        btnExport.setBackground(new Color(0, 150, 60)); // Màu nền xanh lá
        btnExport.setForeground(Color.WHITE); // Màu chữ
        btnExport.setFont(new Font("Arial", Font.BOLD, 14)); // Font của text
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tạo border màu xanh lá nhạt hơn cho nút
        btnExport.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 180, 80), 1), // Border ngoài (màu xanh lá nhạt)
                new EmptyBorder(8, 15, 8, 15) // Padding nội dung
        ));
        btnExport.setContentAreaFilled(true); // Đảm bảo màu nền được tô

        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Xuất hóa đơn chưa được triển khai!", "Thông báo", JOptionPane.INFORMATION_MESSAGE));

        panel.add(btnExport, BorderLayout.EAST);

        return panel;
    }

    /**
     * Tạo JTabbedPane cho các trạng thái hóa đơn.
     */
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabPane.setBackground(Color.WHITE);
        return tabPane;
    }

    /**
     * Tạo Panel chính chứa thanh tìm kiếm và bảng.
     */
    private JPanel createTablePanel(JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // 1. Panel tìm kiếm
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        searchPanel.setOpaque(false);

        txtTimKiem = new JTextField(" Tìm kiếm qua mã hóa đơn/ tên đơn hàng");
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setForeground(Color.GRAY);
        txtTimKiem.setPreferredSize(new Dimension(0, 35));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Xử lý Placeholder
        txtTimKiem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (txtTimKiem.getText().trim().equals(" Tìm kiếm qua mã hóa đơn/ tên đơn hàng")) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(Color.BLACK);
                }
            }
        });
        txtTimKiem.addActionListener(e -> searchHoaDon(txtTimKiem.getText()));

        // Icon tìm kiếm
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        JPanel inputWrapper = new JPanel(new BorderLayout(5, 0));
        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(txtTimKiem, BorderLayout.CENTER);
        searchPanel.add(inputWrapper, BorderLayout.CENTER);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 2. Bảng hóa đơn
        // Lưu ý: Chỉ thêm scrollPane vào panel, không thêm table trực tiếp nhiều lần
        if (scrollPane.getParent() == null) {
            panel.add(scrollPane, BorderLayout.CENTER);
        }

        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        // 3. Phân trang
        JLabel lblPagination = new JLabel("1 / X trang"); // Cần cập nhật động
        lblPagination.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(lblPagination, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Cài đặt các thuộc tính hiển thị cho bảng.
     */
    private void setupTable(JTable table) {
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Cài đặt chiều rộng cột
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(150); // Thời gian
        tcm.getColumn(1).setPreferredWidth(100); // Mã
        tcm.getColumn(2).setPreferredWidth(100); // Nhân viên (Mô phỏng)
        tcm.getColumn(3).setPreferredWidth(200); // Ghi chú (Mô phỏng)
        tcm.getColumn(4).setPreferredWidth(100); // Thanh toán
        tcm.getColumn(5).setPreferredWidth(100); // Tổng tiền
    }

    /**
     * Load dữ liệu từ List<HoaDon> vào JTable.
     */
    private void loadDataToTable(List<HoaDon> list) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (HoaDon hd : list) {
            String maThamChieu = hd.getMaHD();

            // 💡 MÔ PHỎNG DỮ LIỆU KHÔNG CÓ TRONG ENTITY HOA DON:
            // Tên Nhân viên (Trong thực tế cần lấy từ DonDatMon hoặc NhanVienDAO)
            String tenNV_Moc = (maThamChieu.hashCode() % 2 == 0) ? "Huỳnh Quốc Huy" : "Nguyễn Văn A";

            // Ghi chú (Trong thực tế cần lấy từ DonDatMon hoặc ChiTietHoaDon)
            String ghiChu = "Không";
            if (hd.getTongTien() > 1000000) ghiChu = "Yêu cầu xuất VAT";
            else if (hd.getHinhThucThanhToan().equals("Chuyển khoản")) ghiChu = "Đã xác nhận";

            tableModel.addRow(new Object[]{
                    hd.getNgayLap().format(formatter),
                    maThamChieu,
                    tenNV_Moc,
                    ghiChu,
                    hd.getHinhThucThanhToan(),
                    String.format("%,.0f ₫", hd.getTongTien())
            });
        }
    }

    /**
     * Lọc dữ liệu hiển thị dựa trên tab đang chọn.
     */
    private void loadDataForSelectedTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        List<HoaDon> allList = hoaDonDAO.getAllHoaDon(); // Lấy tất cả từ CSDL
        List<HoaDon> list;

        switch (selectedIndex) {
            case 0: // Tất cả hóa đơn
                list = allList;
                break;
            case 1: // Đã thanh toán
                list = allList.stream()
                        .filter(hd -> hd.getTrangThai().equals("Đã thanh toán"))
                        .collect(Collectors.toList());
                break;
            case 2: // Chờ xác nhận thanh toán (Chưa thanh toán & Chuyển khoản)
                list = allList.stream()
                        .filter(hd -> hd.getTrangThai().equals("Chưa thanh toán") && hd.getHinhThucThanhToan().equals("Chuyển khoản"))
                        .collect(Collectors.toList());
                break;
            default:
                list = allList;
        }

        loadDataToTable(list);
    }

    /**
     * Tìm kiếm hóa đơn dựa trên từ khóa.
     */
    private void searchHoaDon(String query) {
        if (query.equals(" Tìm kiếm qua mã hóa đơn/ tên đơn hàng") || query.trim().isEmpty()) {
            loadDataForSelectedTab();
            return;
        }

        // Tìm kiếm theo Mã HD (theo HoaDonDAO mới)
        List<HoaDon> searchResult = hoaDonDAO.timHoaDon(query.trim());

        // Nếu muốn mô phỏng tìm kiếm theo tên NV (dù không có trong DAO):
        /*
        List<HoaDon> fullList = hoaDonDAO.getAllHoaDon();
        searchResult = fullList.stream()
                .filter(hd -> hd.getMaHD().toLowerCase().contains(query.toLowerCase()) ||
                              "Huỳnh Quốc Huy".toLowerCase().contains(query.toLowerCase())) // Giả định tên
                .collect(Collectors.toList());
        */

        loadDataToTable(searchResult);
    }
}