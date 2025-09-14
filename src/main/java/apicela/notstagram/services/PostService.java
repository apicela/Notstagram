package apicela.notstagram.services;

import apicela.notstagram.mappers.PostMapper;
import apicela.notstagram.models.PostType;
import apicela.notstagram.models.dtos.FileDTO;
import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.PostRepository;
import apicela.notstagram.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
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
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final FileStorageService fileStorageService;

    public PostService(PostRepository postRepository, PostMapper postMapper, UserRepository userRepository, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public PostDTO createPost(User user, MultipartFile file, String description) throws IOException {
        String contentType = file.getContentType();
        PostType postType;
        if (contentType != null && contentType.startsWith("image/")) {
            postType = PostType.IMAGE;
        } else if (contentType != null && contentType.startsWith("video/")) {
            postType = PostType.VIDEO;
        } else throw new BadRequestException("Invalid content type");

        FileDTO fileDTO = fileStorageService.saveFile(file);

        Post post = postMapper.toEntity(user, description, fileDTO.path(), postType, contentType);
        postRepository.save(post);
        return postMapper.toDTO(post, user);
    }

    public PostDTO getPost(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return postMapper.toDTO(post, user);
    }

    public List<PostDTO> getPosts(User requester, User target) {
        List<Post> posts = postRepository.findByUser(target);
        return postMapper.toDTOList(posts, requester);
    }

    public PostDTO likePost(User user, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.getLikedBy().add(user);
        postRepository.save(post);
        return postMapper.toDTO(post, user);
    }

    public PostDTO unlikePost(User user, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.getLikedBy().remove(user);
        postRepository.save(post);
        return postMapper.toDTO(post, user);
    }

    public List<PostDTO> getFeed(User user) {
        List<UUID> followingList = userRepository.findFollowingIds(user.getId());
        List<Post> posts = postRepository.findPostsFromFollowing(followingList);
        return postMapper.toDTOList(posts, user);
    }

    public GetMediaDTO loadFile(User user, UUID postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        validatePostVisibility(post, user);
        Path path = Paths.get(post.getMediaPath());
        return new GetMediaDTO(Files.readAllBytes(path), post.getContentType());
    }

    private void validatePostVisibility(Post post, User requester) throws BadRequestException {
        boolean isPostOwner = requester.getId().equals(post.getUser().getId());
        if (isPostOwner) return;

        if (!post.isVisible()) {
            throw new BadRequestException("Post is not visible");
        }

        boolean ownerInactive = post.getUser().isInactive();
        boolean ownerPrivate = !post.getUser().isPublicProfile();
        boolean requesterIsFollower = post.getUser().getFollowers().contains(requester);

        if (ownerInactive || (ownerPrivate && !requesterIsFollower)) {
            throw new BadRequestException("Post is not visible");
        }
    }

}
