package stock.social_service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class FollowId implements Serializable {
    private Long followerId;
    private Long followeeId;

    // 기본 생성자
    public FollowId() {}

    // 모든 필드를 인자로 받는 생성자
    public FollowId(Long followerId, Long followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }
}