package gui; // Hoặc package của bạn

import dao.BanDAO;
import dao.DonDatMonDAO; // Sẽ cần sau
import dao.KhachHangDAO; // Sẽ cần sau
import entity.Ban;
import entity.TrangThaiBan; // Cần import này

import java.awt.event.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.time.LocalDateTime;
import java.awt.*;
import java.time.LocalTime; // Cho giờ
import java.time.format.DateTimeFormatter; // Cho giờ
import java.util.ArrayList;
import java.util.List;

public class ManHinhDatBanGUI extends JPanel {

    // --- DAO ---
    private BanDAO banDAO;
    private KhachHangDAO khachHangDAO;
    private DonDatMonDAO donDatMonDAO; // Sẽ dùng khi bấm nút Đặt

    // --- Panel trái ---
    private JSpinner spinnerSoLuongKhach;
    private JTextField txtThoiGian; // Nên dùng JSpinner hoặc component chọn giờ
    private JTextField txtGhiChu;
    private JPanel pnlBanContainer; // Đổi tên từ leftTableContainer
    private List<Ban> dsBanTrongFull; // Danh sách TẤT CẢ bàn trống
    private Ban banDaChon = null;
    private List<BanPanel> dsBanPanelHienThi = new ArrayList<>();
    private JTextField txtSDTKhach;
    private JTextField txtHoTenKhach;
    private JButton btnDatBan;

    // --- Panel phải ---
    private JTextField txtTimKiemPhieuDat;
    private JList<String> listPhieuDat; // Hoặc JTable
    private DefaultListModel<String> modelListPhieuDat;

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public ManHinhDatBanGUI() {
        // --- Khởi tạo DAO ---
        banDAO = new BanDAO();
        khachHangDAO = new KhachHangDAO();
        donDatMonDAO = new DonDatMonDAO(); // Khởi tạo
        dsBanTrongFull = new ArrayList<>();

        // --- Cấu trúc Layout chính ---
        setLayout(new BorderLayout()); // JPanel chính dùng BorderLayout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(520); // Điều chỉnh vị trí chia
        splitPane.setBorder(null);
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 0, 10, 10));


        // --- Tạo Panel Trái và Phải ---
        JPanel pnlLeft = createLeftPanel_DatBan(); // Gọi hàm tạo panel trái mới
        JPanel pnlRight = createRightPanel();

        splitPane.setLeftComponent(pnlLeft);
        splitPane.setRightComponent(pnlRight);
        add(splitPane, BorderLayout.CENTER);

        // --- Tải dữ liệu ban đầu ---
        taiDanhSachBanTrong(); // Tải list bàn trống
        hienThiBanPhuHop();
        loadDanhSachDatTruoc(); // Tải danh sách đặt trước bên phải
    }

    // ==========================================================
    // PANEL BÊN TRÁI (Đặt bàn)
    // ==========================================================
    private JPanel createLeftPanel_DatBan() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
//        panel.setBorder(new EmptyBorder(15, 15, 15, 10));
        panel.setBackground(Color.WHITE);

        // --- 1. NORTH: Input Số lượng, Thời gian, Ghi chú ---
        JPanel pnlInputNorth = createInputNorthPanel(); // Giữ nguyên hàm này
        panel.add(pnlInputNorth, BorderLayout.NORTH);

        // --- 2. CENTER: Danh sách bàn (Giống ManHinhBanGUI) ---
        // Thay vì createSoDoBanPanel, dùng cấu trúc list giống ManHinhBanGUI
        JPanel listBanPanel = createListBanPanel_DatBan("Chọn Bàn Trống Phù Hợp"); // Hàm mới, tương tự createListPanel
        panel.add(listBanPanel, BorderLayout.CENTER);

        // --- 3. SOUTH: Input Khách hàng và Nút Đặt ---
        JPanel pnlInputSouth = createInputSouthPanel(); // Giữ nguyên hàm này
        panel.add(pnlInputSouth, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createListBanPanel_DatBan(String title) {
        // 1. Panel chính (BorderLayout)
        JPanel panel = new JPanel(new BorderLayout(0, 0)); // Giống gốc
        panel.setOpaque(false); // Nền trong suốt
        panel.setBorder(new EmptyBorder(10, 5, 0, 5)); // Lề giống gốc (trừ bottom)

        // 2. Header Panel (BorderLayout)
        JPanel headerPanel = new JPanel(new BorderLayout(0, 5)); // Giống gốc
        headerPanel.setOpaque(false);

        // 2.1. Title Label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Giống gốc
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // 2.2. "Filter Panel" (Tạm thời để trống hoặc bỏ đi nếu không cần)
        // Nếu bạn muốn có khoảng trống giống như có filter panel:
        JPanel fakeFilterPanel = new JPanel(); // Panel trống
        fakeFilterPanel.setOpaque(false);
        // headerPanel.add(fakeFilterPanel, BorderLayout.CENTER);
        // HOẶC BỎ LUÔN headerPanel.add(..., BorderLayout.CENTER); nếu không cần khoảng trống

        panel.add(headerPanel, BorderLayout.NORTH); // Thêm header vào panel chính

        // 3. Panel chứa các nút bàn (tableContainer)
        // Khởi tạo pnlBanContainer ở đây nếu chưa có, hoặc dùng biến thành viên đã có
        pnlBanContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); // Giống gốc
        pnlBanContainer.setBackground(Color.WHITE); // Giống gốc
        pnlBanContainer.setBorder(new EmptyBorder(5, 5, 5, 5)); // Giống gốc

        // 4. JScrollPane
        JScrollPane scrollPane = new JScrollPane(pnlBanContainer); // Bọc pnlBanContainer
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Giống gốc
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Giống gốc
        scrollPane.getViewport().setBackground(Color.WHITE); // Giống gốc
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Giữ lại tăng tốc độ cuộn

        panel.add(scrollPane, BorderLayout.CENTER); // Thêm scrollPane vào giữa

        return panel;
    }

    private JPanel createInputNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Nền trong suốt
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Hàng 1: Labels ---
        gbc.gridy = 0; // Hàng cho labels
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái cho labels
        gbc.insets = new Insets(0, 5, 2, 5); // Khoảng cách: trên, trái, dưới=2, phải
        gbc.weightx = 0.33; // Chia đều không gian ngang (tương đối)

        // Label Số lượng khách
        JLabel lblSoLuong = new JLabel("Số lượng khách");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        panel.add(lblSoLuong, gbc);

        // Label Thời gian
        JLabel lblThoiGian = new JLabel("Thời gian");
        lblThoiGian.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(lblThoiGian, gbc);

        // Label Ghi chú
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 2;
        panel.add(lblGhiChu, gbc);

        // --- Hàng 2: Input Fields ---
        gbc.gridy = 1; // Hàng cho input fields
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa field (hoặc WEST nếu muốn)
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho field co giãn theo chiều ngang
        gbc.insets = new Insets(0, 5, 10, 5); // Khoảng cách: trên=0, trái, dưới=10, phải

        // Input Số lượng khách (JSpinner)
        spinnerSoLuongKhach = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        spinnerSoLuongKhach.addChangeListener(e -> hienThiBanPhuHop());
        applySpinnerStyle(spinnerSoLuongKhach); // Áp dụng style
        gbc.gridx = 0;
        panel.add(spinnerSoLuongKhach, gbc);

        // Input Thời gian (JTextField)
        txtThoiGian = new JTextField("19:30"); // Giữ giá trị mặc định
        applyTextFieldStyle(txtThoiGian); // Áp dụng style
        // TODO: Validate định dạng giờ HH:mm
        gbc.gridx = 1;
        panel.add(txtThoiGian, gbc);

        // Input Ghi chú (JTextField)
        txtGhiChu = new JTextField();
        applyTextFieldStyle(txtGhiChu); // Áp dụng style
        gbc.gridx = 2;
        panel.add(txtGhiChu, gbc);

        return panel;
    }


    private JPanel createInputSouthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Nền trong suốt
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Cột 0: Thông tin SĐT ---
        gbc.gridx = 0;
        gbc.weightx = 0.5; // Chia đều không gian ngang
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Căn các thành phần sang trái
        gbc.insets = new Insets(0, 5, 2, 10); // Khoảng cách: trên, trái, dưới=2, phải=10 (tạo gap giữa 2 cột)

        // Label SĐT
        JLabel lblSDT = new JLabel("SĐT khách:");
        lblSDT.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 0; // Hàng label
        panel.add(lblSDT, gbc);

        // Input SĐT
        txtSDTKhach = new JTextField();
        applyTextFieldStyle(txtSDTKhach); // Áp dụng style
        txtSDTKhach.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                timKhachHangTheoSDT(); // Gọi hàm tìm KH
            }
        });
        gbc.gridy = 1; // Hàng input
        gbc.insets = new Insets(0, 5, 15, 10); // Tăng khoảng cách dưới input SĐT
        panel.add(txtSDTKhach, gbc);

        // --- Cột 1: Thông tin Họ tên ---
        gbc.gridx = 1;
        gbc.weightx = 0.5; // Chia đều không gian ngang
        gbc.insets = new Insets(0, 10, 2, 5); // Khoảng cách: trên, trái=10, dưới=2, phải

        // Label Họ tên
        JLabel lblTen = new JLabel("Họ tên khách:");
        lblTen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridy = 0; // Hàng label
        panel.add(lblTen, gbc);

        // Input Họ tên
        txtHoTenKhach = new JTextField();
        applyTextFieldStyle(txtHoTenKhach); // Áp dụng style
        gbc.gridy = 1; // Hàng input
        gbc.insets = new Insets(0, 10, 15, 5); // Tăng khoảng cách dưới input Tên
        panel.add(txtHoTenKhach, gbc);

        // --- Hàng 2: Nút Đặt Bàn ---
        btnDatBan = new JButton("ĐẶT BÀN");
        btnDatBan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDatBan.setBackground(COLOR_ACCENT_BLUE); // Màu xanh dương
        btnDatBan.setForeground(Color.WHITE);
        btnDatBan.setFocusPainted(false);
        // btnDatBan.setPreferredSize(new Dimension(150, 45)); // Bỏ PreferredSize để nút tự co giãn
        btnDatBan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDatBan.addActionListener(e -> xuLyDatBan());
        // Style thêm padding cho nút
        btnDatBan.setBorder(BorderFactory.createCompoundBorder(
                btnDatBan.getBorder(), // Giữ border mặc định (nếu có)
                new EmptyBorder(10, 30, 10, 30) // Thêm padding
        ));

        gbc.gridx = 0; // Bắt đầu từ cột 0
        gbc.gridy = 2; // Hàng thứ 2
        gbc.gridwidth = 2; // Kéo dài qua 2 cột
        gbc.fill = GridBagConstraints.NONE; // Không co giãn nút
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa nút
        gbc.insets = new Insets(10, 5, 5, 5); // Khoảng cách trên nút
        panel.add(btnDatBan, gbc);

        return panel;
    }

    private void applyTextFieldStyle(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Viền bo góc nhẹ (dùng LineBorder kết hợp EmptyBorder)
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Viền xám nhạt
                new EmptyBorder(5, 8, 5, 8) // Padding bên trong
        ));
        // Có thể thêm bo góc thực sự bằng cách vẽ custom border,
        // nhưng cách này đơn giản hơn và chấp nhận được.
        tf.setPreferredSize(new Dimension(100, 35)); // Ưu tiên chiều cao
    }
    private void applySpinnerStyle(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Lấy TextField bên trong Spinner để style
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(5, 8, 5, 8)
            ));
            textField.setBackground(Color.WHITE); // Đảm bảo nền trắng
        }
        spinner.setPreferredSize(new Dimension(100, 35)); // Ưu tiên chiều cao
    }
    // ==========================================================
    // PANEL BÊN PHẢI (Danh sách đặt trước)
    // ==========================================================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 10, 15, 15)); // Lề
        panel.setBackground(new Color(245, 245, 245)); // Màu nền hơi xám

        // --- 1. NORTH: Tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(5,0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("🔎"), BorderLayout.WEST);
        txtTimKiemPhieuDat = new JTextField(" Tìm kiếm bàn đặt SĐT/Tên khách...");
        txtTimKiemPhieuDat.setForeground(Color.GRAY);
        // TODO: Thêm placeholder và KeyListener để lọc listPhieuDat
        searchPanel.add(txtTimKiemPhieuDat, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);

        // --- 2. CENTER: Danh sách ---
        modelListPhieuDat = new DefaultListModel<>();
        listPhieuDat = new JList<>(modelListPhieuDat);
        listPhieuDat.setCellRenderer(new PhieuDatListRenderer()); // Renderer tùy chỉnh
        listPhieuDat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // TODO: Thêm MouseListener vào JList để xử lý click nút Xóa

        JScrollPane scrollPaneList = new JScrollPane(listPhieuDat);
        scrollPaneList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPaneList, BorderLayout.CENTER);

        // --- 3. SOUTH: Nút "..." Menu (Tạm thời bỏ) ---
        // (Nếu cần thì thêm lại)

        return panel;
    }

    // ==========================================================
    // LOGIC & HELPER METHODS
    // ==========================================================

    /**
     * Tải danh sách tất cả các bàn đang trống từ CSDL.
     */
    private void taiDanhSachBanTrong() {
        try {
            dsBanTrongFull = banDAO.getDanhSachBanTrong(); // Lấy tất cả bàn trống 1 lần
            System.out.println("Đã tải " + dsBanTrongFull.size() + " bàn trống.");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách bàn trống: " + e.getMessage());
            // Hiển thị lỗi cho người dùng
        }
    }

    /**
     * Cập nhật hiển thị các nút bàn dựa trên số lượng khách.
     */
    private void hienThiBanPhuHop() {
        int soLuongKhach = 1;
        if (spinnerSoLuongKhach != null) {
            soLuongKhach = (Integer) spinnerSoLuongKhach.getValue();
        }

        pnlBanContainer.removeAll(); // Xóa các panel bàn cũ
        dsBanPanelHienThi.clear();   // Xóa list panel cũ
        banDaChon = null;          // Bỏ chọn bàn cũ khi lọc lại

        boolean foundTable = false;
        if (dsBanTrongFull != null) {
            for (Ban ban : dsBanTrongFull) {
                // Chỉ hiển thị bàn TRỐNG và ĐỦ CHỖ
                if (ban.getTrangThai() == TrangThaiBan.TRONG && ban.getSoGhe() >= soLuongKhach) {
                    foundTable = true;

                    // --- SỬA: Tạo BanPanel thay vì JToggleButton ---
                    BanPanel banPanel = new BanPanel(ban); // Tạo BanPanel
                    dsBanPanelHienThi.add(banPanel);      // Thêm vào list quản lý
                    pnlBanContainer.add(banPanel);        // Thêm vào panel hiển thị

                    // --- THÊM MouseListener cho BanPanel ---
                    banPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                // Xử lý logic chọn bàn
                                if (ban.equals(banDaChon)) {
                                    // Bấm lại bàn đã chọn -> Bỏ chọn
                                    banDaChon = null;
                                } else {
                                    // Chọn bàn mới
                                    banDaChon = ban;
                                }
                                // Cập nhật trạng thái selected cho tất cả BanPanel
                                updateBanPanelSelection();
                                System.out.println("Bàn được chọn để đặt: " + (banDaChon != null ? banDaChon.getTenBan() : "Không có"));
                            }
                        }
                    });
                    // --- KẾT THÚC THÊM ---
                }
            }
            if (!foundTable) {
                pnlBanContainer.add(new JLabel("Không có bàn trống nào đủ chỗ cho " + soLuongKhach + " khách."));
            }
        } else {
            pnlBanContainer.add(new JLabel("Lỗi tải danh sách bàn trống."));
        }

        // Vẽ lại giao diện panel chứa bàn
        pnlBanContainer.revalidate();
        pnlBanContainer.repaint();
    }
    private void updateBanPanelSelection() {
        for (BanPanel panel : dsBanPanelHienThi) {
            // Nếu panel này tương ứng với bàn đang được chọn (banDaChon) thì set selected = true
            panel.setSelected(panel.getBan().equals(banDaChon));
        }
    }
    /**
     * Hàm helper tạo JToggleButton cho bàn
     */


    /** Helper để reset style các nút bàn không được chọn */

    /**
     * Tìm khách hàng dựa trên SĐT nhập vào.
     */
    private void timKhachHangTheoSDT() {
        String sdt = txtSDTKhach.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) { // Kiểm tra định dạng 10 số
            txtHoTenKhach.setText(""); // Xóa tên nếu SĐT không hợp lệ
            return;
        }

        entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt); // Dùng hàm DAO đã có
        if (kh != null) {
            txtHoTenKhach.setText(kh.getTenKH());
            // Có thể hiển thị thêm hạng thành viên nếu cần
        } else {
            txtHoTenKhach.setText(""); // Xóa tên nếu không tìm thấy
            // Có thể cho phép người dùng nhập tên mới ở đây
        }
    }

    /**
     * Xử lý logic khi bấm nút "ĐẶT BÀN".
     */
    private void xuLyDatBan() {
        // 1. Validate dữ liệu
        if (banDaChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bàn!", "Chưa chọn bàn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sdt = txtSDTKhach.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtSDTKhach.requestFocus();
            return;
        }
        String tenKH = txtHoTenKhach.getText().trim();
        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtHoTenKhach.requestFocus();
            return;
        }
        // Validate thời gian (cần chuẩn hóa)
        String thoiGianStr = txtThoiGian.getText().trim();
        LocalDateTime thoiGianDat;
        try {
            // Giả sử chỉ nhập giờ:phút, kết hợp với ngày hiện tại hoặc ngày mai?
            // Cần logic phức tạp hơn để xử lý ngày tháng
            LocalTime time = LocalTime.parse(thoiGianStr, DateTimeFormatter.ofPattern("HH:mm"));
            // Tạm thời ghép với ngày hôm nay
            thoiGianDat = LocalDateTime.now().with(time);
            if (thoiGianDat.isBefore(LocalDateTime.now())) {
                // Nếu giờ đã qua trong ngày hôm nay -> lỗi hoặc chuyển sang ngày mai?
                JOptionPane.showMessageDialog(this, "Thời gian đặt phải trong tương lai!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtThoiGian.requestFocus();
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Định dạng thời gian không hợp lệ (HH:mm)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtThoiGian.requestFocus();
            return;
        }


        // 2. Tìm hoặc Tạo Khách Hàng
        entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt);
        String maKHCanDung;
        if (kh == null) {
            // TODO: Nếu không tìm thấy, có thể hiện form/dialog để tạo KH mới
            // Hoặc đơn giản là tạo KH vãng lai mặc định
            JOptionPane.showMessageDialog(this, "Khách hàng mới? (Chức năng tạo KH chưa hoàn thiện)", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            // Tạm thời dùng KH vãng lai (cần có sẵn trong DB)
            kh = khachHangDAO.timTheoMaKH("KH_VANGLAI"); // Giả sử có mã KH_VANGLAI
            if (kh == null) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy khách hàng vãng lai mặc định!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        maKHCanDung = kh.getMaKH();


        // 3. Tạo đối tượng DonDatMon
        // Cần mã nhân viên đang đăng nhập (tạm dùng mã cố định)
        String maNV_LoggedIn = "NV01102"; // Lấy từ session hoặc nơi lưu trữ thông tin đăng nhập
        entity.DonDatMon ddm = new entity.DonDatMon(); // Dùng constructor mặc định tự sinh mã
        ddm.setNgayKhoiTao(LocalDateTime.now()); // Thời điểm bấm nút
        ddm.setMaNV(maNV_LoggedIn);
        ddm.setMaKH(maKHCanDung);
        ddm.setMaBan(banDaChon.getMaBan());
        // TODO: Thêm Ghi chú vào DonDatMon nếu Entity và DB có hỗ trợ

        // 4. Gọi DAO để lưu
        boolean datThanhCong = donDatMonDAO.themDonDatMon(ddm); // Giả sử có hàm này

        if (datThanhCong) {
            // 5. Cập nhật trạng thái bàn
            banDaChon.setTrangThai(TrangThaiBan.DA_DAT_TRUOC);
            banDaChon.setGioMoBan(thoiGianDat); // Giờ khách hẹn đến
            boolean capNhatBanOK = banDAO.updateBan(banDaChon); // Giả sử có hàm này

            if (capNhatBanOK) {
                // 6. Cập nhật giao diện
                taiDanhSachBanTrong(); // Tải lại list bàn trống
                hienThiBanPhuHop();     // Hiển thị lại bàn (bàn vừa đặt sẽ mất)
                loadDanhSachDatTruoc(); // Cập nhật list bên phải
                // Xóa input
                spinnerSoLuongKhach.setValue(1);
                txtThoiGian.setText("19:30");
                txtGhiChu.setText("");
                txtSDTKhach.setText("");
                txtHoTenKhach.setText("");
                banDaChon = null; // Bỏ chọn bàn

                JOptionPane.showMessageDialog(this, "Đặt bàn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Đặt đơn thành công nhưng lỗi cập nhật trạng thái bàn!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                // TODO: Cân nhắc xóa DonDatMon vừa thêm để đồng bộ?
            }
        } else {
            JOptionPane.showMessageDialog(this, "Đặt bàn thất bại! Vui lòng thử lại.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Tải danh sách các phiếu đặt trước (chưa nhận) lên JList bên phải.
     */
    private void loadDanhSachDatTruoc() {
        modelListPhieuDat.clear(); // Xóa list cũ
        try {
            // Giả sử DonDatMonDAO có hàm này và trả về List<DonDatMon> đã JOIN
            // Hoặc trả về List<String> đã format sẵn
            List<entity.DonDatMon> dsDatTruoc = donDatMonDAO.getAllDonDatMonChuaNhan(); // Cần JOIN để có tên bàn, tên KH

            if (dsDatTruoc.isEmpty()) {
                modelListPhieuDat.addElement("Chưa có bàn nào được đặt trước.");
            } else {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                // Cần tạo hàm để lấy tên bàn, tên KH từ mã
                for (entity.DonDatMon ddm : dsDatTruoc) {
                    String tenBan = banDAO.getTenBanByMa(ddm.getMaBan()); // Cần hàm này trong BanDAO
                    entity.KhachHang kh = khachHangDAO.timTheoMaKH(ddm.getMaKH());
                    String tenKH = (kh != null) ? kh.getTenKH() : "Vãng lai";
                    String sdtKH = (kh != null) ? kh.getSdt() : "Không có";
                    // Lấy giờ đặt từ Ban tương ứng (vì DonDatMon chỉ có ngày tạo)
                    Ban banDat = banDAO.getBanByMa(ddm.getMaBan()); // Cần hàm này
                    String gioDen = (banDat != null && banDat.getGioMoBan() != null) ? banDat.getGioMoBan().format(timeFormatter) : "Chưa rõ";
                    // Tạm thời lấy số lượng từ bàn (cần lấy từ DonDatMon nếu có)
                    int soNguoi = (banDat != null) ? banDat.getSoGhe() : 0;

                    // Format chuỗi hiển thị
                    String displayText = String.format("%s (%s) - %s\n%s - %s - %d người",
                            tenBan, sdtKH, tenKH,
                            gioDen, /* Lấy ghi chú từ ddm nếu có */ " ", soNguoi);

                    modelListPhieuDat.addElement(displayText); // TODO: Cần cách lưu maDon/maBan kèm theo
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách đặt trước: " + e.getMessage());
            modelListPhieuDat.addElement("Lỗi tải dữ liệu đặt bàn!");
        }
    }

    // --- Renderer tùy chỉnh cho JList bên phải ---
    // (Class này nên để thành inner class hoặc file riêng)
    private class PhieuDatListRenderer extends DefaultListCellRenderer {
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        private final Font mainFont = new Font("Segoe UI", Font.BOLD, 14);
        private final Font subFont = new Font("Segoe UI", Font.PLAIN, 12);
        private final Color textColor = Color.DARK_GRAY;
        private final Color timeColor = Color.BLUE;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            // Dùng JPanel để chứa nhiều JLabel và nút Xóa
            JPanel itemPanel = new JPanel(new BorderLayout(10, 2)); // Khoảng cách
            itemPanel.setBorder(new EmptyBorder(5, 10, 5, 5)); // Lề

            // Nút Xóa (JButton)
            JButton btnDelete = new JButton("X");
            btnDelete.setForeground(Color.RED);
            btnDelete.setFocusPainted(false);
            btnDelete.setBorder(null);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // TODO: Thêm ActionListener cho btnDelete để xử lý xóa

            // Panel chứa thông tin (2 dòng)
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            if (value instanceof String && !value.toString().startsWith("Lỗi") && !value.toString().startsWith("Chưa có")) {
                // Tách chuỗi hiển thị (đây là cách tạm, nên lưu object DonDatMon vào model)
                String displayString = (String) value;
                String[] lines = displayString.split("\n");
                String line1 = lines.length > 0 ? lines[0] : ""; // VD: "Bàn 1 (0123456789) - Lê Nguyễn Quốc Huy"
                String line2 = lines.length > 1 ? lines[1] : ""; // VD: "12:00 -  - 10 người"

                // Tách dòng 1
                String tenBan = line1.substring(0, line1.indexOf("(")).trim();
                String sdt = line1.substring(line1.indexOf("(") + 1, line1.indexOf(")")).trim();
                String tenKH = line1.substring(line1.indexOf("-") + 1).trim();

                // Tách dòng 2
                String gioDen = line2.substring(0, line2.indexOf("-")).trim();
                // String ghiChu = ... (Lấy ghi chú nếu có)
                String soNguoi = line2.substring(line2.lastIndexOf("-") + 1).trim();


                JLabel lblLine1 = new JLabel(String.format("%s (%s) - %s", tenBan, sdt, tenKH));
                lblLine1.setFont(mainFont);
                lblLine1.setForeground(textColor);

                JLabel lblLine2 = new JLabel(String.format("%s - %s", gioDen, soNguoi));
                lblLine2.setFont(subFont);
                lblLine2.setForeground(timeColor);

                infoPanel.add(lblLine1);
                infoPanel.add(lblLine2);

            } else {
                // Hiển thị thông báo lỗi/trống
                JLabel lblInfo = new JLabel(value.toString());
                lblInfo.setFont(subFont);
                lblInfo.setForeground(Color.GRAY);
                infoPanel.add(lblInfo);
            }


            itemPanel.add(infoPanel, BorderLayout.CENTER);
            itemPanel.add(btnDelete, BorderLayout.EAST);

            // Xử lý màu nền khi chọn
            if (isSelected) {
                itemPanel.setBackground(list.getSelectionBackground());
                itemPanel.setForeground(list.getSelectionForeground());
            } else {
                itemPanel.setBackground(list.getBackground());
                itemPanel.setForeground(list.getForeground());
            }

            return itemPanel;
        }
    }

} // Kết thúc class ManHinhDatBanGUI