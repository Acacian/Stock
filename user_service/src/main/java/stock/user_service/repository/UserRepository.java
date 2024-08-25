package stock.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import stock.user_service.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}