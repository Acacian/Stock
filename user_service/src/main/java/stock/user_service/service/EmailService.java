package stock.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.domain}")
    private String appDomain;

    public void sendVerificationEmail(String to, String token) {
        logger.info("Preparing to send verification email to: {}", to);
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify your email");
            
            String verificationUrl = "https://" + appDomain + ":3001/api/auth/verify?token=" + token;
            String htmlContent = String.format(
                "<html><body><p>Please click <a href='%s'>here</a> to verify your email.</p></body></html>",
                verificationUrl
            );
            
            helper.setText(htmlContent, true);
            
            logger.info("Attempting to send email from: {} to: {}", fromEmail, to);
            emailSender.send(message);
            logger.info("Verification email successfully sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}