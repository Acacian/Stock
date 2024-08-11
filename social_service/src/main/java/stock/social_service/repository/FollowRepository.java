package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.social_service.model.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
}
