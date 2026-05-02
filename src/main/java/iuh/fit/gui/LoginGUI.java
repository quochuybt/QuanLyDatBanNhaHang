package iuh.fit.gui;

import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.entity.TaiKhoan;
import iuh.fit.core.dao.NhanVienRepository;
import iuh.fit.core.service.TaiKhoanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginGUI extends JFrame {

    private static final Color COLOR_ACCENT_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final NhanVienRepository nhanVienRepo = new NhanVienRepository();

    public LoginGUI() {
        setTitle("Đăng nhập - StarGuardian Restaurant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);

        BackgroundPanel backgroundPanel = new BackgroundPanel("/img/DangNhap+Logo/DangNhap.jpg");
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        JPanel loginFormPanel = new JPanel(new BorderLayout());
        loginFormPanel.setPreferredSize(new Dimension(390, 600));
        loginFormPanel.setOpaque(false);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fill(new RoundRectangle2D.Double(5, 5, getWidth() - 10, getHeight() - 10, 25, 25));
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 5, getHeight() - 5, 25, 25));
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        loginFormPanel.add(contentPanel, BorderLayout.CENTER);

        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/img/DangNhap+Logo/LogoDN.png"));
            Image originalImage = originalIcon.getImage();
            int targetWidth = 390 - 40;
            if (originalImage.getWidth(null) > 0) {
                double aspectRatio = (double) originalImage.getHeight(null) / originalImage.getWidth(null);
                int targetHeight = (int) (targetWidth * aspectRatio);
                Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(resizedImage));
                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(logoLabel);
            }
        } catch (Exception e) {
            JLabel fallback = new JLabel("StarGuardian");
            fallback.setFont(new Font("Arial", Font.BOLD, 24));
            fallback.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(fallback);
        }

        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.BLACK);
        contentPanel.add(lblTitle);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel formInputPanel = new JPanel();
        formInputPanel.setOpaque(false);
        formInputPanel.setLayout(new BoxLayout(formInputPanel, BoxLayout.Y_AXIS));
        formInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formInputPanel.add(createInputRow("Tên đăng nhập", txtTenDangNhap = new JTextField(), "Nhập tên đăng nhập"));
        formInputPanel.add(createInputRow("Mật khẩu", txtMatKhau = new JPasswordField(), "Nhập mật khẩu"));
        contentPanel.add(formInputPanel);

        btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDangNhap.setFont(new Font("Arial", Font.BOLD, 16));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setBackground(COLOR_ACCENT_BLUE);
        btnDangNhap.setBorder(new EmptyBorder(12, 0, 15, 0));
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnDangNhap.getPreferredSize().height + 15));

        btnDangNhap.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnDangNhap.setBackground(COLOR_ACCENT_BLUE.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { btnDangNhap.setBackground(COLOR_ACCENT_BLUE); }
        });

        btnDangNhap.addActionListener(e -> handleLogin());

        // Enter key triggers login
        getRootPane().setDefaultButton(btnDangNhap);

        contentPanel.add(btnDangNhap);

        backgroundPanel.add(loginFormPanel, new GridBagConstraints());
    }

    private void handleLogin() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword()).trim();

        if (tenDangNhap.isEmpty() || tenDangNhap.equals("Nhập tên đăng nhập")
                || matKhau.isEmpty() || matKhau.equals("Nhập mật khẩu")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                    "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            TaiKhoan tk = taiKhoanService.login(tenDangNhap, matKhau);

            // Lấy thông tin NhanVien từ TaiKhoan
            NhanVien nv = nhanVienRepo.findByTenTK(tenDangNhap);
            String hoTen = nv != null ? nv.getHoten() : tenDangNhap;
            String vaiTro = nv != null ? nv.getVaiTro().name() : "NHANVIEN";
            String maNV  = nv != null ? nv.getManv() : tenDangNhap;

            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thành công!\nTên: " + hoTen + "\nVai trò: " + vaiTro,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            dispose();

            final String finalVaiTro = vaiTro;
            final String finalHoTen  = hoTen;
            final String finalMaNV   = maNV;
            SwingUtilities.invokeLater(() -> new DashboardGUI(finalVaiTro, finalHoTen, finalMaNV).setVisible(true));

        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg.contains("khóa")) {
                JOptionPane.showMessageDialog(this,
                        "Tài khoản đã bị tạm ngưng!\nVui lòng liên hệ Quản lý để được hỗ trợ.",
                        "Tài khoản bị khóa", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, msg, "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối CSDL!\nChi tiết: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createInputRow(String labelText, JComponent inputComponent, String placeholder) {
        JPanel rowPanel = new JPanel();
        rowPanel.setOpaque(false);
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));
        rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rowPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 5, 0));
        rowPanel.add(label);

        javax.swing.border.Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                new EmptyBorder(5, 10, 5, 10));
        int h = new JTextField().getPreferredSize().height + 12;

        if (inputComponent instanceof JPasswordField pf) {
            JPanel wrapper = new JPanel(new BorderLayout(10, 0));
            wrapper.setOpaque(true);
            wrapper.setBackground(Color.WHITE);
            wrapper.setBorder(border);

            JLabel icon = new JLabel("🔒");
            icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
            wrapper.add(icon, BorderLayout.WEST);

            pf.setFont(new Font("Arial", Font.PLAIN, 14));
            pf.setOpaque(true);
            pf.setBackground(Color.WHITE);
            pf.setBorder(null);
            setupPlaceholder(pf, placeholder);
            wrapper.add(pf, BorderLayout.CENTER);

            wrapper.setMinimumSize(new Dimension(100, h));
            wrapper.setPreferredSize(new Dimension(100, h));
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowPanel.add(wrapper);

        } else if (inputComponent instanceof JTextField tf) {
            tf.setFont(new Font("Arial", Font.PLAIN, 14));
            tf.setForeground(Color.BLACK);
            tf.setBorder(border);
            setupPlaceholder(tf, placeholder);
            tf.setMinimumSize(new Dimension(100, h));
            tf.setPreferredSize(new Dimension(100, h));
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            tf.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowPanel.add(tf);
        }

        return rowPanel;
    }

    private void setupPlaceholder(JTextField tf, String placeholder) {
        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Color.GRAY); }
            }
        });
    }

    private void setupPlaceholder(JPasswordField pf, String placeholder) {
        pf.setText(placeholder);
        pf.setForeground(Color.GRAY);
        pf.setEchoChar((char) 0);
        pf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (new String(pf.getPassword()).equals(placeholder)) {
                    pf.setText(""); pf.setForeground(Color.BLACK); pf.setEchoChar('•');
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (new String(pf.getPassword()).isEmpty()) {
                    pf.setText(placeholder); pf.setForeground(Color.GRAY); pf.setEchoChar((char) 0);
                }
            }
        });
    }

    private static class BackgroundPanel extends JPanel {
        private final Image backgroundImage;
        BackgroundPanel(String imagePath) {
            Image img = null;
            try { img = new ImageIcon(getClass().getResource(imagePath)).getImage(); } catch (Exception ignored) {}
            backgroundImage = img;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage == null) return;
            Graphics2D g2d = (Graphics2D) g.create();
            int pw = getWidth(), ph = getHeight();
            int iw = backgroundImage.getWidth(this), ih = backgroundImage.getHeight(this);
            double scale = Math.max((double) pw / iw, (double) ph / ih);
            int nw = (int)(iw * scale), nh = (int)(ih * scale);
            g2d.drawImage(backgroundImage, (pw - nw) / 2, (ph - nh) / 2, nw, nh, this);
            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}
