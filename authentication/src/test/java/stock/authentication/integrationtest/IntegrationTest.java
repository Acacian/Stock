package stock.authentication.integrationtest;

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
import stock.authentication.dto.JwtAuthenticationResponse;
import stock.authentication.repository.UserRepository;
import stock.authentication.model.User;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

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

        // Login immediately after registration
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
        
        ResponseEntity<Map<String, String>> loginResponse = restTemplate.exchange(
            "/api/auth/login",
            HttpMethod.POST,
            loginRequest,
            new ParameterizedTypeReference<Map<String, String>>() {}
        );
        assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
    }

    @Test
    void testLogoutTokenInvalidation() {
        // Register and login
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", 
            Map.of("email", testEmail, "password", testPassword, "name", "Test User"), String.class);
        assertEquals(200, registerResponse.getStatusCode().value());

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
}