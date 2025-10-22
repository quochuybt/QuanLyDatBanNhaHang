package gui;

import entity.Ban;
import entity.TrangThaiBan;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class QuanLyBanGUI extends JPanel implements ActionListener {
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    private CardLayout contentCardLayout = new CardLayout();
    private JPanel contentCardPanel;
    private ButtonGroup topNavGroup = new ButtonGroup();

    public QuanLyBanGUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topNavPanel = new JPanel(new BorderLayout());
        topNavPanel.setOpaque(true);
        topNavPanel.setBackground(COLOR_ACCENT_BLUE);
        topNavPanel.setBorder(new MatteBorder(1, 1, 1, 1, Color.GRAY));

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonsPanel.setOpaque(false);

        String[] topNavButtons = {"Bàn", "Gọi Món", "Đặt Bàn"};
        String[] topNavCards = {"MAN_HINH_BAN", "MAN_HINH_GOI_MON", "MAN_HINH_DAT_BAN"};
        for (int i = 0; i < topNavButtons.length; i++) {
            String btnName = topNavButtons[i];
            String cardName = topNavCards[i];
            JToggleButton navButton = new JToggleButton(btnName);
            navButton.setFocusPainted(false);
            navButton.setBorder(new EmptyBorder(10, 20, 10, 20));
            navButton.setPreferredSize(new Dimension(120, 40));
            navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            navButton.setBackground(COLOR_ACCENT_BLUE);
            navButton.setForeground(Color.WHITE);

            navButton.addChangeListener(e -> {
                if (navButton.isSelected()) {
                    navButton.setBackground(Color.WHITE);
                    navButton.setForeground(Color.BLACK);
                    // Dùng viền dưới màu xanh khi chọn
                    navButton.setBorder(BorderFactory.createCompoundBorder(
                            new MatteBorder(1, 1, 1, 1, COLOR_ACCENT_BLUE),
                            new EmptyBorder(10, 20, 7, 20)
                    ));

                } else {
                    navButton.setBackground(COLOR_ACCENT_BLUE);
                    navButton.setForeground(Color.WHITE);
                    // Dùng EmptyBorder khi không chọn
                    navButton.setBorder(new EmptyBorder(10, 20, 13, 20)); // Giữ chiều cao
                }
            });
            navButton.addActionListener(e -> {
                contentCardLayout.show(contentCardPanel, cardName);
            });

            topNavGroup.add(navButton);
            leftButtonsPanel.add(navButton); // Thêm vào panel bên trái

            if (i == 0) {
                navButton.setSelected(true);
            }
        }

        // --- Panel chứa nút "..." bên phải ---
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonPanel.setOpaque(false); // Nền trong suốt
        rightButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5)); // Chừa 5px lề phải

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
            popupMenu.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Thêm viền

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
                // Khi click, gọi hàm hiển thị modal làm mờ nền
                InLaiHoaDon();
            });
            itemGhepBan.addActionListener(e_themBan -> {
                // Khi click, gọi hàm hiển thị modal làm mờ nền
                showGhepBanSplitDialog();
            });
            itemChuyenBan.addActionListener(e_themBan -> {
                // Khi click, gọi hàm hiển thị modal làm mờ nền
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
        add(contentCardPanel, BorderLayout.CENTER);

        JPanel banScreenPanel = createBanScreenPanel();
        contentCardPanel.add(banScreenPanel, "MAN_HINH_BAN");
        contentCardPanel.add(createPlaceholderPanel("Màn hình Gọi Món"), "MAN_HINH_GOI_MON");
        contentCardPanel.add(createPlaceholderPanel("Màn hình Đặt Bàn"), "MAN_HINH_DAT_BAN");

        // Hiển thị màn hình Bàn đầu tiên
        contentCardLayout.show(contentCardPanel, "MAN_HINH_BAN");
    }

    private void showChuyenBanDiaLog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);

        // --- SỬA: Gọi class JDialog mới ---
        ChuyenBanDialog dialog = new ChuyenBanDialog(parentFrame);
        dialog.setVisible(true); // Hiển thị dialog
    }

    private void InLaiHoaDon() {
    }

    private void showGhepBanSplitDialog() {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);

        // --- SỬA: Gọi class JDialog mới ---
        GhepBanDialog dialog = new GhepBanDialog(parentFrame);
        dialog.setVisible(true); // Hiển thị dialog
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private JPanel createBanScreenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // Phần lọc bàn ở trên cùng
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);

        return panel;
    }

    // xoá khi có đủ màn hình
    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(244, 247, 252)); // Màu nền chính
        JLabel label = new JLabel("Đây là giao diện " + name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Quản Lý Bàn GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);

        QuanLyBanGUI quanLyBanGUI = new QuanLyBanGUI();
        frame.add(quanLyBanGUI);

        frame.setVisible(true);
    }
}
