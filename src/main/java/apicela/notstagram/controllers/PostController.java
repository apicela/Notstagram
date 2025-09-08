package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.PostDTORequest;
import apicela.notstagram.services.PostService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Log4j2
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> uploadPost(@AuthenticationPrincipal User user,
            @RequestParam("dto") PostDTORequest dto
    ) throws IOException {
        Post savedPost = postService.createPost(user, dto);
        return ResponseEntity.ok(savedPost);
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<byte[]> getMedia(@PathVariable UUID id) throws IOException {
        byte[] file = postService.loadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }
}
