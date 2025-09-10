package apicela.notstagram.models.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_users_roles")
@Data
public class UserRole implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private LocalDateTime expirationDate;

    public UserRole() {
    }

    public UserRole(User user, Role role, int seconds) {
        this.user = user;
        this.role = role;
        this.expirationDate = LocalDateTime.now().plusSeconds(seconds);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(user, role);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRole that = (UserRole) obj;
        return java.util.Objects.equals(user, that.user) && java.util.Objects.equals(role, that.role);
    }
}