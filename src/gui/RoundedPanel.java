package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp Panel tùy chỉnh để vẽ nền bo góc.
 */
public class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius;

    public RoundedPanel(int radius, Color bgColor) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false); // Quan trọng: Đặt JPanel thành trong suốt
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình chữ nhật bo góc với màu nền được chỉ định
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
    }
}