package stock.newsfeed_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import stock.newsfeed_service.repository.UserRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsfeedServiceTest {

    @InjectMocks
    private NewsfeedService newsfeedService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testAddFollowActivity() {
        newsfeedService.addFollowActivity(1L, 2L);
        verify(listOperations).leftPush(eq("newsfeed:2"), contains("User 1 followed User 2"));
        verify(listOperations).trim(eq("newsfeed:2"), eq(0L), eq(99L));
    }

    @Test
    void testAddPostActivity() {
        Set<Long> followers = new HashSet<>(Arrays.asList(2L, 3L));
        when(userRepository.getFollowers(1L)).thenReturn(followers);

        newsfeedService.addPostActivity(1L, 10L);

        verify(listOperations).leftPush(eq("newsfeed:2"), contains("User 1 created a new post: 10"));
        verify(listOperations).leftPush(eq("newsfeed:3"), contains("User 1 created a new post: 10"));
        verify(listOperations, times(2)).trim(anyString(), eq(0L), eq(99L));
    }

    @Test
    void testAddCommentActivity() {
        Set<Long> followers = new HashSet<>(Arrays.asList(2L, 3L));
        when(userRepository.getFollowers(1L)).thenReturn(followers);
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addCommentActivity(1L, 10L, 20L);

        verify(listOperations).leftPush(eq("newsfeed:2"), contains("User 1 commented on post 10"));
        verify(listOperations).leftPush(eq("newsfeed:3"), contains("User 1 commented on post 10"));
        verify(listOperations).leftPush(eq("newsfeed:4"), contains("User 1 commented on post 10"));
        verify(listOperations, times(3)).trim(anyString(), eq(0L), eq(99L));
    }

    @Test
    void testAddLikeActivity() {
        Set<Long> followers = new HashSet<>(Arrays.asList(2L, 3L));
        when(userRepository.getFollowers(1L)).thenReturn(followers);
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addLikeActivity(1L, 10L);

        verify(listOperations).leftPush(eq("newsfeed:2"), contains("User 1 liked post 10"));
        verify(listOperations).leftPush(eq("newsfeed:3"), contains("User 1 liked post 10"));
        verify(listOperations).leftPush(eq("newsfeed:4"), contains("User 1 liked post 10"));
        verify(listOperations, times(3)).trim(anyString(), eq(0L), eq(99L));
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
    void testGetEmptyNewsfeed() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(-1L)))
                .thenReturn(List.of());

        List<String> result = newsfeedService.getNewsfeed(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}