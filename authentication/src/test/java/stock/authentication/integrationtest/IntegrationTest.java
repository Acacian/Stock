package stock.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import stock.common.event.UserEvent;
import stock.common.event.SocialEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, UserEvent> userKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, SocialEvent> socialKafkaTemplate;

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
    void testFullUserFlow() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);

        // Update user profile
        ResponseEntity<String> updateResponse = restTemplate.exchange("/api/users/1", HttpMethod.PUT, 
            new HttpEntity<>(Map.of("name", "Test User", "introduction", "Hello, I'm a test user"), headers), String.class);
        assertEquals(200, updateResponse.getStatusCodeValue());

        // Create a post
        ResponseEntity<String> postResponse = restTemplate.exchange("/api/social/posts", HttpMethod.POST, 
            new HttpEntity<>(Map.of("content", "This is a test post"), headers), String.class);
        assertEquals(200, postResponse.getStatusCodeValue());

        // Simulate Kafka event for post creation
        socialKafkaTemplate.send("social-events", new SocialEvent("POST_CREATED", 1L, 1L));

        // Wait for newsfeed to be updated
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ResponseEntity<String[]> newsfeedResponse = restTemplate.exchange("/api/newsfeed/1", HttpMethod.GET, new HttpEntity<>(headers), String[].class);
            return newsfeedResponse.getStatusCodeValue() == 200 && newsfeedResponse.getBody().length > 0;
        });

        // Follow another user
        restTemplate.exchange("/api/social/follow?followerId=1&followedId=2", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Simulate Kafka event for follow action
        socialKafkaTemplate.send("social-events", new SocialEvent("USER_FOLLOWED", 1L, 2L));

        // Wait for updated newsfeed
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ResponseEntity<String[]> newsfeedResponse = restTemplate.exchange("/api/newsfeed/1", HttpMethod.GET, new HttpEntity<>(headers), String[].class);
            return newsfeedResponse.getStatusCodeValue() == 200 && newsfeedResponse.getBody().length > 1;
        });

        // Logout
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Verify logout
        ResponseEntity<String> logoutCheckResponse = restTemplate.exchange("/api/users/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
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
        ResponseEntity<String> response = restTemplate.exchange("/api/users/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, response.getStatusCodeValue());
    }
}