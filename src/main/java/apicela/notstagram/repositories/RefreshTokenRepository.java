package apicela.notstagram.repositories;

import apicela.notstagram.models.entities.RefreshToken;
import apicela.notstagram.models.entities.Role;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
}
