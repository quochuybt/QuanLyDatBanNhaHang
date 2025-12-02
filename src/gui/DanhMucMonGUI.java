package gui;

import dao.DanhMucMonDAO;
import dao.MonAnDAO;
import entity.DanhMucMon;
import entity.MonAn;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonGUI extends JPanel {

    // --- Components ---
    private JPanel pnlMenuItemContainer;
    private JTextField txtTimKiem;
    private JScrollPane scrollPane;
    private JPanel filterButtonPanel;

    // --- Data & DAO ---
    private MonAnDAO monAnDAO;
    private DanhMucMonDAO danhMucMonDAO;
    private List<MonAn> dsMonAnFull;
    private List<MonAnItemPanel> dsMonAnPanel;
    private String currentCategoryFilter = "Tất cả";
    private String currentKeywordFilter = "";

    // --- Constants ---
    private static final Color COLOR_BACKGROUND = new Color(244, 247, 252);
    private static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

    public DanhMucMonGUI() {
        this.monAnDAO = new MonAnDAO();
        this.danhMucMonDAO = new DanhMucMonDAO();
        this.dsMonAnFull = new ArrayList<>();
        this.dsMonAnPanel = new ArrayList<>();

        setLayout(new BorderLayout(10, 15));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMenuPanel(), BorderLayout.CENTER);

        // Load dữ liệu
        SwingUtilities.invokeLater(() -> {
            loadFilterButtons();
            loadDataFromDB();
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh mục Món ăn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 50));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // --- Panel chứa: Tìm kiếm + Nút Thêm ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        // 1. Ô tìm kiếm
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

        // 2. Nút Thêm Món
        JButton btnThem = new JButton("Thêm món");
        styleMainButton(btnThem, new Color(40, 167, 69));

        // Kiểm tra ảnh
        String iconPath = "/img/icon/add_circle.png";
        URL iconURL = getClass().getResource(iconPath);
        if (iconURL != null) {
            btnThem.setIcon(new ImageIcon(iconURL));
            btnThem.setIconTextGap(8);
        }

        btnThem.addActionListener(e -> showAddMonAnDialog());
        rightPanel.add(btnThem);

        headerPanel.add(rightPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // --- SỬA LỖI CĂN GIỮA TẠI ĐÂY ---
        // Thay đổi FlowLayout: Tăng Vgap từ 0 lên 10 để đẩy nút xuống giữa
        filterButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterButtonPanel.setOpaque(false);

        JScrollPane filterScrollPane = new JScrollPane(filterButtonPanel);
        filterScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filterScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        // Tạo viền xám bao quanh thanh lọc giống trong hình bạn gửi
        filterScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        filterScrollPane.getViewport().setOpaque(false);
        filterScrollPane.setOpaque(false);
        // Tăng chiều cao lên một chút để thoải mái hơn (từ 50 -> 55)
        filterScrollPane.setPreferredSize(new Dimension(0, 55));

        filterScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        panel.add(filterScrollPane, BorderLayout.NORTH);

        // CENTER: Lưới món ăn
        pnlMenuItemContainer = new VerticallyWrappingFlowPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
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

    private void loadFilterButtons() {
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

        List<DanhMucMon> dsDanhMuc = danhMucMonDAO.getAllDanhMuc();
        if (dsDanhMuc != null) {
            for (DanhMucMon dm : dsDanhMuc) {
                JToggleButton button = createFilterButton(dm.getTendm(), false);
                button.setActionCommand(dm.getMadm());
                button.addActionListener(filterListener);
                group.add(button);
                filterButtonPanel.add(button);
            }
        }
        filterButtonPanel.revalidate();
        filterButtonPanel.repaint();
    }

    // --- SỬA LỖI BO GÓC TẠI ĐÂY ---
    private JToggleButton createFilterButton(String text, boolean selected) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        // Padding cho nút
        button.setBorder(new EmptyBorder(6, 16, 6, 16));

        // Tự vẽ lại nút: Dùng fillRect thay vì fillRoundRect
        button.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (button.isSelected()) {
                    g2.setColor(COLOR_ACCENT_BLUE);
                    // SỬA: Dùng fillRect để vuông góc
                    g2.fillRect(0, 0, c.getWidth(), c.getHeight());
                    button.setForeground(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, c.getWidth(), c.getHeight());

                    // Vẽ viền mờ khi không chọn (Vuông góc)
                    g2.setColor(new Color(220, 220, 220));
                    g2.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
                    button.setForeground(Color.DARK_GRAY);
                }

                // Hiệu ứng hover
                if(button.getModel().isRollover() && !button.isSelected()){
                    g2.setColor(new Color(240, 240, 240));
                    g2.fillRect(0, 0, c.getWidth(), c.getHeight());
                    button.setForeground(Color.BLACK);
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

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
    }

    private void loadDataFromDB() {
        dsMonAnFull = monAnDAO.getAllMonAn();
        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        for (MonAn mon : dsMonAnFull) {
            MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Sửa món ăn");
            JMenuItem deleteItem = new JMenuItem("Xóa món ăn");
            editItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            deleteItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            editItem.addActionListener(e -> showEditMonAnDialog(mon));
            deleteItem.addActionListener(e -> deleteMonAn(mon));

            popupMenu.add(editItem);
            popupMenu.add(deleteItem);

            itemPanel.setComponentPopupMenu(popupMenu);
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        showEditMonAnDialog(mon);
                    }
                }
            });

            dsMonAnPanel.add(itemPanel);
            pnlMenuItemContainer.add(itemPanel);
        }
        filterMonAn();
    }

    private void filterMonAn() {
        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true;
            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) show = false;
            }
            if (show && !currentKeywordFilter.isEmpty()) {
                if (!mon.getTenMon().toLowerCase().contains(currentKeywordFilter)) show = false;
            }
            itemPanel.setVisible(show);
        }
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    private void showAddMonAnDialog() {
        MonAnDialog dialog = new MonAnDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            if (monAnDAO.themMonAn(dialog.getMonAn())) {
                JOptionPane.showMessageDialog(this, "Thêm món thành công!");
                loadDataFromDB();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm món thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditMonAnDialog(MonAn mon) {
        MonAnDialog dialog = new MonAnDialog((Frame) SwingUtilities.getWindowAncestor(this), mon);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            if (monAnDAO.capNhatMonAn(dialog.getMonAn())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadDataFromDB();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteMonAn(MonAn mon) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa món: " + mon.getTenMon() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (monAnDAO.xoaMonAn(mon.getMaMonAn())) {
                JOptionPane.showMessageDialog(this, "Đã xóa món ăn.");
                loadDataFromDB();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}