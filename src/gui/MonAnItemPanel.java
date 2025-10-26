package gui;

import entity.MonAn;
import javax.swing.*;
import javax.swing.border.EmptyBorder; // Thêm import này
import java.awt.*;
import java.text.NumberFormat; // Thêm
import java.util.Locale;      // Thêm

public class MonAnItemPanel extends JPanel {

    private MonAn monAn;
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Format tiền

    // Màu sắc
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color HOVER_COLOR = new Color(230, 245, 255);
    // Bạn có thể định nghĩa màu SELECTED_COLOR nếu cần

    public MonAnItemPanel(MonAn monAn) {
        this.monAn = monAn;
        buildUI();
        addMouseHoverEffect();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 5)); // Khoảng cách giữa ảnh và text
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), // Viền ngoài
                new EmptyBorder(10, 10, 10, 10) // Padding bên trong
        ));
        // Đặt kích thước cố định cho mỗi ô
        setPreferredSize(new Dimension(140, 180)); // Tăng chiều cao 1 chút cho giá tiền

        // 1. Hình ảnh món ăn (Placeholder)
        // TODO: Tải hình ảnh thật từ monAn.getHinhAnh()
        // Đảm bảo bạn có file ảnh placeholder này
        ImageIcon icon = null;
        try {
            // Thử tải ảnh thật
            if (monAn.getHinhAnh() != null && !monAn.getHinhAnh().isEmpty()) {
                // Sửa đường dẫn: Bắt đầu bằng /img/monan/
                String imagePath = "/img/MonAn/" + monAn.getHinhAnh();
                icon = new ImageIcon(getClass().getResource(imagePath));

                if (icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    System.err.println("Không tìm thấy ảnh món ăn: " + imagePath);
                    icon = null; // Dùng placeholder nếu lỗi
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh món ăn: " + monAn.getHinhAnh() + " - " + e.getMessage());
            icon = null;
        }

        // Nếu không có ảnh hoặc tải lỗi, dùng placeholder
        if (icon == null) {
            try {
                // Sửa đường dẫn placeholder: Bắt đầu bằng /img/placeholder/
                String placeholderPath = "/img/placeholder/mon_an.png";
                icon = new ImageIcon(getClass().getResource(placeholderPath));

                if (icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                    System.err.println("Không tìm thấy ảnh placeholder: " + placeholderPath);
                    icon = null;
                }
            } catch (Exception e) {
                System.err.println("Lỗi tải ảnh placeholder! " + e.getMessage());
                icon = null; // Đặt null nếu placeholder cũng lỗi
            }
        }

        JLabel lblHinhAnh;
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(120, 100, Image.SCALE_SMOOTH); // Kích thước ảnh
            lblHinhAnh = new JLabel(new ImageIcon(img));
        } else {
            lblHinhAnh = new JLabel("Ảnh lỗi"); // Hoặc để trống
            lblHinhAnh.setPreferredSize(new Dimension(120, 100));
            lblHinhAnh.setOpaque(true);
            lblHinhAnh.setBackground(Color.LIGHT_GRAY);
        }

        lblHinhAnh.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblHinhAnh, BorderLayout.CENTER); // Ảnh ở giữa

        // 2. Panel chứa Tên món và Giá tiền (Xếp dọc)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        infoPanel.setOpaque(false); // Nền trong suốt

        // 2.1 Tên món ăn
        JLabel lblTenMon = new JLabel(monAn.getTenMon());
        lblTenMon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTenMon.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa theo chiều ngang

        // 2.2 Giá tiền
        JLabel lblGiaTien = new JLabel(nf.format(monAn.getDonGia()));
        lblGiaTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaTien.setForeground(Color.RED); // Màu đỏ cho giá
        lblGiaTien.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa

        infoPanel.add(lblTenMon);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3))); // Khoảng cách nhỏ
        infoPanel.add(lblGiaTien);

        add(infoPanel, BorderLayout.SOUTH); // Panel thông tin ở dưới
    }

    // Thêm hiệu ứng khi rê chuột
    private void addMouseHoverEffect() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(HOVER_COLOR);
                // Làm cho infoPanel cũng đổi màu nền theo
                Component[] components = getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) { // Tìm infoPanel
                        component.setBackground(HOVER_COLOR);
                    }
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(Color.WHITE);
                Component[] components = getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        // Ép kiểu sang JPanel để gọi các hàm của JPanel
                        JPanel innerPanel = (JPanel) component;
                        innerPanel.setBackground(Color.WHITE);
                        innerPanel.setOpaque(false); // <-- Giờ sẽ hoạt động
                    }
                }
            }
            // Sự kiện click sẽ được thêm trong ManHinhGoiMonGUI
        });
    }

    // Hàm để ManHinhGoiMonGUI lấy món ăn khi click
    public MonAn getMonAn() {
        return monAn;
    }
}