package stock.user_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import stock.user_service.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.user_service.exception.GlobalExceptionHandler.InvalidTokenException;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.security.JwtTokenProvider;
import stock.user_service.service.AuthService;
import stock.user_service.service.EmailService;
import stock.user_service.dto.UpdateProfileRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        User result = authService.registerUser(user);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertFalse(result.isEnabled());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    void testRegisterUserWithExistingEmail() {
        User user = new User();
        user.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.registerUser(user));
    }

    @Test
    void testAuthenticateUser() {
        String email = "test@example.com";
        String password = "password";

        when(tokenProvider.generateToken(any())).thenReturn("jwtToken");

        String result = authService.authenticateUser(email, password);

        assertNotNull(result);
        assertEquals("jwtToken", result);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void testLogout() {
        String token = "validToken";
        authService.logout(token);

        verify(valueOperations).set(eq("token:validToken"), eq("blacklisted"), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void testUpdatePassword() {
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(oldPassword));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        authService.updatePassword(userId, oldPassword, newPassword);

        verify(userRepository).save(user);
        verify(redisTemplate).delete("user_tokens:" + userId);
    }

    @Test
    void testUpdatePasswordWithIncorrectOldPassword() {
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode("differentOldPassword"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.updatePassword(userId, oldPassword, newPassword));

        verify(userRepository, never()).save(any(User.class));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyUser() {
        String token = "validToken";
        String email = "test@example.com";

        when(valueOperations.get("verification:" + token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        authService.verifyUser(token);

        verify(userRepository).save(any(User.class));
        verify(redisTemplate).delete("verification:" + token);
    }

    @Test
    void testVerifyUserWithInvalidToken() {
        String token = "invalidToken";

        when(valueOperations.get("verification:" + token)).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> authService.verifyUser(token));
    }

    @Test
    void testUpdateProfile() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setIntroduction("Old Introduction");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setIntroduction("New Introduction");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User updatedUser = authService.updateProfile(userId, request);

        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("New Introduction", updatedUser.getIntroduction());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testGetUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = authService.getUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
    }
}