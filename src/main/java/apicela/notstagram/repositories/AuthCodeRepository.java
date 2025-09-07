package apicela.notstagram.repositories;

import apicela.notstagram.models.entities.AuthCode;
import apicela.notstagram.models.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthCodeRepository extends CrudRepository<AuthCode, UUID> {
    @Query("SELECT a FROM AuthCode a WHERE a.user = :user AND a.expiration > :now ORDER BY a.expiration DESC")
    Optional<AuthCode> findLastValidByUser(@Param("user") User user, @Param("now") LocalDateTime now);

}
