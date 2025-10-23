package gui;

import entity.Ban;
import entity.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManHinhBanGUI extends JPanel {

    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254);
    public static final Color COLOR_STATUS_OCCUPIED = new Color(239, 68, 68);
    public static final Color COLOR_STATUS_RESERVED = new Color(187, 247, 208);

    private List<Ban> allTablesFromDB;
    private Ban selectedTable = null;
    private JPanel leftTableContainer;
    private String currentLeftFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();

    // --- THÊM CÁC BIẾN CHO PANEL BÊN PHẢI ---
    private JPanel rightPanel; // Panel chính bên phải

    // Header
    private JLabel lblTenBanHeader;
    private JLabel lblKhuVucHeader;

    // Info Dòng 1
    private JTextField txtNgayVao; // <-- SỬA
    private JTextField txtGioVao; // <-- SỬA
    private JTextField txtTinhTrang; // <-- SỬA
    private JComboBox<String> cmbPTThanhToan; // <-- SỬA

    // Info Dòng 2
    private JTextField txtSDTKhach; // <-- SỬA
    private JTextField txtHoTenKhach; // <-- SỬA
    private JTextField txtThanhVien; // <-- SỬA

    // Info Dòng 3
    private JTextField txtSoLuongKhach; // <-- SỬA
    private JTextField txtGhiChu;
    private JLabel statusColorBox;
    // --- KẾT THÚC THÊM BIẾN ---

    public ManHinhBanGUI() {
        super(new BorderLayout());
        buildUI();
    }
    private JTextField createStyledTextField(boolean isEditable) {
        JTextField tf = new JTextField();
        tf.setColumns(1);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Thêm viền mỏng
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(0, 5, 0, 5) // Lề 5px bên trong
        ));

        if (isEditable) {
            tf.setEditable(true);
            tf.setOpaque(true);
            tf.setBackground(Color.WHITE);
        } else {
            tf.setEditable(false);
            tf.setOpaque(true);
            // Dùng màu xám nhạt cho ô không được sửa
            tf.setBackground(new Color(235, 235, 235));
        }

        return tf;
    }

    private void buildUI() {
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(10, 0, 10, 10));

        // --- 1. KHỞI TẠO DỮ LIỆU ---
        this.allTablesFromDB = new ArrayList<>();
        try {
            LocalDateTime time = LocalDateTime.now().plusHours(1);
            allTablesFromDB.add(new Ban("Bàn 1", 4, TrangThaiBan.TRONG, time, "Tầng trệt"));
            allTablesFromDB.add(new Ban("Bàn 2", 2, TrangThaiBan.DANG_PHUC_VU, time, "Tầng 1"));
            allTablesFromDB.add(new Ban("Bàn 3", 4, TrangThaiBan.DA_DAT_TRUOC, time, "Tầng trệt"));
            allTablesFromDB.add(new Ban("Bàn 4", 6, TrangThaiBan.TRONG, time, "Tầng 1"));
            for (int i = 8; i <= 30; i++) {
                allTablesFromDB.add(new Ban("Bàn " + i, 4, TrangThaiBan.DANG_PHUC_VU, time, "Tầng trệt"));
            }
        } catch (Exception e) { e.printStackTrace(); }


        // --- 2. TẠO PANEL BÊN TRÁI ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        JPanel listPanel = createListPanel("Danh sách toàn bộ bàn");
        JPanel statsPanel = createStatsPanel();
        leftPanel.add(listPanel, BorderLayout.CENTER);
        leftPanel.add(statsPanel, BorderLayout.SOUTH);

        // --- 3. TẠO PANEL BÊN PHẢI (PLACEHOLDER) ---
        this.rightPanel = createRightPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        splitPane.setDividerLocation(520);
        splitPane.setBorder(null);
        splitPane.setOneTouchExpandable(true);

        this.add(splitPane, BorderLayout.CENTER);

        populateLeftPanel(currentLeftFilter);
        updateRightPanelDetails(selectedTable);
    }
    private JPanel createRightPanel() {
        // 1. Panel chính bên phải (vẫn dùng BorderLayout)
        JPanel panel = new JPanel(new BorderLayout(0, 0)); // Gap 10px
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 15, 0, 10));

        // 2. HEADER (Giữ nguyên code header của bạn)
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        statusColorBox = new JLabel();
        statusColorBox.setPreferredSize(new Dimension(48, 48));
        statusColorBox.setBackground(COLOR_STATUS_FREE);
        statusColorBox.setOpaque(true);
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        textPanel.setOpaque(false);
        lblTenBanHeader = new JLabel("Chọn một bàn");
        lblTenBanHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblKhuVucHeader = new JLabel("");
        lblKhuVucHeader.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblKhuVucHeader.setForeground(Color.DARK_GRAY);
        textPanel.add(lblTenBanHeader);
        textPanel.add(lblKhuVucHeader);
        headerPanel.add(statusColorBox, BorderLayout.WEST);
        headerPanel.add(textPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH); // Thêm Header vào TRÊN CÙNG

        // --- SỬA Ở ĐÂY: ĐỔI TỪ GRIDLAYOUT SANG BORDERLAYOUT ---

        // 3. Container này dùng BorderLayout (thay vì GridLayout)
        JPanel container = new JPanel(new BorderLayout(0, 10)); // 10px gap dọc
        container.setOpaque(false);

        // 4. Panel Thông tin (Nửa trên)
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.BOTH;

        // (Khởi tạo các trường - giữ nguyên)
        txtNgayVao = createStyledTextField(false);
        txtGioVao = createStyledTextField(false);
        txtTinhTrang = createStyledTextField(false);
        txtThanhVien = createStyledTextField(false);
        txtSDTKhach = createStyledTextField(true);
        txtHoTenKhach = createStyledTextField(true);
        txtSoLuongKhach = createStyledTextField(true);
        txtGhiChu = createStyledTextField(true);
        cmbPTThanhToan = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        cmbPTThanhToan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbPTThanhToan.setBackground(Color.WHITE);
        cmbPTThanhToan.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        cmbPTThanhToan.setPreferredSize(new Dimension(10, cmbPTThanhToan.getPreferredSize().height));

        txtSDTKhach.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                timKiemThanhVienTuSDT();
            }
        });

        // (Add Dòng 1 - giữ nguyên)
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Ngày vào", txtNgayVao), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("Giờ vào", txtGioVao), gbc);
        gbc.gridx = 2; gbc.weightx = 1.5;
        infoPanel.add(createInfoBox("Tình trạng", txtTinhTrang), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        infoPanel.add(createInfoBox("PT Thanh toán", cmbPTThanhToan), gbc);
        // (Add Dòng 2 - giữ nguyên)
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridwidth = 1;
        infoPanel.add(createInfoBox("SĐT Khách hàng", txtSDTKhach), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        infoPanel.add(createInfoBox("Họ tên Khách", txtHoTenKhach), gbc);
        gbc.gridx = 3; gbc.gridwidth = 1;
        infoPanel.add(createInfoBox("Thành viên", txtThanhVien), gbc);

        // --- SỬA DÒNG 3 (ĐỂ NGĂN NÓ CO GIÃN DỌC) ---
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.gridwidth = 1;
        gbc.weighty = 0.0; // <-- SỬA: Không cho co giãn dọc
        gbc.fill = GridBagConstraints.HORIZONTAL; // <-- SỬA: Chỉ co giãn ngang
        infoPanel.add(createInfoBox("Số lượng khách", txtSoLuongKhach), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH; // Ghi chú thì co giãn cả 2 chiều
        infoPanel.add(createInfoBox("Ghi chú", txtGhiChu), gbc);

        // 5. Panel Bill (Nửa dưới)
        BillPanel billPanel = new BillPanel();

        // 6. Thêm 2 panel vào container
        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, // Chia DỌC
                infoPanel,                 // Nửa TRÊN
                billPanel                  // Nửa DƯỚI
        );

        // --- ĐÂY LÀ CHÌA KHÓA ---
        // Set vị trí thanh chia (ví dụ: 300px từ trên xuống)
        // Bạn có thể chỉnh số này sau
        verticalSplitPane.setDividerLocation(230);
        verticalSplitPane.setBorder(null); // Bỏ viền

        // 6. Thêm JSplitPane vào GIỮA panel chính
        panel.add(verticalSplitPane, BorderLayout.CENTER);

        return panel;
    }
    private void timKiemThanhVienTuSDT() {
        String sdt = txtSDTKhach.getText().trim();

        // --- BẠN SẼ GỌI LOGIC DAO/BUS Ở ĐÂY ---
        // Ví dụ: KhachHang kh = khachHang_DAO.timTheoSDT(sdt);
        // if (kh != null) {
        //    txtThanhVien.setText(kh.getLoaiThanhVien().getTenLoai());
        //    txtHoTenKhach.setText(kh.getHoTen());
        // } else {
        //    txtThanhVien.setText("Chưa là thành viên");
        // }
        // ----------------------------------------

        // (Đây là code GIẢ LẬP để test)
        System.out.println("Đang tìm kiếm SĐT: " + sdt);
        if (sdt.equals("0909123456")) {
            txtThanhVien.setText("Vàng");
            txtHoTenKhach.setText("Nguyễn Văn A"); // (Tùy chọn: Tự điền họ tên)
        } else if (sdt.equals("0987654321")) {
            txtThanhVien.setText("Bạc");
            txtHoTenKhach.setText("Trần Thị B");
        } else {
            txtThanhVien.setText("Chưa là thành viên");
        }
    }
    private JPanel createInfoBox(String labelText, Component field) {
        JPanel box = new JPanel(new BorderLayout(0, 4)); // Gap 4px
        box.setOpaque(false); // Nền trong suốt

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.BLACK);


        box.add(label, BorderLayout.NORTH);
        box.add(field, BorderLayout.CENTER);
        return box;
    }
    private void updateRightPanelDetails(Ban ban) {
        if (ban != null) {
            // (Code xác định statusColor, tinhTrangText giữ nguyên)
            Color statusColor;
            String tinhTrangText;
            switch (ban.getTrangThai()) {
                // ... (code switch case của bạn) ...
                case TRONG:
                    statusColor = COLOR_STATUS_FREE;
                    tinhTrangText = "Trống";
                    break;
                case DA_DAT_TRUOC:
                    statusColor = COLOR_STATUS_RESERVED;
                    tinhTrangText = "Đã đặt trước";
                    break;
                case DANG_PHUC_VU:
                default:
                    statusColor = COLOR_STATUS_OCCUPIED;
                    tinhTrangText = "Đang phục vụ";
                    break;
            }

            // --- CẬP NHẬT HEADER (Giữ nguyên) ---
            // Cập nhật Header
            statusColorBox.setBackground(statusColor);

            // 1. Set Tên Bàn KÈM DẤU GẠCH NỐI
            lblTenBanHeader.setText(ban.getTenBan() + " -");
            // 2. Set Khu Vực
            lblKhuVucHeader.setText(ban.getKhuVuc());

            // --- 3. CẬP NHẬT INFO (SỬA Ở ĐÂY) ---
            txtTinhTrang.setText(tinhTrangText);

            if (ban.getTrangThai() == TrangThaiBan.TRONG) {
                // Nếu bàn trống, reset mọi trường có thể sửa
                cmbPTThanhToan.setSelectedItem("Tiền mặt"); // <-- SỬA
                txtSDTKhach.setText("");
                txtHoTenKhach.setText("");
                txtThanhVien.setText("");
                txtSoLuongKhach.setText("");
                txtGhiChu.setText("");
                txtNgayVao.setText("");
                txtGioVao.setText("");
            } else if (ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                // (Đây là dữ liệu từ Phiếu Đặt Bàn)
                txtNgayVao.setText("2025-10-23");
                txtGioVao.setText("19:00");
                cmbPTThanhToan.setSelectedItem("Chưa thanh toán"); // (Bạn có thể cần thêm mục này)
                txtSDTKhach.setText("0909123456");
                txtHoTenKhach.setText("Nguyễn Văn A");
                txtThanhVien.setText("Vàng");
                txtSoLuongKhach.setText(String.valueOf(ban.getSoGhe()));
                txtGhiChu.setText("Đặt bàn lúc 19:00, " + ban.getSoGhe() + " người.");
            } else { // Đang phục vụ
                // (Đây là dữ liệu từ Hóa Đơn)
                txtNgayVao.setText("2025-10-23");
                txtGioVao.setText("18:15");
                cmbPTThanhToan.setSelectedItem("Tiền mặt"); // <-- SỬA
                txtSDTKhach.setText("0987654321");
                txtHoTenKhach.setText("Trần Thị B");
                txtThanhVien.setText("Bạc");
                txtSoLuongKhach.setText("2");
                txtGhiChu.setText("Khách không ăn hành.");
            }

        } else {
            // --- RESET (SỬA Ở ĐÂY) ---
            statusColorBox.setBackground(COLOR_STATUS_FREE);
            lblTenBanHeader.setText("Chọn một bàn");
            lblKhuVucHeader.setText(""); // 2. Set Khu Vực rỗng
            cmbPTThanhToan.setSelectedItem("Tiền mặt"); // <-- SỬA
        }
    }

    // 4. Chuyển tất cả các hàm helper liên quan đến "Bàn" vào đây

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        int countTrong = 0;
        int countDat = 0;
        int countPhucVu = 0;

        if (allTablesFromDB != null) {
            for (Ban ban : allTablesFromDB) {
                switch (ban.getTrangThai()) {
                    case TRONG:
                        countTrong++;
                        break;
                    case DA_DAT_TRUOC:
                        countDat++;
                        break;
                    case DANG_PHUC_VU:
                        countPhucVu++;
                        break;
                }
            }
        }

        statsPanel.add(createStatItem("Trống", countTrong, COLOR_STATUS_FREE));
        statsPanel.add(createStatItem("Đã đặt", countDat, COLOR_STATUS_RESERVED));
        statsPanel.add(createStatItem("Sử dụng", countPhucVu, COLOR_STATUS_OCCUPIED));

        return statsPanel;
    }

    private JPanel createStatItem(String name, int count, Color color) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        itemPanel.setOpaque(false);

        JPanel iconBox = new JPanel();
        iconBox.setPreferredSize(new Dimension(28, 28));
        iconBox.setBackground(color);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLabel.setForeground(Color.RED);

        itemPanel.add(iconBox);
        itemPanel.add(nameLabel);
        itemPanel.add(countLabel);

        return itemPanel;
    }

    private JPanel createListPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(createFilterPanel(), BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.leftTableContainer = tableContainer;

        JScrollPane scrollPane = new JScrollPane(tableContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        String[] filters = {"Tất cả", "Tầng trệt", "Tầng 1"};

        ActionListener filterListener = e -> {
            String selectedFilter = e.getActionCommand();
            currentLeftFilter = selectedFilter;
            populateLeftPanel(currentLeftFilter);
        };

        for (String filter : filters) {
            JToggleButton button = createFilterButton(filter, filter.equals("Tất cả"));
            button.setActionCommand(filter);
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
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
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

    private void populateLeftPanel(String khuVucFilter) {
        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        for (Ban ban : allTablesFromDB) {
            if (khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter)) {
                BanPanel banPanel = new BanPanel(ban);
                if (ban.equals(selectedTable)) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            toggleSelection(ban, banPanel);
                        }
                    }
                });
                leftTableContainer.add(banPanel);
                leftBanPanelList.add(banPanel);
            }
        }
        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    private void toggleSelection(Ban clickedBan, BanPanel clickedPanel) {
        if (!clickedBan.equals(selectedTable)) {
            if (selectedTable != null) {
                for (BanPanel panel : leftBanPanelList) {
                    if (panel.getBan().equals(selectedTable)) {
                        panel.setSelected(false);
                        break;
                    }
                }
            }
            selectedTable = clickedBan;
            clickedPanel.setSelected(true);
        }
        else {
            selectedTable = null;
            clickedPanel.setSelected(false);
        }
        if (selectedTable != null) {
            System.out.println("Bàn đang được chọn: " + selectedTable.getTenBan());
        } else {
            System.out.println("Không có bàn nào được chọn.");
        }
        updateRightPanelDetails(selectedTable);
    }
}