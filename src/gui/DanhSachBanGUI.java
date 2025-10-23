package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DanhSachBanGUI extends JPanel implements ActionListener {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    private CardLayout contentCardLayout = new CardLayout();
    private JPanel contentCardPanel;
    private ButtonGroup topNavGroup = new ButtonGroup();

    public DanhSachBanGUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 10, 0, 0));
        setBackground(Color.WHITE);

        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(true);
        topNavPanel.setBackground(COLOR_ACCENT_BLUE);

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonsPanel.setOpaque(false);

        String[] topNavButtons = {"Bàn", "Gọi Món", "Đặt Bàn"};
        String[] topNavCards = {"MAN_HINH_BAN", "MAN_HINH_GOI_MON", "MAN_HINH_DAT_BAN"};
        for (int i = 0; i < topNavButtons.length; i++) {
            String btnName = topNavButtons[i];
            String cardName = topNavCards[i];
            JToggleButton navButton = new JToggleButton(btnName);
            navButton.setFocusPainted(false);
            navButton.setBorderPainted(false);

            navButton.setBorder(new EmptyBorder(10, 20, 10, 20));

            navButton.setPreferredSize(new Dimension(120, 40));
            navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            navButton.setBackground(COLOR_ACCENT_BLUE);
            navButton.setForeground(Color.WHITE);

            navButton.addChangeListener(e -> {
                if (navButton.isSelected()) {
                    navButton.setBackground(Color.WHITE);
                    navButton.setForeground(Color.BLACK);
                } else {
                    navButton.setBackground(COLOR_ACCENT_BLUE);
                    navButton.setForeground(Color.WHITE);
                }
            });

            navButton.addActionListener(e -> {
                contentCardLayout.show(contentCardPanel, cardName);
            });

            topNavGroup.add(navButton);
            leftButtonsPanel.add(navButton);

            if (i == 0) {
                navButton.setSelected(true);
            }
        }

        // --- Panel chứa nút "..." bên phải ---
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonPanel.setOpaque(false); // Nền trong suốt
        rightButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

        JButton menuButton = new JButton("...");
        menuButton.setFocusPainted(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menuButton.setBackground(COLOR_ACCENT_BLUE);
        menuButton.setForeground(Color.WHITE);
        menuButton.setPreferredSize(new Dimension(50, 40));
        menuButton.setBorder(new EmptyBorder(10, 15, 10, 15));

        menuButton.addActionListener(e -> {
            // 1. Tạo JPopupMenu
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            // 2. Tạo các mục menu (menu items)
            JMenuItem itemInLaiHoaDon = new JMenuItem("In lại hóa đơn");
            JMenuItem itemGhepBan = new JMenuItem("Ghép bàn");
            JMenuItem itemChuyenBan = new JMenuItem("Chuyển bàn");

            // Tùy chỉnh font cho đẹp
            Font menuFont = new Font("Segoe UI", Font.PLAIN, 13);
            itemInLaiHoaDon.setFont(menuFont);
            itemGhepBan.setFont(menuFont);
            itemChuyenBan.setFont(menuFont);
            itemInLaiHoaDon.setBorder(new EmptyBorder(5, 15, 5, 15));
            itemGhepBan.setBorder(new EmptyBorder(5, 15, 5, 15));
            itemChuyenBan.setBorder(new EmptyBorder(5, 15, 5, 15));

            // 3. Thêm hành động cho mục
            itemInLaiHoaDon.addActionListener(e_themBan -> {
                InLaiHoaDon();
            });
            itemGhepBan.addActionListener(e_themBan -> {
                showGhepBanSplitDialog();
            });
            itemChuyenBan.addActionListener(e_themBan -> {
                showChuyenBanDiaLog();
            });

            // 4. Thêm các mục vào popup và hiển thị
            popupMenu.add(itemInLaiHoaDon);
            popupMenu.add(itemGhepBan);
            popupMenu.add(itemChuyenBan);

            // Hiển thị popup ngay bên dưới nút "..."
            popupMenu.show(menuButton, -100, menuButton.getHeight());
        });

        rightButtonPanel.add(menuButton); // Thêm nút ... vào panel bên phải

        // --- Thêm panel trái và phải vào topNavPanel ---
        topNavPanel.add(leftButtonsPanel, BorderLayout.WEST);
        topNavPanel.add(rightButtonPanel, BorderLayout.EAST);

        add(topNavPanel, BorderLayout.NORTH);

        contentCardPanel = new JPanel(contentCardLayout);
        contentCardPanel.setOpaque(false);

        contentCardPanel.add(new ManHinhBanGUI(), "MAN_HINH_BAN");
        contentCardPanel.add(new ManHinhGoiMonGUI(), "MAN_HINH_GOI_MON");
        contentCardPanel.add(new ManHinhDatBanGUI(), "MAN_HINH_DAT_BAN");

        add(contentCardPanel, BorderLayout.CENTER);

        // Hiển thị màn hình Bàn đầu tiên
        contentCardLayout.show(contentCardPanel, "MAN_HINH_BAN");
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
