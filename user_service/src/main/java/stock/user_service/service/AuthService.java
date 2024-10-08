package stock.user_service.service;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import stock.user_service.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.user_service.exception.GlobalExceptionHandler.InvalidTokenException;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.security.JwtTokenProvider;
import stock.user_service.kafka.AuthEvent;
import stock.user_service.security.UserPrincipal;
import stock.user_service.dto.JwtAuthenticationResponse;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.client.NewsfeedServiceClient;
import java.time.LocalDateTime;
import java.util.Set;

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
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NewsfeedServiceClient newsfeedServiceClient;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Value("${app.jwt.refreshTokenExpirationInMs}")
    private int refreshTokenExpirationInMs;

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
        
        newsfeedServiceClient.profileUpdated(new AuthEvent("PROFILE_UPDATED", id, user.getEmail()));
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

        if (!"test".equals(activeProfile)) {
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

    public JwtAuthenticationResponse authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
    
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
    
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                if (!user.isEnabled()) {
                    throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
                }
            
            saveRefreshToken(user.getEmail(), refreshToken);
            newsfeedServiceClient.userAuthenticated(new AuthEvent("USER_AUTHENTICATED", user.getId()));
            logger.info("사용자 인증 성공: {}", email);
            return new JwtAuthenticationResponse(accessToken, refreshToken);
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {}", email, e);
            throw new BadCredentialsException("Invalid email or password");
        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", email, e);
            throw new UsernameNotFoundException("User not found");
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", email, e);
            throw new RuntimeException("An unexpected error occurred during authentication");
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

        // 사용자의 모든 토큰을 무효화
        String userTokenKey = "user_tokens:" + userId;
        Set<String> userTokens = redisTemplate.opsForSet().members(userTokenKey);
        if (userTokens != null) {
            for (String token : userTokens) {
                redisTemplate.opsForValue().set("token:" + token, "blacklisted", 24, TimeUnit.HOURS);
            }
        }
        redisTemplate.delete(userTokenKey); // 기존 토큰 목록 삭제

        newsfeedServiceClient.passwordUpdated(new AuthEvent("PASSWORD_UPDATED", user.getId()));
        logger.info("사용자 ID: {}의 비밀번호 업데이트 및 모든 장치에서 로그아웃 처리 완료", userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        logger.info("Attempting to refresh token");
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(refreshToken);
            logger.info("Token validated, username: {}", username);
            String storedRefreshToken = getRefreshToken(username);
            if (storedRefreshToken == null) {
                logger.warn("No stored refresh token found for user: {}", username);
                throw new InvalidTokenException("No stored refresh token found");
            }
            if (refreshToken.equals(storedRefreshToken)) {
                logger.info("Stored token matches provided token for user: {}", username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                Authentication authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                String newAccessToken = tokenProvider.generateAccessToken(authentication);
                String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
                
                saveRefreshToken(username, newRefreshToken);
                logger.info("New tokens generated for user: {}", username);
                return new JwtAuthenticationResponse(newAccessToken, newRefreshToken);
            } else {
                logger.warn("Stored token does not match provided token for user: {}", username);
            }
        } else {
            logger.warn("Invalid refresh token provided");
        }
        throw new InvalidTokenException("Invalid refresh token");
    }

    public void saveRefreshToken(String username, String refreshToken) {
        logger.info("Saving refresh token for user: {}", username);
        redisTemplate.opsForValue().set(
            "refresh_token:" + username,
            refreshToken,
            refreshTokenExpirationInMs,
            TimeUnit.MILLISECONDS
        );
        logger.info("Refresh token saved successfully for user: {}", username);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get("refresh_token:" + username);
    }

    @Cacheable(value = "userProfile", key = "#id")
    public User getUserProfile(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @CachePut(value = "userProfile", key = "#id")
    public User updateUserProfile(Long id, UpdateProfileRequest request) {
        User user = getUserProfile(id);
        user.setName(request.getName());
        user.setProfileImage(request.getProfileImage());
        user.setIntroduction(request.getIntroduction());
        User updatedUser = userRepository.save(user);
        
        newsfeedServiceClient.profileUpdated(new AuthEvent("PROFILE_UPDATED", id, user.getEmail()));
        return updatedUser;
    }

    @CacheEvict(value = "userProfile", key = "#id")
    public void deleteUserProfile(Long id) {
        userRepository.deleteById(id);
    }
}