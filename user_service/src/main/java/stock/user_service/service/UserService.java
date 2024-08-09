package main.java.stock.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import main.java.stock.authentication.model.User;
import main.java.stock.authentication.repository.UserRepository;
import stock.user.dto.UpdateProfileRequest;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

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
        
        kafkaTemplate.send("user-updates", "PROFILE_UPDATED," + id);
        return updatedUser;
    }
}