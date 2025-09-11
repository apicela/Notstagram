package apicela.notstagram.mappers;

import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.dtos.UserSummaryDTO;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                new UserSummaryDTO(post.getUser().getUsername()),
                post.getLikedBy().size(),
                likedByMe,
                post.getComments().size(),
                commentMapper.toDTOList(post.getComments())

        );
    }
}
