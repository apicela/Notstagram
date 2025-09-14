package apicela.notstagram.mappers;

import apicela.notstagram.models.dtos.CommentDTO;
import apicela.notstagram.models.dtos.UserSummaryDTO;
import apicela.notstagram.models.entities.Comment;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CommentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {

    public CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                comment.getCreatedAt(),
                new UserSummaryDTO(comment.getUser().getUsername(), comment.getUser().getProfilePhoto())
        );
    }

    public Comment toEntity(CommentRequest dto, User user, Post post) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(dto.text());
        return comment;
    }

    public List<CommentDTO> toDTOList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDTO)
                .toList();
    }
}
