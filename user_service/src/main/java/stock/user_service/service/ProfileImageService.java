package stock.user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stock.user_service.model.User;
import stock.user_service.dto.UpdateProfileRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProfileImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url}")
    private String uploadUrl;

    @Autowired
    private AuthService authService;

    public String uploadProfileImage(MultipartFile file, Long userId) throws Exception {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        String fileUrl = uploadUrl + "/" + fileName;

        User user = authService.getUser(userId);
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setName(user.getName());
        updateRequest.setProfileImage(fileUrl);
        updateRequest.setIntroduction(user.getIntroduction());
        authService.updateProfile(userId, updateRequest);

        return fileUrl;
    }
}