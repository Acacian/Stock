package stock.authentication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stock.authentication.model.User;
import stock.authentication.service.AuthService;
import stock.authentication.dto.LoginRequest;
import stock.authentication.dto.JwtAuthenticationResponse;
import stock.authentication.dto.UpdatePasswordRequest;
import stock.authentication.exception.GlobalExceptionHandler.EmailAlreadyExistsException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(authService.registerUser(user));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        String jwt = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
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
}