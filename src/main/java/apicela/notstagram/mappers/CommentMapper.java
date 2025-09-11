package apicela.notstagram.mappers;

import apicela.notstagram.models.dtos.CommentDTO;
import apicela.notstagram.models.dtos.UserSummaryDTO;
import apicela.notstagram.models.entities.Comment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {

    public CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                comment.getCreatedAt(),
                new UserSummaryDTO(comment.getUser().getUsername())
        );
    }

    public List<CommentDTO> toDTOList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDTO)
                .toList();
    }
}
