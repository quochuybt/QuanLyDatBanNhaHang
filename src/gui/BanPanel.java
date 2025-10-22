package gui;

import entity.Ban;
import entity.TrangThaiBan;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel hiển thị 1 Bàn
 */
public class BanPanel extends JPanel {
    // --- Di chuyển các hằng số màu vào đây ---
    public static final Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);
    public static final Color COLOR_STATUS_FREE = new Color(138, 177, 254); // TRONG
    public static final Color COLOR_STATUS_OCCUPIED = new Color(239, 68, 68); // DANG_PHUC_VU
    public static final Color COLOR_STATUS_RESERVED = new Color(187, 247, 208); // DA_DAT_TRUOC

    private entity.Ban ban;
    private Border defaultBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1);
    private Border selectedBorder = BorderFactory.createLineBorder(new Color(0, 200, 255), 3);
    private boolean isSelected = false;

    public BanPanel(entity.Ban ban) {
        this.ban = ban;
        setLayout(new BorderLayout(0, 3));
        setPreferredSize(new Dimension(110, 100));
        setBackground(getMauTheoTrangThai(ban.getTrangThai()));
        setBorder(defaultBorder);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel statusBar = new JPanel();
        statusBar.setPreferredSize(new Dimension(0, 8));

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(25, 5, 5, 5));

        JLabel tenBanLabel = new JLabel(ban.getTenBan());
        tenBanLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tenBanLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel soGheLabel = new JLabel(ban.getSoGhe() + " ghế");
        soGheLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        soGheLabel.setForeground(Color.BLACK);
        soGheLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(tenBanLabel);
        infoPanel.add(soGheLabel);

        add(infoPanel, BorderLayout.CENTER);
    }

    public entity.Ban getBan() { return ban; }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        if (isSelected) {
            setBorder(selectedBorder);
        } else {
            setBorder(defaultBorder);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    private Color getMauTheoTrangThai(TrangThaiBan trangThai) {
        switch (trangThai) {
            case TRONG: return COLOR_STATUS_FREE;
            case DANG_PHUC_VU: return COLOR_STATUS_OCCUPIED;
            case DA_DAT_TRUOC: return COLOR_STATUS_RESERVED;
            default: return Color.GRAY;
        }
    }
}