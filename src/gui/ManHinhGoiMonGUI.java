package gui;

import javax.swing.*;
import java.awt.*;

public class ManHinhGoiMonGUI extends JPanel {
    public ManHinhGoiMonGUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(244, 247, 252));
        JLabel label = new JLabel("Đây là Giao diện Gọi Món");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(label);
    }
}