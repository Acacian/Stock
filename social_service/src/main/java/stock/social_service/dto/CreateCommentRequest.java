package stock.social_service.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private Long userId;
    private String content;
}