package stock.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import stock.authentication.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}