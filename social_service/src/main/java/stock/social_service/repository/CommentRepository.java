package stock.social_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.social_service.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 필요한 추가 메서드가 있을 경우 여기에 정의
}
