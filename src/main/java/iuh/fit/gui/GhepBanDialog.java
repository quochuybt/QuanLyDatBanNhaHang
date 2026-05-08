package iuh.fit.gui; // Đổi package nếu cần

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.service.BanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GhepBanDialog extends JDialog {

    private List<BanDTO> allTablesFromDB;
    private List<BanDTO> selectedSourceTables = new ArrayList<>();
    private BanDTO selectedTargetTable = null;

    private JPanel leftTableContainer;
    private JPanel rightTableContainer;
    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private List<BanPanel> rightBanPanelList = new ArrayList<>();

    // Sử dụng Service thay cho DAO
    private final BanService banService;

    public GhepBanDialog(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        this.banService = new BanService();

        try {
            // Lấy danh sách DTO từ Service
            this.allTablesFromDB = banService.getAllBan();
        } catch (Exception e) {
            e.printStackTrace();
            this.allTablesFromDB = new ArrayList<>();
        }

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 100));
        setLayout(new GridBagLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setPreferredSize(new Dimension(900, 600));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        contentPanel.add(createTitleBar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel leftPane = createListPanel("Danh sách toàn bộ bàn", true);
        JPanel rightPane = createListPanel("Danh sách bàn đã đặt/ phục vụ", false);
        splitPane.setLeftComponent(leftPane);
        splitPane.setRightComponent(rightPane);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(createBottomBar(), BorderLayout.SOUTH);

        add(contentPanel);
        setSize(parent.getSize());
        setLocationRelativeTo(parent);

        populateLeftPanel(currentLeftFilter);
        populateRightPanel(currentRightFilter);
    }

    private JPanel createTitleBar() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Ghép bàn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        titlePanel.add(closeButton, BorderLayout.EAST);
        return titlePanel;
    }

    private JPanel createBottomBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnHuyBo = new JButton("Hủy bỏ");
        styleDefaultButton(btnHuyBo);
        btnHuyBo.addActionListener(e -> dispose());

        JButton btnGhep = new JButton("Ghép");
        stylePrimaryButton(btnGhep);
        btnGhep.addActionListener(e -> xuLyGhepBan());

        buttonPanel.add(btnGhep);
        buttonPanel.add(btnHuyBo);
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
        String[] filters = { "Tất cả", "Tầng trệt", "Tầng 1" };
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
                button.setBackground(BanPanel.COLOR_ACCENT_BLUE);
                button.setForeground(Color.WHITE);
                button.setBorder(new EmptyBorder(5, 15, 5, 15));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        new EmptyBorder(4, 14, 4, 14)));
            }
        });
        button.setSelected(selected);
        return button;
    }

    private void populateLeftPanel(String khuVucFilter) {
        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        for (BanDTO ban : allTablesFromDB) { // Sử dụng BanDTO
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);
            if (khuVucMatch) {
                BanPanel banPanel = new BanPanel(ban);
                // Kiểm tra dựa trên mã bàn để đảm bảo equals hoạt động đúng
                if (selectedSourceTables.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()))) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1)
                            handleSelectSource(ban, banPanel);
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

        for (BanDTO ban : allTablesFromDB) { // Sử dụng BanDTO
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);

            // So sánh chuỗi cho trạng thái thay vì dùng Enum
            boolean statusMatch = "DANG_PHUC_VU".equalsIgnoreCase(ban.getTrangThai().toString())
                    || "DA_DAT_TRUOC".equalsIgnoreCase(ban.getTrangThai().toString());

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);
                if (selectedTargetTable != null && selectedTargetTable.getMaBan().equals(ban.getMaBan())) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1)
                            handleSelectTarget(ban, banPanel);
                    }
                });
                rightTableContainer.add(banPanel);
                rightBanPanelList.add(banPanel);
            }
        }
        rightTableContainer.revalidate();
        rightTableContainer.repaint();
    }

    private void handleSelectSource(BanDTO ban, BanPanel clickedPanel) {
        if (selectedTargetTable != null && selectedTargetTable.getMaBan().equals(ban.getMaBan())) {
            selectedTargetTable = null;
            for (BanPanel p : rightBanPanelList) {
                if (p.getBan().getMaBan().equals(ban.getMaBan())) {
                    p.setSelected(false);
                    break;
                }
            }
        }

        // Cập nhật logic xóa/thêm sử dụng Stream để so sánh mã bàn an toàn hơn
        boolean removed = selectedSourceTables.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
        if (removed) {
            clickedPanel.setSelected(false);
        } else {
            selectedSourceTables.add(ban);
            clickedPanel.setSelected(true);
        }
    }

    private void handleSelectTarget(BanDTO ban, BanPanel clickedPanel) {
        if (selectedTargetTable != null && selectedTargetTable.getMaBan().equals(ban.getMaBan())) {
            selectedTargetTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        selectedTargetTable = ban;

        for (BanPanel p : rightBanPanelList) {
            p.setSelected(false);
        }
        clickedPanel.setSelected(true);
    }

    private void xuLyGhepBan() {
        if (selectedSourceTables.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 bàn nguồn!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedTargetTable == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bàn đích!", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Truyền danh sách DTO qua Service xử lý
        boolean kq = banService.ghepBanLienKet(selectedSourceTables, selectedTargetTable);

        if (kq) {
            JOptionPane.showMessageDialog(this,
                    "Ghép bàn thành công!\nTất cả hóa đơn đã dồn về bàn " + selectedTargetTable.getTenBan(),
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi ghép bàn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stylePrimaryButton(JButton b) {
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