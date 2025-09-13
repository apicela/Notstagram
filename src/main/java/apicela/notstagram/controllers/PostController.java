package apicela.notstagram.controllers;

import apicela.notstagram.models.dtos.GetMediaDTO;
import apicela.notstagram.models.dtos.PostDTO;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.responses.DefaultApiResponse;
import apicela.notstagram.services.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Invalid content type",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))

    })
    public ResponseEntity<Void> uploadPost(@AuthenticationPrincipal User user,
                                           @RequestPart("description") String description,
                                           @RequestPart("file") MultipartFile file
    ) throws IOException {
        postService.createPost(user, file, description);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar um post por ID",
            description = "Recupera os detalhes de um post específico a partir do seu **UUID**. " +
                    "Retorna um objeto `PostDTO` contendo informações do post e se o usuário atual interage com ele (like, follow, etc.)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post encontrado",
                    content = @Content(schema = @Schema(implementation = PostDTO.class))),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<PostDTO> getPost(@AuthenticationPrincipal User user, @PathVariable UUID id) throws IOException {
        return ResponseEntity.ok().body(postService.getPost(id, user));
    }


    @GetMapping("/feed")
    @Operation(
            summary = "Obter feed do usuário",
            description = "Retorna uma lista de posts (`List<PostDTO>`) publicados pelos usuários que o usuário autenticado segue."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feed carregado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<List<PostDTO>> getFeed(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok().body(postService.getFeed(currentUser));
    }


    @GetMapping("/media/{id}")
    @Operation(
            summary = "Obter mídia de um post",
            description = "Retorna o conteúdo binário (imagem ou vídeo) associado a um post específico. " +
                    "O tipo de mídia (`image/png`, `image/jpeg`, `video/mp4`, etc.) é retornado dinamicamente no cabeçalho `Content-Type`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mídia carregada com sucesso",
                    content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<byte[]> getMedia(@PathVariable UUID id) throws IOException {
        GetMediaDTO dto = postService.loadFile(id);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(dto.contentType()))
                .body(dto.bytes());
    }

}
