package gui;

import entity.Ban;
import entity.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog chức năng Ghép Bàn
 */
public class ChuyenBanDialog extends JDialog {

    private final List<Ban> allTablesFromDB;
    private List<Ban> selectedTables;
    private JPanel leftTableContainer;
    private JPanel rightTableContainer;
    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private List<BanPanel> rightBanPanelList = new ArrayList<>();

    public ChuyenBanDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL); // <-- Quan trọng
        this.selectedTables = new ArrayList<>();

        // (Dữ liệu mẫu giữ nguyên)
        allTablesFromDB = new ArrayList<>();
        try {
            LocalDateTime time = LocalDateTime.now().plusHours(1);
            allTablesFromDB.add(new Ban("Bàn 1", 4, TrangThaiBan.TRONG, time, "Tầng trệt"));
            allTablesFromDB.add(new Ban("Bàn 2", 2, TrangThaiBan.DANG_PHUC_VU, time, "Tầng 1"));
            allTablesFromDB.add(new Ban("Bàn 3", 4, TrangThaiBan.DA_DAT_TRUOC, time, "Tầng trệt"));
            allTablesFromDB.add(new Ban("Bàn 4", 6, TrangThaiBan.TRONG, time, "Tầng 1"));
            // (Thêm dữ liệu khác của bạn)
            for (int i = 8; i <= 30; i++) {
                allTablesFromDB.add(new Ban("Bàn " + i, 4, TrangThaiBan.TRONG, time, "Tầng trệt"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        // --- Thiết lập cho JDialog (thay vì JPanel) ---
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 100)); // Hiệu ứng mờ
        setLayout(new GridBagLayout()); // Để căn giữa

        // Panel nội dung chính
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setPreferredSize(new Dimension(900, 600));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        contentPanel.add(createTitleBar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        JPanel leftPane = createListPanel("Danh sách bàn trống", true);
        JPanel rightPane = createListPanel("Danh sách bàn đã đặt/ phục vụ", false);
        splitPane.setLeftComponent(leftPane);
        splitPane.setRightComponent(rightPane);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        contentPanel.add(createBottomBar(), BorderLayout.SOUTH);

        // Thêm panel nội dung vào JDialog
        add(contentPanel);

        pack(); // Tự động điều chỉnh kích thước
        setLocationRelativeTo(parent); // Căn giữa so với cửa sổ cha

        // Populate
        populateLeftPanel(currentLeftFilter);
        populateRightPanel(currentRightFilter);
    }


    private JPanel createTitleBar() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Chuyển bàn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose()); // <-- Sửa: dispose()
        titlePanel.add(closeButton, BorderLayout.EAST);
        return titlePanel;
    }

    private JPanel createBottomBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton btnHuyBo = new JButton("Hủy bỏ");
        styleDefaultButton(btnHuyBo);
        btnHuyBo.addActionListener(e -> dispose()); // <-- Sửa: dispose()
        JButton btnChuyen = new JButton("Chuyển");
        stylePrimaryButton(btnChuyen);
        btnChuyen.addActionListener(e -> {
            System.out.println("Các bàn được chọn để chuyển: " + selectedTables.size() + " bàn");
            for (Ban b : selectedTables) {
                System.out.println(" - " + b.getTenBan() + " (Mã: " + b.getMaBan() + ")");
            }
            dispose(); // <-- Sửa: dispose()
        });
        buttonPanel.add(btnHuyBo);
        buttonPanel.add(btnChuyen);
        return buttonPanel;
    }

    private JPanel createListPanel(String title, boolean isLeftPanel) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 5, 0, 5));
        JPanel headerPanel = new JPanel(new BorderLayout(0, 5));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(createFilterPanel(isLeftPanel), BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Sử dụng class công khai
        JPanel tableContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
        if (isLeftPanel) {
            this.leftTableContainer = tableContainer;
        } else {
            this.rightTableContainer = tableContainer;
        }
        JScrollPane scrollPane = new JScrollPane(tableContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterPanel(boolean isLeftPanel) {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        String[] filters = {"Tất cả", "Tầng trệt", "Tầng 1"};
        ActionListener filterListener = e -> {
            String selectedFilter = e.getActionCommand();
            if (isLeftPanel) {
                currentLeftFilter = selectedFilter;
                populateLeftPanel(currentLeftFilter);
            } else {
                currentRightFilter = selectedFilter;
                populateRightPanel(currentRightFilter);
            }
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
                // Sử dụng hằng số từ BanPanel
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
        leftTableContainer.removeAll();
        leftBanPanelList.clear();
        for (Ban ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);

            // --- SỬA: Check 2: Lọc trạng thái (chỉ bàn TRONG) ---
            boolean statusMatch = (ban.getTrangThai() == TrangThaiBan.TRONG);

            // Phải khớp CẢ HAI
            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);
                if (selectedTables.contains(ban)) {
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
            }
        }
        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    private void populateRightPanel(String khuVucFilter) {
        rightTableContainer.removeAll();
        rightBanPanelList.clear();
        for (Ban ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);
            TrangThaiBan status = ban.getTrangThai();
            boolean statusMatch = (status == TrangThaiBan.DANG_PHUC_VU || status == TrangThaiBan.DA_DAT_TRUOC);
            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban); // <-- Dùng class BanPanel công khai
                if (selectedTables.contains(ban)) {
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
                rightTableContainer.add(banPanel);
                rightBanPanelList.add(banPanel);
            }
        }
        rightTableContainer.revalidate();
        rightTableContainer.repaint();
    }

    private void toggleSelection(Ban ban, BanPanel clickedPanel) {
        boolean isNowSelected;
        if (clickedPanel.isSelected()) {
            selectedTables.remove(ban);
            isNowSelected = false;
        } else {
            selectedTables.add(ban);
            isNowSelected = true;
        }
        clickedPanel.setSelected(isNowSelected);
        if (leftTableContainer.isAncestorOf(clickedPanel)) {
            updateVisualInList(rightBanPanelList, ban, isNowSelected);
        } else {
            updateVisualInList(leftBanPanelList, ban, isNowSelected);
        }
    }

    private void updateVisualInList(List<BanPanel> panelList, Ban ban, boolean isSelected) {
        for (BanPanel panel : panelList) {
            if (panel.getBan().equals(ban)) {
                panel.setSelected(isSelected);
                break;
            }
        }
    }

    private void stylePrimaryButton(JButton b) {
        // Sử dụng hằng số từ BanPanel
        b.setBackground(BanPanel.COLOR_ACCENT_BLUE);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDefaultButton(JButton b) {
        b.setBackground(new Color(230, 230, 230));
        b.setForeground(new Color(51, 51, 51));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}