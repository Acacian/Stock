package stock.user_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import stock.user_service.model.User;
import stock.user_service.repository.UserRepository;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.kafka.UserEvent;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUser(1L));
    }

    @Test
    void testUpdateProfile() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setProfileImage("new_image.jpg");
        request.setIntroduction("New introduction");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateProfile(1L, request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("new_image.jpg", result.getProfileImage());
        assertEquals("New introduction", result.getIntroduction());
        verify(kafkaTemplate).send(eq("user-events"), any(UserEvent.class));
    }

    @Test
    void testProcessAuthenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.processAuthenticatedUser(1L);

        verify(userRepository).save(user);
        assertNotNull(user.getLastLoginAt());
        assertTrue(user.getLastLoginAt().isBefore(LocalDateTime.now()) || user.getLastLoginAt().isEqual(LocalDateTime.now()));
    }
}