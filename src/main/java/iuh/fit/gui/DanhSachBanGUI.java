/*
package iuh.fit.gui;

import iuh.fit.core.entity.Ban;
import iuh.fit.core.service.DonDatMonService;
import iuh.fit.gui.dialog.ChuyenBanDialog;
import iuh.fit.gui.dialog.GhepBanDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

*/
/**
 * Lớp GUI chính điều phối các màn hình liên quan đến Bàn, Gọi món và Đặt bàn.
 * Sử dụng CardLayout để chuyển đổi linh hoạt giữa các phân hệ.
 *//*

public class DanhSachBanGUI extends JPanel {
    // Constants cho màu sắc và Card Name
    private static final Color PRIMARY_BLUE = new Color(56, 118, 243);
    private static final String TAB_BAN = "MAN_HINH_BAN";
    private static final String TAB_GOI_MON = "MAN_HINH_GOI_MON";
    private static final String TAB_DAT_BAN = "MAN_HINH_DAT_BAN";

    // Layout components
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel containerPanel = new JPanel(cardLayout);

    // Sub-screens
    private ManHinhBanGUI manHinhBanGUI;
    private ManHinhGoiMonGUI manHinhGoiMonGUI;
    private ManHinhDatBanGUI manHinhDatBanGUI;

    // Navigation buttons
    private JToggleButton btnBan, btnGoiMon, btnDatBan;
    private final ButtonGroup navGroup = new ButtonGroup();

    // Dependencies
    private final MainGUI parentMainGUI;
    private final String maNVDangNhap;
    private final DonDatMonService donDatMonService = new DonDatMonService();

    public DanhSachBanGUI(MainGUI main, String maNVDangNhap) {
        this.parentMainGUI = main;
        this.maNVDangNhap = maNVDangNhap;

        initComponent();
        setupListeners();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Khởi tạo thanh điều hướng phía trên
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Khởi tạo các màn hình con
        manHinhBanGUI = new ManHinhBanGUI(this);
        manHinhGoiMonGUI = new ManHinhGoiMonGUI(this, this.maNVDangNhap);
        manHinhDatBanGUI = new ManHinhDatBanGUI(this, parentMainGUI);

        containerPanel.add(manHinhBanGUI, TAB_BAN);
        containerPanel.add(manHinhGoiMonGUI, TAB_GOI_MON);
        containerPanel.add(manHinhDatBanGUI, TAB_DAT_BAN);
        containerPanel.setOpaque(false);

        add(containerPanel, BorderLayout.CENTER);

        // Mặc định hiển thị tab Bàn
        showTab(TAB_BAN);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BLUE);
        header.setPreferredSize(new Dimension(0, 45));

        // Panel chứa các nút điều hướng bên trái
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftNav.setOpaque(false);

        btnBan = createNavButton("Sơ đồ bàn", TAB_BAN);
        btnGoiMon = createNavButton("Gọi món", TAB_GOI_MON);
        btnDatBan = createNavButton("Lịch đặt bàn", TAB_DAT_BAN);

        navGroup.add(btnBan);
        navGroup.add(btnGoiMon);
        navGroup.add(btnDatBan);
        btnBan.setSelected(true);

        leftNav.add(btnBan);
        leftNav.add(btnGoiMon);
        leftNav.add(btnDatBan);

        // Nút menu chức năng bên phải (Ghép/Chuyển bàn)
        JButton btnExtra = new JButton("Chức năng ▾");
        styleExtraButton(btnExtra);
        btnExtra.addActionListener(this::showPopupMenu);

        header.add(leftNav, BorderLayout.WEST);
        header.add(btnExtra, BorderLayout.EAST);

        return header;
    }

    private void setupListeners() {
        // Xử lý riêng cho tab Gọi Món vì cần kiểm tra điều kiện chọn bàn
        for (ActionListener al : btnGoiMon.getActionListeners()) btnGoiMon.removeActionListener(al);

        btnGoiMon.addActionListener(e -> {
            Ban selectedBan = manHinhBanGUI.getSelectedTable();
            if (selectedBan == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn từ sơ đồ trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                btnBan.setSelected(true);
                showTab(TAB_BAN);
                return;
            }

            if (manHinhGoiMonGUI.loadDuLieuBan(selectedBan)) {
                showTab(TAB_GOI_MON);
            } else {
                btnBan.setSelected(true);
                showTab(TAB_BAN);
            }
        });

        // Xử lý riêng cho tab Đặt Bàn để làm mới dữ liệu
        for (ActionListener al : btnDatBan.getActionListeners()) btnDatBan.removeActionListener(al);
        btnDatBan.addActionListener(e -> {
            manHinhDatBanGUI.refreshData();
            showTab(TAB_DAT_BAN);
        });
    }

    private void showTab(String cardName) {
        cardLayout.show(containerPanel, cardName);
        updateButtonStyles();
    }

    private JToggleButton createNavButton(String text, String cardName) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showTab(cardName));
        return btn;
    }

    private void updateButtonStyles() {
        JToggleButton[] btns = {btnBan, btnGoiMon, btnDatBan};
        for (JToggleButton b : btns) {
            if (b.isSelected()) {
                b.setBackground(Color.WHITE);
                b.setForeground(PRIMARY_BLUE);
            } else {
                b.setBackground(PRIMARY_BLUE);
                b.setForeground(Color.WHITE);
            }
        }
    }

    private void showPopupMenu(ActionEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemGhep = new JMenuItem("Ghép bàn nhanh");
        JMenuItem itemChuyen = new JMenuItem("Chuyển bàn");

        itemGhep.addActionListener(al -> {
            new GhepBanDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
            refreshManHinhBan();
        });

        itemChuyen.addActionListener(al -> {
            new ChuyenBanDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
            refreshManHinhBan();
        });

        menu.add(itemGhep);
        menu.add(itemChuyen);
        menu.show((Component) e.getSource(), 0, 45);
    }

    private void styleExtraButton(JButton btn) {
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 15, 0, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    public void refreshManHinhBan() {
        if (manHinhBanGUI != null) manHinhBanGUI.refreshTableList();
    }

    public String getMaNVDangNhap() { return maNVDangNhap; }
}*/
