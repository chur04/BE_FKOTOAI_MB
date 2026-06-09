package com.g5.fokotoai.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class EmailService {

    JavaMailSender javaMailSender ;



    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🔐 Mã OTP đặt lại mật khẩu - FokotoAI");
            helper.setText(buildOtpEmailHtml(otp), true);

            javaMailSender.send(message);
            log.info("OTP email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 32px; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #333;">Đặt lại mật khẩu</h2>
                <p style="color: #555;">Mã OTP của bạn là:</p>
                <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                            color: #4F46E5; background: #F3F4F6;
                            padding: 16px; border-radius: 8px; text-align: center;">
                    %s
                </div>
                <p style="color: #888; margin-top: 16px;">
                    ⏱ Mã có hiệu lực trong <strong>10 phút</strong>.<br/>
                    🔒 Không chia sẻ mã này với bất kỳ ai.
                </p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                <p style="color: #aaa; font-size: 12px;">
                    Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                </p>
            </div>
            """.formatted(otp);
    }
}
