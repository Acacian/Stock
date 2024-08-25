package stock.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.user_service.service.ProfileImageService;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileImageService profileImageService;

    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("userId") Long userId) {
        try {
            String fileUrl = profileImageService.uploadProfileImage(file, userId);
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Profile image uploaded successfully",
                    "fileUrl", fileUrl
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}