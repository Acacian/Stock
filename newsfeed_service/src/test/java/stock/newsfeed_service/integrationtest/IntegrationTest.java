package stock.newsfeed_service.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import stock.newsfeed_service.NewsfeedServiceApplication;
import stock.newsfeed_service.kafka.SocialEvent;
import stock.newsfeed_service.kafka.UserEvent;
import stock.newsfeed_service.model.NewsfeedItem;
import stock.newsfeed_service.service.NewsfeedService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = NewsfeedServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers
class IntegrationTest {
    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6.2"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private NewsfeedService newsfeedService;

    @Autowired
    private KafkaTemplate<String, SocialEvent> socialKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, UserEvent> userKafkaTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        newsfeedService.clearAllNewsfeeds();

        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM follows");
        jdbcTemplate.execute("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO users (id, name, email, password, enabled) VALUES (?, ?, ?, ?, ?)",
                1L, "Alice", "alice@example.com", "password", true);
        jdbcTemplate.update("INSERT INTO users (id, name, email, password, enabled) VALUES (?, ?, ?, ?, ?)",
                4L, "Bob", "bob@example.com", "password", true);
        jdbcTemplate.update("INSERT INTO users (id, name, email, password, enabled) VALUES (?, ?, ?, ?, ?)",
                5L, "Carol", "carol@example.com", "password", true);
        jdbcTemplate.update("INSERT INTO posts (id, user_id, content) VALUES (?, ?, ?)",
                2L, 5L, "Seed post");
    }

    @Test
    void shouldProcessEventsAndUpdateNewsfeed() {
        Long userId = 1L;
        Long postId = 2L;
        Long commentId = 3L;

        userKafkaTemplate.send("user-events", new UserEvent("USER_FOLLOWED", userId, 4L));
        socialKafkaTemplate.send("social-events", new SocialEvent("POST_CREATED", userId, postId, null));
        socialKafkaTemplate.send("social-events", new SocialEvent("COMMENT_CREATED", userId, postId, commentId));
        socialKafkaTemplate.send("social-events", new SocialEvent("POST_LIKED", userId, postId, null));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<NewsfeedItem> newsfeed = newsfeedService.getNewsfeed(userId);
            assertEquals(4, newsfeed.size());
            assertEquals("LIKE", newsfeed.get(0).getType());
            assertEquals("COMMENT", newsfeed.get(1).getType());
            assertEquals("POST", newsfeed.get(2).getType());
            assertEquals("FOLLOW", newsfeed.get(3).getType());
        });
    }
}
