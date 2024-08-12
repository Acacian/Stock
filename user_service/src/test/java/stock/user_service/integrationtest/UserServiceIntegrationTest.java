package stock.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.dto.UpdateProfileRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateAndUpdateUser() {
        // Create a user
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        User savedUser = userRepository.save(user);

        // Get the user
        ResponseEntity<User> getUserResponse = restTemplate.getForEntity("/api/users/" + savedUser.getId(), User.class);
        assertEquals(200, getUserResponse.getStatusCodeValue());
        assertEquals("test@example.com", getUserResponse.getBody().getEmail());

        // Update the user's profile
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setProfileImage("new_image.jpg");
        updateRequest.setIntroduction("New introduction");

        HttpEntity<UpdateProfileRequest> requestEntity = new HttpEntity<>(updateRequest);
        ResponseEntity<User> updateResponse = restTemplate.exchange(
                "/api/users/" + savedUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                User.class
        );

        assertEquals(200, updateResponse.getStatusCodeValue());
        assertEquals("Updated Name", updateResponse.getBody().getName());
        assertEquals("new_image.jpg", updateResponse.getBody().getProfileImage());
        assertEquals("New introduction", updateResponse.getBody().getIntroduction());

        // Verify the update in the database
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
            assertEquals("Updated Name", updatedUser.getName());
            assertEquals("new_image.jpg", updatedUser.getProfileImage());
            assertEquals("New introduction", updatedUser.getIntroduction());
        });
    }

    @Test
    void testProcessAuthenticatedUser() {
        // Create a user
        User user = new User();
        user.setEmail("auth@example.com");
        user.setName("Auth User");
        User savedUser = userRepository.save(user);

        // Simulate authentication process
        restTemplate.postForEntity("/api/users/" + savedUser.getId() + "/authenticate", null, Void.class);

        // Verify last login time is updated
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            User authenticatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
            assertNotNull(authenticatedUser.getLastLoginAt());
        });
    }
}