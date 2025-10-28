package gui;

import dao.*;

import entity.*;

import java.time.format.DateTimeFormatter;

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
    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;
    private KhachHangDAO khachHangDAO;
    private DonDatMonDAO donDatMonDAO;
    private Ban selectedTable = null;
    private JPanel leftTableContainer;
    private JPanel statsPanel;
    private JTextField txtMaKhuyenMai;
    private JButton btnApDungKM;
    private String currentLeftFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private DanhSachBanGUI parentDanhSachBanGUI;
    private KhuyenMaiDAO maKhuyenMaiDAO;

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
    private BillPanel billPanel;
    // --- KẾT THÚC THÊM BIẾN ---

    public ManHinhBanGUI(DanhSachBanGUI parent) {
        super(new BorderLayout());
        this.parentDanhSachBanGUI = parent;
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.donDatMonDAO = new DonDatMonDAO();
        this.maKhuyenMaiDAO = new KhuyenMaiDAO();
        buildUI();
    }
    private void xuLyApDungKhuyenMai() {
        String maKM_input = txtMaKhuyenMai.getText().trim().toUpperCase(); // Lấy mã nhập vào, chuẩn hóa
        HoaDon activeHoaDon = getActiveHoaDon(); // Lấy HĐ hiện tại (cần hàm này)

        if (activeHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Chưa có hóa đơn nào đang hoạt động!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        entity.KhuyenMai km = null;
        String maKMLuuVaoDB = null;
        if (!maKM_input.isEmpty()) {
            // Gọi DAO để kiểm tra mã
            km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(maKM_input);
            if (km == null) {
                JOptionPane.showMessageDialog(this, "Mã khuyến mãi không hợp lệ, đã hết hạn hoặc không tồn tại!", "Lỗi Mã KM", JOptionPane.WARNING_MESSAGE);
                // Xóa mã KM khỏi Hóa đơn nếu trước đó đã áp dụng mã khác
                if (activeHoaDon.getMaKM() != null) {
                    activeHoaDon.setMaKM(null); // Reset mã KM
                    maKMLuuVaoDB = null;
                }else {
                    txtMaKhuyenMai.requestFocus();
                    return;
                }
                txtMaKhuyenMai.requestFocus(); // Focus lại ô nhập
                return; // Dừng lại
            } else {
                // Tìm thấy mã hợp lệ, gán vào Hóa đơn
                activeHoaDon.setMaKM(maKM_input); // Lưu mã đã áp dụng
                maKMLuuVaoDB = maKM_input;
                JOptionPane.showMessageDialog(this, "Đã áp dụng mã: " + km.getTenChuongTrinh(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Nếu người dùng xóa trống ô -> Hủy áp dụng mã
            activeHoaDon.setMaKM(null);
            maKMLuuVaoDB = null;
        }
        boolean updateDAOK = hoaDonDAO.capNhatMaKM(activeHoaDon.getMaHD(), maKMLuuVaoDB);
        if (!updateDAOK) {
            System.err.println("LỖI: Không thể cập nhật maKM vào CSDL!");
            // Có thể báo lỗi cho người dùng, nhưng vẫn tiếp tục tính toán tạm thời
        } else {
            System.out.println("Đã cập nhật maKM '" + maKMLuuVaoDB + "' vào CSDL.");
        }
        // --- Tính toán lại tổng giảm giá và cập nhật HĐ, BillPanel ---
        activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
        updateBillPanelFromHoaDon(activeHoaDon);
    }

    public HoaDon getActiveHoaDon() {
        if (selectedTable != null && selectedTable.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) {
            // Chỉ tìm hóa đơn nếu bàn đang phục vụ
            return hoaDonDAO.getHoaDonChuaThanhToan(selectedTable.getMaBan());
        }
        return null; // Trả về null nếu bàn trống, đã đặt, hoặc chưa chọn
    }
    private void updateBillPanelFromHoaDon(HoaDon hoaDon) {
        if (billPanel != null && hoaDon != null) {
            int tongSoLuong = 0;
            if(hoaDon.getDsChiTiet() != null){
                for(ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
                    tongSoLuong += ct.getSoluong();
                }
            }
            billPanel.loadBillTotals(
                    (long) hoaDon.getTongTien(),
                    (long) hoaDon.getGiamGia(),
                    (long) hoaDon.getVat(), // Giả sử có getVat()
                    (long) hoaDon.getTongThanhToan(),
                    tongSoLuong
            );
        } else if (billPanel != null) {
            billPanel.clearBill(); // Xóa bill nếu không có HĐ
        }
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

        try {
            // Tải danh sách bàn từ CSDL
            this.allTablesFromDB = banDAO.getAllBan();

            // Cập nhật lại bộ đếm static trong class Ban
            // để khi tạo bàn MỚI, mã bàn sẽ tiếp tục từ số lớn nhất
            int maxSoThuTu = banDAO.getSoThuTuBanLonNhat();
            Ban.setSoThuTuBanHienTai(maxSoThuTu);

            System.out.println("Tải thành công " + allTablesFromDB.size() + " bàn từ CSDL.");
            System.out.println("Bộ đếm bàn tiếp theo được set là: " + (maxSoThuTu + 1));

        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, khởi tạo danh sách rỗng để tránh NullPointerException
            this.allTablesFromDB = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối hoặc tải dữ liệu Bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE);
        }


        // --- 2. TẠO PANEL BÊN TRÁI ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        JPanel listPanel = createListPanel("Danh sách toàn bộ bàn");
        this.statsPanel = createStatsPanel();
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
        updateStatsPanel();
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
        gbc.insets = new Insets(5, 8, 10, 8);
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
        gbc.weighty = 0.0;
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL; // <-- SỬA: Chỉ co giãn ngang
        infoPanel.add(createInfoBox("Số lượng khách", txtSoLuongKhach), gbc);

        txtMaKhuyenMai = createStyledTextField(true); // Tạo ô nhập
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 0.5; // Chiếm nhiều không gian hơn
        infoPanel.add(createInfoBox("Mã khuyến mãi", txtMaKhuyenMai), gbc);
        btnApDungKM = new JButton("Áp dụng");
        // Style nút (có thể tạo hàm helper)
        btnApDungKM.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApDungKM.setBackground(ManHinhBanGUI.COLOR_STATUS_RESERVED); // Màu xanh lá
        btnApDungKM.setForeground(Color.DARK_GRAY);
        btnApDungKM.setFocusPainted(false);
        btnApDungKM.setPreferredSize(new Dimension(80, 35)); // Kích thước nút
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.2; // Chiếm ít không gian
        gbc.fill = GridBagConstraints.NONE; // Không co giãn nút
        gbc.anchor = GridBagConstraints.SOUTHWEST; // Đặt nút ở dưới, bên trái ô Mã KM
        gbc.insets = new Insets(20, 0, 10, 8); // Chỉnh lề (trên 20 để xuống dưới label)
        infoPanel.add(btnApDungKM, gbc);

        gbc.gridy = 3; // Hàng thứ 4
        gbc.gridx = 0; gbc.gridwidth = 3; gbc.weightx = 1.0; // Kéo dài cả 3 cột
        gbc.fill = GridBagConstraints.HORIZONTAL; // Chỉ co giãn ngang
        gbc.anchor = GridBagConstraints.CENTER; // Reset anchor
        gbc.insets = new Insets(0, 8, 10, 8); // Reset lề
        infoPanel.add(createInfoBox("Ghi chú", txtGhiChu), gbc);

        // 5. Panel Bill (Nửa dưới)
        this.billPanel = new BillPanel();

        // 6. Thêm 2 panel vào container
        JSplitPane verticalSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, // Chia DỌC
                infoPanel,                 // Nửa TRÊN
                this.billPanel               // Nửa DƯỚI
        );

        // --- ĐÂY LÀ CHÌA KHÓA ---
        // Set vị trí thanh chia (ví dụ: 300px từ trên xuống)
        // Bạn có thể chỉnh số này sau
        verticalSplitPane.setDividerLocation(230);
        verticalSplitPane.setBorder(null); // Bỏ viền

        // 6. Thêm JSplitPane vào GIỮA panel chính
        panel.add(verticalSplitPane, BorderLayout.CENTER);
        btnApDungKM.addActionListener(e -> xuLyApDungKhuyenMai());

        return panel;
    }
    private void timKiemThanhVienTuSDT() {
        String sdt = txtSDTKhach.getText().trim();
        entity.KhachHang kh = null; // Khách hàng tìm được
        String maKH_LuuVaoDB = null; // Mã KH để cập nhật CSDL

        // 1. Tìm khách hàng
        if (!sdt.isEmpty() && sdt.matches("\\d{10}")) { // Chỉ tìm nếu SĐT hợp lệ
            kh = khachHangDAO.timTheoSDT(sdt);
        }

        // 2. Cập nhật các ô JTextField
        if (kh != null) {
            // Nếu tìm thấy
            txtHoTenKhach.setText(kh.getTenKH());
            txtThanhVien.setText(kh.getHangThanhVien().toString());
            maKH_LuuVaoDB = kh.getMaKH(); // Sẽ lưu mã này
        } else {
            // Nếu không tìm thấy (hoặc SĐT rỗng/không hợp lệ)
            txtHoTenKhach.setText(""); // Xóa tên
            txtThanhVien.setText("Chưa là thành viên"); // Hoặc "Vãng lai"
            maKH_LuuVaoDB = null; // Sẽ lưu NULL
        }

        // 3. Lấy Hóa đơn đang hoạt động
        HoaDon activeHoaDon = getActiveHoaDon();

        // 4. Cập nhật Hóa đơn (nếu có HĐ)
        if (activeHoaDon != null) {
            // 4a. Cập nhật maKH trong CSDL
            // (Chỉ cập nhật nếu mã KH thay đổi so với HĐ hiện tại để tránh gọi DB thừa)
            String maKHHienTai = activeHoaDon.getMaKH();
            boolean needDBUpdate = (maKHHienTai == null && maKH_LuuVaoDB != null) ||
                    (maKHHienTai != null && !maKHHienTai.equals(maKH_LuuVaoDB));

            if (needDBUpdate) {
                System.out.println("Cập nhật maKH trong CSDL: " + maKH_LuuVaoDB);
                boolean updateOK = donDatMonDAO.capNhatMaKH(activeHoaDon.getMaDon(), maKH_LuuVaoDB);
                if (!updateOK) {
                    System.err.println("LỖI: Không thể cập nhật maKH vào CSDL!");
                    // Vẫn tiếp tục để tính toán tạm thời
                }
            }

            // 4b. Cập nhật maKH trên object trong bộ nhớ
            activeHoaDon.setMaKH(maKH_LuuVaoDB);

            // 4c. Tính toán lại giảm giá
            tinhVaCapNhatGiamGia(activeHoaDon);

            // 4d. Cập nhật BillPanel
            updateBillPanelFromHoaDon(activeHoaDon);

        } else {
            System.out.println("timKiemThanhVienTuSDT: Không có hóa đơn hoạt động để áp dụng giảm giá TV.");
        }
    }
    private void tinhVaCapNhatGiamGia(HoaDon hoaDon) {
        if (hoaDon == null) return;

        // Lấy tổng tiền món ăn (chưa giảm) từ Hóa đơn
        // Đảm bảo HĐ đã được cập nhật list chi tiết và tính tổng tiền
        // (Giả sử getTongTien() trả về tổng tiền món ăn)
        float tongCong = hoaDon.getTongTien();
        float giamGiaTV = 0;
        float giamGiaMa = 0;

        // 1. Tính giảm giá thành viên
        if (hoaDon.getMaKH() != null) {
            KhachHang kh = khachHangDAO.timTheoMaKH(hoaDon.getMaKH());
            if (kh != null) {
                // Lấy % giảm giá theo hạng (từ hàm helper bên dưới)
                float phanTramGiamTV = getPhanTramGiamTheoHang(kh.getHangThanhVien());
                giamGiaTV = tongCong * phanTramGiamTV / 100;
            }
        }

        // 2. Tính giảm giá theo Mã KM (nếu có)
        if (hoaDon.getMaKM() != null && !hoaDon.getMaKM().isEmpty()) {
            entity.KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(hoaDon.getMaKM());
            if (km != null) {
                // Kiểm tra điều kiện (tổng tiền tối thiểu)
                if (tongCong >= km.getDieuKienApDung()) {
                    if ("Phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm theo phần trăm".equalsIgnoreCase(km.getLoaiKhuyenMai())) {
                        giamGiaMa = tongCong * (float)km.getGiaTri() / 100;
                    } else if ("Số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai()) || "Giảm giá số tiền".equalsIgnoreCase(km.getLoaiKhuyenMai())){
                        giamGiaMa = (float)km.getGiaTri();
                    }
                } else {
                    // Không đủ điều kiện -> Báo lỗi và tự động hủy mã
                    JOptionPane.showMessageDialog(this, "Hóa đơn không đủ điều kiện (cần " + km.getDieuKienApDung() + "đ) để áp dụng mã " + hoaDon.getMaKM(), "Không đủ điều kiện", JOptionPane.WARNING_MESSAGE);
                    hoaDon.setMaKM(null); // Reset mã KM trên HĐ
                    txtMaKhuyenMai.setText(""); // Xóa ô nhập
                    hoaDonDAO.capNhatMaKM(hoaDon.getMaHD(), null); // Cập nhật CSDL
                }
            } else {
                // Mã KM trên HĐ không còn hợp lệ (hết hạn,...) -> Reset
                System.out.println("Mã KM " + hoaDon.getMaKM() + " không còn hợp lệ.");
                hoaDon.setMaKM(null);
                txtMaKhuyenMai.setText(""); // Xóa ô nhập
                hoaDonDAO.capNhatMaKM(hoaDon.getMaHD(), null); // Cập nhật CSDL
            }
        }

        // 3. Tính tổng giảm giá (Cộng dồn)
        float tongGiamGia = giamGiaTV + giamGiaMa;

        // 4. Cập nhật vào đối tượng HoaDon
        hoaDon.setGiamGia(tongGiamGia);
        hoaDon.setVat(0); // Tạm thời VAT = 0
        hoaDon.tinhLaiTongThanhToan(); // Gọi hàm tính tổng cuối cùng
    }

    /**
     * HELPER: Lấy % giảm giá thanh toán dựa trên hạng thành viên.
     * (Dựa theo ảnh image_7a8bd0.png bạn gửi)
     */
    private float getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) return 0.0f;
        switch (hang) {
            case DIAMOND: return 10.0f; // Kim Cương -10%
            case GOLD:    return 5.0f;  // Vàng -5%
            case SILVER:  return 3.0f;  // Bạc -3%
            case BRONZE:  return 2.0f;  // Đồng -2%
            case MEMBER:  return 0.0f;  // Thành viên member 0%
            case NONE: default: return 0.0f;
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
        // --- Định dạng ngày giờ ---
        DateTimeFormatter dtfNgay = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter dtfGio = DateTimeFormatter.ofPattern("HH:mm");
        HoaDon activeHoaDon = null;
        if (ban != null) {
            // (Code xác định statusColor, tinhTrangText giữ nguyên)
            Color statusColor;
            String tinhTrangText;
            switch (ban.getTrangThai()) {
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
            statusColorBox.setBackground(statusColor);
            lblTenBanHeader.setText(ban.getTenBan() + " -");
            lblKhuVucHeader.setText(ban.getKhuVuc());

            // --- 3. CẬP NHẬT INFO (SỬA Ở ĐÂY) ---
            txtTinhTrang.setText(tinhTrangText);

            // Lấy giờ mở bàn (nếu có)
            LocalDateTime gioMoBan = ban.getGioMoBan();

            if (ban.getTrangThai() == TrangThaiBan.TRONG) {
                // Nếu bàn trống, reset mọi trường
                cmbPTThanhToan.setSelectedItem("Tiền mặt");
                txtSDTKhach.setText("");
                txtHoTenKhach.setText("");
                txtThanhVien.setText("");
                txtSoLuongKhach.setText("");
                txtGhiChu.setText("");
                txtNgayVao.setText("");
                txtGioVao.setText("");
            }
            else if (ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC) {
                // Lấy thông tin từ Bàn (Giờ đặt)
                txtNgayVao.setText(gioMoBan != null ? gioMoBan.format(dtfNgay) : "");
                txtGioVao.setText(gioMoBan != null ? gioMoBan.format(dtfGio) : "");
                // Lấy số ghế làm số lượng khách tạm thời
                txtSoLuongKhach.setText(String.valueOf(ban.getSoGhe()));

                // --- GỌI DAO ĐỂ LẤY THÔNG TIN ĐẶT BÀN ---
                entity.DonDatMon ddm = donDatMonDAO.getDonDatMonDatTruoc(ban.getMaBan());
                System.out.println("DEBUG: DonDatMon tìm thấy: " + ddm);
                entity.KhachHang kh = null;

                if (ddm != null) { // Thêm kiểm tra null ở đây cho an toàn
                    System.out.println("DEBUG: Ghi chú từ ddm: '" + ddm.getGhiChu() + "'"); // In ra giá trị ghiChu
                    if (ddm.getMaKH() != null) {
                        kh = khachHangDAO.timTheoMaKH(ddm.getMaKH());
                    }
                }

                // Cập nhật các ô
                cmbPTThanhToan.setSelectedItem("Chưa thanh toán");
                if (kh != null) {
                    txtSDTKhach.setText(kh.getSdt());
                    txtHoTenKhach.setText(kh.getTenKH());
                    txtThanhVien.setText(kh.getHangThanhVien().toString());
                } else {
                    txtSDTKhach.setText("");
                    txtHoTenKhach.setText("");
                    txtThanhVien.setText("Vãng lai");
                }

                txtGhiChu.setText( (ddm != null && ddm.getGhiChu() != null) ? ddm.getGhiChu() : "" );
            }
            else { // Đang phục vụ
                // Lấy thông tin từ Bàn (Giờ vào)
                activeHoaDon = hoaDonDAO.getHoaDonChuaThanhToan(ban.getMaBan());

                if (activeHoaDon != null) {
                    // Lấy thông tin từ Hóa Đơn (như cũ)
                    LocalDateTime gioVao = activeHoaDon.getNgayLap();
                    txtNgayVao.setText(gioVao != null ? gioVao.format(dtfNgay) : "");
                    txtGioVao.setText(gioVao != null ? gioVao.format(dtfGio) : "");
                    cmbPTThanhToan.setSelectedItem(activeHoaDon.getHinhThucThanhToan());

                    // --- THAY THẾ KHỐI TODO BẰNG CODE NÀY ---
                    entity.KhachHang kh = null;
                    if (activeHoaDon.getMaKH() != null) {
                        // Lấy khách hàng từ maKH trên hóa đơn
                        kh = khachHangDAO.timTheoMaKH(activeHoaDon.getMaKH());
                    }

                    if (kh != null) {
                        txtSDTKhach.setText(kh.getSdt());
                        txtHoTenKhach.setText(kh.getTenKH());
                        // Chuyển Enum (VD: HangThanhVien.GOLD) thành String ("GOLD")
                        txtThanhVien.setText(kh.getHangThanhVien().toString());
                    } else {
                        // Trường hợp không tìm thấy KH (dữ liệu lỗi)
                        txtSDTKhach.setText("");
                        txtHoTenKhach.setText("");
                        txtThanhVien.setText("Vãng lai");
                    }
                    // --- KẾT THÚC THAY THẾ ---
                    txtMaKhuyenMai.setText(activeHoaDon.getMaKM() != null ? activeHoaDon.getMaKM() : "");
                    // (Để trống 2 ô này, chúng sẽ được nhập tay)
                    txtSoLuongKhach.setText("");
                    txtGhiChu.setText("");
                }
            }

        } else {
            // --- RESET (Giữ nguyên) ---
            statusColorBox.setBackground(COLOR_STATUS_FREE);
            lblTenBanHeader.setText("Chọn một bàn");
            lblKhuVucHeader.setText("");
            cmbPTThanhToan.setSelectedItem("Tiền mặt");
            txtSDTKhach.setText("");
            txtHoTenKhach.setText("");
            txtThanhVien.setText("");
            txtMaKhuyenMai.setText("");
            txtSoLuongKhach.setText("");
            txtGhiChu.setText("");
            txtNgayVao.setText("");
            txtGioVao.setText("");
        }
        if (activeHoaDon != null) {
            activeHoaDon.tinhLaiGiamGiaVaTongTien(khachHangDAO, maKhuyenMaiDAO);
        }
        updateBillPanelFromHoaDon(activeHoaDon);
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
    private void updateStatsPanel() {
        // Lấy panel cha của statsPanel hiện tại (là leftPanel)
        Container parent = statsPanel.getParent();
        if (parent != null) {
            // Xóa statsPanel cũ khỏi panel cha
            parent.remove(statsPanel);

            // Tạo statsPanel MỚI với dữ liệu cập nhật
            this.statsPanel = createStatsPanel(); // Gọi lại hàm create để tính toán lại

            // Thêm statsPanel mới vào vị trí cũ (SOUTH của leftPanel)
            parent.add(this.statsPanel, BorderLayout.SOUTH);

            // Vẽ lại panel cha để hiển thị thay đổi
            parent.revalidate();
            parent.repaint();
            System.out.println("Stats Panel updated!"); // Debug
        } else {
            System.err.println("Không tìm thấy panel cha của statsPanel để cập nhật!");
        }
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
        System.out.println("populateLeftPanel: Bắt đầu xóa components..."); // DEBUG
        leftTableContainer.removeAll();
        leftBanPanelList.clear();
        System.out.println("populateLeftPanel: Đã xóa. Bắt đầu thêm lại..."); // DEBUG
        int countAdded = 0; // Đếm số panel đã thêm
        for (Ban ban : allTablesFromDB) {
            if (khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter)) {
                System.out.println("  -> Tạo BanPanel cho: " + ban.getMaBan() + " - Trạng thái: " + ban.getTrangThai());
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
                countAdded++;
            }
        }
        System.out.println("populateLeftPanel: Đã thêm " + countAdded + " BanPanel."); // DEBUG
        System.out.println("populateLeftPanel: Gọi revalidate/repaint..."); // DEBUG
        leftTableContainer.revalidate();
        leftTableContainer.repaint();
        System.out.println("populateLeftPanel: Kết thúc."); // DEBUG
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
        updateRightPanelDetails(selectedTable);

    }
    public Ban getSelectedTable() {
        return selectedTable; // Trả về biến thành viên selectedTable
    }
    public void refreshTableList() {
        System.out.println("Bắt đầu refreshTableList...");
        try {
            List<Ban> oldData = this.allTablesFromDB;
            // 1. Tải lại dữ liệu mới nhất
            this.allTablesFromDB = banDAO.getAllBan();
            System.out.println("ManHinhBanGUI: Dữ liệu bàn mới đã tải. Size: " + allTablesFromDB.size()); // DEBUG
            if (selectedTable != null) { // Giả sử selectedTable là bàn vừa thanh toán
                for(Ban b : allTablesFromDB) {
                    if (b.getMaBan().equals(selectedTable.getMaBan())) {
                        System.out.println("DEBUG: Trạng thái bàn " + b.getMaBan() + " sau khi tải lại: " + b.getTrangThai());
                        break;
                    }
                }
            }
            // 2. Vẽ lại panel bàn (dùng lại hàm populateLeftPanel)
            System.out.println("ManHinhBanGUI: Đang gọi populateLeftPanel..."); // DEBUG
            populateLeftPanel(currentLeftFilter);
            System.out.println("ManHinhBanGUI: populateLeftPanel đã chạy xong."); // DEBUG

            updateStatsPanel();

            // 4. (Tùy chọn) Bỏ chọn bàn hiện tại nếu nó không còn nữa hoặc đổi trạng thái
            if (selectedTable != null) {
                // Tìm xem bàn đã chọn còn tồn tại và trạng thái có hợp lệ không
                boolean stillExists = false;
                for(Ban ban : allTablesFromDB) {
                    if (ban.equals(selectedTable)) {
                        // Cập nhật lại selectedTable với dữ liệu mới nhất
                        selectedTable = ban;
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) {
                    selectedTable = null; // Bỏ chọn nếu bàn đã bị xóa (ít xảy ra)
                }
                updateRightPanelDetails(selectedTable); // Cập nhật lại panel phải
            }


        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi làm mới danh sách bàn.\nChi tiết: " + e.getMessage(),
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("ManHinhBanGUI: Kết thúc refreshTableList.");
    }
}