package stock.authentication.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import stock.authentication.model.User;
import stock.authentication.repository.UserRepository;
import stock.authentication.security.JwtTokenProvider;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public User registerUser(User user) {
        logger.info("Registering new user: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Email {} is already in use", user.getEmail());
            throw new RuntimeException("Email is already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Send verification email
        kafkaTemplate.send("email-topic", "VERIFICATION", user.getEmail());
        logger.info("Verification email sent to: {}", user.getEmail());

        return savedUser;
    }

    public String authenticateUser(String email, String password) {
        logger.info("Authenticating user: {}", email);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = tokenProvider.generateToken(authentication);
        logger.info("User authenticated successfully: {}", email);
        return token;
    }

    public void logout(String token) {
        logger.info("Logging out user");
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, "blacklisted", 24, TimeUnit.HOURS);
        logger.info("Token blacklisted: {}", token);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("Updating password for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Old password is incorrect for user ID: {}", userId);
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Logout from all devices
        String userTokenKey = "user_tokens:" + userId;
        redisTemplate.delete(userTokenKey);
        logger.info("Password updated and logged out from all devices for user ID: {}", userId);
    }
}