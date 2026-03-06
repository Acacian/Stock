package stock.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import stock.user_service.client.NewsfeedServiceClient;
import stock.user_service.dto.JwtAuthenticationResponse;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.user_service.exception.GlobalExceptionHandler.InvalidTokenException;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.security.JwtTokenProvider;
import stock.user_service.security.UserPrincipal;
import stock.user_service.service.AuthService;
import stock.user_service.service.CustomUserDetailsService;
import stock.user_service.service.EmailService;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private SetOperations<String, String> setOperations;

    @Mock
    private EmailService emailService;

    @Mock
    private NewsfeedServiceClient newsfeedServiceClient;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        ReflectionTestUtils.setField(authService, "activeProfile", "test");
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationInMs", 604800000);
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(user);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertFalse(result.isEnabled());
        verify(userRepository).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
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

        User user = new User();
        user.setId(1L);
        user.setName("Tester");
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.generateAccessToken(authentication)).thenReturn("accessToken");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refreshToken");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        JwtAuthenticationResponse result = authService.authenticateUser(email, password);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        verify(valueOperations).set(
                eq("refresh_token:" + email),
                eq("refreshToken"),
                eq(604800000L),
                eq(TimeUnit.MILLISECONDS)
        );
        verify(newsfeedServiceClient).userAuthenticated(any());
    }

    @Test
    void testLogout() {
        authService.logout("validToken");

        verify(valueOperations).set(
                eq("token:validToken"),
                eq("blacklisted"),
                eq(24L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testUpdatePassword() {
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(setOperations.members("user_tokens:" + userId)).thenReturn(Set.of("token-a"));

        authService.updatePassword(userId, oldPassword, newPassword);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
        verify(valueOperations).set(eq("token:token-a"), eq("blacklisted"), eq(24L), eq(TimeUnit.HOURS));
        verify(redisTemplate).delete("user_tokens:" + userId);
        verify(newsfeedServiceClient).passwordUpdated(any());
    }

    @Test
    void testUpdatePasswordWithIncorrectOldPassword() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setPassword("encodedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.updatePassword(userId, "wrongPassword", "newPassword"));

        verify(userRepository, never()).save(any(User.class));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyUser() {
        String token = "validToken";
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(valueOperations.get("verification:" + token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authService.verifyUser(token);

        assertTrue(user.isEnabled());
        verify(userRepository).save(user);
        verify(redisTemplate).delete("verification:" + token);
    }

    @Test
    void testVerifyUserWithInvalidToken() {
        when(valueOperations.get("verification:invalidToken")).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> authService.verifyUser("invalidToken"));
    }

    @Test
    void testUpdateProfile() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("test@example.com");
        existingUser.setName("Old Name");
        existingUser.setIntroduction("Old Introduction");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setIntroduction("New Introduction");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = authService.updateProfile(userId, request);

        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("New Introduction", updatedUser.getIntroduction());
        verify(newsfeedServiceClient).profileUpdated(any());
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
