package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import entity.Ban; // Thêm import Ban

public class DanhSachBanGUI extends JPanel implements ActionListener {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    private CardLayout contentCardLayout = new CardLayout();
    private JPanel contentCardPanel;
    private ButtonGroup topNavGroup = new ButtonGroup();
    private ManHinhBanGUI manHinhBanGUI;
    private ManHinhGoiMonGUI manHinhGoiMonGUI;
    private ManHinhDatBanGUI manHinhDatBanGUI;
    private JToggleButton btnTabBan;
    private JToggleButton btnTabGoiMon;
    private JToggleButton btnTabDatBan;
    private MainGUI mainGUI_Parent;

    // Constructor của DanhSachBanGUI
    public DanhSachBanGUI(MainGUI main) {
        this.mainGUI_Parent = main;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 10, 0, 0)); // Lề trên 20, trái 10
        setBackground(Color.WHITE);

        // --- Panel Thanh Điều Hướng Trên Cùng ---
        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(true);
        topNavPanel.setBackground(COLOR_ACCENT_BLUE); // Màu nền xanh

        // Panel chứa các nút tab bên trái
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Sát trái, không gap
        leftButtonsPanel.setOpaque(false); // Nền trong suốt

        // --- Tạo các nút tab (sử dụng hàm createTopNavButton đã sửa) ---
        // Nút "Bàn" được chọn mặc định ban đầu
        btnTabBan = createTopNavButton("Bàn", "MAN_HINH_BAN", true);
        btnTabGoiMon = createTopNavButton("Gọi Món", "MAN_HINH_GOI_MON", false);
        btnTabDatBan = createTopNavButton("Đặt Bàn", "MAN_HINH_DAT_BAN", false);

        // --- XỬ LÝ RIÊNG CHO NÚT "GỌI MÓN" ---
        // 1. Xóa ActionListener mặc định (chỉ chuyển card) mà createTopNavButton đã thêm
        // Cách xóa listener dựa trên lambda có thể không ổn định, đây là cách an toàn hơn:
        ActionListener[] defaultGoiMonListeners = btnTabGoiMon.getActionListeners();
        for (ActionListener al : defaultGoiMonListeners) {
            btnTabGoiMon.removeActionListener(al); // Xóa hết listener cũ (nếu có)
        }

        // 2. Thêm ActionListener mới, đặc biệt cho nút "Gọi Món"
        btnTabGoiMon.addActionListener(e -> {
            // Lấy bàn đang được chọn từ màn hình Bàn
            if (btnTabGoiMon.isSelected()) {
                Ban banDangChon = manHinhBanGUI.getSelectedTable();

                if (banDangChon != null) {
                    boolean shouldShowGoiMon = manHinhGoiMonGUI.loadDuLieuBan(banDangChon);
                    if (shouldShowGoiMon) {
                        contentCardLayout.show(contentCardPanel, "MAN_HINH_GOI_MON");
                        updateTopNavButtonStyles();
                    }else {
                        System.out.println("loadDuLieuBan trả về false, quay lại tab Bàn."); // Debug
                        SwingUtilities.invokeLater(() -> {
                            btnTabBan.setSelected(true);
                        });
                    }
                } else {
                    // Nếu chưa chọn bàn:
                    // a. Hiển thị thông báo lỗi
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn một bàn từ tab 'Bàn' trước khi chuyển sang 'Gọi Món'.",
                            "Chưa chọn bàn",
                            JOptionPane.WARNING_MESSAGE);
                    SwingUtilities.invokeLater(() -> {
                        btnTabBan.setSelected(true); // Chọn lại nút Bàn
                        // Không cần gọi show card lại vì setSelected sẽ trigger listener của btnTabBan
                    });
                }
            }
        });
        // --- KẾT THÚC XỬ LÝ RIÊNG CHO "GỌI MÓN" ---

        // --- Thêm các nút tab vào ButtonGroup và Panel ---
        topNavGroup.add(btnTabBan);
        topNavGroup.add(btnTabGoiMon);
        topNavGroup.add(btnTabDatBan);

        leftButtonsPanel.add(btnTabBan);
        leftButtonsPanel.add(btnTabGoiMon);
        leftButtonsPanel.add(btnTabDatBan);

        // --- Panel chứa nút "..." bên phải (Code giữ nguyên như của bạn) ---
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JButton menuButton = new JButton("...");
        // ... (Cài đặt style và ActionListener cho menuButton như cũ) ...
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.setBackground(COLOR_ACCENT_BLUE);
        menuButton.setForeground(Color.WHITE);
        menuButton.setPreferredSize(new Dimension(50, 40));
        menuButton.setBorder(new EmptyBorder(10, 15, 10, 15));
        menuButton.addActionListener(e -> {
            JPopupMenu popupMenu = new JPopupMenu();
            // ... (Code tạo và hiển thị JPopupMenu như cũ) ...
            popupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            JMenuItem itemInLaiHoaDon = new JMenuItem("In lại hóa đơn");
            // ... (Thêm các menu item khác và action listener) ...
            JMenuItem itemGhepBan = new JMenuItem("Ghép bàn");
            JMenuItem itemChuyenBan = new JMenuItem("Chuyển bàn");
            Font menuFont = new Font("Segoe UI", Font.PLAIN, 13);
            itemInLaiHoaDon.setFont(menuFont); // ... (Set font, border)
            itemGhepBan.setFont(menuFont);
            itemChuyenBan.setFont(menuFont);
            itemInLaiHoaDon.setBorder(new EmptyBorder(5, 15, 5, 15));
            itemGhepBan.setBorder(new EmptyBorder(5, 15, 5, 15));
            itemChuyenBan.setBorder(new EmptyBorder(5, 15, 5, 15));

            itemInLaiHoaDon.addActionListener(e_themBan -> InLaiHoaDon());
            itemGhepBan.addActionListener(e_themBan -> showGhepBanSplitDialog());
            itemChuyenBan.addActionListener(e_themBan -> showChuyenBanDiaLog());


            popupMenu.add(itemInLaiHoaDon);
            popupMenu.add(itemGhepBan);
            popupMenu.add(itemChuyenBan);

            popupMenu.show(menuButton, -100, menuButton.getHeight());
        });
        rightButtonPanel.add(menuButton);

        // --- Gắn các panel con vào thanh điều hướng chính ---
        topNavPanel.add(leftButtonsPanel, BorderLayout.WEST);
        topNavPanel.add(rightButtonPanel, BorderLayout.EAST);

        // --- Panel Nội Dung Chính (CardLayout) ---
        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false); // Nền trong suốt

        // --- Khởi tạo các màn hình con ---
        // Truyền 'this' (DanhSachBanGUI) vào ManHinhBanGUI
        manHinhBanGUI = new ManHinhBanGUI(this);
        manHinhGoiMonGUI = new ManHinhGoiMonGUI(this);
        manHinhDatBanGUI = new ManHinhDatBanGUI(this, mainGUI_Parent);

        // --- Thêm các màn hình con vào CardLayout ---
        contentCardPanel.add(manHinhBanGUI, "MAN_HINH_BAN");
        contentCardPanel.add(manHinhGoiMonGUI, "MAN_HINH_GOI_MON");
        contentCardPanel.add(manHinhDatBanGUI, "MAN_HINH_DAT_BAN");

        // --- Gắn thanh điều hướng và panel nội dung vào DanhSachBanGUI ---
        add(topNavPanel, BorderLayout.NORTH);
        add(contentCardPanel, BorderLayout.CENTER);

        // --- Hiển thị màn hình Bàn mặc định ---
        contentCardLayout.show(contentCardPanel, "MAN_HINH_BAN");

        // --- Cập nhật màu sắc nút tab lần đầu tiên (quan trọng) ---
        // Dùng invokeLater để đảm bảo UI đã sẵn sàng
        SwingUtilities.invokeLater(this::updateTopNavButtonStyles);
    } // Kết thúc constructor
    private JToggleButton createTopNavButton(String text, String cardName, boolean selected) {
        JToggleButton navButton = new JToggleButton(text);
        navButton.setFocusPainted(false);
        navButton.setBorderPainted(false); // Bỏ viền mặc định
        navButton.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding
        navButton.setPreferredSize(new Dimension(120, 40)); // Kích thước nút
        navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navButton.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Font chữ
        navButton.addActionListener(e -> {
            if (navButton.isSelected()) {
                contentCardLayout.show(contentCardPanel, cardName);
            }
        });
        navButton.setSelected(selected);


        return navButton;
    }
    private void updateTopNavButtonStyles() {
        // Mảng chứa các nút tab để dễ lặp qua
        JToggleButton[] buttons = {btnTabBan, btnTabGoiMon, btnTabDatBan};
        for (JToggleButton btn : buttons) {
            if (btn != null) { // Kiểm tra null cho an toàn
                if (btn.isSelected()) {
                    // Style cho nút đang được chọn
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                } else {
                    // Style cho các nút không được chọn
                    btn.setBackground(COLOR_ACCENT_BLUE);
                    btn.setForeground(Color.WHITE);
                }
            }
        }
    }
    public void refreshManHinhBan() {
        System.out.println("Đã nhận yêu cầu refresh...");
        if (manHinhBanGUI != null) {
            manHinhBanGUI.refreshTableList();
        }
    }
    public void switchToTab(String cardName) {
        // 1. Chuyển CardLayout
        contentCardLayout.show(contentCardPanel, cardName);

        // 2. Chọn nút tab tương ứng
        if ("MAN_HINH_BAN".equals(cardName) && btnTabBan != null) {
            btnTabBan.setSelected(true);
        } else if ("MAN_HINH_GOI_MON".equals(cardName) && btnTabGoiMon != null) {
            btnTabGoiMon.setSelected(true);
        } else if ("MAN_HINH_DAT_BAN".equals(cardName) && btnTabDatBan != null) {
            btnTabDatBan.setSelected(true);
        }
        // ButtonGroup sẽ tự động bỏ chọn các nút khác

        // 3. Cập nhật lại style của tất cả các nút tab
        updateTopNavButtonStyles();
        System.out.println("Đã chuyển sang tab: " + cardName); // Debug
    }
    private void showChuyenBanDiaLog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);
        ChuyenBanDialog dialog = new ChuyenBanDialog(parentFrame);
        dialog.setVisible(true);
    }

    private void InLaiHoaDon() {
    }

    private void showGhepBanSplitDialog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);
        GhepBanDialog dialog = new GhepBanDialog(parentFrame);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
