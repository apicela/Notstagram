package apicela.notstagram.controllers;

import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.services.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private User mockUser;
    private UUID mockPostId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setUsername("mockUser");
        mockPostId = UUID.randomUUID();
    }

    @Test
    void testUploadPost_success() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "image.png", "image/png", "image content".getBytes()
        );
        String description = "Post description";

        doNothing().when(postService).createPost(mockUser, mockFile, description);

        ResponseEntity<Void> response = postController.uploadPost(mockUser, description, mockFile);

        assertEquals(204, response.getStatusCodeValue());
        verify(postService, times(1)).createPost(mockUser, mockFile, description);
    }

    @Test
    void testGetPost_success() throws IOException {
        PostDTO mockPostDTO = new PostDTO(
                mockPostId,
                "Description",
                "/media/image.png",
                null,
                "image/png",
                null,
                null,
                0,
                false,
                0);

        when(postService.getPost(mockPostId, mockUser)).thenReturn(mockPostDTO);

        ResponseEntity<PostDTO> response = postController.getPost(mockUser, mockPostId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockPostDTO, response.getBody());
        verify(postService, times(1)).getPost(mockPostId, mockUser);
    }

    @Test
    void testGetFeed_success() {
        PostDTO mockPostDTO = new PostDTO(
                mockPostId,
                "Description",
                "/media/image.png",
                null,
                "image/png",
                null,
                null,
                0,
                false,
                0);

        List<PostDTO> mockFeed = List.of(mockPostDTO);
        when(postService.getFeed(mockUser)).thenReturn(mockFeed);

        ResponseEntity<List<PostDTO>> response = postController.getFeed(mockUser);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockFeed, response.getBody());
        verify(postService, times(1)).getFeed(mockUser);
    }

    @Test
    void testGetMedia_success() throws IOException {
        byte[] bytes = "media content".getBytes();
        String contentType = "image/png";

        GetMediaDTO mockDTO = new GetMediaDTO(bytes, contentType);

        when(postService.loadFile(mockUser,mockPostId)).thenReturn(mockDTO);

        ResponseEntity<byte[]> response = postController.getMedia(mockUser,mockPostId);

        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(bytes, response.getBody());
        assertEquals(contentType, response.getHeaders().getContentType().toString());
        verify(postService, times(1)).loadFile(mockUser,mockPostId);
    }
}
