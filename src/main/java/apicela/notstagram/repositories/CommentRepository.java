package apicela.notstagram.repositories;

import apicela.notstagram.models.entities.Comment;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CommentRepository extends CrudRepository<Comment, UUID> {
}
