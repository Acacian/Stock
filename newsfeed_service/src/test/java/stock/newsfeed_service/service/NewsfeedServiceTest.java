package stock.newsfeed_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import stock.newsfeed_service.kafka.SocialEvent;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsfeedServiceTest {

    @InjectMocks
    private NewsfeedService newsfeedService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testConsumeSocialEvent() {
        SocialEvent event = new SocialEvent("POST_CREATED", 1L, 2L);
        newsfeedService.consumeSocialEvent(event);

        verify(listOperations).leftPush(eq("newsfeed:1"), anyString());
        verify(listOperations).trim(eq("newsfeed:1"), eq(0L), eq(99L));
    }

    @Test
    void testGetNewsfeed() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(-1L)))
                .thenReturn(Arrays.asList("event1", "event2", "event3"));

        List<String> result = newsfeedService.getNewsfeed(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("event1", result.get(0));
        assertEquals("event2", result.get(1));
        assertEquals("event3", result.get(2));
    }

    @Test
    void testNewsfeedOrder() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(-1L)))
                .thenReturn(Arrays.asList("newestEvent", "olderEvent", "oldestEvent"));

        List<String> result = newsfeedService.getNewsfeed(1L);

        assertEquals("newestEvent", result.get(0));
        assertEquals("oldestEvent", result.get(2));
    }

    @Test
    void testNewsfeedLimit() {
        newsfeedService.consumeSocialEvent(new SocialEvent("TEST_EVENT", 1L, 1L));
        verify(listOperations).trim(eq("newsfeed:1"), eq(0L), eq(99L));
    }

    @Test
    void testConsumeMultipleSocialEvents() {
        SocialEvent event1 = new SocialEvent("POST_CREATED", 1L, 2L);
        SocialEvent event2 = new SocialEvent("COMMENT_ADDED", 1L, 3L);
        SocialEvent event3 = new SocialEvent("POST_LIKED", 1L, 4L);

        newsfeedService.consumeSocialEvent(event1);
        newsfeedService.consumeSocialEvent(event2);
        newsfeedService.consumeSocialEvent(event3);

        verify(listOperations, times(3)).leftPush(eq("newsfeed:1"), anyString());
        verify(listOperations, times(3)).trim(eq("newsfeed:1"), eq(0L), eq(99L));
    }

    @Test
    void testGetEmptyNewsfeed() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(-1L)))
                .thenReturn(List.of());

        List<String> result = newsfeedService.getNewsfeed(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}