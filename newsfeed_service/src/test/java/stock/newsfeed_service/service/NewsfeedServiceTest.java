package stock.newsfeed_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import stock.newsfeed_service.model.NewsfeedItem;
import stock.newsfeed_service.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsfeedServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NewsfeedService newsfeedService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        newsfeedService = new NewsfeedService();
        ReflectionTestUtils.setField(newsfeedService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(newsfeedService, "userRepository", userRepository);
        ReflectionTestUtils.setField(newsfeedService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(newsfeedService, "messagingTemplate", messagingTemplate);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void testAddFollowActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getUserName(2L)).thenReturn("Bob");
        when(userRepository.getFollowers(1L)).thenReturn(Set.of());

        newsfeedService.addFollowActivity(1L, 2L);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations).leftPush(eq("newsfeed:1"), valueCaptor.capture());
        verify(listOperations).trim(eq("newsfeed:1"), eq(0L), eq(99L));

        NewsfeedItem item = objectMapper.readValue(valueCaptor.getValue(), NewsfeedItem.class);
        assertEquals("FOLLOW", item.getType());
        assertEquals(1L, item.getUserId());
        assertEquals("Alice", item.getUserName());
        assertEquals(2L, item.getTargetId());
        assertEquals("Bob", item.getTargetUserName());
    }

    @Test
    void testAddPostActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(Set.of(2L, 3L));

        newsfeedService.addPostActivity(1L, 10L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations, times(3)).leftPush(keyCaptor.capture(), valueCaptor.capture());
        verify(listOperations, times(3)).trim(anyString(), eq(0L), eq(99L));

        assertEquals(Set.of("newsfeed:1", "newsfeed:2", "newsfeed:3"), Set.copyOf(keyCaptor.getAllValues()));

        NewsfeedItem item = objectMapper.readValue(valueCaptor.getAllValues().get(0), NewsfeedItem.class);
        assertEquals("POST", item.getType());
        assertEquals(1L, item.getUserId());
        assertEquals("Alice", item.getUserName());
        assertEquals(10L, item.getTargetId());
    }

    @Test
    void testAddCommentActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(Set.of(2L, 3L));
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addCommentActivity(1L, 10L, 20L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations, times(4)).leftPush(keyCaptor.capture(), valueCaptor.capture());
        verify(listOperations, times(4)).trim(anyString(), eq(0L), eq(99L));

        assertEquals(Set.of("newsfeed:1", "newsfeed:2", "newsfeed:3", "newsfeed:4"), Set.copyOf(keyCaptor.getAllValues()));

        NewsfeedItem item = objectMapper.readValue(valueCaptor.getAllValues().get(0), NewsfeedItem.class);
        assertEquals("COMMENT", item.getType());
        assertEquals(1L, item.getUserId());
        assertEquals(10L, item.getTargetId());
    }

    @Test
    void testAddLikeActivity() throws Exception {
        when(userRepository.getUserName(1L)).thenReturn("Alice");
        when(userRepository.getFollowers(1L)).thenReturn(Set.of(2L, 3L));
        when(userRepository.getPostOwner(10L)).thenReturn(4L);

        newsfeedService.addLikeActivity(1L, 10L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(listOperations, times(4)).leftPush(keyCaptor.capture(), valueCaptor.capture());
        verify(listOperations, times(4)).trim(anyString(), eq(0L), eq(99L));

        assertEquals(Set.of("newsfeed:1", "newsfeed:2", "newsfeed:3", "newsfeed:4"), Set.copyOf(keyCaptor.getAllValues()));

        NewsfeedItem item = objectMapper.readValue(valueCaptor.getAllValues().get(0), NewsfeedItem.class);
        assertEquals("LIKE", item.getType());
        assertEquals(1L, item.getUserId());
        assertEquals(10L, item.getTargetId());
    }

    @Test
    void testGetNewsfeed() throws Exception {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(Arrays.asList(
                        objectMapper.writeValueAsString(new NewsfeedItem("POST", 1L, "Alice", 10L, null, null)),
                        objectMapper.writeValueAsString(new NewsfeedItem("COMMENT", 1L, "Alice", 10L, null, null)),
                        objectMapper.writeValueAsString(new NewsfeedItem("LIKE", 2L, "Bob", 10L, null, null))
                ));

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertEquals(3, result.size());
        assertEquals("POST", result.get(0).getType());
        assertEquals("COMMENT", result.get(1).getType());
        assertEquals("LIKE", result.get(2).getType());
    }

    @Test
    void testNewsfeedOrder() throws Exception {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(Arrays.asList(
                        objectMapper.writeValueAsString(new NewsfeedItem("LIKE", 1L, "Alice", 10L, null, null)),
                        objectMapper.writeValueAsString(new NewsfeedItem("COMMENT", 1L, "Alice", 10L, null, null)),
                        objectMapper.writeValueAsString(new NewsfeedItem("POST", 1L, "Alice", 10L, null, null))
                ));

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertEquals("LIKE", result.get(0).getType());
        assertEquals("POST", result.get(2).getType());
    }

    @Test
    void testGetEmptyNewsfeed() {
        when(listOperations.range(eq("newsfeed:1"), eq(0L), eq(99L)))
                .thenReturn(List.of());

        List<NewsfeedItem> result = newsfeedService.getNewsfeed(1L);

        assertTrue(result.isEmpty());
    }
}
