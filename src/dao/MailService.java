package dao;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {

    public static boolean send(String toEmail, String subject, String content) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MailConfig.SMTP_HOST);
        props.put("mail.smtp.port", MailConfig.SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            // ⭐️ Sử dụng Jakarta.mail.PasswordAuthentication
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.EMAIL_USERNAME, MailConfig.EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MailConfig.EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            String htmlContent = "<html>"
                    + "<body>"
                    + "<h3>Yêu cầu đặt lại mật khẩu</h3>"
                    + "<p>Mã xác minh (OTP) của bạn là:</p>"
                    + "<h1 style='color: #4CAF50; background-color: #f0f0f0; padding: 10px; border-radius: 5px; display: inline-block;'>"
                    + content + "</h1>"
                    + "<p>Vui lòng nhập mã này vào ứng dụng để đặt lại mật khẩu. Mã chỉ có hiệu lực trong vài phút.</p>"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                    + "</body>"
                    + "</html>";

            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}