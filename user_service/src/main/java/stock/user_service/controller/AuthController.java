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

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.dto.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        try {
            return ResponseEntity.ok(authService.registerUser(user));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            JwtAuthenticationResponse jwt = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(jwt);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
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
        authService.verifyUser(token);
        return ResponseEntity.ok("Email verified successfully");
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