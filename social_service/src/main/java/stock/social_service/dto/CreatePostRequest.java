package stock.social_service.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private Long userId;
    private String title;
    private String content;
    private Long stockId;
}