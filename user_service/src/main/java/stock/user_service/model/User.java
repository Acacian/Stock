package stock.user_service.model;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String profileImage;
    private String introduction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
}