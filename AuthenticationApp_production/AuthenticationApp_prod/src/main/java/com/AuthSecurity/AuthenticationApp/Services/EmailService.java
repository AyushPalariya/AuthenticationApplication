package com.AuthSecurity.AuthenticationApp.Services;

import com.AuthSecurity.AuthenticationApp.Entities.Users;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void emailSend(Users user) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to AuthApp — Account Created");
            helper.setText(buildHtmlEmail(user), true);
            javaMailSender.send(message);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildHtmlEmail(Users user) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <h2 style="color:#2563EB">Welcome, %s!</h2>
              <p>Your account has been successfully created.</p>
              <table style="border-collapse:collapse;width:100%%">
                <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Email</strong></td>
                    <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
                <tr><td style="padding:8px;border:1px solid #e5e7eb"><strong>Username</strong></td>
                    <td style="padding:8px;border:1px solid #e5e7eb">%s</td></tr>
              </table>
              <p style="color:#6b7280;font-size:12px;margin-top:24px">
                If you did not create this account, please contact support immediately.
              </p>
            </body></html>
            """.formatted(user.getName(), user.getEmail(), user.getUsername());
    }
}
