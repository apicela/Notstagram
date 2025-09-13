package apicela.notstagram.services;

import apicela.notstagram.exceptions.UnauthorizedException;
import apicela.notstagram.mappers.CommentMapper;
import apicela.notstagram.models.entities.Comment;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CommentRequest;
import apicela.notstagram.repositories.CommentRepository;
import apicela.notstagram.repositories.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Log4j2
public class CommentService {
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    public CommentService(PostRepository postRepository, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
    }

    public void commentPost(User user, CommentRequest dto, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        Comment comment = commentMapper.toEntity(dto, user, post);
        commentRepository.save(comment);
    }

    public void removeComment(User user, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("User dont have permission to do that");
        }
        commentRepository.delete(comment);
    }

}
