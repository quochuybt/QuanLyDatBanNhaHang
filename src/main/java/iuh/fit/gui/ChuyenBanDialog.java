package iuh.fit.gui;


import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.service.BanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChuyenBanDialog extends JDialog {

    private List<BanDTO> allTablesFromDB;
    private BanDTO selectedSourceTable = null;
    private BanDTO selectedTargetTable = null;

    private JPanel leftTableContainer;
    private JPanel rightTableContainer;

    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";

    private List<BanPanel> leftBanPanelList = new ArrayList<>();
    private List<BanPanel> rightBanPanelList = new ArrayList<>();

    private final BanService banService = new BanService();

    public ChuyenBanDialog(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);

        try {
            this.allTablesFromDB = banService.getAllBan();

            System.out.println("Dialog Chuyển Bàn: Tải thành công "
                    + allTablesFromDB.size() + " bàn.");

        } catch (Exception e) {
            e.printStackTrace();
            this.allTablesFromDB = new ArrayList<>();
        }

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 100));
        setLayout(new GridBagLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setPreferredSize(new Dimension(1000, 600));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        contentPanel.add(createTitleBar(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel leftPane = createListPanel("Danh sách bàn trống", true);
        JPanel rightPane = createListPanel("Danh sách bàn đã đặt/ phục vụ", false);

        splitPane.setLeftComponent(rightPane);
        splitPane.setRightComponent(leftPane);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(createBottomBar(), BorderLayout.SOUTH);

        add(contentPanel);

        if (parent != null) {
            setSize(parent.getSize());
            setLocationRelativeTo(parent);
        } else {
            setSize(1100, 700);
            setLocationRelativeTo(null);
        }

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

        JButton btnChuyen = new JButton("Chuyển");
        stylePrimaryButton(btnChuyen);
        btnChuyen.addActionListener(e -> xuLyChuyenBan());

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

        JPanel tableContainer = new VerticallyWrappingFlowPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8)
        );

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

        boolean targetStillVisible = false;

        for (BanDTO ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả")
                    || khuVucFilter.equals(ban.getKhuVuc());

            boolean statusMatch = ban.getTrangThai() == TrangThaiBan.TRONG;

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);

                if (isSameBan(selectedTargetTable, ban)) {
                    banPanel.setSelected(true);
                    targetStillVisible = true;
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectTarget(ban, banPanel);
                        }
                    }
                });

                leftTableContainer.add(banPanel);
                leftBanPanelList.add(banPanel);
            }
        }

        if (!targetStillVisible) {
            selectedTargetTable = null;
        }

        leftTableContainer.revalidate();
        leftTableContainer.repaint();
    }

    private void populateRightPanel(String khuVucFilter) {
        rightTableContainer.removeAll();
        rightBanPanelList.clear();

        boolean sourceStillVisible = false;

        for (BanDTO ban : allTablesFromDB) {
            boolean khuVucMatch = khuVucFilter.equals("Tất cả")
                    || khuVucFilter.equals(ban.getKhuVuc());

            TrangThaiBan status = ban.getTrangThai();

            boolean statusMatch = status == TrangThaiBan.DANG_PHUC_VU
                    || status == TrangThaiBan.DA_DAT_TRUOC;

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);

                if (isSameBan(selectedSourceTable, ban)) {
                    banPanel.setSelected(true);
                    sourceStillVisible = true;
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectSource(ban, banPanel);
                        }
                    }
                });

                rightTableContainer.add(banPanel);
                rightBanPanelList.add(banPanel);
            }
        }

        if (!sourceStillVisible) {
            selectedSourceTable = null;
        }

        rightTableContainer.revalidate();
        rightTableContainer.repaint();
    }

    private void handleSelectSource(BanDTO ban, BanPanel clickedPanel) {
        if (isSameBan(selectedSourceTable, ban)) {
            selectedSourceTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        selectedSourceTable = ban;

        for (BanPanel p : rightBanPanelList) {
            p.setSelected(false);
        }

        clickedPanel.setSelected(true);
    }

    private void handleSelectTarget(BanDTO ban, BanPanel clickedPanel) {
        if (isSameBan(selectedTargetTable, ban)) {
            selectedTargetTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        selectedTargetTable = ban;

        for (BanPanel p : leftBanPanelList) {
            p.setSelected(false);
        }

        clickedPanel.setSelected(true);
    }

    private void xuLyChuyenBan() {
        if (selectedSourceTable == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn bàn cần chuyển (Bên trái)!",
                    "Chưa chọn bàn nguồn",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedTargetTable == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn bàn mới để đến (Bên phải)!",
                    "Chưa chọn bàn đích",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format(
                        "Xác nhận chuyển toàn bộ đơn từ bàn [%s] sang bàn [%s]?",
                        selectedSourceTable.getTenBan(),
                        selectedTargetTable.getTenBan()
                ),
                "Xác nhận chuyển bàn",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean ketQua = thucHienChuyenBanTrongDB(selectedSourceTable, selectedTargetTable);

        if (ketQua) {
            JOptionPane.showMessageDialog(
                    this,
                    "Chuyển bàn thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Chuyển bàn thất bại! Vui lòng kiểm tra lại.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean thucHienChuyenBanTrongDB(BanDTO banCu, BanDTO banMoi) {
        try {
            return banService.chuyenBan(banCu, banMoi);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hệ thống khi chuyển bàn: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private boolean isSameBan(BanDTO a, BanDTO b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getMaBan() == null || b.getMaBan() == null) {
            return false;
        }

        return a.getMaBan().equals(b.getMaBan());
    }

    private Ban toBanEntityForPanel(BanDTO dto) {
        if (dto == null) {
            return null;
        }

        Ban ban = new Ban();

        ban.setMaBan(dto.getMaBan());
        ban.setTenBan(dto.getTenBan());
        ban.setSoGhe(dto.getSoGhe());
        ban.setTrangThai(dto.getTrangThai());
        ban.setGioMoBan(dto.getGioMoBan());
        ban.setKhuVuc(dto.getKhuVuc());

        return ban;
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