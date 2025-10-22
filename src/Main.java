import gui.TaiKhoanGUI;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        // Chạy tất cả giao diện trên Luồng Xử lý Sự kiện (EDT) của Swing
        SwingUtilities.invokeLater(() -> {

            // --- 1. Cài đặt Look and Feel ---
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- 2. Tùy chỉnh giao diện (Lấy từ hàm main test của bạn) ---
            Color COLOR_INPUT_BORDER = new Color(150, 150, 150);
            Color COLOR_ACCENT_BLUE = new Color(56, 118, 243);

            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER), // Viền xám
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER), // Viền xám
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_INPUT_BORDER), // Viền xám
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            UIManager.put("Button.background", COLOR_ACCENT_BLUE);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
            UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));

            // --- 3. Khởi tạo và hiển thị cửa sổ Đăng nhập (TaiKhoanGUI) ---
            TaiKhoanGUI loginWindow = new TaiKhoanGUI(); // Sửa ở đây
            loginWindow.setVisible(true);

        });
    }
}