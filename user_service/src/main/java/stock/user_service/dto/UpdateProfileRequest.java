package stock.user_service.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String profileImage;
    private String introduction;
}