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
        setLayout(new BorderLayout(0, 0)); // Giảm gap
        setPreferredSize(new Dimension(110, 100));
        setBackground(getMauTheoTrangThai(ban.getTrangThai()));
        setBorder(defaultBorder);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- 1. THÊM TOOLTIP (Khi rê chuột vào sẽ hiện tên đầy đủ) ---
        setToolTipText(ban.getTenBan() + " (" + ban.getSoGhe() + " ghế)");

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        // --- 2. GIẢM PADDING (Để có nhiều chỗ trống hơn) ---
        // Cũ: EmptyBorder(25, 5, 5, 5) -> Mới: EmptyBorder(10, 2, 2, 2)
        infoPanel.setBorder(new EmptyBorder(10, 2, 2, 2));

        // --- 3. SỬ DỤNG HTML ĐỂ TỰ ĐỘNG XUỐNG DÒNG ---
        // Logic: Dùng thẻ <html><center>...<br>...</center></html>
        String tenBanHienThi = ban.getTenBan();

        // Mẹo nhỏ: Nếu tên quá dài (như bàn ghép), ta cho font nhỏ lại một chút
        String fontSizeStyle = "font-size:11px"; // Mặc định to
        if (tenBanHienThi.length() > 10) {
            fontSizeStyle = "font-size:8px"; // Chữ dài thì nhỏ lại xíu
        }

        // Tạo chuỗi HTML
        String htmlText = "<html><div style='text-align: center; width: 85px; " + fontSizeStyle + "'>"
                + tenBanHienThi + "</div></html>";

        JLabel tenBanLabel = new JLabel(htmlText);
        // tenBanLabel.setFont(...) -> Không cần set font ở đây nữa vì đã set trong HTML
        // Nhưng nếu muốn set font gốc:
        tenBanLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tenBanLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tenBanLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel soGheLabel = new JLabel(ban.getSoGhe() + " ghế");
        soGheLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Giảm size ghế xíu cho gọn
        soGheLabel.setForeground(Color.BLACK);
        soGheLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Canh giữa theo chiều dọc
        infoPanel.add(Box.createVerticalGlue()); // Đẩy xuống
        infoPanel.add(tenBanLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(soGheLabel);
        infoPanel.add(Box.createVerticalGlue()); // Đẩy lên (Kết hợp Glue trên dưới sẽ căn giữa nội dung)

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