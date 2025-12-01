package gui;

import dao.BanDAO;
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


public class GhepBanDialog extends JDialog {

    private List<Ban> allTablesFromDB;
    private List<Ban> selectedSourceTables = new ArrayList<>();
    private Ban selectedTargetTable = null;
    private JPanel leftTableContainer;
    private JPanel rightTableContainer;
    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";
    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private List<BanPanel> rightBanPanelList = new ArrayList<>();
    private BanDAO banDAO;

    public GhepBanDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        this.banDAO = new BanDAO();

        try {
            this.allTablesFromDB = banDAO.getAllBan();
        } catch (Exception e) {
            e.printStackTrace();
            this.allTablesFromDB = new ArrayList<>();
        }

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

        JPanel leftPane = createListPanel("Danh sách toàn bộ bàn", true);
        JPanel rightPane = createListPanel("Danh sách bàn đã đặt/ phục vụ", false);
        splitPane.setLeftComponent(leftPane);
        splitPane.setRightComponent(rightPane);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(createBottomBar(), BorderLayout.SOUTH);

        // Thêm panel nội dung vào JDialog
        add(contentPanel);
        setSize(parent.getSize());
        setLocationRelativeTo(parent);

        // Populate
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
            if (khuVucMatch) {
                BanPanel banPanel = new BanPanel(ban);
                if (selectedSourceTables.contains(ban)) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) handleSelectSource(ban, banPanel);
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
            // Chỉ hiện bàn Đang phục vụ hoặc Đã đặt
            boolean khuVucMatch = khuVucFilter.equals("Tất cả") || ban.getKhuVuc().equals(khuVucFilter);
            boolean statusMatch = (ban.getTrangThai() == TrangThaiBan.DANG_PHUC_VU || ban.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC);

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);
                if (selectedTargetTable != null && selectedTargetTable.equals(ban)) {
                    banPanel.setSelected(true);
                }
                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) handleSelectTarget(ban, banPanel);
                    }
                });
                rightTableContainer.add(banPanel);
                rightBanPanelList.add(banPanel);
            }
        }
        rightTableContainer.revalidate();
        rightTableContainer.repaint();
    }
    private void handleSelectSource(Ban ban, BanPanel clickedPanel) {
        // --- LOGIC MỚI: TỰ ĐỘNG GỠ KHỎI ĐÍCH ---
        // Nếu bàn này đang là Đích -> Hủy chọn Đích
        if (selectedTargetTable != null && selectedTargetTable.equals(ban)) {
            selectedTargetTable = null;
            // Cập nhật giao diện bên phải (Bỏ highlight bàn này bên phải)
            for (BanPanel p : rightBanPanelList) {
                if (p.getBan().equals(ban)) {
                    p.setSelected(false);
                    break;
                }
            }
        }
        // ----------------------------------------

        // Logic toggle chọn nhiều (giữ nguyên)
        if (selectedSourceTables.contains(ban)) {
            selectedSourceTables.remove(ban);
            clickedPanel.setSelected(false);
        } else {
            selectedSourceTables.add(ban);
            clickedPanel.setSelected(true);
        }
    }

    private void handleSelectTarget(Ban ban, BanPanel clickedPanel) {
        // Logic cũ: Bỏ chọn nếu click lại bàn đang chọn
        if (selectedTargetTable != null && selectedTargetTable.equals(ban)) {
            selectedTargetTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        // --- LOGIC MỚI: TỰ ĐỘNG GỠ KHỎI NGUỒN ---
        // Nếu bàn này đang được chọn làm Nguồn -> Xóa nó khỏi danh sách Nguồn
        if (selectedSourceTables.contains(ban)) {
            selectedSourceTables.remove(ban);
            // Cập nhật giao diện bên trái (Bỏ highlight bàn này bên trái)
            for (BanPanel p : leftBanPanelList) {
                if (p.getBan().equals(ban)) {
                    p.setSelected(false);
                    break;
                }
            }
        }
        // ----------------------------------------

        // Chọn bàn mới làm Đích
        selectedTargetTable = ban;

        // Reset visual các bàn khác bên phải (vì chỉ được chọn 1 đích)
        for (BanPanel p : rightBanPanelList) {
            p.setSelected(false);
        }
        clickedPanel.setSelected(true); // Highlight bàn vừa chọn
    }

    private void xuLyGhepBan() {
        if (selectedSourceTables.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 bàn nguồn!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedTargetTable == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bàn đích!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gọi DAO
        boolean kq = banDAO.ghepBanLienKet(selectedSourceTables, selectedTargetTable);

        if (kq) {
            JOptionPane.showMessageDialog(this, "Ghép bàn thành công!\nTất cả hóa đơn đã dồn về bàn " + selectedTargetTable.getTenBan(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi ghép bàn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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