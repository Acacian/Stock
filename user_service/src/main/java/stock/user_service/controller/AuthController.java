package stock.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.user_service.model.User;
import stock.user_service.service.AuthService;
import stock.user_service.dto.LoginRequest;
import stock.user_service.dto.JwtAuthenticationResponse;
import stock.user_service.dto.UpdatePasswordRequest;
import stock.user_service.exception.GlobalExceptionHandler.EmailAlreadyExistsException;
import stock.user_service.exception.GlobalExceptionHandler.InvalidTokenException;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.dto.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        logger.debug("Received registration request: {}", user);
        try {
            return ResponseEntity.ok(authService.registerUser(user));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> loginInfo() {
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("loginEndpoint", "/api/auth/login");
        loginInfo.put("method", "POST");
        loginInfo.put("requiredFields", Arrays.asList("email", "password"));
        return ResponseEntity.ok(loginInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            JwtAuthenticationResponse jwt = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(jwt);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 올바르지 않습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        authService.logout(token.substring(7));
        return ResponseEntity.ok("Logged out successfully");
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String token) {
        logger.info("Received verification request with token: {}", token);
        try {
            authService.verifyUser(token);
            return ResponseEntity.ok("Email verified successfully");
        } catch (InvalidTokenException e) {
            logger.error("Invalid token: {}", token, e);
            return ResponseEntity.badRequest().body("Invalid or expired token");
        } catch (Exception e) {
            logger.error("Error during email verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during verification");
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok("Authenticated");
    }

    @GetMapping("/verify-status")
    public ResponseEntity<String> checkVerificationStatus(@RequestParam String email) {
        User user = authService.getUserByEmail(email);
        return ResponseEntity.ok(user.isEnabled() ? "VERIFIED" : "NOT_VERIFIED");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUser(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(id, request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            JwtAuthenticationResponse newTokens = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(newTokens);
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }
}