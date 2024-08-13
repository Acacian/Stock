package stock.authentication.service;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

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

import stock.authentication.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.authentication.exception.GlobalExceptionHandler.InvalidTokenException;
import stock.authentication.model.User;
import stock.authentication.repository.UserRepository;
import stock.authentication.security.JwtTokenProvider;
import stock.authentication.kafka.UserEvent;

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
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    // 회원가입 및 이메일 인증 처리
    public User registerUser(User user) {
        logger.info("새로운 사용자 등록: {}", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("이메일이 이미 사용 중입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        String token = generateVerificationToken();
        redisTemplate.opsForValue().set("verification:" + token, savedUser.getEmail(), 24, TimeUnit.HOURS);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);
        
        return savedUser;
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    // 이메일 인증 처리
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

    // 인증 처리
    public String authenticateUser(String email, String password) {
        logger.info("사용자 인증: {}", email);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = tokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        kafkaTemplate.send("user-events", new UserEvent("USER_AUTHENTICATED", user.getId(), user.getEmail()));
        logger.info("사용자 인증 성공: {}", email);
        return token;
    }

    // 로그아웃 처리
    public void logout(String token) {
        logger.info("사용자 로그아웃");
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, "blacklisted", 24, TimeUnit.HOURS);
        logger.info("토큰 블랙리스트 등록: {}", token);
    }

    // 비밀번호 업데이트
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
        kafkaTemplate.send("user-events", new UserEvent("PASSWORD_UPDATED", userId, null));
        logger.info("사용자 ID: {}의 비밀번호 업데이트 및 모든 장치에서 로그아웃 처리 완료", userId);
    }
}
