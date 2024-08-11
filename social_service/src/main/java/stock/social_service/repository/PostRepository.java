package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.social_service.model.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}
