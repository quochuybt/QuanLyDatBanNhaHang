package iuh.fit.gui;

import iuh.fit.core.dto.MonAnDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class MonAnItemPanel extends JPanel {

    private MonAnDTO monAn;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final java.util.function.Function<String, java.awt.Image> imageLoader;

    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color HOVER_COLOR = new Color(230, 245, 255);

    public MonAnItemPanel(MonAnDTO monAn, java.util.function.Function<String, java.awt.Image> imageLoader) {
        this.monAn = monAn;
        this.imageLoader = imageLoader;
        buildUI();
        addMouseHoverEffect();
    }

    // Constructor tương thích ngược (không có ảnh từ server)
    public MonAnItemPanel(MonAnDTO monAn) {
        this(monAn, null);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 5));
        setBackground(Color.WHITE);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));

        setPreferredSize(new Dimension(140, 180));

        JLabel lblHinhAnh = new JLabel();
        lblHinhAnh.setHorizontalAlignment(SwingConstants.CENTER);

        String imgName = monAn.getHinhAnh();
        ImageIcon icon = null;

        if (imgName != null && !imgName.isEmpty() && imageLoader != null) {
            try {
                java.awt.Image img = imageLoader.apply(imgName);
                if (img != null) {
                    icon = new ImageIcon(img);
                }
            } catch (Exception e) {
                // bỏ qua, hiển thị placeholder
            }
        }

        if (icon == null) {
            try {
                java.net.URL placeholderUrl = getClass().getResource("/img/placeholder/mon_an.png");
                if (placeholderUrl != null) {
                    icon = new ImageIcon(placeholderUrl);
                }
            } catch (Exception e) {
                // bỏ qua nếu không có ảnh placeholder
            }
        }

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

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTenMon = new JLabel(monAn.getTenMon());
        lblTenMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTenMon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGiaTien = new JLabel(nf.format(monAn.getDonGia()));
        lblGiaTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaTien.setForeground(Color.RED);
        lblGiaTien.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(lblTenMon);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(lblGiaTien);

        add(infoPanel, BorderLayout.SOUTH);
    }

    private void addMouseHoverEffect() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(HOVER_COLOR);
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

    public MonAnDTO getMonAn() {
        return monAn;
    }
}