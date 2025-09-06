package apicela.notstagram.repositories;

import apicela.notstagram.models.AuthCode;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AuthCodeRepository extends CrudRepository<AuthCode, UUID> {
}
