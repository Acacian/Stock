package stock.social_service.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import stock.social_service.model.Post;
import stock.social_service.model.Comment;
import stock.social_service.repository.PostRepository;
import stock.social_service.repository.CommentRepository;
import stock.social_service.repository.FollowRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
class SocialServiceIntegrationTest {

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
    void testCreatePostAndComment() {
        // Create a post
        HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(Map.of(
            "userId", 1L,
            "content", "Test post"
        ));
        ResponseEntity<Post> postResponse = restTemplate.postForEntity("/api/social/posts", postRequest, Post.class);
        assertEquals(200, postResponse.getStatusCodeValue());
        assertNotNull(postResponse.getBody());
        Long postId = postResponse.getBody().getId();

        // Add a comment to the post
        HttpEntity<Map<String, Object>> commentRequest = new HttpEntity<>(Map.of(
            "userId", 2L,
            "content", "Test comment"
        ));
        ResponseEntity<Comment> commentResponse = restTemplate.postForEntity("/api/social/posts/" + postId + "/comments", commentRequest, Comment.class);
        assertEquals(200, commentResponse.getStatusCodeValue());
        assertNotNull(commentResponse.getBody());

        // Verify the comment was added
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ResponseEntity<Post[]> getPostResponse = restTemplate.getForEntity("/api/social/posts/user/1", Post[].class);
            assertEquals(200, getPostResponse.getStatusCodeValue());
            Post[] posts = getPostResponse.getBody();
            assertNotNull(posts);
            assertEquals(1, posts.length);
            assertEquals(1, posts[0].getComments().size());
        });
    }

    @Test
    void testFollowAndLikePost() {
        // User 1 creates a post
        HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(Map.of(
            "userId", 1L,
            "content", "Test post"
        ));
        ResponseEntity<Post> postResponse = restTemplate.postForEntity("/api/social/posts", postRequest, Post.class);
        Long postId = postResponse.getBody().getId();

        // User 2 follows User 1
        restTemplate.postForEntity("/api/social/follow?followerId=2&followedId=1", null, Void.class);

        // User 2 likes User 1's post
        restTemplate.postForEntity("/api/social/posts/" + postId + "/likes?userId=2", null, Void.class);

        // Verify the follow and like
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(followRepository.existsByFollowerIdAndFollowedId(2L, 1L));
            Post post = postRepository.findById(postId).orElseThrow();
            assertTrue(post.getLikes().contains(2L));
        });
    }
}