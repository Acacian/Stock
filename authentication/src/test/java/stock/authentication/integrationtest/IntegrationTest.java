package stock.authentication.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setup() {
        // Register and login a user
        ResponseEntity<String> registerResponse = restTemplate.postForEntity("/api/auth/register", 
            Map.of("email", "test@example.com", "password", "password"), String.class);
        assertEquals(200, registerResponse.getStatusCodeValue());

        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", 
            Map.of("email", "test@example.com", "password", "password"), String.class);
        authToken = loginResponse.getHeaders().getFirst("Authorization");
    }

    @Test
    void testAuthFlow() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);

        // Verify authentication
        ResponseEntity<String> authCheckResponse = restTemplate.exchange("/api/auth/check", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(200, authCheckResponse.getStatusCodeValue());

        // Logout
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Verify logout
        ResponseEntity<String> logoutCheckResponse = restTemplate.exchange("/api/auth/check", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, logoutCheckResponse.getStatusCodeValue());
    }

    @Test
    void testInvalidLogin() {
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", 
            Map.of("email", "test@example.com", "password", "wrongpassword"), String.class);
        assertEquals(401, loginResponse.getStatusCodeValue());
    }

    @Test
    void testLogoutTokenInvalidation() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);

        // Logout
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Try to use the same token
        ResponseEntity<String> response = restTemplate.exchange("/api/auth/check", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, response.getStatusCodeValue());
    }
}