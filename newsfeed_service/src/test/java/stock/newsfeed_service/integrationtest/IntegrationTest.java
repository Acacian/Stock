package stock.newsfeed_service.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import stock.newsfeed_service.service.NewsfeedService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

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

    @BeforeEach
    void setUp() {
        newsfeedService.clearAllNewsfeeds();
    }

    @Test
    void shouldProcessEventsAndUpdateNewsfeed() {
        Long userId = 1L;
        Long postId = 2L;
        Long commentId = 3L;

        // Test follow event
        userKafkaTemplate.send("user-events", new UserEvent("USER_FOLLOWED", userId, 4L));

        // Test post creation
        socialKafkaTemplate.send("social-events", new SocialEvent("POST_CREATED", userId, postId, null));

        // Test comment creation
        socialKafkaTemplate.send("social-events", new SocialEvent("COMMENT_CREATED", userId, postId, commentId));

        // Test like event
        socialKafkaTemplate.send("social-events", new SocialEvent("POST_LIKED", userId, postId, null));

        // Wait for events to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> newsfeed = newsfeedService.getNewsfeed(userId);
            assertEquals(4, newsfeed.size());
            assertTrue(newsfeed.get(0).contains("liked post"));
            assertTrue(newsfeed.get(1).contains("commented on post"));
            assertTrue(newsfeed.get(2).contains("created a new post"));
            assertTrue(newsfeed.get(3).contains("followed User"));
        });
    }
}