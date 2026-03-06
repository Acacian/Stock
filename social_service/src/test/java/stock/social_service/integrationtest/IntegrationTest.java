package stock.social_service.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import stock.social_service.SocialServiceApplication;
import stock.social_service.model.Comment;
import stock.social_service.model.Post;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;
import stock.social_service.repository.PostRepository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SocialServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@DirtiesContext
@Testcontainers
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();
        commentRepository.deleteAll();
        followRepository.deleteAll();
    }

    @Test
    void shouldCreatePostAndAddComment() {
        HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(Map.of(
                "userId", 1L,
                "title", "Test title",
                "content", "Test post"
        ));
        ResponseEntity<Post> postResponse = restTemplate.postForEntity("/api/social/posts", postRequest, Post.class);
        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        assertNotNull(postResponse.getBody());
        Long postId = postResponse.getBody().getId();

        HttpEntity<Map<String, Object>> commentRequest = new HttpEntity<>(Map.of(
                "userId", 2L,
                "content", "Test comment"
        ));
        ResponseEntity<Comment> commentResponse = restTemplate.postForEntity("/api/social/posts/" + postId + "/comments", commentRequest, Comment.class);
        assertEquals(HttpStatus.OK, commentResponse.getStatusCode());
        assertNotNull(commentResponse.getBody());

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<String> getPostResponse = restTemplate.getForEntity("/api/social/posts/user/1", String.class);
            assertEquals(HttpStatus.OK, getPostResponse.getStatusCode());
            assertNotNull(getPostResponse.getBody());
            assertTrue(getPostResponse.getBody().contains("Test post"));
            assertTrue(getPostResponse.getBody().contains("Test comment"));
        });
    }

    @Test
    void shouldFollowUserAndLikePost() {
        HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(Map.of(
                "userId", 1L,
                "title", "Test title",
                "content", "Test post"
        ));
        ResponseEntity<Post> postResponse = restTemplate.postForEntity("/api/social/posts", postRequest, Post.class);
        Long postId = postResponse.getBody().getId();

        restTemplate.postForEntity("/api/social/follow?followerId=2&followeeId=1", null, Void.class);
        restTemplate.postForEntity("/api/social/posts/" + postId + "/like?userId=2", null, Void.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(followRepository.existsByFollowerIdAndFolloweeId(2L, 1L));
            ResponseEntity<String> getPostResponse = restTemplate.getForEntity("/api/social/posts/" + postId, String.class);
            assertEquals(HttpStatus.OK, getPostResponse.getStatusCode());
            assertNotNull(getPostResponse.getBody());
            assertTrue(getPostResponse.getBody().contains("\"likes\":[2]"));
        });
    }

    @Test
    void shouldGetPostsWithActivity() {
        HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(Map.of(
                "userId", 1L,
                "title", "Test title",
                "content", "Test post"
        ));
        ResponseEntity<Post> postResponse = restTemplate.postForEntity("/api/social/posts", postRequest, Post.class);
        Long postId = postResponse.getBody().getId();

        restTemplate.postForEntity("/api/social/posts/" + postId + "/like?userId=2", null, Void.class);

        HttpEntity<Map<String, Object>> commentRequest = new HttpEntity<>(Map.of(
                "userId", 3L,
                "content", "Test comment"
        ));
        restTemplate.postForEntity("/api/social/posts/" + postId + "/comments", commentRequest, Comment.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<String> getPostsResponse = restTemplate.getForEntity("/api/social/posts/activity/1", String.class);
            assertEquals(HttpStatus.OK, getPostsResponse.getStatusCode());
            assertNotNull(getPostsResponse.getBody());
            assertTrue(getPostsResponse.getBody().contains("\"commentCount\":1"));
            assertTrue(getPostsResponse.getBody().contains("\"likeCount\":1"));
        });
    }
}
