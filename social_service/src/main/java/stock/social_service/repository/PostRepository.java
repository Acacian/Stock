package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stock.social_service.model.Post;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    
    @Query("SELECT p FROM Post p WHERE p.userId = :userId ORDER BY p.createdAt DESC LIMIT 10")
    List<Post> findRecentPostsByUserId(Long userId);
    
    Post findTopByUserIdOrderByCreatedAtDesc(Long userId);
}