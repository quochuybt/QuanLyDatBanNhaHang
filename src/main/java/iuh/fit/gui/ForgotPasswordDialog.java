package iuh.fit.gui;

import dao.MailConfig; // (Lưu ý: Bạn nên chuyển các lớp này sang package utils hoặc service)
import dao.MailService;
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

    // Khai báo Services thay cho DAO
    private final NhanVienService nhanVienService = new NhanVienService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    public ForgotPasswordDialog(JFrame parent) {
        super(parent, "Quên Mật khẩu - Đặt lại", true);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createStep1Panel(), "STEP_1_INPUT");
        cardPanel.add(createStep2Panel(), "STEP_2_OTP");
        cardPanel.add(createStep3Panel(), "STEP_3_RESET");

        add(cardPanel);
        cardLayout.show(cardPanel, "STEP_1_INPUT");
    }

    private boolean sendOTP(String tenTK, String email) {
        generatedOTP = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        otpStorage.put(tenTK, generatedOTP);
        return MailService.send(email, MailConfig.EMAIL_SUBJECT, generatedOTP);
    }

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

            // Đổi giao diện nút trong lúc chờ gửi email
            btnSendOTP.setEnabled(false);
            btnSendOTP.setText("Đang gửi...");

            // Sử dụng SwingWorker để gửi email dưới nền (tránh đơ UI)
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
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Lỗi hệ thống: " + error.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        boolean isSent = get();
                        if (email == null) {
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Tài khoản không tồn tại hoặc chưa liên kết Email.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        } else if (isSent) {
                            if (emailDisplayLabel != null) {
                                emailDisplayLabel.setText("<html><h3>2. Nhập Mã OTP</h3><p>Mã đã gửi tới Email: <b>" + maskEmail(currentEmail) + "</b></p></html>");
                            }
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Mã OTP đã được gửi đến: " + maskEmail(currentEmail) + "\n(Vui lòng kiểm tra mục Thư rác/Spam)", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            cardLayout.show(cardPanel, "STEP_2_OTP");
                        } else {
                            JOptionPane.showMessageDialog(ForgotPasswordDialog.this, "Lỗi khi gửi OTP. Vui lòng kiểm tra mạng.", "Lỗi Gửi Email", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });
        return panel;
    }

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
                JOptionPane.showMessageDialog(this, "Xác nhận OTP thành công! Vui lòng đặt mật khẩu mới.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, "STEP_3_RESET");
            } else {
                JOptionPane.showMessageDialog(this, "Mã OTP không đúng hoặc đã hết hạn. Vui lòng kiểm tra lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

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
                // Đẩy logic Validate Password xuống Service hoặc xử lý tại GUI nếu đơn giản
                if (newPass.length() < 6) {
                    throw new IllegalArgumentException("Mật khẩu phải dài ít nhất 6 ký tự.");
                }

                if (taiKhoanService.updatePassword(currentTenTK, newPass)) {
                    JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Đóng Dialog
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể cập nhật mật khẩu. Vui lòng thử lại.", "Lỗi Cập nhật", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không hợp lệ:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String user = parts[0];
        String domain = parts[1];
        if (user.length() < 3) return email;
        return user.substring(0, 2) + "*****" + user.substring(user.length() - 1) + "@" + domain;
    }
}