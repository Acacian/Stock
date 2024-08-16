package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import stock.social_service.model.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    long countByPostId(Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.userId = :userId ORDER BY c.createdAt DESC LIMIT 10")
    List<Comment> findRecentCommentsByUserId(Long userId);
}