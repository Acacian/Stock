package stock.social_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stock.social_service.model.Post;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    
    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.parent IS NULL ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsByUserId(@Param("userId") Long userId);
    
    Post findTopByUserIdAndParentIsNullOrderByCreatedAtDesc(Long userId);

    Page<Post> findByStockId(Long stockId, Pageable pageable);

    Page<Post> findByContentContainingOrUserIdIn(String content, List<Long> userIds, Pageable pageable);

    List<Post> findByParentId(Long parentId);
    
    long countByParentId(Long parentId);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.parent IS NOT NULL ORDER BY p.createdAt DESC")
    List<Post> findRecentCommentsByUserId(@Param("userId") Long userId);

    Page<Post> findByContentContaining(String query, Pageable pageable);

    Page<Post> findByContentContainingAndStockId(String query, Long stockId, Pageable pageable);

    Page<Post> findByUserId(Long userId, Pageable pageable);
}