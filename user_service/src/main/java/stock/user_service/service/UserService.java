package stock.user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import stock.user_service.model.User;
import stock.user_service.repository.*;
import stock.user_service.dto.UpdateProfileRequest;
import stock.user_service.kafka.UserEvent;

import java.time.LocalDateTime;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUser(id);
        user.setName(request.getName());
        user.setProfileImage(request.getProfileImage());
        user.setIntroduction(request.getIntroduction());
        User updatedUser = userRepository.save(user);
        
        kafkaTemplate.send("user-events", new UserEvent("PROFILE_UPDATED", id, null));
        return updatedUser;
    }

    public void processAuthenticatedUser(Long userId) {
        logger.info("Processing authenticated user with ID: {}", userId);
        User user = getUser(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Updated last login time for user ID: {}", userId);
    }
}