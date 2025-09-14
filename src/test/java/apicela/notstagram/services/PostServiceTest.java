package apicela.notstagram.services;


import apicela.notstagram.mappers.PostMapper;
import apicela.notstagram.models.PostType;
import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.dtos.UserSummaryDTO;
import apicela.notstagram.models.entities.Post;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.PostRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private User mockUser;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("mockUser");

        // Mockar diretório de upload temporário
        tempDir = Files.createTempDirectory("uploadTest");
        ReflectionTestUtils.setField(postService, "uploadDir", tempDir.toString());
    }

    @Test
    void testCreatePost_image_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        postService.createPost(mockUser, file, "Descrição do post");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testCreatePost_video_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake-video-content".getBytes()
        );

        postService.createPost(mockUser, file, "Vídeo do post");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testCreatePost_invalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake-pdf-content".getBytes()
        );

        assertThrows(BadRequestException.class,
                () -> postService.createPost(mockUser, file, "Descrição inválida"));
    }

    @Test
    void testGetPost_success() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        UserSummaryDTO userSummary = new UserSummaryDTO(mockUser.getUsername(), mockUser.getProfilePhoto());
        PostDTO postDTO = new PostDTO(
                postId,
                "Descrição do post",
                "/media/test.png",
                PostType.IMAGE,
                "image/png",
                LocalDateTime.now(),
                userSummary,
                0,
                false,
                0
        );

        when(postMapper.toDTO(post, mockUser)).thenReturn(postDTO);

        PostDTO result = postService.getPost(postId, mockUser);
        assertEquals(postDTO, result);
    }


    @Test
    void testGetFeed_success() {
        // Lista de usuários que o mockUser segue
        List<UUID> followingList = List.of(UUID.randomUUID());
        when(userService.findFollowingList(mockUser.getId())).thenReturn(followingList);

        // Lista de posts mockados retornados pelo repositório
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setDescription("Descrição do post");
        post.setContentType("image/png");
        post.setType(PostType.IMAGE);
        post.setUser(mockUser);
        post.setMediaPath("/media/test.png");
        post.setCreatedAt(LocalDateTime.now());

        List<Post> posts = List.of(post);
        when(postRepository.findPostsFromFollowing(followingList)).thenReturn(posts);

        // Lista de PostDTOs que o mapper deve retornar
        UserSummaryDTO userSummary = new UserSummaryDTO(mockUser.getUsername(), mockUser.getProfilePhoto());

        PostDTO postDTO = new PostDTO(
                post.getId(),
                post.getDescription(),
                post.getMediaPath(),
                post.getType(),
                post.getContentType(),
                post.getCreatedAt(),
                userSummary,
                0,       // likesCount
                false,   // likedByMe
                0       // commentsCount
        );

        List<PostDTO> dtoList = List.of(postDTO);
        when(postMapper.toDTOList(posts, mockUser)).thenReturn(dtoList);

        // Executa o método
        List<PostDTO> result = postService.getFeed(mockUser);

        // Verifica o resultado
        assertEquals(dtoList, result);
        verify(userService, times(1)).findFollowingList(mockUser.getId());
        verify(postRepository, times(1)).findPostsFromFollowing(followingList);
        verify(postMapper, times(1)).toDTOList(posts, mockUser);
    }


    @Test
    void testLoadFile_success() throws IOException {
        UUID postId = UUID.randomUUID();
        Path filePath = tempDir.resolve("test.png");
        Files.write(filePath, "conteudo".getBytes());

        Post post = new Post();
        post.setMediaPath(filePath.toString());
        post.setContentType("image/png");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        GetMediaDTO mediaDTO = postService.loadFile(mockUser, postId);
        assertArrayEquals("conteudo".getBytes(), mediaDTO.bytes());
        assertEquals("image/png", mediaDTO.contentType());
    }
}
