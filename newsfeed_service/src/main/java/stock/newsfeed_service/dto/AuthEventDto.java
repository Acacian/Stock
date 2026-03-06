package stock.newsfeed_service.dto;

import lombok.Data;

@Data
public class AuthEventDto {
    private String type;
    private Long userId;
    private String email;
}
