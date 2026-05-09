package iuh.fit.gui;

import iuh.fit.core.dto.DanhMucMonDTO;
import iuh.fit.core.dto.MonAnDTO;
import iuh.fit.core.net.client.DanhMucMonRemoteService;
import iuh.fit.core.net.client.MonAnAdminRemoteService;
import iuh.fit.core.net.client.NetClientContext;
import iuh.fit.core.net.client.SocketClientConnection;
import iuh.fit.core.net.protocol.EventType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DanhMucMonGUI extends BaseEventAwarePanel {

    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private JScrollPane scrollPane;
    private JPanel filterButtonPanel;

    private final MonAnAdminRemoteService monAnRemoteService;
    private final DanhMucMonRemoteService danhMucMonRemoteService;

    private List<MonAnDTO> dsMonAnFull;
    private List<MonAnItemPanel> dsMonAnPanel;

    private String currentCategoryFilter = "Tất cả";
    private String currentKeywordFilter = "";

    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public DanhMucMonGUI() {
        this(Objects.requireNonNull(NetClientContext.getConnection(), "SocketClientConnection không được null."));
    }

    public DanhMucMonGUI(SocketClientConnection connection) {
        super(connection);
        this.monAnRemoteService = new MonAnAdminRemoteService(connection);
        this.danhMucMonRemoteService = new DanhMucMonRemoteService(connection);

        this.dsMonAnFull = new ArrayList<>();
        this.dsMonAnPanel = new ArrayList<>();

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMenuPanel(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            loadFilterButtons();
            loadDataFromDB();
        });
    }

    @Override
    protected void onBusinessEvent(EventType eventType) {
        if (eventType == EventType.MENU_UPDATED) {
            SwingUtilities.invokeLater(() -> {
                loadFilterButtons();
                loadDataFromDB();
            });
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh mục Món ăn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 50));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        JPanel searchWrapper = new JPanel(new BorderLayout(8, 0));
        searchWrapper.setBackground(Color.WHITE);
        searchWrapper.setPreferredSize(new Dimension(280, 40));
        searchWrapper.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));

        JLabel iconLabel = new JLabel("🔎");
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        iconLabel.setForeground(Color.GRAY);
        searchWrapper.add(iconLabel, BorderLayout.WEST);

        txtTimKiem = new JTextField();
        txtTimKiem.setBorder(null);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setOpaque(false);
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                currentKeywordFilter = txtTimKiem.getText().trim().toLowerCase();
                filterMonAn();
            }
        });

        searchWrapper.add(txtTimKiem, BorderLayout.CENTER);
        rightPanel.add(searchWrapper);

        JButton btnThem = new JButton("Thêm món");
        styleMainButton(btnThem, new Color(40, 167, 69));
        btnThem.addActionListener(e -> showAddMonAnDialog());

        rightPanel.add(btnThem);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel topContainer = new JPanel(new BorderLayout(10, 0));
        topContainer.setOpaque(false);

        filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterButtonPanel.setOpaque(false);

        JScrollPane filterScrollPane = new JScrollPane(filterButtonPanel);
        filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        filterScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        filterScrollPane.getViewport().setOpaque(false);
        filterScrollPane.setOpaque(false);
        filterScrollPane.setPreferredSize(new Dimension(0, 55));
        filterScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        topContainer.add(filterScrollPane, BorderLayout.CENTER);

        JButton btnAddCategory = new JButton("...");
        btnAddCategory.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnAddCategory.setForeground(Color.WHITE);
        btnAddCategory.setBackground(COLOR_ACCENT_BLUE);
        btnAddCategory.setFocusPainted(false);
        btnAddCategory.setBorderPainted(false);
        btnAddCategory.setPreferredSize(new Dimension(55, 55));
        btnAddCategory.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAddCategory.addActionListener(e -> {
            QuanLyDanhMucDialog qlDialog =
                    new QuanLyDanhMucDialog((Frame) SwingUtilities.getWindowAncestor(this));
            qlDialog.setVisible(true);

            if (qlDialog.isDataChanged()) {
                loadFilterButtons();
                loadDataFromDB();
            }
        });

        topContainer.add(btnAddCategory, BorderLayout.EAST);

        panel.add(topContainer, BorderLayout.NORTH);

        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(
                new FlowLayout(FlowLayout.LEFT, 20, 20)
        );
        pnlMenuItemContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlMenuItemContainer.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(pnlMenuItemContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(COLOR_BACKGROUND);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadDataFromDB() {
        try {
            dsMonAnFull = monAnRemoteService.findAll();

            pnlMenuItemContainer.removeAll();
            dsMonAnPanel.clear();

            for (MonAnDTO mon : dsMonAnFull) {
                MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Sửa món ăn");
                JMenuItem deleteItem = new JMenuItem("Xóa món ăn");

                editItem.addActionListener(e -> showEditMonAnDialog(mon));
                deleteItem.addActionListener(e -> deleteMonAn(mon));

                popupMenu.add(editItem);
                popupMenu.add(deleteItem);

                itemPanel.setComponentPopupMenu(popupMenu);

                itemPanel.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }

                    public void mousePressed(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }

                        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                            showEditMonAnDialog(mon);
                        }
                    }
                });

                dsMonAnPanel.add(itemPanel);
                pnlMenuItemContainer.add(itemPanel);
            }

            filterMonAn();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách món ăn: " + e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void filterMonAn() {
        pnlMenuItemContainer.removeAll();

        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAnDTO mon = itemPanel.getMonAn();

            boolean show = true;

            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) {
                    show = false;
                }
            }

            if (show && !currentKeywordFilter.isEmpty()) {
                String tenMon = mon.getTenMon() != null ? mon.getTenMon().toLowerCase() : "";

                if (!tenMon.contains(currentKeywordFilter)) {
                    show = false;
                }
            }

            if (show) {
                pnlMenuItemContainer.add(itemPanel);
            }
        }

        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    private void showAddMonAnDialog() {
        MonAnDialog dialog = new MonAnDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSucceeded()) {
            try {
                monAnRemoteService.add(dialog.getMonAnDTO());
                JOptionPane.showMessageDialog(this, "Thêm món thành công!");
                loadDataFromDB();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm món thất bại: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void showEditMonAnDialog(MonAnDTO mon) {
        MonAnDialog dialog = new MonAnDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                mon
        );
        dialog.setVisible(true);

        if (dialog.isSucceeded()) {
            try {
                monAnRemoteService.update(dialog.getMonAnDTO());
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataFromDB();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật thất bại: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void deleteMonAn(MonAnDTO mon) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa món: " + mon.getTenMon() + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                monAnRemoteService.delete(mon.getMaMonAn());
                JOptionPane.showMessageDialog(this, "Đã xóa món ăn.");
                loadDataFromDB();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Xóa thất bại: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void loadFilterButtons() {
        try {
            filterButtonPanel.removeAll();

            ButtonGroup group = new ButtonGroup();

            JToggleButton btnTatCa = createFilterButton("Tất cả", true);
            btnTatCa.setActionCommand("Tất cả");

            group.add(btnTatCa);
            filterButtonPanel.add(btnTatCa);

            ActionListener filterListener = e -> {
                currentCategoryFilter = e.getActionCommand();
                filterMonAn();
            };

            btnTatCa.addActionListener(filterListener);

            List<DanhMucMonDTO> dsDanhMuc = danhMucMonRemoteService.findAll();

            if (dsDanhMuc != null) {
                for (DanhMucMonDTO dm : dsDanhMuc) {
                    JToggleButton button = createFilterButton(dm.getTendm(), false);
                    button.setActionCommand(dm.getMadm());
                    button.addActionListener(filterListener);

                    group.add(button);
                    filterButtonPanel.add(button);
                }
            }

            filterButtonPanel.revalidate();
            filterButtonPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh mục món: " + e.getMessage(),
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);

        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(6, 16, 6, 16));

        button.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (button.isSelected()) {
                    g2.setColor(COLOR_ACCENT_BLUE);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    button.setForeground(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    g2.setColor(new Color(220, 220, 220));
                    g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
                    button.setForeground(Color.DARK_GRAY);
                }

                g2.dispose();
                super.paint(g, c);
            }
        });

        button.setSelected(selected);

        return button;
    }

    public static void styleMainButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
    }
}
