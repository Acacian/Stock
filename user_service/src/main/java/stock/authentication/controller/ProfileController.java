package stock.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.user_service.model.User;
import stock.user_service.service.AuthService;
import stock.user_service.dto.UpdateProfileRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private AuthService authService;

    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadProfileImage(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("userId") Long userId) {
        try {
            // 이미지 저장 경로 설정
            String uploadDir = "uploads/profile_images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);  // 디렉토리 생성
            }

            // 파일 이름 생성
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 사용자 정보 업데이트
            User user = authService.getUser(userId);
            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setName(user.getName()); 
            updateRequest.setProfileImage(filePath.toString()); 
            updateRequest.setIntroduction(user.getIntroduction());  
            authService.updateProfile(userId, updateRequest);

            return ResponseEntity.ok("Profile image uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image.");
        }
    }
}
