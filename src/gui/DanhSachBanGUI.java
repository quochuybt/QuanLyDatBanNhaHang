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

        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(true);
        topNavPanel.setBackground(COLOR_ACCENT_BLUE);

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonsPanel.setOpaque(false);

        btnTabBan = createTopNavButton("Bàn", "MAN_HINH_BAN", true);
        btnTabGoiMon = createTopNavButton("Gọi Món", "MAN_HINH_GOI_MON", false);
        btnTabDatBan = createTopNavButton("Đặt Bàn", "MAN_HINH_DAT_BAN", false);

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
        ActionListener[] defaultDatBanListeners = btnTabDatBan.getActionListeners();
        for (ActionListener al : defaultDatBanListeners) {
            btnTabDatBan.removeActionListener(al);
        }

        btnTabDatBan.addActionListener(e -> {
            if (btnTabDatBan.isSelected()) {
                contentCardLayout.show(contentCardPanel, "MAN_HINH_DAT_BAN");
                updateTopNavButtonStyles();

                if (manHinhDatBanGUI != null) {
                    manHinhDatBanGUI.refreshData();
                }
            }
        });
        topNavGroup.add(btnTabBan);
        topNavGroup.add(btnTabGoiMon);
        topNavGroup.add(btnTabDatBan);

        leftButtonsPanel.add(btnTabBan);
        leftButtonsPanel.add(btnTabGoiMon);
        leftButtonsPanel.add(btnTabDatBan);

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
            JMenuItem itemGhepBan = new JMenuItem("Ghép bàn");
            JMenuItem itemChuyenBan = new JMenuItem("Chuyển bàn");
            Font menuFont = new Font("Segoe UI", Font.PLAIN, 13);
            itemGhepBan.setFont(menuFont);
            itemChuyenBan.setFont(menuFont);
            itemGhepBan.setBorder(new EmptyBorder(5, 15, 5, 15));
            itemChuyenBan.setBorder(new EmptyBorder(5, 15, 5, 15));

            itemGhepBan.addActionListener(e_themBan -> showGhepBanSplitDialog());
            itemChuyenBan.addActionListener(e_themBan -> {
                showChuyenBanDiaLog();
                if (manHinhBanGUI != null) {
                    manHinhBanGUI.refreshTableList();
                }
            });


            popupMenu.add(itemGhepBan);
            popupMenu.add(itemChuyenBan);

            popupMenu.show(menuButton, -100, menuButton.getHeight());
        });
        rightButtonPanel.add(menuButton);

        topNavPanel.add(leftButtonsPanel, BorderLayout.WEST);
        topNavPanel.add(rightButtonPanel, BorderLayout.EAST);

        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false);

        manHinhBanGUI = new ManHinhBanGUI(this);
        manHinhGoiMonGUI = new ManHinhGoiMonGUI(this, this.maNVDangNhap);
        manHinhDatBanGUI = new ManHinhDatBanGUI(this, mainGUI_Parent);

        contentCardPanel.add(manHinhBanGUI, "MAN_HINH_BAN");
        contentCardPanel.add(manHinhGoiMonGUI, "MAN_HINH_GOI_MON");
        contentCardPanel.add(manHinhDatBanGUI, "MAN_HINH_DAT_BAN");

        add(topNavPanel, BorderLayout.NORTH);
        add(contentCardPanel, BorderLayout.CENTER);

        contentCardLayout.show(contentCardPanel, "MAN_HINH_BAN");

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

    private void showChuyenBanDiaLog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);
         ChuyenBanDialog dialog = new ChuyenBanDialog(parentFrame);
         dialog.setVisible(true);
    }


    private void showGhepBanSplitDialog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);
         GhepBanDialog dialog = new GhepBanDialog(parentFrame);
         dialog.setVisible(true);
        if (manHinhBanGUI != null) {
            System.out.println("Đang làm mới danh sách bàn sau khi ghép...");
            manHinhBanGUI.refreshTableList();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}