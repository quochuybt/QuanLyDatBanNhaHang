package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.net.client.SocketClientConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class DanhSachBanGUI extends JPanel implements ActionListener {

    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    private final CardLayout contentCardLayout = new CardLayout();
    private JPanel contentCardPanel;

    private final ButtonGroup topNavGroup = new ButtonGroup();

    private ManHinhBanGUI manHinhBanGUI;
    private ManHinhGoiMonGUI manHinhGoiMonGUI;
    private ManHinhDatBanGUI manHinhDatBanGUI;

    private JToggleButton btnTabBan;
    private JToggleButton btnTabGoiMon;
    private JToggleButton btnTabDatBan;

    private final DashboardGUI mainGUI_Parent;
    private final String maNVDangNhap;
    private final SocketClientConnection connection;

    public DanhSachBanGUI(
            DashboardGUI dashboardGUI,
            String maNVDangNhap,
            SocketClientConnection connection
    ) {
        this.mainGUI_Parent = dashboardGUI;
        this.maNVDangNhap = maNVDangNhap;
        this.connection = Objects.requireNonNull(
                connection,
                "SocketClientConnection không được null."
        );

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

        removeDefaultTabListeners(btnTabGoiMon);
        btnTabGoiMon.addActionListener(e -> xuLyChuyenSangTabGoiMon());

        removeDefaultTabListeners(btnTabDatBan);
        btnTabDatBan.addActionListener(e -> xuLyChuyenSangTabDatBan());

        topNavGroup.add(btnTabBan);
        topNavGroup.add(btnTabGoiMon);
        topNavGroup.add(btnTabDatBan);

        leftButtonsPanel.add(btnTabBan);
        leftButtonsPanel.add(btnTabGoiMon);
        leftButtonsPanel.add(btnTabDatBan);

        JPanel rightButtonPanel = createRightMenuPanel();

        topNavPanel.add(leftButtonsPanel, BorderLayout.WEST);
        topNavPanel.add(rightButtonPanel, BorderLayout.EAST);

        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false);

        /*
         * Ba màn con đều dùng chung socket connection sau login.
         */
        manHinhBanGUI = new ManHinhBanGUI(this, connection);
        manHinhGoiMonGUI = new ManHinhGoiMonGUI(this, this.maNVDangNhap, connection);
        manHinhDatBanGUI = new ManHinhDatBanGUI(this, mainGUI_Parent, connection);

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

    public SocketClientConnection getConnection() {
        return connection;
    }

    private void removeDefaultTabListeners(JToggleButton button) {
        ActionListener[] listeners = button.getActionListeners();

        for (ActionListener listener : listeners) {
            button.removeActionListener(listener);
        }
    }

    private void xuLyChuyenSangTabGoiMon() {
        if (!btnTabGoiMon.isSelected()) {
            return;
        }

        BanDTO banDangChon = JsonMapper.convert(
                manHinhBanGUI.getSelectedTable(),
                BanDTO.class
        );

        if (banDangChon == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một bàn từ tab 'Bàn' trước khi chuyển sang 'Gọi Món'.",
                    "Chưa chọn bàn",
                    JOptionPane.WARNING_MESSAGE
            );

            quayVeTabBan();
            return;
        }

        boolean shouldShowGoiMon = manHinhGoiMonGUI.loadDuLieuBan(banDangChon);

        if (shouldShowGoiMon) {
            contentCardLayout.show(contentCardPanel, "MAN_HINH_GOI_MON");
            updateTopNavButtonStyles();
        } else {
            System.out.println("loadDuLieuBan trả về false, quay lại tab Bàn.");
            quayVeTabBan();
        }
    }

    private void xuLyChuyenSangTabDatBan() {
        if (!btnTabDatBan.isSelected()) {
            return;
        }

        contentCardLayout.show(contentCardPanel, "MAN_HINH_DAT_BAN");
        updateTopNavButtonStyles();

        if (manHinhDatBanGUI != null) {
            manHinhDatBanGUI.refreshData();
        }
    }

    private void quayVeTabBan() {
        SwingUtilities.invokeLater(() -> {
            btnTabBan.setSelected(true);
            contentCardLayout.show(contentCardPanel, "MAN_HINH_BAN");
            updateTopNavButtonStyles();
        });
    }

    private JPanel createRightMenuPanel() {
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

            itemGhepBan.addActionListener(event -> showGhepBanSplitDialog());

            itemChuyenBan.addActionListener(event -> showChuyenBanDiaLog());

            popupMenu.add(itemGhepBan);
            popupMenu.add(itemChuyenBan);

            popupMenu.show(menuButton, -100, menuButton.getHeight());
        });

        rightButtonPanel.add(menuButton);

        return rightButtonPanel;
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
        JToggleButton[] buttons = {
                btnTabBan,
                btnTabGoiMon,
                btnTabDatBan
        };

        for (JToggleButton btn : buttons) {
            if (btn == null) {
                continue;
            }

            if (btn.isSelected()) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            } else {
                btn.setBackground(COLOR_ACCENT_BLUE);
                btn.setForeground(Color.WHITE);
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

        ChuyenBanDialog dialog = new ChuyenBanDialog(parentFrame, connection);
        dialog.setVisible(true);

        refreshManHinhBan();
    }

    private void showGhepBanSplitDialog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);

        GhepBanDialog dialog = new GhepBanDialog(
                parentFrame,
                maNVDangNhap,
                connection
        );

        dialog.setVisible(true);

        System.out.println("Đang làm mới danh sách bàn sau khi ghép...");
        refreshManHinhBan();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Không dùng, giữ lại vì class implements ActionListener.
    }
}