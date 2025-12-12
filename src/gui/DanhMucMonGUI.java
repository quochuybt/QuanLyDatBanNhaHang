package gui;

import dao.DanhMucMonDAO;
import dao.MonAnDAO;
import entity.DanhMucMon;
import entity.MonAn;

import javax.swing.*;
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
    private List<MonAnItemPanel> dsMonAnPanel; // Danh sách các panel con
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

        // Load dữ liệu khi khởi chạy
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
        styleMainButton(btnThem, new Color(40, 167, 69)); // Màu xanh lá

        // Logic khi bấm nút Thêm
        btnThem.addActionListener(e -> showAddMonAnDialog());
        rightPanel.add(btnThem);

        headerPanel.add(rightPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Thanh lọc danh mục
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

        panel.add(filterScrollPane, BorderLayout.NORTH);

        // CENTER: Lưới chứa món ăn (Sử dụng Layout Wrap)
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

    // --- LOGIC LOAD DỮ LIỆU ---
    private void loadDataFromDB() {
        dsMonAnFull = monAnDAO.getAllMonAn();
        pnlMenuItemContainer.removeAll();
        dsMonAnPanel.clear();

        for (MonAn mon : dsMonAnFull) {
            // Tạo Panel con cho từng món (Class ở dưới)
            MonAnItemPanel itemPanel = new MonAnItemPanel(mon);

            // Context Menu (Chuột phải)
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Sửa món ăn");
            JMenuItem deleteItem = new JMenuItem("Xóa món ăn");

            editItem.addActionListener(e -> showEditMonAnDialog(mon));
            deleteItem.addActionListener(e -> deleteMonAn(mon));

            popupMenu.add(editItem);
            popupMenu.add(deleteItem);

            itemPanel.setComponentPopupMenu(popupMenu);

            // Sự kiện Click đúp để sửa
            itemPanel.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
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
        filterMonAn(); // Áp dụng lọc ngay sau khi load
    }

    private void filterMonAn() {
        pnlMenuItemContainer.removeAll();
        for (MonAnItemPanel itemPanel : dsMonAnPanel) {
            MonAn mon = itemPanel.getMonAn();
            boolean show = true;
            // Lọc theo danh mục
            if (!currentCategoryFilter.equals("Tất cả")) {
                if (mon.getMaDM() == null || !mon.getMaDM().equals(currentCategoryFilter)) show = false;
            }
            // Lọc theo từ khóa
            if (show && !currentKeywordFilter.isEmpty()) {
                if (!mon.getTenMon().toLowerCase().contains(currentKeywordFilter)) show = false;
            }

            if (show) pnlMenuItemContainer.add(itemPanel);
        }
        pnlMenuItemContainer.revalidate();
        pnlMenuItemContainer.repaint();
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

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

    // --- UI HELPERS ---
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (button.isSelected()) {
                    g2.setColor(COLOR_ACCENT_BLUE);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10); // Bo góc
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