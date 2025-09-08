package apicela.notstagram.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private User user;

    @Column(unique = true, nullable = false)
    private String token =  UUID.randomUUID().toString();

    private LocalDateTime expiryDate;
}
