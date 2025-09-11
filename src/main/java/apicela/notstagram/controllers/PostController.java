package apicela.notstagram.controllers;

import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.services.PostService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/post")
@Log4j2
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadPost(@AuthenticationPrincipal User user,
                                           @RequestPart("description") String description,
                                           @RequestPart("file") MultipartFile file
    ) throws IOException {
        postService.createPost(user, file, description);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@AuthenticationPrincipal User user, @PathVariable UUID id) throws IOException {
        return ResponseEntity.ok().body(postService.getPost(id, user));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostDTO>> getFeed(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok().body(postService.getFeed(currentUser));
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<byte[]> getMedia(@PathVariable UUID id) throws IOException {
        GetMediaDTO dto = postService.loadFile(id);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(dto.contentType()))
                .body(dto.bytes());
    }


}
