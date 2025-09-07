package apicela.notstagram.models.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Values {
        BASIC(2L),
        ADMIN(1L);

        long Id;

        Values(long id) {
            this.Id = id;
        }
    }

}