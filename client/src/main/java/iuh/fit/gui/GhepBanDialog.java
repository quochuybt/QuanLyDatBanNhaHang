package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.entity.TrangThaiBan;
import iuh.fit.core.net.client.BanRemoteService;
import iuh.fit.core.net.client.SocketClientConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GhepBanDialog extends JDialog {

    private List<BanDTO> allTablesFromDB = new ArrayList<>();
    private final List<BanDTO> selectedSourceTables = new ArrayList<>();
    private BanDTO selectedTargetTable = null;

    private JPanel leftTableContainer;
    private JPanel rightTableContainer;

    private String currentLeftFilter = "Tất cả";
    private String currentRightFilter = "Tất cả";

    private final List<BanPanel> leftBanPanelList = new ArrayList<>();
    private final List<BanPanel> rightBanPanelList = new ArrayList<>();

    private final BanRemoteService banRemoteService;
    private final String maNVDangNhap;

    private JButton btnGhep;
    private JButton btnHuyBo;

    private boolean busy = false;

    public GhepBanDialog(Window parent, SocketClientConnection socketConnection) {
        this(parent, null, socketConnection);
    }

    public GhepBanDialog(Window parent, String maNVDangNhap, SocketClientConnection socketConnection) {
        super(parent, ModalityType.APPLICATION_MODAL);

        this.maNVDangNhap = maNVDangNhap;
        this.banRemoteService = new BanRemoteService(
                Objects.requireNonNull(socketConnection, "SocketClientConnection không được null.")
        );

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

        if (parent != null) {
            setSize(parent.getSize());
        } else {
            setSize(1000, 650);
        }

        setLocationRelativeTo(parent);

        loadTablesAsync();
    }

    private void loadTablesAsync() {
        setBusy(true);

        new SwingWorker<List<BanDTO>, Void>() {
            @Override
            protected List<BanDTO> doInBackground() {
                return banRemoteService.getAllBan();
            }

            @Override
            protected void done() {
                try {
                    List<BanDTO> data = get();
                    allTablesFromDB = data != null ? data : new ArrayList<>();

                    populateLeftPanel(currentLeftFilter);
                    populateRightPanel(currentRightFilter);

                    System.out.println("[Client] Dialog Ghép Bàn: tải thành công "
                            + allTablesFromDB.size()
                            + " bàn qua socket.");

                } catch (Exception e) {
                    allTablesFromDB = new ArrayList<>();
                    populateLeftPanel(currentLeftFilter);
                    populateRightPanel(currentRightFilter);

                    JOptionPane.showMessageDialog(
                            GhepBanDialog.this,
                            "Lỗi tải danh sách bàn: " + getRootMessage(e),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private JPanel createTitleBar() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Ghép bàn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(new EmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(closeButton, BorderLayout.EAST);

        return titlePanel;
    }

    private JPanel createBottomBar() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnGhep = new JButton("Ghép");
        stylePrimaryButton(btnGhep);
        btnGhep.addActionListener(e -> xuLyGhepBan());

        btnHuyBo = new JButton("Hủy bỏ");
        styleDefaultButton(btnHuyBo);
        btnHuyBo.addActionListener(e -> dispose());

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
        if (leftTableContainer == null) {
            return;
        }

        leftTableContainer.removeAll();
        leftBanPanelList.clear();

        if (allTablesFromDB == null) {
            allTablesFromDB = new ArrayList<>();
        }

        for (BanDTO ban : allTablesFromDB) {
            if (ban == null || ban.getMaBan() == null) {
                continue;
            }

            boolean khuVucMatch = khuVucFilter.equals("Tất cả")
                    || khuVucFilter.equals(ban.getKhuVuc());

            if (khuVucMatch) {
                BanPanel banPanel = new BanPanel(ban);

                boolean selected = selectedSourceTables.stream()
                        .anyMatch(b -> isSameBan(b, ban));

                if (selected) {
                    banPanel.setSelected(true);
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (busy) {
                            return;
                        }

                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectSource(ban, banPanel);
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
        if (rightTableContainer == null) {
            return;
        }

        rightTableContainer.removeAll();
        rightBanPanelList.clear();

        if (allTablesFromDB == null) {
            allTablesFromDB = new ArrayList<>();
        }

        for (BanDTO ban : allTablesFromDB) {
            if (ban == null || ban.getMaBan() == null || ban.getTrangThai() == null) {
                continue;
            }

            boolean khuVucMatch = khuVucFilter.equals("Tất cả")
                    || khuVucFilter.equals(ban.getKhuVuc());

            boolean statusMatch = laTrangThai(ban, TrangThaiBan.DANG_PHUC_VU)
                    || laTrangThai(ban, TrangThaiBan.DA_DAT_TRUOC);

            if (khuVucMatch && statusMatch) {
                BanPanel banPanel = new BanPanel(ban);

                if (isSameBan(selectedTargetTable, ban)) {
                    banPanel.setSelected(true);
                }

                banPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (busy) {
                            return;
                        }

                        if (e.getButton() == MouseEvent.BUTTON1) {
                            handleSelectTarget(ban, banPanel);
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

    private void handleSelectSource(BanDTO ban, BanPanel clickedPanel) {
        if (ban == null || ban.getMaBan() == null) {
            return;
        }

        if (isSameBan(selectedTargetTable, ban)) {
            selectedTargetTable = null;

            for (BanPanel p : rightBanPanelList) {
                if (p != null && p.getBan() != null && isSameBan(p.getBan(), ban)) {
                    p.setSelected(false);
                    break;
                }
            }
        }

        boolean removed = selectedSourceTables.removeIf(b -> isSameBan(b, ban));

        if (removed) {
            clickedPanel.setSelected(false);
        } else {
            selectedSourceTables.add(ban);
            clickedPanel.setSelected(true);
        }
    }

    private void handleSelectTarget(BanDTO ban, BanPanel clickedPanel) {
        if (ban == null || ban.getMaBan() == null) {
            return;
        }

        if (isSameBan(selectedTargetTable, ban)) {
            selectedTargetTable = null;
            clickedPanel.setSelected(false);
            return;
        }

        selectedTargetTable = ban;

        selectedSourceTables.removeIf(b -> isSameBan(b, ban));

        for (BanPanel p : leftBanPanelList) {
            if (p != null && p.getBan() != null && isSameBan(p.getBan(), ban)) {
                p.setSelected(false);
            }
        }

        for (BanPanel p : rightBanPanelList) {
            p.setSelected(false);
        }

        clickedPanel.setSelected(true);
    }

    private boolean laTrangThai(BanDTO ban, TrangThaiBan trangThai) {
        if (ban == null || ban.getTrangThai() == null || trangThai == null) {
            return false;
        }

        return trangThai.name().equalsIgnoreCase(ban.getTrangThai().toString());
    }

    private void xuLyGhepBan() {
        if (selectedSourceTables.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ít nhất 1 bàn nguồn!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedTargetTable == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn 1 bàn đích!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        List<BanDTO> dsNguonGuiDi = new ArrayList<>(selectedSourceTables);
        dsNguonGuiDi.removeIf(b -> isSameBan(b, selectedTargetTable));

        if (dsNguonGuiDi.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn phải chọn thêm ít nhất 1 bàn khác để ghép vào bàn đích!",
                    "Lỗi chọn bàn",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!laTrangThai(selectedTargetTable, TrangThaiBan.DANG_PHUC_VU)
                && !laTrangThai(selectedTargetTable, TrangThaiBan.DA_DAT_TRUOC)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bàn đích phải là bàn đang phục vụ hoặc đã đặt trước.",
                    "Không thể ghép",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận ghép " + dsNguonGuiDi.size()
                        + " bàn vào bàn [" + selectedTargetTable.getTenBan() + "]?",
                "Xác nhận ghép bàn",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        thucHienGhepBanQuaSocket(dsNguonGuiDi, selectedTargetTable);
    }

    private void thucHienGhepBanQuaSocket(List<BanDTO> dsNguon, BanDTO banDich) {
        setBusy(true);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return banRemoteService.ghepBanLienKet(dsNguon, banDich);
            }

            @Override
            protected void done() {
                try {
                    boolean success = Boolean.TRUE.equals(get());

                    if (success) {
                        JOptionPane.showMessageDialog(
                                GhepBanDialog.this,
                                "Ghép bàn thành công!",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                GhepBanDialog.this,
                                "Ghép bàn thất bại. Vui lòng kiểm tra lại.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        setBusy(false);
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            GhepBanDialog.this,
                            "Có lỗi xảy ra khi ghép bàn: " + getRootMessage(ex),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    setBusy(false);
                }
            }
        }.execute();
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

    private void setBusy(boolean busy) {
        this.busy = busy;

        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor()
        );

        if (btnGhep != null) {
            btnGhep.setEnabled(!busy);
        }

        if (btnHuyBo != null) {
            btnHuyBo.setEnabled(!busy);
        }

        if (leftTableContainer != null) {
            leftTableContainer.setEnabled(!busy);
        }

        if (rightTableContainer != null) {
            rightTableContainer.setEnabled(!busy);
        }
    }

    private String getRootMessage(Exception e) {
        Throwable t = e;

        while (t.getCause() != null) {
            t = t.getCause();
        }

        return t.getMessage() != null ? t.getMessage() : e.getMessage();
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