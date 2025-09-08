package apicela.notstagram.repositories;

import apicela.notstagram.models.entities.AuthCode;
import apicela.notstagram.models.entities.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
}
