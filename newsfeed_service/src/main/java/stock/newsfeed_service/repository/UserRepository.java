package stock.newsfeed_service.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Set<Long> getFollowers(Long userId) {
        String sql = "SELECT follower_id FROM follows WHERE followee_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }

    public Long getPostOwner(Long postId) {
        String sql = "SELECT user_id FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, postId);
    }
}