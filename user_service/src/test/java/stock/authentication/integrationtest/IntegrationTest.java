package stock.user_service.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import stock.user_service.dto.JwtAuthenticationResponse;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.service.AuthService;

import java.net.HttpRetryException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthService authService;

    @Value("${test.email}")
    private String testEmail;

    private String authToken;
    private final String testPassword = "testPassword123";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testRegistrationAndAuthFlow() {
        // Register
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", 
            Map.of("email", testEmail, "password", testPassword, "name", "Test User"), String.class);
        assertEquals(200, registerResponse.getStatusCode().value());
    
        // Simulate email verification
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        user.setEnabled(true);
        userRepository.save(user);
    
        // Login
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", testEmail, testPassword);
        HttpEntity<String> loginRequest = new HttpEntity<>(loginBody, headers);
        
        ResponseEntity<JwtAuthenticationResponse> loginResponse = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            loginRequest,
            JwtAuthenticationResponse.class
        );
    
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        authToken = loginResponse.getBody().getToken();
        assertNotNull(authToken);
    
        // Verify authentication
        headers.set("Authorization", "Bearer " + authToken);
        ResponseEntity<String> authCheckResponse = restTemplate.exchange(
            "/api/auth/check", 
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        assertEquals(HttpStatus.OK, authCheckResponse.getStatusCode());

        // Update user profile
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Updated Test User");
        updateRequest.setIntroduction("Updated introduction");
        
        ResponseEntity<User> updateResponse = restTemplate.exchange(
            "/api/users/" + user.getId(),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headers),
            User.class
        );
        
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated Test User", updateResponse.getBody().getName());
        assertEquals("Updated introduction", updateResponse.getBody().getIntroduction());

        // Logout
        ResponseEntity<Void> logoutResponse = restTemplate.exchange(
            "/api/auth/logout",
            HttpMethod.POST,
            new HttpEntity<>(headers),
            Void.class
        );
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

        // Verify logout
        ResponseEntity<String> logoutCheckResponse = restTemplate.exchange(
            "/api/auth/check", 
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, logoutCheckResponse.getStatusCode());
    }

    @Test
    void testInvalidLogin() {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@example.com";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String loginBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", uniqueEmail, "wrongpassword");
        HttpEntity<String> loginRequest = new HttpEntity<>(loginBody, headers);
        
        try {
            ResponseEntity<Map<String, String>> loginResponse = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                loginRequest,
                new ParameterizedTypeReference<Map<String, String>>() {}
            );
            assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof HttpRetryException) {
                HttpRetryException hre = (HttpRetryException) e.getCause();
                assertEquals(401, hre.responseCode());
            } else {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
    }

    @Test
    void testLogoutTokenInvalidation() {
        // Register and verify user
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(testPassword);
        user.setName("Test User");
        user = authService.registerUser(user);
        user.setEnabled(true);
        userRepository.save(user);
    
        // Login
        ResponseEntity<JwtAuthenticationResponse> loginResponse = restTemplate.postForEntity("/api/auth/login", 
            Map.of("email", testEmail, "password", testPassword), JwtAuthenticationResponse.class);
        assertEquals(200, loginResponse.getStatusCode().value());
        authToken = loginResponse.getBody().getToken();
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
    
        // Logout
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);
    
        // Try to use the same token
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/check", 
            HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void shouldCreateAndUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setEnabled(true);
        user.setIntroduction("This is a test user.");
        userRepository.save(user);

        User foundUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");

        foundUser.setName("Updated Test User");
        userRepository.save(foundUser);

        User updatedUser = userRepository.findById(foundUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("Updated Test User");
    }
}