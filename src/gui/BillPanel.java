package gui;

import dao.BanDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import java.time.format.DateTimeFormatter;
import entity.Ban;
import entity.TrangThaiBan;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
/**
 * Panel này hiển thị chi tiết hóa đơn (JTable) VÀ BẢNG ĐIỀU KHIỂN THANH TOÁN.
 */
public class BillPanel extends JPanel {

    // Hằng số màu cho các nút
    private static final Color COLOR_BUTTON_BLUE = new Color(56, 118, 243);
    // Các thành phần trong panel thanh toán
    private JLabel lblTongCong; // (Tạm tính)
    private JLabel lblKhuyenMai;
    private JLabel lblVAT;
    private JLabel lblTongThanhToan; // (Tổng cộng cuối)
    private JLabel lblTongSoLuong; // (VD: số 4)
    private JLabel lblPhanTramVAT; // (VD: 0%)
    private JLabel lblTienThoi;
    private JTextField txtKhachTra;

    private JButton btnLuuMon, btnInTamTinh, btnThanhToan;

    private ManHinhGoiMonGUI parentGoiMonGUI;
    private ChiTietHoaDonDAO chiTietDAO;
    private HoaDonDAO hoaDonDAO;
    private BanDAO banDAO;

    private long currentTotal = 0; // Lưu tổng tiền (dạng số)
    private JPanel suggestedCashPanel; // Panel chứa 6 nút
    private final JButton[] suggestedCashButtons = new JButton[6];
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public BillPanel(ManHinhGoiMonGUI parent) {
        super(new BorderLayout(0, 10)); // Gap 10px
        this.parentGoiMonGUI = parent; // <-- Lưu lại parent
        this.chiTietDAO = new ChiTietHoaDonDAO(); // Khởi tạo DAO
        this.hoaDonDAO = new HoaDonDAO();
        this.banDAO = new BanDAO();

        setBackground(Color.WHITE);
        JPanel checkoutPanel = createCheckoutPanel();
        add(checkoutPanel, BorderLayout.SOUTH); // Thanh toán ở dưới

        if (parent != null) {
            btnLuuMon.addActionListener(e -> xuLyLuuMon_Clicked());
            btnInTamTinh.addActionListener(e -> hienThiXemTamTinh());
            btnThanhToan.addActionListener(e -> xuLyThanhToan());
            // Có thể thêm listener cho các nút khác ở đây nếu cần parent
        } else {
            // Vô hiệu hóa các nút không cần thiết nếu dùng trong ManHinhBanGUI
            btnLuuMon.setEnabled(false);
            btnInTamTinh.setEnabled(false); // Hoặc viết logic riêng cho nút này
            btnThanhToan.setEnabled(false);
        }

        // --- THÊM PHẦN KEY BINDING CHO F2 ---
        // 1. Lấy InputMap và ActionMap của BillPanel
        // WHEN_IN_FOCUSED_WINDOW nghĩa là phím tắt hoạt động ngay cả khi focus không nằm trực tiếp trên BillPanel
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        // 2. Định nghĩa KeyStroke cho phím F2
        KeyStroke f2KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0); // 0 = không có modifier (Shift, Ctrl, Alt)

        // 3. Đặt tên cho hành động (một chuỗi bất kỳ)
        String saveActionKey = "saveOrderAction";

        // 4. Liên kết KeyStroke với tên hành động trong InputMap
        inputMap.put(f2KeyStroke, saveActionKey);

        // 5. Tạo và liên kết Hành động (Action) với tên hành động trong ActionMap
        actionMap.put(saveActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Hành động cần thực hiện khi F2 được nhấn:
                // Kích hoạt sự kiện click của nút btnLuuMon
                if (btnLuuMon.isEnabled()) { // Chỉ thực hiện nếu nút đang được bật
                    btnLuuMon.doClick();
                }
            }
        });
        // --- THÊM KEY BINDING CHO F1 ---
//        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        ActionMap actionMap = this.getActionMap();
        KeyStroke f1KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        String checkoutActionKey = "checkoutAction";
        inputMap.put(f1KeyStroke, checkoutActionKey);
        actionMap.put(checkoutActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (btnThanhToan.isEnabled()) {
                    btnThanhToan.doClick(); // Kích hoạt nút Thanh toán
                }
            }
        });
    }
    private void xuLyThanhToan() {
        System.out.println("Xử lý Thanh Toán..."); // Debug

        // 1. Kiểm tra parent và lấy dữ liệu
        if (parentGoiMonGUI == null) return;
        Ban banHienTai = parentGoiMonGUI.getBanHienTai();
        HoaDon activeHoaDon = parentGoiMonGUI.getActiveHoaDon(); // Lấy HĐ mới nhất
        String maHDCuoiCung = activeHoaDon.getMaHD();
        DefaultTableModel model = parentGoiMonGUI.getModelChiTietHoaDon();

        if (banHienTai == null || activeHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Chưa có bàn hoặc hóa đơn hợp lệ để thanh toán!", "Lỗi Thanh Toán", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn trống, không thể thanh toán!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 🌟 BƯỚC SỬA: GỌI LƯU MÓN để đảm bảo TỔNG TIỀN được tính và lưu vào CSDL/currentTotal
        boolean luuThanhCong = luuMonAnVaoCSDL(false);
        if (!luuThanhCong) {
            return; // Dừng thanh toán nếu lưu thất bại
        }

        parentGoiMonGUI.updateBillPanelTotals();
        System.out.println("Đã cập nhật totals trước khi thanh toán.");

        // 2. Validate Tiền Khách Trả
        long tienKhachTraLong = 0;
        // Lấy tổng tiền đã được cập nhật từ xuLyLuuMon() -> loadBillTotals() -> updateSuggestedCash()
        long tongPhaiTraLong = this.currentTotal;

        try {
            String khachTraStr = txtKhachTra.getText().replace(",", "").replace(".", "");
            tienKhachTraLong = Long.parseLong(khachTraStr);
            if (tienKhachTraLong < tongPhaiTraLong) {
                JOptionPane.showMessageDialog(this, "Tiền khách đưa không đủ!\n(Cần ít nhất: " + nf.format(tongPhaiTraLong) + ")", "Lỗi Thanh Toán", JOptionPane.WARNING_MESSAGE);
                txtKhachTra.requestFocus();
                txtKhachTra.selectAll();
                return; // Dừng lại
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền khách trả không hợp lệ!", "Lỗi Thanh Toán", JOptionPane.WARNING_MESSAGE);
            txtKhachTra.requestFocus();
            txtKhachTra.selectAll();
            return; // Dừng lại
        }

        // 3. Xác nhận (Tùy chọn)
        long tienThoiLong = tienKhachTraLong - tongPhaiTraLong;
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận thanh toán cho bàn %s?\n\nTổng cộng: %s\nTiền khách đưa: %s\nTiền thối: %s\n\nXác nhận thanh toán?",
                        banHienTai.getTenBan(),
                        lblTongThanhToan.getText(), // Lấy từ label đã format
                        nf.format(tienKhachTraLong),
                        nf.format(tienThoiLong)),
                "Xác nhận thanh toán",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // Người dùng không đồng ý
        }

        // 4. Cập nhật CSDL
        boolean thanhToanOK = false;
        boolean updateBanOK = false;
        try {
            // Lấy hình thức thanh toán (Cần lấy từ ManHinhBanGUI nếu combobox ở đó, tạm dùng Tiền mặt)
            String hinhThucTT = "Tiền mặt"; // TODO: Lấy hình thức TT đúng

            // Gọi DAO cập nhật Hóa đơn
            // Lưu ý: activeHoaDon.getMaHD() vẫn là mã hóa đơn gốc, tổng tiền đã được cập nhật trong CSDL ở bước 1.
            thanhToanOK = hoaDonDAO.thanhToanHoaDon(activeHoaDon.getMaHD(), tienKhachTraLong, hinhThucTT);
            if (thanhToanOK) {

                // Đổi trạng thái object Ban thành Trống
                banHienTai.setTrangThai(TrangThaiBan.TRONG);
                banHienTai.setGioMoBan(null); // Reset giờ

                // Gọi BanDAO để cập nhật CSDL
                System.out.println("Đang cập nhật trạng thái Bàn " + banHienTai.getMaBan() + " về TRONG..."); // Debug
                updateBanOK = banDAO.updateBan(banHienTai); // <-- GỌI LẠI Ở ĐÂY

                if (!updateBanOK) {
                    System.err.println("LỖI: Thanh toán HĐ OK nhưng không cập nhật được trạng thái Bàn!");
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái bàn sau thanh toán!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    // Tiếp tục chạy để cập nhật UI, nhưng cần ghi log lỗi
                } else {
                    System.out.println("Cập nhật trạng thái Bàn thành công."); // Debug
                }

                // TODO: Cập nhật Tổng chi tiêu Khách hàng (nếu cần)

            } else {
                JOptionPane.showMessageDialog(this, "Thanh toán hóa đơn thất bại trong CSDL!", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi thực hiện thanh toán:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            return; // Dừng nếu có lỗi
        }

        // 5. Cập nhật Giao Diện (Chỉ khi thanh toán HĐ thành công)
        if (thanhToanOK) {
            // Thông báo thành công
            xuatPhieuIn("HÓA ĐƠN THANH TOÁN", true, tienKhachTraLong, tienThoiLong, maHDCuoiCung);
            // Xóa trắng màn hình gọi món
            parentGoiMonGUI.xoaThongTinGoiMon();

            // Yêu cầu màn hình Bàn cập nhật lại
            if (parentGoiMonGUI.getParentDanhSachBanGUI() != null) {
                System.out.println("Đang gọi refreshManHinhBan...");
                parentGoiMonGUI.getParentDanhSachBanGUI().refreshManHinhBan();
            }
        }
    }
    private void xuLyLuuMon_Clicked() {
        // Gọi hàm lưu với tham số true để hiện thông báo thành công
        boolean luuThanhCong = luuMonAnVaoCSDL(true);
    }
    public BillPanel() {
        // Gọi constructor kia với parent là null
        this(null);
    }
    private boolean luuMonAnVaoCSDL(boolean hienThongBaoThanhCong) {
        System.out.println("Xử lý Lưu Món..."); // Debug

        // 1. Lấy thông tin cần thiết từ parent
        if (parentGoiMonGUI == null) return false;
        Ban banHienTai = parentGoiMonGUI.getBanHienTai();
        HoaDon activeHoaDon = parentGoiMonGUI.getActiveHoaDon();
        DefaultTableModel model = parentGoiMonGUI.getModelChiTietHoaDon();

        // Kiểm tra điều kiện
        if (banHienTai == null || activeHoaDon == null || activeHoaDon.getMaDon() == null) {
            if (hienThongBaoThanhCong) { // Chỉ báo lỗi nếu người dùng chủ động bấm lưu
                JOptionPane.showMessageDialog(this, "Chưa có hóa đơn hợp lệ để lưu!", "Lỗi Lưu Món", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        String maDon = activeHoaDon.getMaDon();
        System.out.println("Lưu món cho Hóa đơn (Đơn): " + maDon); // Debug

        // 2. Lấy danh sách món HIỆN TẠI trên bảng (GUI)
        // Dùng Map để dễ truy cập: MaMon -> SoLuong
        Map<String, Integer> itemsTrenGUI = new HashMap<>();
        float tongTienMoiGUI = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String maMon = (String) model.getValueAt(i, 1); // Cột Mã Món (ẩn)
            Integer soLuong = (Integer) model.getValueAt(i, 3); // Cột SL
            Float thanhTien = (Float) model.getValueAt(i, 5); // Cột Thành tiền
            if (maMon != null && soLuong != null && thanhTien != null) {
                itemsTrenGUI.put(maMon, soLuong);
                tongTienMoiGUI += thanhTien;
            }
        }
        System.out.println("Items trên GUI: " + itemsTrenGUI); // Debug

        // 3. Lấy danh sách món ĐÃ LƯU trong CSDL
        List<ChiTietHoaDon> itemsTrongDB_List = chiTietDAO.getChiTietTheoMaDon(maDon);
        // Chuyển sang Map để dễ so sánh: MaMon -> ChiTietHoaDon object
        Map<String, ChiTietHoaDon> itemsTrongDB = new HashMap<>();
        for (ChiTietHoaDon ct : itemsTrongDB_List) {
            itemsTrongDB.put(ct.getMaMon(), ct);
        }
        System.out.println("Items trong DB: " + itemsTrongDB.keySet()); // Debug

        // --- Biến cờ để kiểm tra thành công ---
        boolean coLoi = false;

        // 4. So sánh và Cập nhật CSDL
        try {
            // --- THÊM MÓN MỚI ---
            for (Map.Entry<String, Integer> entryGUI : itemsTrenGUI.entrySet()) {
                String maMonGUI = entryGUI.getKey();
                int soLuongGUI = entryGUI.getValue();

                if (!itemsTrongDB.containsKey(maMonGUI)) { // Nếu món trên GUI không có trong DB
                    // Lấy đơn giá (cần truy cập MonAnDAO hoặc lấy từ bảng?)
                    // Tạm lấy từ bảng (Cột 4 - Đơn giá)
                    float donGia = 0;
                    for (int i = 0; i < model.getRowCount(); i++) {
                        if (maMonGUI.equals(model.getValueAt(i, 1))) {
                            donGia = (Float) model.getValueAt(i, 4);
                            break;
                        }
                    }

                    if (donGia > 0) {
                        ChiTietHoaDon ctMoi = new ChiTietHoaDon(maMonGUI, maDon, soLuongGUI, donGia);
                        System.out.println("Thêm mới: " + ctMoi); // Debug
                        if (!chiTietDAO.themChiTiet(ctMoi)) { // Gọi DAO thêm
                            coLoi = true;
                            System.err.println("Lỗi khi thêm chi tiết: " + maMonGUI);
                        }
                    } else {
                        System.err.println("Không tìm thấy đơn giá cho món mới: " + maMonGUI);
                        coLoi = true;
                    }
                }
            }

            // --- XÓA MÓN ---
            for (Map.Entry<String, ChiTietHoaDon> entryDB : itemsTrongDB.entrySet()) {
                String maMonDB = entryDB.getKey();
                if (!itemsTrenGUI.containsKey(maMonDB)) { // Nếu món trong DB không có trên GUI
                    System.out.println("Xóa món: " + maMonDB); // Debug
                    if (!chiTietDAO.xoaChiTiet(maDon, maMonDB)) { // Gọi DAO xóa
                        coLoi = true;
                        System.err.println("Lỗi khi xóa chi tiết: " + maMonDB);
                    }
                }
            }

            // --- SỬA SỐ LƯỢNG ---
            for (Map.Entry<String, Integer> entryGUI : itemsTrenGUI.entrySet()) {
                String maMonGUI = entryGUI.getKey();
                int soLuongGUI = entryGUI.getValue();

                if (itemsTrongDB.containsKey(maMonGUI)) { // Nếu món có cả trên GUI và DB
                    ChiTietHoaDon ctTrongDB = itemsTrongDB.get(maMonGUI);
                    if (ctTrongDB.getSoluong() != soLuongGUI) { // Nếu số lượng khác nhau
                        // Cập nhật số lượng mới vào object
                        ctTrongDB.setSoluong(soLuongGUI);
                        System.out.println("Sửa số lượng: " + ctTrongDB); // Debug
                        if (!chiTietDAO.suaChiTiet(ctTrongDB)) { // Gọi DAO sửa
                            coLoi = true;
                            System.err.println("Lỗi khi sửa chi tiết: " + maMonGUI);
                        }
                    }
                }
            }

            // 5. Cập nhật Tổng tiền Hóa đơn
            if (!coLoi) {
                float tongTienGoc = 0; // Tính lại tổng tiền gốc từ bảng
                for (int i = 0; i < model.getRowCount(); i++) {
                    tongTienGoc += (Float) model.getValueAt(i, 4) * (Integer) model.getValueAt(i, 3); // Đơn giá * SL
                }
                System.out.println("Cập nhật tổng tiền GỐC Hóa đơn " + activeHoaDon.getMaHD() + " thành: " + tongTienGoc);
                if (!hoaDonDAO.capNhatTongTien(activeHoaDon.getMaHD(), tongTienGoc)) { // Cập nhật TONG TIEN GOC
                    coLoi = true;
                    System.err.println("Lỗi khi cập nhật tổng tiền hóa đơn!");
                }
            }

        } catch (Exception ex) {
            coLoi = true;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lưu món ăn:\n" + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }

        if (parentGoiMonGUI != null) {
            System.out.println("xuLyLuuMon: Đang gọi updateBillPanelTotals để tính lại giảm giá...");
            parentGoiMonGUI.updateBillPanelTotals();
        }
        // 6. Thông báo kết quả
        if (!coLoi) {
            if (hienThongBaoThanhCong) { // Chỉ hiện popup nếu được yêu cầu
                JOptionPane.showMessageDialog(this, "Đã lưu các thay đổi món ăn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình lưu món ăn.", "Lỗi Lưu Món", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private JPanel createCheckoutPanel() {
        // Panel chính cho phần checkout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 10)); // Gap
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 10, 10, 10)); // Lề

        // --- 1. WEST: Panel chứa 2 nút "Lưu" và "In" ---
        JPanel leftActionPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 hàng, 1 cột
        leftActionPanel.setOpaque(false);

        btnLuuMon = createBigButton("Lưu món (F2)", COLOR_BUTTON_BLUE);
        btnInTamTinh = createBigButton("Xem tạm tính", COLOR_BUTTON_BLUE);

        leftActionPanel.add(btnLuuMon);
        leftActionPanel.add(btnInTamTinh);
        mainPanel.add(leftActionPanel, BorderLayout.WEST);

        // --- 2. SOUTH: Panel chứa nút "Thanh toán" ---
        btnThanhToan = createBigButton("Thanh toán (F1)", COLOR_BUTTON_BLUE);
        mainPanel.add(btnThanhToan, BorderLayout.SOUTH);

        // --- 3. CENTER: Panel chi tiết (Tóm tắt, Khách trả, Gợi ý) ---
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        detailsPanel.setOpaque(false);

        detailsPanel.add(createSummaryPanel()); // Tóm tắt (Tổng, VAT...)
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(createKhachTraPanel()); // "Khách trả"
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(createSuggestedCashPanel()); // Các nút tiền gợi ý

        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        return mainPanel;
    }
    private void xuatPhieuIn(String tieuDe, boolean daThanhToan, long tienKhachDua, long tienThoi, String maHD) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (parentGoiMonGUI == null) return;
        Ban banHienTai = parentGoiMonGUI.getBanHienTai();
//        HoaDon activeHoaDon = parentGoiMonGUI.getActiveHoaDon();
        DefaultTableModel model = parentGoiMonGUI.getModelChiTietHoaDon();

        if (banHienTai == null || model.getRowCount() == 0) {
            // (Thông báo lỗi nếu cần, nhưng thường nút sẽ bị disable hoặc đã check trước đó)
            return;
        }

        // 2. Tạo nội dung hóa đơn
        StringBuilder billText = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        // --- Header ---
        billText.append("===================================================\n");
        billText.append("                   ").append(tieuDe).append("\n");
        billText.append("===================================================\n");
        billText.append("Mã HĐ: ").append(maHD != null ? maHD : "---").append("\n");
        billText.append("Ngày:  ").append(LocalDateTime.now().format(dtf)).append("\n");
        billText.append("Bàn:   ").append(banHienTai.getTenBan()).append(" - ").append(banHienTai.getKhuVuc()).append("\n");
        // Nếu có khách hàng thì hiện tên
        // billText.append("Khách: ").append(...).append("\n");
        billText.append("---------------------------------------------------\n");

        // --- Danh sách món ---
        billText.append(String.format("%-20s %5s %10s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));
        billText.append("---------------------------------------------------\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            String tenMon = (String) model.getValueAt(i, 2);
            int soLuong = (int) model.getValueAt(i, 3);
            float donGia = (float) model.getValueAt(i, 4);
            float thanhTien = (float) model.getValueAt(i, 5);

            String tenMonDisplay = tenMon.length() > 18 ? tenMon.substring(0, 17) + "." : tenMon;

            billText.append(String.format("%-20s %5d %10s %12s\n",
                    tenMonDisplay, soLuong, nf.format(donGia), nf.format(thanhTien)));
        }
        billText.append("---------------------------------------------------\n");

        // --- Tổng kết ---
        billText.append(String.format("%-28s %20s\n", "Tổng cộng:", lblTongCong.getText()));
        if (!lblKhuyenMai.getText().equals("0 ₫") && !lblKhuyenMai.getText().equals("0")) {
            billText.append(String.format("%-28s %20s\n", "Giảm giá:", lblKhuyenMai.getText()));
        }
        if (!lblVAT.getText().equals("0 ₫") && !lblVAT.getText().equals("0")) {
            billText.append(String.format("%-28s %20s\n", "VAT (" + lblPhanTramVAT.getText() + "):", lblVAT.getText()));
        }

        billText.append("===================================================\n");
        billText.append(String.format("%-28s %20s\n", "TỔNG THANH TOÁN:", lblTongThanhToan.getText()));

        // --- Phần thêm cho Hóa đơn đã thanh toán ---
        if (daThanhToan) {
            billText.append(String.format("%-28s %20s\n", "Tiền khách đưa:", nf.format(tienKhachDua)));
            billText.append(String.format("%-28s %20s\n", "Tiền thối lại:", nf.format(tienThoi)));
            billText.append("---------------------------------------------------\n");
            billText.append("               XIN CẢM ƠN VÀ HẸN GẶP LẠI!       \n");
        } else {
            billText.append("\n(Phiếu này chỉ để kiểm tra, chưa thanh toán)\n");
        }
        billText.append("===================================================\n");

        // 3. Hiển thị JDialog
        JDialog previewDialog = new JDialog(SwingUtilities.getWindowAncestor(this), tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        previewDialog.setSize(420, 600);
        previewDialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea(billText.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> previewDialog.dispose());

        // (Tùy chọn) Thêm nút In thật nếu muốn
        // JButton btnPrintReal = new JButton("In ra máy in");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnClose);

        previewDialog.add(scrollPane, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }
    private void hienThiXemTamTinh() {
        // Gọi hàm chung với cờ daThanhToan = false
        HoaDon hd = parentGoiMonGUI.getActiveHoaDon();
        String maHD = (hd != null) ? hd.getMaHD() : "---";
        xuatPhieuIn("PHIẾU TẠM TÍNH", false, 0, 0,maHD);
    }
    private long roundUpToNearest(long number, long nearest) {
        if (nearest <= 0) return number;
        if (number % nearest == 0) return number;
        return ((number / nearest) + 1) * nearest;
    }
    private void tinhTienThoi() {
        try {
            // Lấy số từ ô "Khách trả"
            long khachTra = Long.parseLong(txtKhachTra.getText().replace(",", "").replace(".", ""));

            // Tính tiền thối (currentTotal được set trong loadBillData)
            long tienThoi = khachTra - this.currentTotal;

            // Định dạng số (ví dụ: 120,000)
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
            lblTienThoi.setText(nf.format(tienThoi));

            // Đổi màu (nếu âm thì màu đỏ)
            lblTienThoi.setForeground(tienThoi < 0 ? Color.RED : Color.BLUE);

        } catch (NumberFormatException ex) {
            lblTienThoi.setText("..."); // Nếu nhập chữ
        }
    }
    private void updateSuggestedCash(long total) {
        // 1. Lưu lại tổng tiền
        this.currentTotal = total;

        // 2. Ẩn tất cả các nút
        for (JButton btn : suggestedCashButtons) {
            btn.setVisible(false);
        }

        // Nếu tổng tiền <= 0, không cần gợi ý
        if (total <= 0) {
            return;
        }

        // 3. Tạo danh sách 6 gợi ý (Theo logic của hình ảnh bạn gửi)
        long[] suggestions = new long[6];
        suggestions[0] = roundUpToNearest(total, 1000);   // Gợi ý 1: Làm tròn lên 1.000 (vd: 119,400 -> 120,000)
        suggestions[1] = roundUpToNearest(total, 50000);  // Gợi ý 2: Làm tròn lên 50.000 (vd: 119,400 -> 150,000)
        suggestions[2] = roundUpToNearest(total, 100000); // Gợi ý 3: Làm tròn lên 100.000 (vd: 119,400 -> 200,000)
        suggestions[3] = suggestions[2] + 20000;          // Gợi ý 4: (vd: 220,000)
        suggestions[4] = suggestions[2] + 50000;          // Gợi ý 5: (vd: 250,000)
        suggestions[5] = 500000;                          // Gợi ý 6: Luôn là 500,000

        // 4. Lọc các gợi ý trùng lặp và đảm bảo chúng lớn hơn tổng
        java.util.LinkedHashSet<Long> uniqueSuggestions = new java.util.LinkedHashSet<>();
        for (long s : suggestions) {
            if (s >= total) { // Chỉ thêm nếu gợi ý >= tổng
                uniqueSuggestions.add(s);
            }
        }

        // (Nếu không đủ 6, có thể thêm các mệnh giá 1.000.000, 2.000.000...)

        // 5. Cập nhật 6 nút bấm
        int i = 0;
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        for (Long s : uniqueSuggestions) {
            if (i >= 6) break; // Dừng lại nếu đã đủ 6 nút

            suggestedCashButtons[i].setText(nf.format(s));
            suggestedCashButtons[i].setVisible(true);
            i++;
        }
    }
    /**
     * HÀM MỚI (Helper): Tạo panel tóm tắt (Tổng, VAT...)
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5); // Khoảng cách
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- KHỞI TẠO TẤT CẢ 6 JLABEL ---
        lblTongCong = new JLabel("0");
        lblKhuyenMai = new JLabel("0");
        lblVAT = new JLabel("0");
        lblTongThanhToan = new JLabel("0");
        lblTongSoLuong = new JLabel("0"); // <-- KHỞI TẠO Ở ĐÂY
        lblPhanTramVAT = new JLabel("0%"); // <-- KHỞI TẠO Ở ĐÂY

        // --- Set Font ---
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font valueFont = new Font("Segoe UI", Font.BOLD, 14);
        Font totalFont = new Font("Segoe UI", Font.BOLD, 16);

        // Căn lề phải cho các giá trị
        lblTongCong.setFont(valueFont);
        lblTongCong.setHorizontalAlignment(SwingConstants.RIGHT);
        lblKhuyenMai.setFont(valueFont);
        lblKhuyenMai.setHorizontalAlignment(SwingConstants.RIGHT);
        lblVAT.setFont(valueFont);
        lblVAT.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongThanhToan.setFont(totalFont);
        lblTongThanhToan.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongSoLuong.setFont(valueFont);
        lblTongSoLuong.setHorizontalAlignment(SwingConstants.RIGHT);
        lblPhanTramVAT.setFont(valueFont);
        lblPhanTramVAT.setHorizontalAlignment(SwingConstants.RIGHT);


        // --- Hàng 1: Tổng cộng ---
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 1.0; // Text "Tổng cộng"
        JLabel lbl1 = new JLabel("Tổng cộng:");
        lbl1.setFont(labelFont);
        panel.add(lbl1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.2; // Cột số lượng
        panel.add(lblTongSoLuong, gbc);

        gbc.gridx = 2; gbc.weightx = 0.5; // Cột tiền
        panel.add(lblTongCong, gbc);

        // --- Hàng 2: Khuyến mãi ---
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lbl2 = new JLabel("Khuyến mãi + giảm TV:");
        lbl2.setFont(labelFont);
        panel.add(lbl2, gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(""), gbc); // Bỏ trống cột số lượng

        gbc.gridx = 2;
        panel.add(lblKhuyenMai, gbc);

        // --- Hàng 3: VAT ---
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lbl3 = new JLabel("VAT:");
        lbl3.setFont(labelFont);
        panel.add(lbl3, gbc);

        gbc.gridx = 1; // Cột % VAT
        panel.add(lblPhanTramVAT, gbc);

        gbc.gridx = 2;
        panel.add(lblVAT, gbc);

        // --- Hàng 4: TỔNG THANH TOÁN ---
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lbl4 = new JLabel("TỔNG THANH TOÁN:"); // Sửa text ở đây nếu muốn
        lbl4.setFont(totalFont);
        panel.add(lbl4, gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(""), gbc); // Bỏ trống

        gbc.gridx = 2;
        panel.add(lblTongThanhToan, gbc);

        return panel;
    }

    /**
     * HÀM MỚI (Helper): Tạo panel "Khách trả"
     */
    private JPanel createKhachTraPanel() {
        // (Code cũ của bạn dùng BoxLayout)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Khách trả:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtKhachTra = new JTextField("0", 10);
        txtKhachTra.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtKhachTra.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhachTra.setMaximumSize(txtKhachTra.getPreferredSize());

        // --- THÊM SỰ KIỆN NÀY ---
        txtKhachTra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Gọi hàm tính tiền thối mỗi khi gõ phím
                tinhTienThoi();
            }
        });
        // -------------------------

        lblTienThoi = new JLabel("0");
        lblTienThoi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTienThoi.setForeground(Color.BLUE);
        lblTienThoi.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(lbl);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(txtKhachTra);
        panel.add(Box.createHorizontalGlue());
        panel.add(lblTienThoi);

        return panel;
    }

    /**
     * HÀM MỚI (Helper): Tạo panel 6 nút tiền gợi ý
     */
    private JPanel createSuggestedCashPanel() {
        // Sửa: Dùng biến toàn cục
        suggestedCashPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        suggestedCashPanel.setOpaque(false);

        // --- SỬA: Thay vòng lặp cũ ---
        for (int i = 0; i < 6; i++) {
            JButton btn = new JButton("..."); // 1. Tạo nút rỗng
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBackground(COLOR_BUTTON_BLUE);
            btn.setForeground(Color.WHITE);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setVisible(false); // 2. Ẩn đi lúc đầu

            btn.addActionListener(e -> {
                // 3. Khi click, lấy text của nút...
                String buttonText = ((JButton) e.getSource()).getText();
                // ...đặt vào ô Khách trả
                txtKhachTra.setText(buttonText.replace(",", "").replace(".", ""));
                // ...và tính tiền thối
                tinhTienThoi();
            });

            suggestedCashButtons[i] = btn; // 4. Lưu vào mảng
            suggestedCashPanel.add(btn);   // 5. Thêm vào panel
        }

        return suggestedCashPanel;
    }

    /**
     * HÀM MỚI (Helper): Tạo một nút bấm lớn màu xanh
     */
    private JButton createBigButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 60)); // Set chiều cao
        return btn;
    }
    public void loadBill(HoaDon hoaDon) {
        int tongSoLuong = 0;

        // 2. Thêm món ăn vào bảng
        for (ChiTietHoaDon ct : hoaDon.getDsChiTiet()) {
            tongSoLuong += ct.getSoluong();
        }

        // 3. Cập nhật các JLabel tóm tắt
        lblTongSoLuong.setText(String.valueOf(tongSoLuong));
        lblTongCong.setText(nf.format(hoaDon.getTongTien()));
        lblKhuyenMai.setText(nf.format(hoaDon.getGiamGia()));
        lblPhanTramVAT.setText("0%"); // TODO: Cập nhật sau
        lblVAT.setText(nf.format(hoaDon.getVat()));
        lblTongThanhToan.setText(nf.format(hoaDon.getTongThanhToan()));

        // 4. Cập nhật gợi ý tiền
        updateSuggestedCash((long) hoaDon.getTongThanhToan());
        tinhTienThoi(); // Reset tiền thối
    }
    /**
     * (Hàm này sau này sẽ nhận 1 Hóa Đơn và load)
     */
    public void loadBillTotals(long tongCong, long khuyenMai, long vat, long tongThanhToan, int tongSoLuong) {

        // 1. Cập nhật các JLabel tóm tắt
        lblTongSoLuong.setText(String.valueOf(tongSoLuong));
        lblTongCong.setText(nf.format(tongCong));         // Dùng NumberFormat nf đã khai báo
        lblKhuyenMai.setText(nf.format(khuyenMai));
        lblPhanTramVAT.setText(vat == 0 ? "0%" : "..."); // TODO: Cập nhật % VAT sau
        lblVAT.setText(nf.format(vat));
        lblTongThanhToan.setText(nf.format(tongThanhToan));

        // 2. Cập nhật gợi ý tiền mặt
        updateSuggestedCash(tongThanhToan);

        // 3. Reset tiền thối (vì tổng tiền đã thay đổi)
        // Nếu muốn giữ lại tiền khách nhập thì comment dòng này
        // txtKhachTra.setText("0");
        tinhTienThoi();
        if (tongThanhToan == 0) {
            txtKhachTra.setText("0"); // Đặt lại ô khách trả về 0
            tinhTienThoi();          // Tính lại tiền thối (sẽ là 0)
        }
    }
    public void clearBill() {
        lblTongSoLuong.setText("0");
        lblTongCong.setText(nf.format(0));
        lblKhuyenMai.setText(nf.format(0));
        lblPhanTramVAT.setText("0%");
        lblVAT.setText(nf.format(0));
        lblTongThanhToan.setText(nf.format(0));
        lblTienThoi.setText(nf.format(0));
        txtKhachTra.setText("0");

        // 3. Ẩn các nút gợi ý
        updateSuggestedCash(0);
    }
}