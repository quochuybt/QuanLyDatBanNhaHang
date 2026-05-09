package iuh.fit;

import iuh.fit.gui.ServerConnectionGUI;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point cho cấu trúc mới (src/main/java/iuh/fit/*).
 */
public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            setupGlobalUI();

            ServerConnectionGUI connectionGUI = new ServerConnectionGUI();
            connectionGUI.setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void setupGlobalUI() {
        Color inputBorder = new Color(150, 150, 150);
        Color accentBlue = new Color(56, 118, 243);

        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inputBorder),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inputBorder),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inputBorder),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        UIManager.put("Button.background", accentBlue);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 16));
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
    }
}
