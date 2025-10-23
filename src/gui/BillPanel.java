package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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

    private long currentTotal = 0; // Lưu tổng tiền (dạng số)
    private JPanel suggestedCashPanel; // Panel chứa 6 nút
    private final JButton[] suggestedCashButtons = new JButton[6];
    private final int[] denominations = {1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000};

    public BillPanel() {
        super(new BorderLayout(0, 10)); // Gap 10px
        setBackground(Color.WHITE);
        // Bỏ viền cũ, panel mới sẽ tự có viền
        // setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));




        // 3. Panel Thanh Toán MỚI (thay thế footer cũ)
        JPanel checkoutPanel = createCheckoutPanel();
        add(checkoutPanel, BorderLayout.SOUTH);

        // (Thêm dữ liệu mẫu để test)
        loadBillData();
    }

    /**
     * HÀM MỚI: Tạo toàn bộ panel thanh toán phức tạp
     */
    private JPanel createCheckoutPanel() {
        // Panel chính cho phần checkout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 10)); // Gap
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 10, 10, 10)); // Lề

        // --- 1. WEST: Panel chứa 2 nút "Lưu" và "In" ---
        JPanel leftActionPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // 2 hàng, 1 cột
        leftActionPanel.setOpaque(false);

        btnLuuMon = createBigButton("Lưu món (F2)", COLOR_BUTTON_BLUE);
        btnInTamTinh = createBigButton("In tạm tính", COLOR_BUTTON_BLUE);

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
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
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

    /**
     * (Hàm này sau này sẽ nhận 1 Hóa Đơn và load)
     */
    public void loadBillData() {
        long tongCong = 145000;
        long khuyenMai = -500;
        long vatValue = 0;
        long tongThanhToan = tongCong + khuyenMai + vatValue;

        // Định dạng số (NumberFormat)
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();

        // Cập nhật các JLabel
        lblTongSoLuong.setText("5"); // (2 + 3)
        lblTongCong.setText(nf.format(tongCong));
        lblKhuyenMai.setText(nf.format(khuyenMai));
        lblPhanTramVAT.setText("0%");
        lblVAT.setText(nf.format(vatValue));
        lblTongThanhToan.setText(nf.format(tongThanhToan));

        // Gọi hàm cập nhật gợi ý
        updateSuggestedCash(tongThanhToan);
        // Gọi hàm tính tiền thối (để reset)
        tinhTienThoi();
    }

    public void clearBill() {
        // ... (Code reset các JLabel giữ nguyên)
        lblTongSoLuong.setText("0");
        lblTongCong.setText("0");
        lblKhuyenMai.setText("0");
        lblPhanTramVAT.setText("0%");
        lblVAT.setText("0");
        lblTongThanhToan.setText("0");
        lblTienThoi.setText("0");
        txtKhachTra.setText("0");

        // --- THÊM DÒNG NÀY ---
        // Ẩn các nút gợi ý khi không có hóa đơn
        updateSuggestedCash(0);
    }
}