package gui; // Hoặc package của bạn

import dao.BanDAO;
import dao.DonDatMonDAO; // Sẽ cần sau
import dao.KhachHangDAO; // Sẽ cần sau
import entity.Ban;
import entity.TrangThaiBan; // Cần import này

import java.awt.event.*;

import javax.swing.SpinnerDateModel;
import java.util.Date;
import java.util.Calendar;
import java.time.ZoneId;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.time.LocalDateTime;
import java.awt.*;
import java.time.LocalTime; // Cho giờ
import java.time.format.DateTimeFormatter; // Cho giờ
import java.util.ArrayList;
import java.util.List;
import entity.DonDatMon; // Cần import DonDatMon
import entity.KhachHang; // Cần import KhachHang
import javax.swing.event.ListSelectionListener; // Có thể cần nếu muốn xử lý chọn item
import javax.swing.event.ListSelectionEvent;

public class ManHinhDatBanGUI extends JPanel {

    // --- DAO ---
    private BanDAO banDAO;
    private KhachHangDAO khachHangDAO;
    private DonDatMonDAO donDatMonDAO; // Sẽ dùng khi bấm nút Đặt
    private DanhSachBanGUI parentDanhSachBanGUI_DatBan;

    // --- Panel trái ---
    private JSpinner spinnerSoLuongKhach;
    private JSpinner dateSpinner;    // Thêm spinner ngày
    private JSpinner timeSpinner;
    private JTextField txtGhiChu;
    private JPanel pnlBanContainer; // Đổi tên từ leftTableContainer
    private List<Ban> dsTatCaBan;
    private List<Ban> dsBanDaChon = new ArrayList<>();
    private List<BanPanel> dsBanPanelHienThi = new ArrayList<>();
    private JTextField txtSDTKhach;
    private JTextField txtHoTenKhach;
    private JButton btnDatBan;
    private MainGUI mainGUI_DatBan;

    // --- Panel phải ---
    private JTextField txtTimKiemPhieuDat;
    private JList<DonDatMon> listPhieuDat; // Hoặc JTable
    private DefaultListModel<DonDatMon> modelListPhieuDat;

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public ManHinhDatBanGUI(DanhSachBanGUI parent,MainGUI main) {
        this.parentDanhSachBanGUI_DatBan = parent;
        this.mainGUI_DatBan = main;
        // --- Khởi tạo DAO ---
        banDAO = new BanDAO();
        khachHangDAO = new KhachHangDAO();
        donDatMonDAO = new DonDatMonDAO(); // Khởi tạo
//        dsBanTrongFull = new ArrayList<>();

        // --- Cấu trúc Layout chính ---
        setLayout(new BorderLayout()); // JPanel chính dùng BorderLayout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(610); // Điều chỉnh vị trí chia
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
    private List<List<Ban>> timGoiYGhepBan(int soLuongKhach,List<Ban> sourceList) {
        List<List<Ban>> dsGoiY = new ArrayList<>();

        // 1. Phân loại bàn theo khu vực
        java.util.Map<String, List<Ban>> banTheoKhuVuc = new java.util.HashMap<>();
        for (Ban ban : sourceList) {
            banTheoKhuVuc.computeIfAbsent(ban.getKhuVuc(), k -> new ArrayList<>()).add(ban);
        }

        // 2. Duyệt từng khu vực
        for (String khuVuc : banTheoKhuVuc.keySet()) {
            List<Ban> bansInZone = banTheoKhuVuc.get(khuVuc);

            // --- LOGIC MỚI: Kiểm tra tổng sức chứa trước ---
            int tongSucChuaKhuVuc = bansInZone.stream().mapToInt(Ban::getSoGhe).sum();

            if (tongSucChuaKhuVuc < soLuongKhach) {
                // Nếu cả khu vực cộng lại không đủ chỗ, bỏ qua khu vực này
                // Hoặc có thể thêm một gợi ý đặc biệt: "Lấy hết bàn khu vực này (vẫn thiếu ... ghế)"
                continue;
            }

            // Nếu đủ chỗ, bắt đầu tìm tổ hợp
            // Sắp xếp bàn từ lớn đến nhỏ để ưu tiên lấy bàn to trước -> ghép ít bàn hơn
            bansInZone.sort((b1, b2) -> Integer.compare(b2.getSoGhe(), b1.getSoGhe()));

            timToHopBan(bansInZone, soLuongKhach, 0, new ArrayList<>(), dsGoiY);
        }

        // 3. Sắp xếp kết quả: Ưu tiên ít bàn nhất
        dsGoiY.sort((list1, list2) -> Integer.compare(list1.size(), list2.size()));

        // Chỉ lấy tối đa 3 gợi ý tốt nhất
        if (dsGoiY.size() > 3) {
            return dsGoiY.subList(0, 3);
        }
        return dsGoiY;
    }
    private void timToHopBan(List<Ban> bans, int target, int index, List<Ban> current, List<List<Ban>> results) {
        // Đã tìm thấy đủ số lượng gợi ý thì dừng cho đỡ tốn tài nguyên
        if (results.size() >= 50) return;

        int currentSeats = current.stream().mapToInt(Ban::getSoGhe).sum();

        // Điều kiện dừng: Đủ chỗ
        if (currentSeats >= target) {
            // Logic tối ưu: Không quá dư thừa (ví dụ dư tối đa 6 ghế)
            if (currentSeats - target <= 8) {
                results.add(new ArrayList<>(current));
            }
            return;
        }

        // --- SỬA: Tăng giới hạn số bàn hoặc bỏ giới hạn ---
        // Cho phép ghép tối đa 10 bàn (hoặc bỏ luôn dòng này nếu muốn ghép bao nhiêu cũng được)
        if (current.size() >= 10) {
            return;
        }
        // --------------------------------------------------

        // Duyệt tiếp
        for (int i = index; i < bans.size(); i++) {
            current.add(bans.get(i));
            timToHopBan(bans, target, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }
    private JPanel createInputNorthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Nền trong suốt
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Hàng 1: Labels ---
        gbc.gridy = 0; // Hàng cho labels
        gbc.anchor = GridBagConstraints.WEST; // Căn lề trái cho labels
        gbc.weightx = 0.33; // Chia đều không gian ngang (tương đối)

        // Label Số lượng khách
        JLabel lblSoLuong = new JLabel("Số lượng khách");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        panel.add(lblSoLuong, gbc);

        JLabel lblNgayDat = new JLabel("Ngày đặt:");
        lblNgayDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; panel.add(lblNgayDat, gbc);

        JLabel lblGioDat = new JLabel("Giờ đặt:");
        lblGioDat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 2; panel.add(lblGioDat, gbc);

        // --- Hàng 2: Input Fields ---
        gbc.gridy = 1; // Hàng cho input fields
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa field (hoặc WEST nếu muốn)
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho field co giãn theo chiều ngang
        gbc.insets = new Insets(0, 5, 10, 5); // Khoảng cách: trên=0, trái, dưới=10, phải

        // Input Số lượng khách (JSpinner)
        spinnerSoLuongKhach = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spinnerSoLuongKhach.addChangeListener(e -> hienThiBanPhuHop());
        applySpinnerStyle(spinnerSoLuongKhach); // Áp dụng style
        gbc.gridx = 0;
        panel.add(spinnerSoLuongKhach, gbc);

        Date earliestDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), // Giá trị ban đầu (hôm nay)
                earliestDate, // Ngày nhỏ nhất (hôm nay)
                null,        // Ngày lớn nhất (không giới hạn)
                Calendar.DAY_OF_MONTH); // Bước nhảy
        dateSpinner = new JSpinner(dateModel);
        // Định dạng hiển thị ngày
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        applySpinnerStyle(dateSpinner); // Áp dụng style (có thể cần chỉnh hàm style)
        gbc.gridx = 1; panel.add(dateSpinner, gbc);
        dateSpinner.addChangeListener(e -> hienThiBanPhuHop());

        // Spinner chọn Giờ
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        // Định dạng hiển thị giờ:phút
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        applySpinnerStyle(timeSpinner); // Áp dụng style
        // Đặt giá trị mặc định (1 tiếng sau, làm tròn)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        timeSpinner.setValue(cal.getTime());
        gbc.gridx = 2; panel.add(timeSpinner, gbc);
        timeSpinner.addChangeListener(e -> hienThiBanPhuHop());

        // --- Hàng 3: Label Ghi chú ---
        gbc.gridy = 2; // Hàng mới cho label Ghi chú
        gbc.gridx = 0; // Bắt đầu từ cột 0
        gbc.gridwidth = 4; // Kéo dài qua cả 4 cột
        gbc.anchor = GridBagConstraints.WEST; // Căn trái
        gbc.insets = new Insets(5, 5, 2, 5); // Lề trên 5, dưới 2
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblGhiChu, gbc);


        // --- Hàng 4: Input Ghi chú ---
        gbc.gridy = 3; // Hàng mới cho input Ghi chú
        gbc.gridx = 0; // Bắt đầu từ cột 0
        gbc.gridwidth = 4; // Kéo dài qua cả 4 cột
        gbc.fill = GridBagConstraints.HORIZONTAL; // Co giãn ngang
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa (hoặc WEST)
        gbc.insets = new Insets(0, 5, 10, 5); // Lề dưới 10
        txtGhiChu = new JTextField();
        applyTextFieldStyle(txtGhiChu); // Áp dụng style
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
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Khoảng cách dọc 10
        panel.setBorder(new EmptyBorder(15, 10, 15, 15));   // Lề xung quanh
        panel.setBackground(new Color(245, 245, 245));      // Nền xám nhạt

        // --- 1. NORTH: Panel Tìm kiếm ---
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0)); // Panel riêng cho tìm kiếm
        searchPanel.setOpaque(false); // Nền trong suốt để thấy màu nền của panel cha
        searchPanel.setBorder(new EmptyBorder(0, 0, 5, 0)); // Lề dưới cho search panel

        // Icon kính lúp
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        searchPanel.add(searchIcon, BorderLayout.WEST);

        // Ô nhập liệu tìm kiếm
        final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách..."; // Lưu placeholder
        txtTimKiemPhieuDat = new JTextField(placeholder); // Đặt placeholder ban đầu
        txtTimKiemPhieuDat.setForeground(Color.GRAY);
        applyTextFieldStyle(txtTimKiemPhieuDat);
        txtTimKiemPhieuDat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().equals(placeholder)) {
                    txtTimKiemPhieuDat.setText("");
                    txtTimKiemPhieuDat.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiemPhieuDat.getText().isEmpty()) {
                    txtTimKiemPhieuDat.setForeground(Color.GRAY);
                    txtTimKiemPhieuDat.setText(placeholder);
                }
            }
        });
        // --- KẾT THÚC PLACEHOLDER ---

        // --- THÊM KEYLISTENER ĐỂ TÌM KIẾM ---
        txtTimKiemPhieuDat.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Gọi hàm tìm kiếm mỗi khi người dùng nhả phím
                timKiemPhieuDat();
            }
        });
        searchPanel.add(txtTimKiemPhieuDat, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        // --- Đảm bảo dòng trên tồn tại ---

        // --- 2. CENTER: Danh sách đặt trước ---
        modelListPhieuDat = new DefaultListModel<>();
        listPhieuDat = new JList<>(modelListPhieuDat);
        listPhieuDat.setCellRenderer(new PhieuDatListRenderer());
        listPhieuDat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPhieuDat.setBackground(Color.WHITE);

        // Thêm MouseListener để xử lý click nút Xóa (như code cũ)
        listPhieuDat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = listPhieuDat.locationToIndex(e.getPoint());
                if (index != -1) {
                    DonDatMon ddm = modelListPhieuDat.getElementAt(index);
                    Rectangle itemBounds = listPhieuDat.getCellBounds(index, index); // Vùng bao của cả item

                    // Lấy component renderer để lấy kích thước nút thật và border
                    Component rendererComp = listPhieuDat.getCellRenderer().getListCellRendererComponent(listPhieuDat, ddm, index, false, false);
                    Component mainPanelComp = null; // Panel chứa text và nút X
                    Component deleteBtnComp = null; // Nút Xóa

                    // Tìm mainPanel và nút xóa bên trong cấu trúc renderer
                    if (rendererComp instanceof JPanel) { // containerPanel
                        mainPanelComp = ((JPanel) rendererComp).getComponent(0); // mainPanel
                        if (mainPanelComp instanceof JPanel) {
                            deleteBtnComp = ((JPanel) mainPanelComp).getComponent(1); // JButton (EAST)
                        }
                    }

                    if (deleteBtnComp instanceof JButton && mainPanelComp instanceof JPanel) { // Đảm bảo tìm thấy cả hai
                        JButton btnDelete = (JButton) deleteBtnComp;
                        JPanel itemMainPanel = (JPanel) mainPanelComp; // Panel có border

                        // --- SỬA CÁCH LẤY BORDER INSETS ---
                        Insets borderInsets = new Insets(0,0,0,0); // Mặc định không có lề
                        Border border = itemMainPanel.getBorder(); // Lấy border của mainPanel
                        if (border != null) {
                            borderInsets = border.getBorderInsets(itemMainPanel); // Lấy insets từ border
                        }
                        // --- KẾT THÚC SỬA ---

                        // Tính toán vùng của nút Xóa tương đối so với itemBounds
                        int btnX = itemBounds.x + itemBounds.width - btnDelete.getWidth()
                                - borderInsets.right // <-- Dùng insets đã lấy
                                - ((BorderLayout)itemMainPanel.getLayout()).getHgap(); // Khoảng cách ngang layout
                        int btnY = itemBounds.y + (itemBounds.height - btnDelete.getHeight()) / 2; // Căn giữa Y (ước lượng)

                        Rectangle deleteButtonBounds = new Rectangle(btnX, btnY, btnDelete.getWidth(), btnDelete.getHeight());

                        // Kiểm tra click
                        if (deleteButtonBounds.contains(e.getPoint())) {
                            System.out.println("Clicked delete button for: " + ddm.getMaDon());
                            xuLyHuyDatBan(ddm, index);
                        }
                    } else {
                        System.err.println("Không tìm thấy JButton xóa hoặc mainPanel trong renderer!");
                    }
                }
            }
        });

        JScrollPane scrollPaneList = new JScrollPane(listPhieuDat);
        scrollPaneList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPaneList, BorderLayout.CENTER);

        return panel;
    }
    private void timKiemPhieuDat() {
        String query = txtTimKiemPhieuDat.getText().trim();
        final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách..."; // Lấy lại placeholder

        // Xóa model hiện tại trước khi thêm kết quả mới
        modelListPhieuDat.clear();

        try {
            List<entity.DonDatMon> dsKetQua;

            // Nếu ô tìm kiếm trống hoặc là placeholder -> hiển thị tất cả
            if (query.isEmpty() || query.equals(placeholder)) {
                dsKetQua = donDatMonDAO.getAllDonDatMonChuaNhan(); // Lấy tất cả
            } else {
                // Nếu có từ khóa -> gọi hàm tìm kiếm của DAO
                dsKetQua = donDatMonDAO.timDonDatMonChuaNhan(query); // Tìm theo query
            }

            // Hiển thị kết quả lên JList
            if (dsKetQua.isEmpty() && !(query.isEmpty() || query.equals(placeholder))) {
                // Nếu tìm kiếm có query mà không ra kết quả
                modelListPhieuDat.addElement(null); // Thêm null để renderer biết hiển thị "Không tìm thấy"
                // Hoặc thêm một String đặc biệt
                // modelListPhieuDat.addElement("Không tìm thấy kết quả nào.");
            } else if (dsKetQua.isEmpty() && (query.isEmpty() || query.equals(placeholder))) {
                // Nếu không có đơn đặt nào cả
                modelListPhieuDat.addElement(null); // Thêm null để renderer hiển thị "Chưa có..."
                // modelListPhieuDat.addElement("Chưa có bàn nào được đặt trước.");
            }
            else {
                for (entity.DonDatMon ddm : dsKetQua) {
                    modelListPhieuDat.addElement(ddm); // Thêm các đơn tìm thấy
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm phiếu đặt: " + e.getMessage());
            modelListPhieuDat.clear(); // Xóa hết nếu lỗi
            modelListPhieuDat.addElement(null); // Hiển thị lỗi
            // modelListPhieuDat.addElement("Lỗi khi tìm kiếm dữ liệu!");
        }
        // Cập nhật lại model cho JList (quan trọng)
        listPhieuDat.setModel(modelListPhieuDat);
        listPhieuDat.repaint();
    }
    // ==========================================================
    // LOGIC & HELPER METHODS
    // ==========================================================

    /**
     * Tải danh sách tất cả các bàn đang trống từ CSDL.
     */
    private void taiDanhSachBanTrong() {
        try {
            dsTatCaBan = banDAO.getAllBan();
            System.out.println("Đã tải " + dsTatCaBan.size() + " bàn trống.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật hiển thị các nút bàn dựa trên số lượng khách.
     */
    private void hienThiBanPhuHop() {
        // 1. Lấy thông tin từ GUI
        int soLuongKhach = (Integer) spinnerSoLuongKhach.getValue();
        LocalDateTime thoiGianDat = null;

        try {
            Date d = (Date) dateSpinner.getValue();
            Date t = (Date) timeSpinner.getValue();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(t);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            thoiGianDat = cal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) { return; } // Chưa load xong UI

        // 2. Xác định khoảng thời gian "Bận" (Trước và sau 2 tiếng)
        LocalDateTime startCheck = thoiGianDat.minusHours(2);
        LocalDateTime endCheck = thoiGianDat.plusHours(2);

        // 3. Lấy danh sách các bàn ĐÃ CÓ ĐƠN trong khoảng này
        List<String> maBanBan = donDatMonDAO.getMaBanDaDatTrongKhoang(startCheck, endCheck);

        // Lưu ý thêm: Nếu thời gian đặt là "Hiện tại" (sai số nhỏ),
        // thì các bàn đang DANG_PHUC_VU cũng phải tính là bận.
        boolean isBookingNow = java.time.Duration.between(LocalDateTime.now(), thoiGianDat).abs().toMinutes() < 30;

        pnlBanContainer.removeAll();
        dsBanPanelHienThi.clear();
        dsBanDaChon.clear();

        // 4. Lọc danh sách bàn
        List<Ban> dsBanKhaDung = new ArrayList<>();
        if (dsTatCaBan != null) {
            for (Ban ban : dsTatCaBan) {
                boolean isBusy = false;

                // Check 1: Có nằm trong danh sách đặt trước trùng giờ không?
                if (maBanBan.contains(ban.getMaBan())) {
                    isBusy = true;
                }

                // Check 2: Nếu đặt ngay bây giờ, bàn đang phục vụ cũng là bận
                if (isBookingNow && ban.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
                    isBusy = true;
                }

                // Nếu không bận -> Thêm vào danh sách khả dụng
                if (!isBusy) {
                    dsBanKhaDung.add(ban);
                }
            }
        }

        // 5. Hiển thị (Logic cũ nhưng dùng dsBanKhaDung)
        boolean coBanDon = false;
        for (Ban ban : dsBanKhaDung) {
            // Chỉ hiện bàn đủ số lượng ghế
            if (ban.getSoGhe() >= soLuongKhach) {
                coBanDon = true;
                addBanPanelToView(ban); // Hiển thị bàn đơn
            }
        }

        // 6. Gợi ý ghép bàn (Dùng dsBanKhaDung thay vì dsBanTrongFull)
        if (!coBanDon) {
            // Bạn cần sửa hàm timGoiYGhepBan để nhận dsBanKhaDung làm tham số
            List<List<Ban>> dsGoiY = timGoiYGhepBan(soLuongKhach, dsBanKhaDung);

            if (!dsGoiY.isEmpty()) {
                JLabel lblGoiY = new JLabel("<html>Không có bàn đơn đủ chỗ. Gợi ý ghép bàn trống lúc " +
                        thoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm")) + ":</html>");
                lblGoiY.setForeground(Color.BLUE);
                pnlBanContainer.add(lblGoiY);
                for (List<Ban> capBan : dsGoiY) {
                    createNutGhepBan(capBan);
                }
            } else {
                pnlBanContainer.add(new JLabel("Không có bàn trống phù hợp vào giờ này."));
            }
        }

        pnlBanContainer.revalidate();
        pnlBanContainer.repaint();
    }

    // HÀM MỚI: Tạo nút cho Bàn Ghép
    private void createNutGhepBan(List<Ban> groupBan) {
        int tongGhe = groupBan.stream().mapToInt(Ban::getSoGhe).sum();
        String khuVuc = groupBan.get(0).getKhuVuc();

        StringBuilder sb = new StringBuilder("<html><center>");
        sb.append("Ghép ").append(groupBan.size()).append(" bàn:<br><b>");
        for (int i = 0; i < groupBan.size(); i++) {
            sb.append(groupBan.get(i).getTenBan());
            if (i < groupBan.size() - 1) sb.append(", ");
            // Xuống dòng sau mỗi 3 bàn để không bị tràn nút
            if ((i + 1) % 2 == 0 && i < groupBan.size() - 1) sb.append("<br>");
        }
        sb.append("</b><br><i>(Tổng ").append(tongGhe).append(" ghế)</i></center></html>");

        JToggleButton btnGhep = new JToggleButton();
        btnGhep.setLayout(new BorderLayout());

        btnGhep.setPreferredSize(new Dimension(180, 100));
        btnGhep.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGhep.setBackground(Color.WHITE);
        btnGhep.setForeground(Color.BLACK);

        JLabel lblIcon = new JLabel("🔗", SwingConstants.CENTER); // Icon liên kết
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 24));

        JLabel lblInfo = new JLabel(sb.toString(), SwingConstants.CENTER);

        btnGhep.add(lblIcon, BorderLayout.WEST);
        btnGhep.add(lblInfo, BorderLayout.CENTER);

        // Sự kiện click
        btnGhep.addActionListener(e -> {
            // Logic chọn bàn ghép
            dsBanDaChon.clear();
            dsBanDaChon.addAll(groupBan); // Thêm cả 2 bàn vào danh sách chọn

            // Reset các BanPanel đơn (nếu có)
            for (BanPanel bp : dsBanPanelHienThi) bp.setSelected(false);

            // Reset các nút ghép khác (thủ công vì không dùng ButtonGroup chung với BanPanel)
            Component[] comps = pnlBanContainer.getComponents();
            for (Component c : comps) {
                if (c instanceof JToggleButton && c != btnGhep) {
                    ((JToggleButton)c).setSelected(false);
                    c.setForeground(Color.BLACK);
                    c.setBackground(Color.WHITE);
                    updateLabelsColor((JToggleButton)c, Color.BLACK);
                }
            }

            if (btnGhep.isSelected()) {
                btnGhep.setBackground(new Color(56, 118, 243)); // Màu cam nhạt
                btnGhep.setForeground(Color.WHITE);
                updateLabelsColor(btnGhep, Color.WHITE);
            } else {
                dsBanDaChon.clear(); // Bỏ chọn
                btnGhep.setBackground(Color.WHITE);
                btnGhep.setForeground(Color.BLACK);
                updateLabelsColor(btnGhep, Color.BLACK);
            }
        });

        pnlBanContainer.add(btnGhep);
    }
    private void updateLabelsColor(JToggleButton button, Color color) {
        for (Component c : button.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(color);
            }
        }
    }
    private void updateBanPanelSelection() {
        for (BanPanel panel : dsBanPanelHienThi) {
            boolean isSelected = false;
            for (Ban b : dsBanDaChon) {
                if (panel.getBan().equals(b)) {
                    isSelected = true;
                    break;
                }
            }
            panel.setSelected(isSelected);
        }
        if (!dsBanDaChon.isEmpty() && dsBanDaChon.size() == 1) {
            Component[] comps = pnlBanContainer.getComponents();
            for (Component c : comps) {
                if (c instanceof JToggleButton) {
                    ((JToggleButton)c).setSelected(false);
                    c.setBackground(Color.WHITE);
                }
            }
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
        if (dsBanDaChon.isEmpty()) {
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
        LocalDateTime thoiGianDat = null;
        try {
            // Lấy Date từ spinner
            Date selectedDate = (Date) dateSpinner.getValue();
            Date selectedTime = (Date) timeSpinner.getValue();

            // Dùng Calendar để kết hợp ngày và giờ
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(selectedDate);

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(selectedTime);

            // Đặt giờ, phút, giây từ timeCal vào dateCal
            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            dateCal.set(Calendar.SECOND, 0); // Đặt giây = 0

            // Chuyển Calendar kết hợp sang LocalDateTime
            thoiGianDat = dateCal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            // Kiểm tra thời gian phải trong tương lai
            if (thoiGianDat.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Thời gian đặt phải trong tương lai!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return; // Dừng lại
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ngày hoặc giờ không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            ex.printStackTrace(); // In lỗi ra console để debug
            return; // Dừng lại
        }


        // 2. Tìm hoặc Tạo Khách Hàng
        entity.KhachHang kh = khachHangDAO.timTheoSDT(sdt);
        String maKHCanDung;
        if (kh == null) {
            // Nếu không tìm thấy khách hàng với SĐT này -> Khách hàng mới
            // Hiển thị hộp thoại hỏi có muốn thêm thành viên không
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Khách hàng mới với SĐT '" + sdt + "'.\nBạn có muốn thêm khách hàng này làm thành viên (Hạng MEMBER) không?",
                    "Xác nhận thêm khách hàng",
                    JOptionPane.YES_NO_CANCEL_OPTION, // Thêm nút Cancel
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                // --- Người dùng chọn CÓ (Thêm làm MEMBER) ---
                // Tạo khách hàng mới
                kh = new entity.KhachHang(); // Dùng constructor mặc định tự sinh mã KH
                kh.setTenKH(tenKH);          // Lấy tên từ ô nhập
                kh.setSdt(sdt);              // Lấy SĐT từ ô nhập
                kh.setHangThanhVien(entity.HangThanhVien.MEMBER); // Đặt hạng MEMBER
                // Đặt các giá trị mặc định khác nếu cần (Entity của bạn có thể đã làm)
                kh.setGioitinh("Khác"); // Hoặc một giá trị mặc định khác
                kh.setNgaySinh(java.time.LocalDate.of(2000, 1, 1)); // Mặc định
                kh.setDiaChi("");
                kh.setEmail(null);
                kh.setTongChiTieu(0);
                kh.setNgayThamGia(java.time.LocalDate.now());

                // Gọi DAO để thêm vào CSDL
                boolean themOK = khachHangDAO.themKhachHang(kh);
                if (themOK) {
                    maKHCanDung = kh.getMaKH(); // Lấy mã KH vừa tạo
                    JOptionPane.showMessageDialog(this, "Đã thêm khách hàng mới với hạng MEMBER.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    if (mainGUI_DatBan != null) {
                        mainGUI_DatBan.refreshKhachHangScreen(); // <-- GỌI HÀM CỦA MAIN GUI
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng mới vào CSDL!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    return; // Dừng xử lý nếu không thêm được KH
                }

            } else if (choice == JOptionPane.NO_OPTION) {
                // --- Người dùng chọn KHÔNG (Thêm làm NONE) ---
                // Tạo khách hàng mới
                kh = new entity.KhachHang();
                kh.setTenKH(tenKH);
                kh.setSdt(sdt);
                kh.setHangThanhVien(entity.HangThanhVien.NONE); // Đặt hạng NONE
                // Đặt các giá trị mặc định khác
                kh.setGioitinh("Khác");
                kh.setNgaySinh(java.time.LocalDate.of(2000, 1, 1));
                kh.setDiaChi("");
                kh.setEmail(null);
                kh.setTongChiTieu(0);
                kh.setNgayThamGia(java.time.LocalDate.now());

                // Gọi DAO để thêm vào CSDL
                boolean themOK = khachHangDAO.themKhachHang(kh);
                if (themOK) {
                    maKHCanDung = kh.getMaKH(); // Lấy mã KH vừa tạo
                    JOptionPane.showMessageDialog(this, "Đã thêm khách hàng mới (không phải thành viên).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    if (mainGUI_DatBan != null) {
                        mainGUI_DatBan.refreshKhachHangScreen(); // <-- THÊM Ở ĐÂY
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm khách hàng mới vào CSDL!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    return; // Dừng xử lý
                }

            } else {
                // Người dùng bấm Cancel hoặc đóng hộp thoại
                JOptionPane.showMessageDialog(this, "Đã hủy thao tác đặt bàn.", "Hủy bỏ", JOptionPane.INFORMATION_MESSAGE);
                return; // Dừng xử lý đặt bàn
            }
        } else {
            // Nếu khách hàng đã tồn tại (tìm thấy theo SĐT)
            maKHCanDung = kh.getMaKH(); // Lấy mã KH đã có
            // (Không cần cập nhật tên KH ở đây trừ khi bạn muốn cho phép sửa)
        }


        // 3 & 4. Tạo Đơn và Gọi DAO cho TỪNG BÀN
        boolean tatCaThanhCong = true;

        for (Ban ban : dsBanDaChon) {
            // Tạo đơn cho bàn này
            entity.DonDatMon ddm = new entity.DonDatMon();
            ddm.setNgayKhoiTao(LocalDateTime.now());
            ddm.setThoiGianDen(thoiGianDat);
            ddm.setMaNV("NV01102");
            ddm.setMaKH(maKHCanDung);
            ddm.setMaBan(ban.getMaBan()); // Set từng bàn

            // Với bàn ghép, nên ghi chú thêm vào
            if (dsBanDaChon.size() > 1) {
                String ghiChuGhep = "Ghép với ";
                for (Ban bKhac : dsBanDaChon) {
                    if (!bKhac.equals(ban)) ghiChuGhep += bKhac.getTenBan() + " ";
                }
                ddm.setGhiChu(txtGhiChu.getText() + " (" + ghiChuGhep.trim() + ")");
            } else {
                ddm.setGhiChu(txtGhiChu.getText());
            }

            // Lưu đơn
            if (donDatMonDAO.themDonDatMon(ddm)) {
                // Cập nhật bàn
                ban.setTrangThai(TrangThaiBan.DA_DAT_TRUOC);
                ban.setGioMoBan(thoiGianDat);
                if (!banDAO.updateBan(ban)) {
                    tatCaThanhCong = false;
                }
            } else {
                tatCaThanhCong = false;
            }
        }

        if (tatCaThanhCong) {
            // Tải lại list bàn trống
            taiDanhSachBanTrong();
            // Hiển thị lại bàn (các bàn vừa đặt sẽ mất khỏi panel trái)
            hienThiBanPhuHop();
            // Cập nhật list bên phải
            loadDanhSachDatTruoc();

            // Xóa input
            spinnerSoLuongKhach.setValue(1);
            Calendar calReset = Calendar.getInstance();
            calReset.add(Calendar.HOUR_OF_DAY, 1);
            calReset.set(Calendar.MINUTE, 0);
            timeSpinner.setValue(calReset.getTime());
            dateSpinner.setValue(new java.util.Date()); // Reset ngày về hôm nay

            txtGhiChu.setText("");
            txtSDTKhach.setText("");
            txtHoTenKhach.setText("");

            // Reset biến chọn
            dsBanDaChon.clear();

            // Gọi làm mới màn hình Bàn (ManHinhBanGUI)
            if (parentDanhSachBanGUI_DatBan != null) {
                parentDanhSachBanGUI_DatBan.refreshManHinhBan();
            }

            JOptionPane.showMessageDialog(this, "Đặt bàn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi đặt bàn! Vui lòng kiểm tra lại.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            // TODO: Có thể cần logic Rollback (xóa các đơn đã tạo lỡ dở) ở đây nếu muốn hoàn hảo
        }
    }


    /**
     * Tải danh sách các phiếu đặt trước (chưa nhận) lên JList bên phải.
     */
    private void loadDanhSachDatTruoc() {
        modelListPhieuDat.clear(); // Xóa list cũ
        try {
            List<entity.DonDatMon> dsDatTruoc = donDatMonDAO.getAllDonDatMonChuaNhan(); // Lấy list object

            if (dsDatTruoc.isEmpty()) {
                // Thêm một object đặc biệt hoặc để trống
                // modelListPhieuDat.addElement(null); // Hoặc không thêm gì cả
                System.out.println("Không có đơn đặt trước nào."); // Hoặc hiển thị label
            } else {
                for (entity.DonDatMon ddm : dsDatTruoc) {
                    modelListPhieuDat.addElement(ddm); // Thêm object vào model
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải danh sách đặt trước: " + e.getMessage());
            // modelListPhieuDat.addElement(null); // Hoặc thông báo lỗi
        }
        // Cập nhật JList (quan trọng)
        listPhieuDat.setModel(modelListPhieuDat); // Đặt lại model để JList nhận biết thay đổi data type
        listPhieuDat.repaint();
    }
    public void refreshData() {
        donDatMonDAO.tuDongHuyDonQuaGio();
        // 1. Tải lại danh sách bàn từ CSDL (để cập nhật trạng thái Trống/Đang phục vụ)
        taiDanhSachBanTrong();

        // 2. Vẽ lại giao diện bàn (dựa trên giờ đang chọn trong spinner)
        hienThiBanPhuHop();

        // 3. Cập nhật danh sách phiếu đặt bên phải
        loadDanhSachDatTruoc();

        System.out.println("ManHinhDatBanGUI: Đã làm mới dữ liệu!");
    }
    private void xuLyHuyDatBan(DonDatMon ddmToCancel, int index) {
        // 1. Xác nhận
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy đặt bàn cho mã đơn '" + ddmToCancel.getMaDon() + "'?",
                "Xác nhận hủy đặt bàn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // 2. Gọi DAO xóa DonDatMon
            boolean xoaDonOK = donDatMonDAO.xoaDonDatMon(ddmToCancel.getMaDon());

            if (xoaDonOK) {
                // 3. Tìm và cập nhật lại trạng thái Bàn
                Ban banCanUpdate = banDAO.getBanByMa(ddmToCancel.getMaBan());
                if (banCanUpdate != null && banCanUpdate.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                    banCanUpdate.setTrangThai(TrangThaiBan.TRONG);
                    banCanUpdate.setGioMoBan(null); // Reset giờ đặt
                    boolean updateBanOK = banDAO.updateBan(banCanUpdate);
                    if (!updateBanOK) {
                        JOptionPane.showMessageDialog(this, "Hủy đơn thành công nhưng lỗi cập nhật lại trạng thái bàn!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.err.println("Không tìm thấy bàn " + ddmToCancel.getMaBan() + " hoặc trạng thái không phải DA_DAT_TRUOC để reset.");
                }

                // 4. Cập nhật giao diện
                // Xóa item khỏi JList bên phải
                modelListPhieuDat.removeElementAt(index);
                // Tải lại danh sách bàn trống và hiển thị lại panel bên trái
                taiDanhSachBanTrong();
                hienThiBanPhuHop();
                if (parentDanhSachBanGUI_DatBan != null) {
                    parentDanhSachBanGUI_DatBan.refreshManHinhBan(); // <-- GỌI LÀM MỚI
                }

                JOptionPane.showMessageDialog(this, "Đã hủy đặt bàn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Hủy đặt bàn thất bại! Vui lòng thử lại.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Renderer tùy chỉnh cho JList bên phải ---
    // (Class này nên để thành inner class hoặc file riêng)
    private class PhieuDatListRenderer implements ListCellRenderer<DonDatMon> { // Sửa: Dùng ListCellRenderer<DonDatMon>

        private final JPanel mainPanel;
        private final JPanel textPanel;
        private final JLabel lblLine1; // Dòng trên: Bàn (SDT)
        private final JLabel lblLine2; // Dòng dưới: Giờ - Tên KH - Số người
        private final JButton btnDelete;
        private final JSeparator separator; // Đường kẻ phân cách

        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        private final Font mainFont = new Font("Segoe UI", Font.BOLD, 14);
        private final Font subFont = new Font("Segoe UI", Font.PLAIN, 13); // Tăng size chữ phụ
        private final Color textColor = Color.DARK_GRAY;
        private final Color timeColor = Color.BLACK; // Đổi màu giờ thành đen
        private final Color separatorColor = new Color(220, 220, 220); // Màu đường kẻ

        public PhieuDatListRenderer() {
            // --- Cấu trúc Panel cho mỗi Item ---
            mainPanel = new JPanel(new BorderLayout(10, 0)); // Panel chính, cách nút Xóa 10px
            mainPanel.setBorder(new EmptyBorder(8, 10, 8, 10)); // Lề trên/dưới 8, trái/phải 10

            // Panel chứa 2 dòng text (xếp dọc)
            textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false); // Nền trong suốt

            lblLine1 = new JLabel(" "); // Khởi tạo label trống
            lblLine1.setFont(mainFont);
            lblLine1.setForeground(textColor);

            lblLine2 = new JLabel(" "); // Khởi tạo label trống
            lblLine2.setFont(subFont);
            lblLine2.setForeground(timeColor);

            textPanel.add(lblLine1);
            textPanel.add(Box.createRigidArea(new Dimension(0, 3))); // Khoảng cách nhỏ giữa 2 dòng
            textPanel.add(lblLine2);

            // Nút Xóa (JButton màu đỏ)
            btnDelete = new JButton("X");
            btnDelete.setFont(new Font("Arial", Font.BOLD, 16));
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setBackground(new Color(239, 68, 68)); // Màu đỏ giống ManHinhBanGUI
            btnDelete.setFocusPainted(false);
            btnDelete.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding cho nút
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setPreferredSize(new Dimension(40, 40)); // Kích thước nút X
            // Không thêm ActionListener ở đây, JList sẽ xử lý

            // Đường kẻ phân cách
            separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setForeground(separatorColor);

            // Gắn các thành phần vào mainPanel
            mainPanel.add(textPanel, BorderLayout.CENTER);
            mainPanel.add(btnDelete, BorderLayout.EAST);
            // Không thêm separator trực tiếp vào item panel
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends DonDatMon> list, DonDatMon value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            // Lấy dữ liệu và cập nhật Labels (Giống logic cũ, gọi DAO)
            if (value instanceof DonDatMon) {
                DonDatMon ddm = value;
                String tenBan = banDAO.getTenBanByMa(ddm.getMaBan());
                KhachHang kh = (ddm.getMaKH() != null) ? khachHangDAO.timTheoMaKH(ddm.getMaKH()) : null;
                String tenKH = (kh != null) ? kh.getTenKH() : "Vãng lai";
                String sdtKH = (kh != null) ? kh.getSdt() : "--";
//                Ban banDat = banDAO.getBanByMa(ddm.getMaBan());
//                String gioDen = (banDat != null && banDat.getGioMoBan() != null) ? banDat.getGioMoBan().format(timeFormatter) : "N/A";
                // Lấy số người từ spinner lúc đặt (Cần lưu vào DonDatMon)
                String gioDen = "N/A";
                if (ddm.getThoiGianDen() != null) {
                    gioDen = ddm.getThoiGianDen().format(timeFormatter);
                } else if (ddm.getNgayKhoiTao() != null) {
                    // Fallback nếu thoiGianDen null (dữ liệu cũ)
                    gioDen = ddm.getNgayKhoiTao().format(timeFormatter);
                }
                // Tạm thời vẫn dùng số ghế:
                int soNguoi = 0;
                Ban banDat = banDAO.getBanByMa(ddm.getMaBan());
                if (banDat != null) soNguoi = banDat.getSoGhe();

                lblLine1.setText(String.format("%s (%s)", tenBan, sdtKH));
                lblLine2.setText(String.format("%s - %s - %d người", gioDen, tenKH, soNguoi));

                // Hiện nút xóa cho item hợp lệ
                btnDelete.setVisible(true);
            } else {
                String message;
                // Lấy text tìm kiếm và placeholder
                String currentSearchText = txtTimKiemPhieuDat.getText().trim();
                final String placeholder = " Tìm kiếm bàn đặt SĐT/Tên khách...";

                // Kiểm tra xem người dùng có đang tìm kiếm không
                if (!currentSearchText.isEmpty() && !currentSearchText.equals(placeholder)) {
                    // Nếu đang tìm kiếm mà value là null -> Không tìm thấy
                    message = "Không tìm thấy kết quả phù hợp.";
                } else {
                    // Nếu không tìm kiếm mà value là null -> Chưa có đơn nào
                    message = "Chưa có bàn nào được đặt trước.";
                }

                lblLine1.setText(message); // Hiển thị thông báo ở dòng 1
                lblLine1.setFont(subFont);    // Dùng font nhỏ hơn
                lblLine1.setForeground(Color.GRAY); // Màu xám
                lblLine2.setText(" ");       // Dòng 2 để trống
                btnDelete.setVisible(false); // Ẩn nút xóa
            }

            // Xử lý màu nền khi chọn/không chọn
            if (isSelected) {
                mainPanel.setBackground(list.getSelectionBackground()); // Màu nền khi chọn
                mainPanel.setForeground(list.getSelectionForeground()); // Màu chữ khi chọn (thường không cần)
                textPanel.setOpaque(true); // Cần đặt opaque để thấy màu nền
                textPanel.setBackground(list.getSelectionBackground());
                lblLine1.setForeground(Color.WHITE); // Đổi chữ thành trắng khi nền xanh
                lblLine2.setForeground(Color.WHITE);
            } else {
                mainPanel.setBackground(list.getBackground());
                mainPanel.setForeground(list.getForeground());
                textPanel.setOpaque(false);
                lblLine1.setForeground(textColor);
                lblLine2.setForeground(timeColor);
            }
            btnDelete.setBackground(mainPanel.getBackground());
            if (isSelected) btnDelete.setForeground(Color.DARK_GRAY); else btnDelete.setForeground(Color.RED);
            JPanel containerPanel = new JPanel(new BorderLayout());
            containerPanel.setBackground(list.getBackground());
            containerPanel.add(mainPanel, BorderLayout.CENTER);
            containerPanel.add(separator, BorderLayout.SOUTH);
            return containerPanel;
        }
    }
    private void addBanPanelToView(Ban ban) {
        // Tạo panel giao diện cho bàn
        BanPanel banPanel = new BanPanel(ban);

        // Thêm vào danh sách quản lý để sau này đổi màu khi chọn
        dsBanPanelHienThi.add(banPanel);

        // Thêm vào giao diện chính
        pnlBanContainer.add(banPanel);

        // Thêm sự kiện Click chuột
        banPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Logic khi chọn bàn đơn:
                    // 1. Xóa hết các bàn đã chọn trước đó (vì đang chọn đơn)
                    dsBanDaChon.clear();

                    // 2. Thêm bàn này vào danh sách chọn
                    dsBanDaChon.add(ban);

                    // 3. Reset các nút "Ghép bàn" (nếu có) về trạng thái không chọn
                    Component[] comps = pnlBanContainer.getComponents();
                    for (Component c : comps) {
                        if (c instanceof JToggleButton) {
                            ((JToggleButton)c).setSelected(false);
                            c.setBackground(Color.WHITE);
                            // Reset màu chữ nút ghép (nếu có hàm helper)
                            if (c instanceof JToggleButton) {
                                updateLabelsColor((JToggleButton)c, Color.BLACK);
                            }
                        }
                    }

                    // 4. Cập nhật giao diện (tô viền xanh cho bàn được chọn)
                    updateBanPanelSelection();

                    System.out.println("Đã chọn bàn đơn: " + ban.getTenBan());
                }
            }
        });
    }
}