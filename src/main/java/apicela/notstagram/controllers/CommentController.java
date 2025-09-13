package apicela.notstagram.controllers;


import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CommentRequest;
import apicela.notstagram.services.CommentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@Log4j2
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> commentPost(@AuthenticationPrincipal User user,
                                            @PathVariable("postId") UUID postId,
                                            @RequestBody CommentRequest commentRequest) {
        commentService.commentPost(user, commentRequest, postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> removeComment(@AuthenticationPrincipal User user,
                                              @PathVariable("commentId") UUID commentId) {
        commentService.removeComment(user, commentId);
        return ResponseEntity.noContent().build();
    }

}
