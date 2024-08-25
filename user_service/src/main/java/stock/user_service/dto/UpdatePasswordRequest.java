package stock.user_service.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
}