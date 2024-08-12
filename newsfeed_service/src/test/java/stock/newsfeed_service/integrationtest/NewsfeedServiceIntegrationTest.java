package stock.newsfeed_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import stock.common.event.SocialEvent;
import stock.newsfeed_service.service.NewsfeedService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
@Testcontainers
class NewsfeedServiceIntegrationTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6.2"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private NewsfeedService newsfeedService;

    @Autowired
    private KafkaTemplate<String, SocialEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        // Clear Redis before each test
        newsfeedService.clearAllNewsfeeds();
    }

    @Test
    void testNewsfeedIntegration() {
        Long userId = 1L;

        // Send a social event via Kafka
        SocialEvent event = new SocialEvent("POST_CREATED", userId, 2L);
        kafkaTemplate.send("social-events", event);

        // Wait for the event to be processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> newsfeed = newsfeedService.getNewsfeed(userId);
            assertFalse(newsfeed.isEmpty());
            assertTrue(newsfeed.get(0).contains("POST_CREATED"));
        });

        // Send another event
        SocialEvent event2 = new SocialEvent("COMMENT_ADDED", userId, 3L);
        kafkaTemplate.send("social-events", event2);

        // Check if both events are in the newsfeed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<String> newsfeed = newsfeedService.getNewsfeed(userId);
            assertEquals(2, newsfeed.size());
            assertTrue(newsfeed.get(0).contains("COMMENT_ADDED"));
            assertTrue(newsfeed.get(1).contains("POST_CREATED"));
        });
    }
}