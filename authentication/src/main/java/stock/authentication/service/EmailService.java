package stock.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @KafkaListener(topics = "email-topic", groupId = "email-group")
    public void listenEmailTopic(String message) {
        String[] parts = message.split(",");
        if (parts.length == 2) {
            String type = parts[0];
            String email = parts[1];
            if ("VERIFICATION".equals(type)) {
                sendVerificationEmail(email);
            }
        }
    }

    public void sendVerificationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your email");
        message.setText("Please click the link to verify your email: http://yourdomain.com/verify?email=" + toEmail);
        
        emailSender.send(message);
        logger.info("Verification email sent to: {}", toEmail);
    }
}