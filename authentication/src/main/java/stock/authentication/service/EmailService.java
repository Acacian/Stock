package stock.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${sending.email}")
    private String fromEmail;

    @Value("${app.domain}")
    private String appDomain;

    public void sendVerificationEmail(String toEmail, String token) {
        logger.info("Preparing to send verification email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify your email");
            String verificationUrl = "http://" + appDomain + "/api/auth/verify?token=" + token;
            message.setText("Please click the link to verify your email: " + verificationUrl);
            
            logger.info("Attempting to send email from: {} to: {}", fromEmail, toEmail);
            emailSender.send(message);
            logger.info("Verification email successfully sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
}