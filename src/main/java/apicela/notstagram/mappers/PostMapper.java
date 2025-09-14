package apicela.notstagram.mappers;

import apicela.notstagram.models.PostType;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.dtos.UserSummaryDTO;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostMapper {
    final CommentMapper commentMapper;

    public PostMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public PostDTO toDTO(Post post, User loggedUser) {
        boolean likedByMe = post.getLikedBy().stream()
                .anyMatch(user -> user.getId().equals(loggedUser.getId()));
        return new PostDTO(
                post.getId(),
                post.getDescription(),
                "/post/" + post.getId() + "/media",
                post.getType(),
                post.getContentType(),
                post.getCreatedAt(),
                new UserSummaryDTO(post.getUser().getUsername(), post.getUser().getProfilePhoto()),
                post.getLikedBy().size(),
                likedByMe,
                post.getComments().size()
        );
    }

    public Post toEntity(User user, String description, String mediaPath, PostType postType, String contentType) {
        Post post = new Post();
        post.setUser(user);
        post.setDescription(description);
        post.setMediaPath(mediaPath);
        post.setType(postType);
        post.setContentType(contentType);
        return post;
    }

    public List<PostDTO> toDTOList(List<Post> posts, User loggedUser) {
        return posts.stream()
                .map(post -> toDTO(post, loggedUser))
                .toList();
    }
}
