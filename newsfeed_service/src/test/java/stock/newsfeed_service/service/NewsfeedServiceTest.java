package stock.newsfeed_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import stock.newsfeed_service.model.NewsfeedItem;
import stock.newsfeed_service.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsfeedServiceTest {

    @InjectMocks
    private NewsfeedService newsfeedService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testAddFollowActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getUserName(2L)).thenReturn("Bob");
        when(userRepository.getFollowers(1L)).thenReturn(Set.of(3L));

        newsfeedService.addFollowActivity(1L, 2L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(listOperations, times(2)).leftPush(keyCaptor.capture(), payloadCaptor.capture());
        verify(listOperations, times(2)).trim(any(), eq(0L), eq(99L));
        verify(messagingTemplate).convertAndSend(eq("/topic/newsfeed/1"), any(NewsfeedItem.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/newsfeed/3"), any(NewsfeedItem.class));

        assertEquals(List.of("newsfeed:1", "newsfeed:3"), keyCaptor.getAllValues());

        for (String payload : payloadCaptor.getAllValues()) {
            NewsfeedItem item = objectMapper.readValue(payload, NewsfeedItem.class);
            assertEquals("FOLLOW", item.getType());
            assertEquals(1L, item.getUserId());
            assertEquals("Alice", item.getUserName());
            assertEquals(2L, item.getTargetId());
            assertEquals("Bob", item.getTargetUserName());
        }
    }

    @Test
    void testAddPostActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(new LinkedHashSet<>(List.of(2L, 3L)));

        newsfeedService.addPostActivity(1L, 10L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(listOperations, times(3)).leftPush(keyCaptor.capture(), payloadCaptor.capture());
        verify(listOperations, times(3)).trim(any(), eq(0L), eq(99L));

        assertEquals(List.of("newsfeed:1", "newsfeed:2", "newsfeed:3"), keyCaptor.getAllValues());

        for (String payload : payloadCaptor.getAllValues()) {
            NewsfeedItem item = objectMapper.readValue(payload, NewsfeedItem.class);
            assertEquals("POST", item.getType());
            assertEquals(1L, item.getUserId());
            assertEquals("Alice", item.getUserName());
            assertEquals(10L, item.getTargetId());
        }
    }

    @Test
    void testAddCommentActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(new LinkedHashSet<>(List.of(2L, 3L)));
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addCommentActivity(1L, 10L, 20L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(listOperations, times(4)).leftPush(keyCaptor.capture(), payloadCaptor.capture());
        verify(listOperations, times(4)).trim(any(), eq(0L), eq(99L));

        assertEquals(List.of("newsfeed:1", "newsfeed:2", "newsfeed:3", "newsfeed:4"), keyCaptor.getAllValues());

        for (String payload : payloadCaptor.getAllValues()) {
            NewsfeedItem item = objectMapper.readValue(payload, NewsfeedItem.class);
            assertEquals("COMMENT", item.getType());
            assertEquals(1L, item.getUserId());
            assertEquals("Alice", item.getUserName());
            assertEquals(10L, item.getTargetId());
        }
    }

    @Test
    void testAddLikeActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(new LinkedHashSet<>(List.of(2L, 3L)));
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addLikeActivity(1L, 10L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(listOperations, times(4)).leftPush(keyCaptor.capture(), payloadCaptor.capture());
        verify(listOperations, times(4)).trim(any(), eq(0L), eq(99L));

        assertEquals(List.of("newsfeed:1", "newsfeed:2", "newsfeed:3", "newsfeed:4"), keyCaptor.getAllValues());

        for (String payload : payloadCaptor.getAllValues()) {
            NewsfeedItem item = objectMapper.readValue(payload, NewsfeedItem.class);
            assertEquals("LIKE", item.getType());
            assertEquals(1L, item.getUserId());
            assertEquals("Alice", item.getUserName());
            assertEquals(10L, item.getTargetId());
        }
    }

    @Test
    void testGetNewsfeed() throws Exception {
        NewsfeedItem first = new NewsfeedItem("POST", 1L, "Alice", 10L, null, null);
        NewsfeedItem second = new NewsfeedItem("LIKE", 2L, "Bob", 10L, null, null);
        String firstJson = objectMapper.writeValueAsString(first);
        String secondJson = objectMapper.writeValueAsString(second);

        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(List.of(firstJson, secondJson));

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertEquals(2, result.size());
        assertEquals("POST", result.get(0).getType());
        assertEquals("LIKE", result.get(1).getType());
    }

    @Test
    void testNewsfeedOrder() throws Exception {
        NewsfeedItem newest = new NewsfeedItem("LIKE", 1L, "Alice", 30L, null, null);
        NewsfeedItem oldest = new NewsfeedItem("POST", 1L, "Alice", 10L, null, null);
        String newestJson = objectMapper.writeValueAsString(newest);
        String oldestJson = objectMapper.writeValueAsString(oldest);

        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(List.of(newestJson, oldestJson));

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertEquals("LIKE", result.get(0).getType());
        assertEquals("POST", result.get(1).getType());
    }

    @Test
    void testGetEmptyNewsfeed() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(List.of());

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertTrue(result.isEmpty());
    }
}
