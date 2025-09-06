package apicela.notstagram.repositories;

import apicela.notstagram.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.inactive = false")
    User findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.inactive = false")
    User findByEmail(@Param("email") String email);
}
