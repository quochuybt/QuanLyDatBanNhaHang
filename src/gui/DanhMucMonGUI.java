package gui;

import dao.DanhMucMonDAO; // DAO mới để lấy danh mục
import dao.MonAnDAO;
import entity.DanhMucMon; // Entity mới
import entity.MonAn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonGUI extends JPanel {

    // --- Components ---
    private JPanel pnlMenuItemContainer; // Panel chứa các MonAnItemPanel
    private JTextField txtTimKiem;
    private JScrollPane scrollPane;
    private JPanel filterButtonPanel; // Panel chứa các nút lọc

    // --- Data & DAO ---
    private MonAnDAO monAnDAO;
    private DanhMucMonDAO danhMucMonDAO;
    private List<MonAn> dsMonAnFull; // Danh sách tất cả món ăn
    private List<MonAnItemPanel> dsMonAnPanel; // Danh sách các panel item
    private String currentCategoryFilter = "Tất cả"; // Mã DM đang lọc
    private String currentKeywordFilter = "";      // Từ khóa đang lọc

    // --- Constants ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public DanhMucMonGUI() {
        // Khởi tạo DAO
        this.monAnDAO = new MonAnDAO();
        this.danhMucMonDAO = new DanhMucMonDAO(); // Khởi tạo DAO danh mục

        // Khởi tạo List
        this.dsMonAnFull = new ArrayList<>();
        this.dsMonAnPanel = new ArrayList<>();

        // Cấu hình Panel chính
        setLayout(new BorderLayout(10, 15)); // Khoảng cách
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20)); // Padding

        // --- Tạo giao diện ---
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMenuPanel(), BorderLayout.CENTER);

        // --- Tải dữ liệu ---
        SwingUtilities.invokeLater(() -> {
            loadFilterButtons(); // Tải các nút lọc danh mục
            loadDataFromDB();    // Tải danh sách món ăn
        });
    }

    /**
     * Tạo panel header chứa tiêu đề và ô tìm kiếm.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh mục Món ăn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // --- Ô tìm kiếm ---
        JPanel searchWrapper = new JPanel(new BorderLayout(5, 0));
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(300, 38)); // Kích thước ô tìm kiếm

        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        searchWrapper.add(searchIcon, BorderLayout.WEST);

        txtTimKiem = new JTextField("Tìm theo tên món...");
        txtTimKiem.setForeground(Color.GRAY);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(5, 8, 5, 8)
        ));

        // Placeholder handler
        txtTimKiem.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().equals("Tìm theo tên món...")) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtTimKiem.getText().isEmpty()) {
                    txtTimKiem.setText("Tìm theo tên món...");
                    txtTimKiem.setForeground(Color.GRAY);
                }
            }
        });

        // Search listener
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                currentKeywordFilter = txtTimKiem.getText().trim().toLowerCase();
                filterMonAn(); // Gọi hàm lọc khi gõ phím
            }
        });

        searchWrapper.add(txtTimKiem, BorderLayout.CENTER);
        headerPanel.add(searchWrapper, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Tạo panel trung tâm chứa các nút lọc và lưới món ăn.
     */
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); // Khoảng cách giữa filter và lưới
        panel.setOpaque(false);

        // 1. NORTH: Thanh lọc theo danh mục
        filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterButtonPanel.setOpaque(false);
        JScrollPane filterScrollPane = new JScrollPane(filterButtonPanel); // Cho phép cuộn ngang nếu nhiều nút
        filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        filterScrollPane.setBorder(null);
        filterScrollPane.setOpaque(false);
        filterScrollPane.getViewport().setOpaque(false);
        filterScrollPane.setPreferredSize(new Dimension(0, 45)); // Set chiều cao cố định cho thanh filter
        panel.add(filterScrollPane, BorderLayout.NORTH);

        // 2. CENTER: Lưới hiển thị món ăn (dùng VerticallyWrappingFlowPanel)
        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlMenuItemContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(pnlMenuItemContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tải các nút lọc từ CSDL (bảng DanhMucMon).
     */
    private void loadFilterButtons() {
        filterButtonPanel.removeAll(); // Xóa nút cũ
        ButtonGroup group = new ButtonGroup();

        // 1. Tạo nút "Tất cả"
        JToggleButton btnTatCa = createFilterButton("Tất cả", true);
        btnTatCa.setActionCommand("Tất cả"); // Action command là "Tất cả"
        group.add(btnTatCa);
        filterButtonPanel.add(btnTatCa);

        // 2. Tạo listener chung
        ActionListener filterListener = e -> {
            currentCategoryFilter = e.getActionCommand(); // Lấy maDM (hoặc "Tất cả")
            filterMonAn(); // Lọc lại món ăn
        };
        btnTatCa.addActionListener(filterListener);

        // 3. Tải danh mục từ DAO
        List<DanhMucMon> dsDanhMuc = danhMucMonDAO.getAllDanhMuc();
        if (dsDanhMuc != null) {
            for (DanhMucMon dm : dsDanhMuc) {
                JToggleButton button = createFilterButton(dm.getTendm(), false); // Tên hiển thị
                button.setActionCommand(dm.getMadm()); // Action command là Mã DM
                button.addActionListener(filterListener);
                group.add(button);
                filterButtonPanel.add(button);
            }
        }

        filterButtonPanel.revalidate();
        filterButtonPanel.repaint();
    }

    /**
     * Helper tạo JToggleButton cho thanh filter.
     */
    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        if (selected) {
            button.setBackground(COLOR_ACCENT_BLUE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    new EmptyBorder(4, 14, 4, 14)
            ));
        }

        // Listener đổi màu khi được chọn/bỏ chọn
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)
                ));
            }
        });
        button.setSelected(selected);
        return button;
    }


    /**
     * Tải dữ liệu món ăn từ CSDL và tạo các MonAnItemPanel.
     */
    private void loadDataFromDB() {
        // 1. Tải danh sách từ DAO (Đã sửa lỗi kết nối)
        this.dsMonAnFull = monAnDAO.getAllMonAn();
        System.out.println("DanhMucMonGUI: Đã tải " + dsMonAnFull.size() + " món ăn từ CSDL.");

        // 2. Tạo các Panel Item
        pnlMenuItemContainer.removeAll(); // Xóa các item cũ
        dsMonAnPanel.clear(); // Xóa danh sách panel cũ

        if (dsMonAnFull.isEmpty()) {
            pnlMenuItemContainer.add(new JLabel("Không có món ăn nào trong CSDL."));
        } else {
            for (MonAn mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon); // Dùng class bạn đã cung cấp

                // --- (Tùy chọn) Thêm sự kiện click nếu Quản lý muốn Sửa/Xóa món ---
                itemPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            // Ví dụ: Mở dialog sửa món
                            System.out.println("Double clicked: " + mon.getTenMon());
                            // showEditMonAnDialog(mon); // Cần tạo hàm này
                        }
                    }
                });
                // ---------------------------------------------------------------

                dsMonAnPanel.add(itemPanel);      // Thêm vào list quản lý
                pnlMenuItemContainer.add(itemPanel); // Thêm vào panel hiển thị
            }
        }

        // Lọc hiển thị theo danh mục mặc định ban đầu ("Tất cả")
        filterMonAn();

        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    /**
     * Lọc và hiển thị các món ăn dựa trên filter danh mục và từ khóa.
     */
    private void filterMonAn() {
        System.out.println("Filtering: Category='" + currentCategoryFilter + "', Keyword='" + currentKeywordFilter + "'");

        boolean found = false;
        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true;

            // 1. Lọc theo Danh mục
            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) {
                    show = false;
                }
            }

            // 2. Lọc theo Từ khóa (chỉ lọc nếu show vẫn là true)
            if (show && !currentKeywordFilter.isEmpty()) {
                if (!mon.getTenMon().toLowerCase().contains(currentKeywordFilter)) {
                    show = false;
                }
            }

            itemPanel.setVisible(show); // Ẩn hoặc hiện panel
            if (show) {
                found = true;
            }
        }

        // (Tùy chọn) Hiển thị thông báo nếu không tìm thấy kết quả
        // Cần thêm/xóa JLabel này một cách cẩn thận
        // if (!found) { ... }

        // Cập nhật lại layout sau khi ẩn/hiện
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
        // Cuộn lên đầu
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

}