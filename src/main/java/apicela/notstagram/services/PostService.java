package apicela.notstagram.services;

import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.PostDTORequest;
import apicela.notstagram.repositories.PostRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Log4j2
public class PostService {
    private final PostRepository postRepository;
    @Value("${upload.dir}")
    private String uploadDir;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(User user, PostDTORequest dto) throws IOException {
        MultipartFile file = dto.file();
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
        post.setContentType(file.getContentType());
        post.setDescription(dto.description());
        return postRepository.save(post);
    }

    public byte[] loadFile(UUID postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Path path = Paths.get(post.getMediaPath());
        return Files.readAllBytes(path);
    }

}
