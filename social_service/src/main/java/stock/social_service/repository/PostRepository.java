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

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.parent IS NOT NULL ORDER BY p.createdAt DESC")
    List<Post> findRecentCommentsByUserId(@Param("userId") Long userId);

    Page<Post> findByUserId(Long userId, Pageable pageable);

    Page<Post> findByTitleContaining(String title, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN User u ON p.userId = u.id WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%'))")
    Page<Post> findByUserNameContaining(@Param("userName") String userName, Pageable pageable);

    Page<Post> findByTitleContainingOrContentContainingAndStockId(String title, String content, Long stockId, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN User u ON p.userId = u.id WHERE " +
    "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
    "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
    "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN User u ON p.userId = u.id WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Post> findByUserNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);
}