package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stock.social_service.model.Follow;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
    
    @Query("SELECT f.followerId FROM Follow f WHERE f.followedId = :followedId")
    List<Long> findFollowersByFollowedId(Long followedId);
}