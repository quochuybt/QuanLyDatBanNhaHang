package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import entity.Ban;

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
    private final String maNVDangNhap;

    public DanhSachBanGUI(MainGUI main, String maNVDangNhap) {
        this.mainGUI_Parent = main;
        this.maNVDangNhap = maNVDangNhap;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 10, 0, 0));
        setBackground(Color.WHITE);

        // --- Panel Thanh Điều Hướng Trên Cùng ---
        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(true);
        topNavPanel.setBackground(COLOR_ACCENT_BLUE);

        // Panel chứa các nút tab bên trái
        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonsPanel.setOpaque(false);

        // --- Tạo các nút tab ---
        btnTabBan = createTopNavButton("Bàn", "MAN_HINH_BAN", true);
        btnTabGoiMon = createTopNavButton("Gọi Món", "MAN_HINH_GOI_MON", false);
        btnTabDatBan = createTopNavButton("Đặt Bàn", "MAN_HINH_DAT_BAN", false);

        // --- XỬ LÝ RIÊNG CHO NÚT "GỌI MÓN" ---
        ActionListener[] defaultGoiMonListeners = btnTabGoiMon.getActionListeners();
        for (ActionListener al : defaultGoiMonListeners) {
            btnTabGoiMon.removeActionListener(al);
        }

        btnTabGoiMon.addActionListener(e -> {
            if (btnTabGoiMon.isSelected()) {
                Ban banDangChon = manHinhBanGUI.getSelectedTable();

                if (banDangChon != null) {
                    boolean shouldShowGoiMon = manHinhGoiMonGUI.loadDuLieuBan(banDangChon);
                    if (shouldShowGoiMon) {
                        contentCardLayout.show(contentCardPanel, "MAN_HINH_GOI_MON");
                        updateTopNavButtonStyles();
                    }else {
                        System.out.println("loadDuLieuBan trả về false, quay lại tab Bàn.");
                        SwingUtilities.invokeLater(() -> {
                            btnTabBan.setSelected(true);
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn một bàn từ tab 'Bàn' trước khi chuyển sang 'Gọi Món'.",
                            "Chưa chọn bàn",
                            JOptionPane.WARNING_MESSAGE);
                    SwingUtilities.invokeLater(() -> {
                        btnTabBan.setSelected(true);
                    });
                }
            }
        });
        // --- Thêm các nút tab vào ButtonGroup và Panel ---
        topNavGroup.add(btnTabBan);
        topNavGroup.add(btnTabGoiMon);
        topNavGroup.add(btnTabDatBan);

        leftButtonsPanel.add(btnTabBan);
        leftButtonsPanel.add(btnTabGoiMon);
        leftButtonsPanel.add(btnTabDatBan);

        // --- Panel chứa nút "..." bên phải ---
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        JButton menuButton = new JButton("...");
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuButton.setBackground(COLOR_ACCENT_BLUE);
        menuButton.setForeground(Color.WHITE);
        menuButton.setPreferredSize(new Dimension(50, 40));
        menuButton.setBorder(new EmptyBorder(10, 15, 10, 15));
        menuButton.addActionListener(e -> {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            JMenuItem itemInLaiHoaDon = new JMenuItem("In lại hóa đơn");
            JMenuItem itemGhepBan = new JMenuItem("Ghép bàn");
            JMenuItem itemChuyenBan = new JMenuItem("Chuyển bàn");
            Font menuFont = new Font("Segoe UI", Font.PLAIN, 13);
            itemInLaiHoaDon.setFont(menuFont);
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
        contentCardPanel.setOpaque(false);

        // --- Khởi tạo các màn hình con ---
        manHinhBanGUI = new ManHinhBanGUI(this);
        manHinhGoiMonGUI = new ManHinhGoiMonGUI(this, this.maNVDangNhap);
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

        // --- Cập nhật màu sắc nút tab lần đầu tiên ---
        SwingUtilities.invokeLater(this::updateTopNavButtonStyles);
    }
    public String getMaNVDangNhap() {
        return maNVDangNhap;
    }

    private JToggleButton createTopNavButton(String text, String cardName, boolean selected) {
        JToggleButton navButton = new JToggleButton(text);
        navButton.setFocusPainted(false);
        navButton.setBorderPainted(false);
        navButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        navButton.setPreferredSize(new Dimension(120, 40));
        navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navButton.addActionListener(e -> {
            if (navButton.isSelected()) {
                contentCardLayout.show(contentCardPanel, cardName);
                updateTopNavButtonStyles();
            }
        });
        navButton.setSelected(selected);


        return navButton;
    }
    private void updateTopNavButtonStyles() {
        JToggleButton[] buttons = {btnTabBan, btnTabGoiMon, btnTabDatBan};
        for (JToggleButton btn : buttons) {
            if (btn != null) {
                if (btn.isSelected()) {
                    btn.setBackground(Color.WHITE);
                    btn.setForeground(Color.BLACK);
                } else {
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
        updateTopNavButtonStyles();
        System.out.println("Đã chuyển sang tab: " + cardName);
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