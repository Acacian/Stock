package stock.user_service.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}