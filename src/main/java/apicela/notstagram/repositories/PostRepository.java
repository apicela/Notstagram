package apicela.notstagram.repositories;

import apicela.notstagram.models.entities.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
    @Query("SELECT p FROM Post p WHERE p.user.id IN :following ORDER BY p.createdAt DESC")
    List<Post> findPostsFromFollowing(@Param("following") List<UUID> following);
}
