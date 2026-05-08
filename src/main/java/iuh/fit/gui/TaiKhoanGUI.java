package iuh.fit.gui;

import iuh.fit.core.entity.TaiKhoan;
import iuh.fit.core.entity.NhanVien;
import iuh.fit.core.service.TaiKhoanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class TaiKhoanGUI extends JFrame {
    private static final Color COLOR_ACCENT_BLUE = new Color(40, 28, 244);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    public TaiKhoanGUI() {
        setTitle("Đăng nhập - StarGuardian Restaurant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        loadAppIcon();

        BackgroundPanel backgroundPanel = new BackgroundPanel("/img/DangNhap+Logo/DangNhap.jpg");
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        JPanel loginFormPanel = new JPanel(new BorderLayout());
        loginFormPanel.setPreferredSize(new Dimension(390, 600));
        loginFormPanel.setMaximumSize(new Dimension(390, 600));
        loginFormPanel.setOpaque(false);

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fill(new RoundRectangle2D.Double(5, 5, getWidth() - 10, getHeight() - 10, 25, 25));
                // Dùng màu trắng mờ
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
                ImageIcon resizedIcon = new ImageIcon(resizedImage);
                JLabel logoLabel = new JLabel(resizedIcon);

                logoLabel.setOpaque(false);

                logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(logoLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel lblLoginTitle = new JLabel("Đăng nhập");
        lblLoginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLoginTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblLoginTitle.setForeground(Color.BLACK);
        contentPanel.add(lblLoginTitle);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel formInputPanel = new JPanel();
        formInputPanel.setOpaque(false);
        formInputPanel.setLayout(new BoxLayout(formInputPanel, BoxLayout.Y_AXIS));

        formInputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formInputPanel.add(createInputRow(
                "Tên đăng nhập",
                txtTenDangNhap = new JTextField(),
                "Nhập tên đăng nhập",
                "\uE77B" // icon user của Segoe MDL2 Assets
        ));

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
            @Override
            public void mouseEntered(MouseEvent e) {
                btnDangNhap.setBackground(COLOR_ACCENT_BLUE.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnDangNhap.setBackground(COLOR_ACCENT_BLUE);
            }
        });

        btnDangNhap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tenDangNhap = txtTenDangNhap.getText().trim();
                String matKhau = new String(txtMatKhau.getPassword()).trim();

                if (tenDangNhap.isEmpty() || matKhau.isEmpty() || tenDangNhap.equals("Nhập tên đăng nhập") || matKhau.equals("Nhập mật khẩu")) {
                    JOptionPane.showMessageDialog(TaiKhoanGUI.this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                            "Lỗi Đăng nhập", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    TaiKhoan tk = taiKhoanService.login(tenDangNhap, matKhau);
                    NhanVien nv = tk.getNhanVien();

                    String userRole = nv != null ? nv.getVaiTro().name() : "NHANVIEN";
                    String userName = nv != null ? nv.getHoten() : tenDangNhap;
                    String maNV = nv != null ? nv.getManv() : tenDangNhap;

                    JOptionPane.showMessageDialog(TaiKhoanGUI.this,
                            "Đăng nhập thành công!\nTên: " + userName + "\nVai trò: " + userRole,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);

                    dispose();

                    final String finalUserRole = userRole;
                    final String finalUserName = userName;
                    final String finalMaNV = maNV;

                    SwingUtilities.invokeLater(() -> {
                        MainGUI mainGUI = new MainGUI(finalUserRole, finalUserName, finalMaNV);
                        mainGUI.setVisible(true);
                    });

                } catch (IllegalArgumentException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("bị khóa")) {
                        JOptionPane.showMessageDialog(TaiKhoanGUI.this,
                                "**Tài khoản đã bị tạm ngưng hoạt động!**\nVui lòng liên hệ Quản lý để được hỗ trợ kích hoạt lại.",
                                "Tài khoản bị Khóa", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TaiKhoanGUI.this, "Sai tên tài khoản hoặc mật khẩu!",
                                "Lỗi Đăng nhập", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(TaiKhoanGUI.this,
                            "Lỗi kết nối CSDL! Vui lòng kiểm tra lại.\nChi tiết: " + ex.getMessage(),
                            "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        contentPanel.add(btnDangNhap);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linkPanel.setOpaque(false);

        JLabel lblQuenMatKhau = new JLabel("Quên mật khẩu?");
        lblQuenMatKhau.setForeground(COLOR_ACCENT_BLUE);
        lblQuenMatKhau.setFont(new Font("Arial", Font.PLAIN, 12));
        lblQuenMatKhau.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblQuenMatKhau.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ForgotPasswordDialog(TaiKhoanGUI.this).setVisible(true);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblQuenMatKhau.setText("<html><u>Quên mật khẩu?</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblQuenMatKhau.setText("Quên mật khẩu?");
            }
        });

        linkPanel.add(lblQuenMatKhau);
        linkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, linkPanel.getPreferredSize().height));
        contentPanel.add(linkPanel);

        backgroundPanel.add(loginFormPanel, new GridBagConstraints());
    }

    private void loadAppIcon() {
        try {
            URL url = getClass().getResource("/img/DangNhap+Logo/Logo.jpg");
            if (url != null) {
                this.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createInputRow(String labelText, JComponent inputComponent, String placeholder) {
        return createInputRow(labelText, inputComponent, placeholder, null);
    }

    private JPanel createInputRow(String labelText, JComponent inputComponent, String placeholder, String iconText) {

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

        javax.swing.border.Border standardBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                new EmptyBorder(5, 10, 5, 10)
        );

        int standardHeight = new JTextField().getPreferredSize().height + 12;

        if (inputComponent instanceof JPasswordField) {
            JPanel fakeFieldPanel = new JPanel(new BorderLayout(10, 0));
            fakeFieldPanel.setOpaque(true);
            fakeFieldPanel.setBackground(Color.WHITE);
            fakeFieldPanel.setBorder(standardBorder);

            JLabel iconLabel = new JLabel("🔒");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            iconLabel.setForeground(Color.BLACK);
            fakeFieldPanel.add(iconLabel, BorderLayout.WEST);

            JPasswordField pf = (JPasswordField) inputComponent;
            pf.setFont(new Font("Arial", Font.PLAIN, 14));
            pf.setForeground(Color.BLACK);
            pf.setOpaque(true);
            pf.setBackground(Color.WHITE);
            pf.setBorder(null);
            setupPlaceholder(pf, placeholder);
            fakeFieldPanel.add(pf, BorderLayout.CENTER);

            fakeFieldPanel.setMinimumSize(new Dimension(100, standardHeight));
            fakeFieldPanel.setPreferredSize(new Dimension(100, standardHeight));
            fakeFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, standardHeight));
            fakeFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            rowPanel.add(fakeFieldPanel);

        } else if (inputComponent instanceof JComboBox) {

            JComboBox<?> cb = (JComboBox<?>) inputComponent;
            cb.setFont(new Font("Arial", Font.PLAIN, 14));

            cb.setMinimumSize(new Dimension(100, standardHeight));
            cb.setPreferredSize(new Dimension(100, standardHeight));
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, standardHeight));
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);

            rowPanel.add(cb);

        } else if (inputComponent instanceof JTextField) {

            JPanel fakeFieldPanel = new JPanel(new BorderLayout(10, 0));
            fakeFieldPanel.setOpaque(true);
            fakeFieldPanel.setBackground(Color.WHITE);
            fakeFieldPanel.setBorder(standardBorder);

            if (iconText != null && !iconText.trim().isEmpty()) {
                JLabel iconLabel = new JLabel(iconText);
                iconLabel.setFont(new Font("Segoe MDL2 Assets", Font.PLAIN, 16));
                iconLabel.setForeground(Color.BLACK);
                fakeFieldPanel.add(iconLabel, BorderLayout.WEST);
            }

            JTextField tf = (JTextField) inputComponent;
            tf.setFont(new Font("Arial", Font.PLAIN, 14));
            tf.setForeground(Color.BLACK);
            tf.setOpaque(true);
            tf.setBackground(Color.WHITE);
            tf.setBorder(null);

            setupPlaceholder(tf, placeholder);
            fakeFieldPanel.add(tf, BorderLayout.CENTER);

            fakeFieldPanel.setMinimumSize(new Dimension(100, standardHeight));
            fakeFieldPanel.setPreferredSize(new Dimension(100, standardHeight));
            fakeFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, standardHeight));
            fakeFieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            rowPanel.add(fakeFieldPanel);
        }

        return rowPanel;
    }
    private void setupPlaceholder(JTextField tf, String placeholder) {
        if (placeholder == null) return;

        tf.setText(placeholder);
        tf.setForeground(Color.GRAY);
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void setupPlaceholder(JPasswordField pf, String placeholder) {
        if (placeholder == null) return;

        pf.setText(placeholder);
        pf.setForeground(Color.GRAY);
        pf.setEchoChar((char)0);

        pf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(pf.getPassword()).equals(placeholder)) {
                    pf.setText("");
                    pf.setForeground(Color.BLACK);
                    pf.setEchoChar('•');
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (new String(pf.getPassword()).isEmpty()) {
                    pf.setText(placeholder);
                    pf.setForeground(Color.GRAY);
                    pf.setEchoChar((char)0);
                }
            }
        });
    }

    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                int imgWidth = backgroundImage.getWidth(this);
                int imgHeight = backgroundImage.getHeight(this);
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.max(scaleX, scaleY);
                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);
                int x = (panelWidth - newWidth) / 2;
                int y = (panelHeight - newHeight) / 2;
                g2d.drawImage(backgroundImage, x, y, newWidth, newHeight, this);
                g2d.dispose();
            }
        }
    }
}
