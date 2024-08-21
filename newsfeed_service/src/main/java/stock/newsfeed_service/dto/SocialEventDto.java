package stock.newsfeed_service.dto;

import lombok.Data;

@Data
public class SocialEventDto {
    private String type;
    private Long userId;
    private Long targetId;
    private Long additionalId;
    private String content;
}