package apicela.notstagram.services;

import apicela.notstagram.mappers.PostMapper;
import apicela.notstagram.models.PostType;
import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostMapper postMapper;
    @Value("${upload.dir}")
    private String uploadDir;

    public PostService(PostRepository postRepository, PostMapper postMapper, UserService userService) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.userService = userService;
    }

    public void createPost(User user, MultipartFile file, String description) throws IOException {
        String contentType = file.getContentType();
        PostType postType;
        if (contentType != null && contentType.startsWith("image/")) {
            postType = PostType.IMAGE;
        } else if (contentType != null && contentType.startsWith("video/")) {
            postType = PostType.VIDEO;
        } else throw new BadRequestException("Invalid content type");

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = Paths.get(uploadDir, fileName);

        // Cria diretório se não existir
        Files.createDirectories(filePath.getParent());

        // Salva fisicamente no sistema de arquivos
        Files.write(filePath, file.getBytes());

        // Salva no banco apenas o caminho relativo
        Post post = new Post();
        post.setUser(user);
        post.setMediaPath(filePath.toString());
        post.setType(postType);
        post.setContentType(contentType);
        post.setDescription(description);
        postRepository.save(post);
    }

    public PostDTO getPost(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return postMapper.toDTO(post, user);
    }

    public List<PostDTO> getFeed(User user) {
        List<UUID> followingList = userService.findFollowingList(user.getId());
        List<Post> posts = postRepository.findPostsFromFollowing(followingList);
        return postMapper.toDTOList(posts, user);
    }

    public GetMediaDTO loadFile(UUID postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        Path path = Paths.get(post.getMediaPath());
        return new GetMediaDTO(Files.readAllBytes(path), post.getContentType());
    }
}
