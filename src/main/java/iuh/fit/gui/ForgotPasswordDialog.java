package iuh.fit.gui;

import iuh.fit.core.util.MailConfig;
import iuh.fit.core.util.MailService;
import iuh.fit.core.service.NhanVienService;
import iuh.fit.core.service.TaiKhoanService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ForgotPasswordDialog extends JDialog {

    private String currentTenTK = "";
    private String currentEmail = "";
    private String generatedOTP = "";

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JTextField txtTK_Email;
    private JTextField txtOTP;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    private JLabel emailDisplayLabel;

    private static final Map<String, String> otpStorage = new HashMap<>();

    // Sử dụng Service thay cho DAO (kiến trúc JPA mới)
    private final NhanVienService nhanVienService = new NhanVienService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Quên Mật khẩu - Đặt lại", true);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel step1 = createStep1Panel();
        JPanel step2 = createStep2Panel();
        JPanel step3 = createStep3Panel();

        cardPanel.add(step1, "STEP_1_INPUT");
        cardPanel.add(step2, "STEP_2_OTP");
        cardPanel.add(step3, "STEP_3_RESET");

        add(cardPanel);
        cardLayout.show(cardPanel, "STEP_1_INPUT");
    }

    private boolean sendOTP(String tenTK, String email) {
        generatedOTP = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        otpStorage.put(tenTK, generatedOTP);
        return MailService.send(email, MailConfig.EMAIL_SUBJECT, generatedOTP);
    }

    // ==================== BƯỚC 1: NHẬP TÊN ĐĂNG NHẬP ====================
    private JPanel createStep1Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        txtTK_Email = new JTextField(20);
        JButton btnSendOTP = new JButton("Gửi Mã OTP");
        btnSendOTP.setBackground(new Color(40, 28, 244));
        btnSendOTP.setForeground(Color.WHITE);
        btnSendOTP.setPreferredSize(new Dimension(100, 40));
        btnSendOTP.setFocusPainted(false);
        btnSendOTP.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        centerPanel.add(new JLabel("Tên đăng nhập:"));
        centerPanel.add(txtTK_Email);
        centerPanel.add(Box.createVerticalStrut(1));
        centerPanel.add(btnSendOTP);

        panel.add(new JLabel("<html><h3>1. Xác thực Tài khoản</h3></html>"), BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        btnSendOTP.addActionListener(e -> {
            String tenTK = txtTK_Email.getText().trim();
            if (tenTK.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên đăng nhập.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Đổi giao diện nút trong lúc chờ gửi email (tránh đơ UI)
            btnSendOTP.setEnabled(false);
            btnSendOTP.setText("Đang gửi...");

            new SwingWorker<Boolean, Void>() {
                private String email;
                private Exception error;

                @Override
                protected Boolean doInBackground() {
                    try {
                        email = nhanVienService.getEmailByTenTK(tenTK);
                        if (email != null) {
                            currentTenTK = tenTK;
                            currentEmail = email;
                            return sendOTP(currentTenTK, currentEmail);
                        }
                    } catch (Exception ex) {
                        error = ex;
                    }
                    return false;
                }

                @Override
                protected void done() {
                    btnSendOTP.setEnabled(true);
                    btnSendOTP.setText("Gửi Mã OTP");

                    if (error != null) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Lỗi kết nối CSDL: " + error.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        boolean isSent = get();
                        if (email == null) {
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                    "Tài khoản không tồn tại hoặc chưa liên kết Email.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        } else if (isSent) {
                            if (emailDisplayLabel != null) {
                                emailDisplayLabel.setText("<html><h3>2. Nhập Mã OTP</h3><p>Mã đã gửi tới Email: <b>"
                                        + maskEmail(currentEmail) + "</b></p></html>");
                            }
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                    "Mã OTP đã được gửi đến: " + maskEmail(currentEmail)
                                            + "\n(Vui lòng kiểm tra mục Thư rác/Spam)",
                                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            cardLayout.show(cardPanel, "STEP_2_OTP");
                        } else {
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                    "Lỗi khi gửi OTP. Vui lòng kiểm tra cấu hình email và kết nối mạng.",
                                    "Lỗi Gửi Email", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });
        return panel;
    }

    // ==================== BƯỚC 2: NHẬP OTP ====================
    private JPanel createStep2Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        txtOTP = new JTextField(10);
        JButton btnVerifyOTP = new JButton("Xác nhận OTP");
        btnVerifyOTP.setBackground(new Color(40, 28, 244));
        btnVerifyOTP.setForeground(Color.WHITE);
        btnVerifyOTP.setPreferredSize(new Dimension(100, 40));
        btnVerifyOTP.setFocusPainted(false);
        btnVerifyOTP.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        centerPanel.add(new JLabel("Mã OTP:"));
        centerPanel.add(txtOTP);
        centerPanel.add(Box.createVerticalStrut(1));
        centerPanel.add(btnVerifyOTP);

        emailDisplayLabel = new JLabel("<html><h3>2. Nhập Mã OTP</h3><p>Mã đã gửi tới Email: <b>[Email Placeholder]</b></p></html>");

        panel.add(emailDisplayLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        btnVerifyOTP.addActionListener(e -> {
            String inputOTP = txtOTP.getText().trim();
            String storedOTP = otpStorage.get(currentTenTK);

            if (storedOTP != null && inputOTP.equals(storedOTP)) {
                otpStorage.remove(currentTenTK); // Xóa OTP sau khi dùng
                JOptionPane.showMessageDialog(this,
                        "Xác nhận OTP thành công! Vui lòng đặt mật khẩu mới.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, "STEP_3_RESET");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Mã OTP không đúng hoặc đã hết hạn. Vui lòng kiểm tra lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    // ==================== BƯỚC 3: ĐẶT MẬT KHẨU MỚI ====================
    private JPanel createStep3Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        txtNewPassword = new JPasswordField(20);
        txtConfirmPassword = new JPasswordField(20);
        JButton btnResetPass = new JButton("Đổi Mật khẩu");
        btnResetPass.setBackground(new Color(40, 28, 244));
        btnResetPass.setForeground(Color.WHITE);
        btnResetPass.setPreferredSize(new Dimension(100, 40));
        btnResetPass.setFocusPainted(false);
        btnResetPass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        panel.add(new JLabel("<html><h3>3. Đặt Mật khẩu Mới</h3></html>"), gbc);
        panel.add(Box.createVerticalStrut(10), gbc);

        panel.add(new JLabel("Mật khẩu mới:"), gbc);
        panel.add(txtNewPassword, gbc);

        panel.add(new JLabel("Nhập lại Mật khẩu:"), gbc);
        panel.add(txtConfirmPassword, gbc);

        panel.add(Box.createVerticalStrut(15), gbc);
        panel.add(btnResetPass, gbc);

        btnResetPass.addActionListener(e -> {
            String newPass = new String(txtNewPassword.getPassword()).trim();
            String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mật khẩu mới.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới và Nhập lại Mật khẩu không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (newPass.length() < 6) {
                    throw new IllegalArgumentException("Mật khẩu phải dài ít nhất 6 ký tự.");
                }

                if (taiKhoanService.updatePassword(currentTenTK, newPass)) {
                    JOptionPane.showMessageDialog(this,
                            "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Không thể cập nhật mật khẩu. Vui lòng thử lại.", "Lỗi Cập nhật", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Mật khẩu không hợp lệ:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    // ==================== TIỆN ÍCH ====================
    private String maskEmail(String email) {
        if (email == null || email.indexOf('@') == -1) return email;
        String[] parts = email.split("@");
        String user = parts[0];
        String domain = parts[1];
        if (user.length() < 3) return email;
        return user.substring(0, 2) + "*****" + user.substring(user.length() - 1) + "@" + domain;
    }
}