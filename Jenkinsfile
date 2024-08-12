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
import stock.authentication.model.User;
import stock.authentication.dto.LoginRequest;
import stock.social_service.model.Post;
import stock.user_service.dto.UpdateProfileRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setup() {
        // Register and login a user
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        restTemplate.postForEntity("/api/auth/register", user, String.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        ResponseEntity<String> loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, String.class);
        authToken = loginResponse.getHeaders().getFirst("Authorization");
    }

    @Test
    void testFullUserFlow() {
        // Update user profile
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setName("Test User");
        updateRequest.setIntroduction("Hello, I'm a test user");
        HttpEntity<UpdateProfileRequest> updateEntity = new HttpEntity<>(updateRequest, headers);
        ResponseEntity<User> updateResponse = restTemplate.exchange("/api/users/1", HttpMethod.PUT, updateEntity, User.class);
        assertEquals(200, updateResponse.getStatusCodeValue());
        assertEquals("Test User", updateResponse.getBody().getName());

        // Create a post
        Post post = new Post();
        post.setContent("This is a test post");
        HttpEntity<Post> postEntity = new HttpEntity<>(post, headers);
        ResponseEntity<Post> postResponse = restTemplate.exchange("/api/social/posts", HttpMethod.POST, postEntity, Post.class);
        assertEquals(200, postResponse.getStatusCodeValue());
        assertNotNull(postResponse.getBody().getId());

        // Get newsfeed
        ResponseEntity<String[]> newsfeedResponse = restTemplate.exchange("/api/newsfeed/1", HttpMethod.GET, new HttpEntity<>(headers), String[].class);
        assertEquals(200, newsfeedResponse.getStatusCodeValue());
        assertTrue(newsfeedResponse.getBody().length > 0);

        // Follow another user
        restTemplate.exchange("/api/social/follow?followerId=1&followedId=2", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Check updated newsfeed
        newsfeedResponse = restTemplate.exchange("/api/newsfeed/1", HttpMethod.GET, new HttpEntity<>(headers), String[].class);
        assertEquals(200, newsfeedResponse.getStatusCodeValue());
        assertTrue(newsfeedResponse.getBody().length > 1);

        // Logout
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        // Verify logout
        ResponseEntity<String> logoutCheckResponse = restTemplate.exchange("/api/users/1", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(401, logoutCheckResponse.getStatusCodeValue());
    }
}