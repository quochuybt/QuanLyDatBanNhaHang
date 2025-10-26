package gui;

import dao.MonAnDAO; // Giả sử bạn sẽ tạo DAO này
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener; // Thêm
import java.awt.event.MouseAdapter;  // Thêm
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dao.HoaDonDAO; // Thêm import HoaDonDAO

import javax.swing.table.TableColumn;

public class ManHinhGoiMonGUI extends JPanel {
    private Ban banHienTai; // Thêm biến để lưu bàn đang được hiển thị
    private HoaDonDAO hoaDonDAO_GoiMon; // Dùng instance DAO riêng nếu cần
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Panel bên trái
    private MonAnDAO monAnDAO;
    private List<MonAn> dsMonAnFull; // Danh sách TẤT CẢ món ăn (để lọc)
    private List<MonAnItemPanel> dsMonAnPanel; // Thêm: Danh sách các panel item
    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private String currentCategoryFilter = "Tất cả";
    private JLabel statusColorBox;

    // Panel bên phải
    private JLabel lblTenBanHeader;
    private JTable tblChiTietHoaDon;
    private DefaultTableModel modelChiTietHoaDon;
    private BillPanel billPanel; // TÁI SỬ DỤNG BILLPANEL CỦA BẠN

    public ManHinhGoiMonGUI() {
        super(new BorderLayout());
        this.monAnDAO = new MonAnDAO();
        this.dsMonAnFull = new ArrayList<>(); // Khởi tạo list
        this.hoaDonDAO_GoiMon = new HoaDonDAO();
        this.dsMonAnPanel = new ArrayList<>(); // Khởi tạo list
        buildUI();
        loadDataFromDB();
        xoaThongTinGoiMon();
    }
    public void loadDuLieuBan(Ban banDuocChon) {
        this.banHienTai = banDuocChon; // Lưu lại bàn hiện tại

        // 1. Cập nhật Header
        lblTenBanHeader.setText(banDuocChon.getTenBan() + " - " + banDuocChon.getKhuVuc());

        Color statusColor;
        switch (banDuocChon.getTrangThai()) {
            case TRONG: // Mặc dù ít khi gọi món cho bàn trống, nhưng để đủ case
                statusColor = ManHinhBanGUI.COLOR_STATUS_FREE;
                break;
            case DA_DAT_TRUOC:
                statusColor = ManHinhBanGUI.COLOR_STATUS_RESERVED;
                break;
            case DANG_PHUC_VU:
            default:
                statusColor = ManHinhBanGUI.COLOR_STATUS_OCCUPIED;
                break;
        }
        statusColorBox.setBackground(statusColor);
        // 2. Xóa chi tiết đơn hàng cũ
        modelChiTietHoaDon.setRowCount(0);

        // 3. Tìm hóa đơn chưa thanh toán hiện tại của bàn này
        HoaDon activeHoaDon = null;
        // Chỉ tìm hóa đơn nếu bàn đang phục vụ
        if (banDuocChon.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            activeHoaDon = hoaDonDAO_GoiMon.getHoaDonChuaThanhToan(banDuocChon.getMaBan());
        }
        // TODO: Xử lý trường hợp bàn DA_DAT_TRUOC nếu cần (tải DonDatMon?)

        // 4. Tải các món nếu tìm thấy hóa đơn
        if (activeHoaDon != null) {
            System.out.println("Đang tải hóa đơn: " + activeHoaDon.getMaHD() + " cho bàn " + banDuocChon.getMaBan());
            List<ChiTietHoaDon> dsChiTiet = activeHoaDon.getDsChiTiet();
            if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                for (ChiTietHoaDon ct : dsChiTiet) {
                    // Thêm dòng vào JTable
                    Object[] rowData = {
                            "X",                // Cột 0: Nút xóa
                            ct.getMaMon(),      // Cột 1: Mã Món (Ẩn)
                            ct.getTenMon(),     // Cột 2: Tên Món
                            Integer.valueOf(ct.getSoluong()),    // Cột 3: Số lượng
                            ct.getDongia(),     // Cột 4: Đơn giá
                            ct.getThanhtien()   // Cột 5: Thành tiền
                    };
                    modelChiTietHoaDon.addRow(rowData);
                }
            } else {
                System.out.println("Hóa đơn " + activeHoaDon.getMaHD() + " không có chi tiết hoặc danh sách chi tiết rỗng.");
            }
        } else {
            System.out.println("Không tìm thấy hóa đơn chưa thanh toán cho bàn " + banDuocChon.getMaBan() + " hoặc bàn không ở trạng thái Đang phục vụ.");
            // Có thể hiển thị thông báo cho người dùng ở đây nếu muốn
        }

        // 5. Cập nhật BillPanel (ngay cả khi không có hóa đơn, tổng sẽ là 0)
        updateBillPanelTotals();
    }
    private void xoaThongTinGoiMon() {
        lblTenBanHeader.setText("Chưa chọn bàn");
        modelChiTietHoaDon.setRowCount(0);
        billPanel.clearBill();
        this.banHienTai = null;
        if (statusColorBox != null) { // Kiểm tra null phòng trường hợp gọi trước khi buildUI xong
            statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        }
    }
    private void addMonAnToOrder(MonAn monAn) {
        if (banHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn trước khi gọi món!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Chỉ cho phép thêm món nếu bàn đang phục vụ (hoặc trạng thái hợp lệ khác)
        // if (banHienTai.getTrangThai() != TrangThaiBan.DANG_PHUC_VU) {
        //    JOptionPane.showMessageDialog(this, "Không thể thêm món cho bàn ở trạng thái này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        //    return;
        // }

        String maMon = monAn.getMaMonAn();
        String tenMon = monAn.getTenMon();
        float donGia = monAn.getDonGia();

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            String maMonTrongBang = (String) modelChiTietHoaDon.getValueAt(i, 1);
            if (maMon.equals(maMonTrongBang)) {
                int soLuongHienTai = (int) modelChiTietHoaDon.getValueAt(i, 3);
                int soLuongMoi = soLuongHienTai + 1;
                modelChiTietHoaDon.setValueAt(soLuongMoi, i, 3);
                float thanhTienMoi = soLuongMoi * donGia;
                modelChiTietHoaDon.setValueAt(thanhTienMoi, i, 5); // Cập nhật thành tiền
                updateBillPanelTotals();
                return;
            }
        }

        Object[] rowData = {
                "X",
                maMon,
                tenMon,
                Integer.valueOf(1),
                donGia,
                donGia
        };
        modelChiTietHoaDon.addRow(rowData);
        updateBillPanelTotals();

        // TODO: Sau khi thêm món, cần LƯU thay đổi vào CSDL
        // (Tạo ChiTietHoaDon mới hoặc cập nhật số lượng)
    }
    private void updateBillPanelTotals() {
        long tongCong = 0;
        int tongSoLuong = 0;

        for (int i = 0; i < modelChiTietHoaDon.getRowCount(); i++) {
            // Lấy Số lượng từ cột 3
            Object slObj = modelChiTietHoaDon.getValueAt(i, 3);
            int soLuong = (slObj instanceof Integer) ? (Integer) slObj : 0; // Chuyển đổi cẩn thận

            // Lấy Thành tiền từ cột 5
            Object ttObj = modelChiTietHoaDon.getValueAt(i, 5);
            float thanhTien = 0;
            if (ttObj instanceof Float) {
                thanhTien = (Float) ttObj;
            } else if (ttObj instanceof Double) {
                thanhTien = ((Double) ttObj).floatValue();
            } else if (ttObj instanceof Number) {
                thanhTien = ((Number) ttObj).floatValue();
            } else {
                System.err.println("Lỗi kiểu dữ liệu cột thành tiền ở hàng " + i + ": " + (ttObj != null ? ttObj.getClass().getName() : "null"));
            }


            tongCong += Math.round(thanhTien);
            tongSoLuong += soLuong;
        }

        long khuyenMai = 0; // TODO: Lấy từ HoaDon nếu có
        long vat = 0;       // TODO: Tính VAT
        long tongThanhToan = tongCong - khuyenMai + vat;

        billPanel.loadBillTotals(tongCong, khuyenMai, vat, tongThanhToan, tongSoLuong);
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        // 1. Panel bên trái (Menu)
        JPanel pnlLeft = createMenuPanel();

        // 2. Panel bên phải (Hóa đơn)
        JPanel pnlRight = createOrderPanel();

        // 3. Tạo JSplitPane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                pnlLeft,
                pnlRight
        );
        splitPane.setDividerLocation(520); // Sửa lại vị trí chia nếu cần
        splitPane.setBorder(null);

        this.add(splitPane, BorderLayout.CENTER);

        // Bỏ loadDataFromDB() ở đây, chuyển lên constructor
    }
    private void loadDataFromDB() {
        // 1. Tải danh sách từ DAO
        this.dsMonAnFull = monAnDAO.getAllMonAn();
        System.out.println("Đã tải " + dsMonAnFull.size() + " món ăn từ CSDL."); // Debug

        // 2. Tạo các Panel Item và thêm vào container
        pnlMenuItemContainer.removeAll(); // Xóa các item cũ (nếu có)
        dsMonAnPanel.clear(); // Xóa list panel cũ

        if (dsMonAnFull.isEmpty()) {
            pnlMenuItemContainer.add(new JLabel("Không có món ăn nào trong CSDL."));
        } else {
            for (MonAn mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

                // --- THÊM SỰ KIỆN CLICK VÀO MÓN ĂN ---
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Chỉ xử lý click chuột trái
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            System.out.println("Clicked on: " + itemPanel.getMonAn().getTenMon()); // Debug
                             addMonAnToOrder(itemPanel.getMonAn()); // Sẽ thêm hàm này ở bước sau
                        }
                    }
                });
                // ----------------------------------------

                dsMonAnPanel.add(itemPanel); // Thêm vào list để quản lý filter
                pnlMenuItemContainer.add(itemPanel); // Thêm vào panel để hiển thị
            }
        }

        // Lọc hiển thị theo danh mục mặc định ban đầu
        filterMonAn();

        // Cập nhật lại giao diện panel cuộn
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }
    private void filterMonAn() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        System.out.println("Filtering: Category='" + currentCategoryFilter + "', Keyword='" + tuKhoa + "'"); // Debug

        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true; // Mặc định là hiển thị

            // 1. Lọc theo Danh mục (currentCategoryFilter là mã DM)
            if (!currentCategoryFilter.equals("Tất cả")) {
                // Nếu mã DM của món không khớp với filter đang chọn -> ẩn
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) {
                    show = false;
                }
            }

            // 2. Lọc theo Từ khóa (chỉ lọc nếu show vẫn là true)
            if (show && !tuKhoa.isEmpty()) {
                // Nếu tên món không chứa từ khóa -> ẩn
                if (!mon.getTenMon().toLowerCase().contains(tuKhoa)) {
                    show = false;
                }
            }

            itemPanel.setVisible(show); // Ẩn/Hiện panel tương ứng
        }
        // Cập nhật lại layout sau khi ẩn/hiện
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    /**
     * Tạo Panel Menu bên trái
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Giảm khoảng cách dọc
//        panel.setBorder(new EmptyBorder(10, 10, 10, 5)); // Lề xung quanh
        panel.setBackground(Color.WHITE);

        // 1. NORTH: Bộ lọc (Category + Search)
        JPanel pnlFilter = new JPanel(new BorderLayout(0, 5)); // Khoảng cách giữa category và search
        pnlFilter.setOpaque(false);
        pnlFilter.add(createCategoryFilterPanel(), BorderLayout.NORTH); // Các nút category
        pnlFilter.add(createSearchPanel(), BorderLayout.SOUTH); // Ô tìm kiếm
        panel.add(pnlFilter, BorderLayout.NORTH);

        // 2. CENTER: Danh sách món ăn
        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 15, 15)); // Khoảng cách giữa các món
        pnlMenuItemContainer.setBackground(Color.WHITE);
        pnlMenuItemContainer.setBorder(new EmptyBorder(10, 10, 10, 10)); // Lề trong panel món ăn

        JScrollPane scrollPane = new JScrollPane(pnlMenuItemContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Tăng tốc độ cuộn chuột
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Bỏ item giả ở đây, sẽ được load từ DB
        // pnlMenuItemContainer.add(new JLabel("Món ăn 1 (placeholder)"));

        return panel;
    }

    /**
     * Tạo Panel Hóa đơn bên phải
     */
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); // Giảm khoảng cách dọc
        panel.setBorder(new EmptyBorder(10, 5, 10, 10)); // Lề xung quanh
        panel.setBackground(Color.WHITE);

        // 1. NORTH: Header (Tên bàn) - Đã bỏ nút "..."
        panel.add(createOrderHeaderPanel(), BorderLayout.NORTH);

        // 2. SOUTH: Panel thanh toán (Tái sử dụng BillPanel)
        this.billPanel = new BillPanel();
        panel.add(billPanel, BorderLayout.SOUTH);

        // 3. CENTER: Bảng chi tiết hóa đơn
        String[] cols = {"X", "Mã Món", "Tên món", "SL", "Đơn giá", "Thành tiền"}; // Giữ nguyên 6 cột
        modelChiTietHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho sửa cột SL (giờ là cột 3)
                return column == 0|| column == 3;
            }
            // ... (getColumnClass giữ nguyên)
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return String.class;     // X
                    case 1: return String.class;     // Mã Món (ẩn)
                    case 2: return String.class;     // Tên Món
                    case 3: return Integer.class;    // SL
                    case 4: return Float.class;      // Đơn giá
                    case 5: return Float.class;      // Thành tiền
                    default: return Object.class;
                }
            }
        };
        tblChiTietHoaDon = new JTable(modelChiTietHoaDon);
        TableColumn columnX = tblChiTietHoaDon.getColumnModel().getColumn(0);
        columnX.setCellRenderer(new ButtonRenderer()); // Gọi inner class
        // Sửa: Không cần truyền callback vào ButtonEditor
        columnX.setCellEditor(new ButtonEditor(new JCheckBox()));

        TableColumn columnSL = tblChiTietHoaDon.getColumnModel().getColumn(3);
        columnSL.setCellRenderer(new SpinnerRenderer()); // Gọi inner class
        // Sửa: Không cần truyền callback vào SpinnerEditor
        columnSL.setCellEditor(new SpinnerEditor());

        // Cấu hình cột (Giữ nguyên)
        tblChiTietHoaDon.setRowHeight(30);
        // ... (Code ẩn cột Mã Món và set chiều rộng giữ nguyên)
        TableColumn colMaMon = tblChiTietHoaDon.getColumnModel().getColumn(1);
        colMaMon.setMinWidth(0);
        colMaMon.setMaxWidth(0);
        colMaMon.setPreferredWidth(0);

        tblChiTietHoaDon.getColumnModel().getColumn(0).setPreferredWidth(30);  // X
        tblChiTietHoaDon.getColumnModel().getColumn(2).setPreferredWidth(150); // Tên món
        tblChiTietHoaDon.getColumnModel().getColumn(3).setPreferredWidth(50);  // SL
        tblChiTietHoaDon.getColumnModel().getColumn(4).setPreferredWidth(80);  // Đơn giá
        tblChiTietHoaDon.getColumnModel().getColumn(5).setPreferredWidth(90);  // Thành tiền

        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    // Định dạng số thành tiền tệ Việt Nam
                    value = nf.format(((Number) value).doubleValue());
                }
                setHorizontalAlignment(JLabel.RIGHT); // Căn phải
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        tblChiTietHoaDon.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        tblChiTietHoaDon.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);
        JScrollPane scrollPane = new JScrollPane(tblChiTietHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // --- CÁC HÀM HELPER (Tạm thời) ---

    private JPanel createCategoryFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Khoảng cách giữa các nút
        filterPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();

        // Dữ liệu Danh mục (lấy từ CSDL của bạn)
        // Cần thêm "Tất cả" vào đầu
        String[][] categories = {
                {"Tất cả", "Tất cả"}, // Thêm "Tất cả"
                {"DM0001", "Món ăn"},
                {"DM0002", "Giải khát"},
                {"DM0003", "Rượu vang"}
        };

        ActionListener filterListener = e -> {
            String selectedCategory = e.getActionCommand();
            currentCategoryFilter = selectedCategory;
            filterMonAn(); // Gọi hàm lọc khi chọn category
        };

        for (int i = 0; i < categories.length; i++) {
            String maDM = categories[i][0];
            String tenDM = categories[i][1];

            // Nút đầu tiên ("Tất cả") được chọn mặc định
            JToggleButton button = createFilterButton(tenDM, i == 0); // Sửa: Chọn nút đầu tiên
            button.setActionCommand(maDM); // Action command là Mã DM
            button.addActionListener(filterListener);
            group.add(button);
            filterPanel.add(button);
        }
        return filterPanel;
    }
    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(5, 15, 5, 15)); // Padding
        button.setContentAreaFilled(false);
        button.setOpaque(true); // Để thấy màu nền
        // Style nút ban đầu
        if (selected) {
            button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            // Thêm viền xám nhạt cho nút không được chọn
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(4, 14, 4, 14) // Padding nhỏ hơn 1px vì có viền
            ));
        }
        // Style khi trạng thái selected thay đổi
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15)); // Bỏ viền khi chọn
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)
                ));
            }
        });
        // Set trạng thái ban đầu (quan trọng)
        button.setSelected(selected);
        return button;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0)); // Khoảng cách icon và textfield
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Lề trên

        // Icon tìm kiếm (dùng ký tự Unicode)
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16)); // Font hỗ trợ ký tự đặc biệt
        panel.add(searchIcon, BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setPreferredSize(new Dimension(0, 35)); // Chiều cao ô tìm kiếm

        // Thêm sự kiện gõ phím để lọc
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterMonAn(); // Lọc mỗi khi gõ phím
            }
        });

        panel.add(txtTimKiem, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrderHeaderPanel() {
        // Sử dụng BorderLayout để đặt ô màu bên trái, tên bàn ở giữa
        JPanel panel = new JPanel(new BorderLayout(15, 0)); // Thêm khoảng cách ngang 15px
        panel.setOpaque(false);
        // Lề dưới cho header
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // --- TẠO Ô MÀU ---
        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48)); // Kích thước giống bên ManHinhBanGUI
        // Đặt màu mặc định (ví dụ: màu trống)
        statusColorBox.setBackground(ManHinhBanGUI.COLOR_STATUS_FREE);
        statusColorBox.setOpaque(true);
        // --- KẾT THÚC TẠO Ô MÀU ---

        // Tên bàn (giữ nguyên)
        lblTenBanHeader = new JLabel("Chưa chọn bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Có thể tăng font size

        // Thêm ô màu vào bên TRÁI
        panel.add(statusColorBox, BorderLayout.WEST);
        // Thêm tên bàn vào GIỮA
        panel.add(lblTenBanHeader, BorderLayout.CENTER);

        return panel;
    }
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.RED);
            setBackground(Color.WHITE);
            setBorder(null);
            setText("X");
            setFont(new Font("Arial", Font.BOLD, 14));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int editingRow;
        private JTable table;
        // Không cần callback vì có thể gọi trực tiếp updateBillPanelTotals()
        // private Runnable updateBillCallback;

        public ButtonEditor(JCheckBox checkBox) { // Bỏ callback khỏi constructor
            super(checkBox);
            // this.updateBillCallback = updateBillCallback; // Bỏ dòng này
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.RED);
            button.setBackground(Color.WHITE);
            button.setBorder(null);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && table != null) {
                final DefaultTableModel finalModel = (DefaultTableModel) table.getModel();
                final int rowToRemove = editingRow;
                // Kiểm tra dòng hợp lệ trước khi xóa
                SwingUtilities.invokeLater(() -> {
                    // Kiểm tra lại index một lần nữa trước khi xóa (phòng ngừa)
                    if (rowToRemove >= 0 && rowToRemove < finalModel.getRowCount()) {
                        finalModel.removeRow(rowToRemove);
                        updateBillPanelTotals(); // Gọi cập nhật sau khi xóa
                    } else {
                        System.err.println("ButtonEditor (invokeLater): Lỗi index dòng khi xóa: " + rowToRemove);
                    }
                });
            }
            isPushed = false;
            editingRow = -1; // Reset dòng ngay sau khi xử lý
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false; // Đặt lại flag
            boolean stopped = super.stopCellEditing(); // Gọi hàm gốc
            editingRow = -1; // Reset dòng đang sửa SAU KHI dừng
            return stopped;
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // --- Inner class cho Spinner Số lượng ---
    class SpinnerRenderer extends JSpinner implements TableCellRenderer {
        public SpinnerRenderer() {
            super(new SpinnerNumberModel(1, 1, 100, 1));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(null);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setValue(value);
            } else {
                setValue(1);
            }
            return this;
        }
    }

    class SpinnerEditor extends DefaultCellEditor {
        JSpinner spinner;
        JSpinner.DefaultEditor editor;
        JTextField textField;
        boolean valueSet;
        private int editingRow = -1; // Khởi tạo -1
        private JTable table;
        // private Runnable updateBillCallback; // Bỏ callback

        public SpinnerEditor() { // Bỏ callback
            super(new JTextField());
            // this.updateBillCallback = updateBillCallback; // Bỏ dòng này

            spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            editor = ((JSpinner.DefaultEditor) spinner.getEditor());
            textField = editor.getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setBorder(null);
            spinner.setBorder(null);

            spinner.addChangeListener(e -> {
                if (table != null && editingRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    // Đảm bảo row index còn hợp lệ sau khi có thể đã xóa dòng khác
                    if (editingRow < model.getRowCount()) {
                        int currentQuantity = (Integer) spinner.getValue();
                        float donGia = (Float) model.getValueAt(editingRow, 4);
                        float thanhTienMoi = currentQuantity * donGia;

                        SwingUtilities.invokeLater(() -> {
                            // Kiểm tra lại row index trước khi set
                            if (editingRow < model.getRowCount()) {
                                model.setValueAt(thanhTienMoi, editingRow, 5);
                                updateBillPanelTotals(); // Gọi trực tiếp
                            }
                        });
                    }
                    fireEditingStopped(); // Gọi sau khi tính toán xong
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row;
            // valueSet = false; // Có thể không cần flag này
            spinner.setValue(value); // Luôn đặt giá trị khi bắt đầu edit
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public boolean stopCellEditing() {
            try {
                // Cố gắng commit giá trị cuối cùng người dùng nhập (nếu họ gõ tay)
                editor.commitEdit();
                spinner.commitEdit(); // Đảm bảo giá trị spinner được cập nhật
            } catch (java.text.ParseException e) {
                // Xử lý lỗi nếu nhập không phải số - ví dụ: reset về giá trị cũ
                // Hoặc hiển thị thông báo lỗi
                Object oldValue = spinner.getValue(); // Lấy giá trị hợp lệ cuối cùng
                spinner.setValue(oldValue); // Đặt lại giá trị cũ
                // JOptionPane.showMessageDialog(spinner, "Vui lòng nhập số.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                // return false; // Ngăn không cho dừng edit nếu giá trị sai
            }
            editingRow = -1; // Reset dòng đang sửa khi dừng edit
            // valueSet = false; // Reset flag nếu dùng
            return super.stopCellEditing();
        }
    }
}