package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stock.social_service.model.Follow;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    
    @Query("SELECT f.followerId FROM Follow f WHERE f.followeeId = :followeeId")
    List<Long> findFollowersByFolloweeId(Long followeeId);
}