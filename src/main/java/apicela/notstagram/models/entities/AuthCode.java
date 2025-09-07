package apicela.notstagram.models.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AuthCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User user;

    private Integer code;

    private LocalDateTime expiration;

    private LocalDateTime createdAt = LocalDateTime.now();

}
