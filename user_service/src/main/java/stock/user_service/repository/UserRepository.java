package stock.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.user_service.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}