package stock.user_service.service;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import stock.user_service.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.user_service.exception.GlobalExceptionHandler.InvalidTokenException;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.security.JwtTokenProvider;
import stock.user_service.kafka.AuthEvent;
import stock.user_service.security.UserPrincipal;
import stock.user_service.dto.UpdateProfileRequest;
import java.time.LocalDateTime;

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
    private KafkaTemplate<String, AuthEvent> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUser(id);
        user.setName(request.getName());
        user.setProfileImage(request.getProfileImage());
        user.setIntroduction(request.getIntroduction());
        User updatedUser = userRepository.save(user);
        
        kafkaTemplate.send("user-events", new AuthEvent("PROFILE_UPDATED", id, user.getEmail()));
        return updatedUser;
    }

    public void processAuthenticatedUser(Long userId) {
        logger.info("Processing authenticated user with ID: {}", userId);
        User user = getUser(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Updated last login time for user ID: {}", userId);
    }

    public User registerUser(User user) {
        logger.info("새로운 사용자 등록: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("이메일이 이미 사용 중입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        if ("test".equals(activeProfile)) {
            // 테스트 환경에서는 이메일 검증 패스
            // savedUser.setEnabled(true);
            // userRepository.save(savedUser);
        } else {
            String token = generateVerificationToken();
            redisTemplate.opsForValue().set("verification:" + token, savedUser.getEmail(), 24, TimeUnit.HOURS);
            emailService.sendVerificationEmail(savedUser.getEmail(), token);
        }
        
        return savedUser;
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public void verifyUser(String token) {
        String email = redisTemplate.opsForValue().get("verification:" + token);
        if (email == null) {
            throw new InvalidTokenException("유효하지 않거나 만료된 인증 토큰입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("사용자를 찾을 수 없습니다."));
        user.setEnabled(true);
        userRepository.save(user);

        redisTemplate.delete("verification:" + token);
    }

    public String authenticateUser(String email, String password) {
        try {
            Authentication user_service = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            String token = tokenProvider.generateToken(user_service);
            UserPrincipal userPrincipal = (UserPrincipal) user_service.getPrincipal();
            User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            kafkaTemplate.send("user-events", new AuthEvent("USER_AUTHENTICATED", user.getId()));
            logger.info("사용자 인증 성공: {}", email);
            return token;
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {}", email, e);
            throw e;
        }
    }

    public void logout(String token) {
        logger.info("사용자 로그아웃");
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, "blacklisted", 24, TimeUnit.HOURS);
        logger.info("토큰 블랙리스트 등록: {}", token);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("사용자 ID: {}의 비밀번호 업데이트", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("사용자 ID: {}의 기존 비밀번호가 올바르지 않습니다.", userId);
            throw new RuntimeException("기존 비밀번호가 올바르지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String userTokenKey = "user_tokens:" + userId;
        redisTemplate.delete(userTokenKey);
        kafkaTemplate.send("user-events", new AuthEvent("PASSWORD_UPDATED", user.getId()));
        logger.info("사용자 ID: {}의 비밀번호 업데이트 및 모든 장치에서 로그아웃 처리 완료", userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}