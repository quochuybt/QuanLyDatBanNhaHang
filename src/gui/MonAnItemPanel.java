package gui;

import entity.MonAn;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class MonAnItemPanel extends JPanel {

    private MonAn monAn;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Màu sắc (Giống code cũ)
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color HOVER_COLOR = new Color(230, 245, 255);

    public MonAnItemPanel(MonAn monAn) {
        this.monAn = monAn;
        buildUI();
        addMouseHoverEffect();
    }

    private void buildUI() {
        // --- 1. SETUP LAYOUT & KÍCH THƯỚC CŨ ---
        setLayout(new BorderLayout(0, 5)); // Khoảng cách giữa ảnh và text
        setBackground(Color.WHITE);

        // Viền và Padding giống code cũ
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Kích thước cố định giống code cũ
        setPreferredSize(new Dimension(140, 180));

        // --- 2. XỬ LÝ ẢNH (LOGIC MỚI - THÔNG MINH HƠN) ---
        JLabel lblHinhAnh = new JLabel();
        lblHinhAnh.setHorizontalAlignment(SwingConstants.CENTER);

        String imgName = monAn.getHinhAnh();
        ImageIcon icon = null;

        // Bước 1: Tìm ảnh theo đường dẫn file thực tế (Dành cho ảnh vừa upload/copy vào src)
        if (imgName != null && !imgName.isEmpty()) {
            try {
                String projectPath = System.getProperty("user.dir") + "/src/img/MonAn/" + imgName;
                File f = new File(projectPath);

                if (f.exists()) {
                    icon = new ImageIcon(projectPath);
                } else {
                    // Bước 2: Nếu không thấy file, tìm trong Resource (Dành cho ảnh có sẵn khi build JAR)
                    java.net.URL url = getClass().getResource("/img/MonAn/" + imgName);
                    if (url != null) {
                        icon = new ImageIcon(url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Bước 3: Nếu vẫn không có ảnh, dùng Placeholder
        if (icon == null) {
            try {
                java.net.URL placeholderUrl = getClass().getResource("/img/placeholder/mon_an.png");
                if (placeholderUrl != null) {
                    icon = new ImageIcon(placeholderUrl);
                }
            } catch (Exception e) { }
        }

        // Render ảnh lên Label (Resize về 120x100 giống code cũ)
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(120, 100, Image.SCALE_SMOOTH);
            lblHinhAnh.setIcon(new ImageIcon(img));
        } else {
            lblHinhAnh.setText("No Image");
            lblHinhAnh.setPreferredSize(new Dimension(120, 100));
            lblHinhAnh.setOpaque(true);
            lblHinhAnh.setBackground(new Color(240, 240, 240));
            lblHinhAnh.setForeground(Color.GRAY);
        }

        add(lblHinhAnh, BorderLayout.CENTER);

        // --- 3. PANEL THÔNG TIN (GIỐNG CODE CŨ) ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Tên món
        JLabel lblTenMon = new JLabel(monAn.getTenMon());
        lblTenMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTenMon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Giá tiền
        JLabel lblGiaTien = new JLabel(nf.format(monAn.getDonGia()));
        lblGiaTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaTien.setForeground(Color.RED);
        lblGiaTien.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(lblTenMon);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3))); // Gap nhỏ
        infoPanel.add(lblGiaTien);

        add(infoPanel, BorderLayout.SOUTH);
    }

    // --- 4. HIỆU ỨNG HOVER (GIỮ NGUYÊN) ---
    private void addMouseHoverEffect() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(HOVER_COLOR);
                // Làm cho infoPanel cũng đổi màu nền theo (nếu nó không opaque=false)
                for (Component component : getComponents()) {
                    if (component instanceof JPanel) {
                        component.setBackground(HOVER_COLOR);
                    }
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.WHITE);
                for (Component component : getComponents()) {
                    if (component instanceof JPanel) {
                        JPanel innerPanel = (JPanel) component;
                        innerPanel.setBackground(Color.WHITE);
                        innerPanel.setOpaque(false);
                    }
                }
            }
        });
    }

    public MonAn getMonAn() {
        return monAn;
    }
}