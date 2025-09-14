package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.EditProfileRequest;
import apicela.notstagram.models.responses.DefaultApiResponse;
import apicela.notstagram.services.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{username}/follow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário seguido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<Void> followUser(@AuthenticationPrincipal User user, @PathVariable String username) {
        userService.followUser(user, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/follow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deixado de seguir com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal User user, @PathVariable String username) {
        userService.unfollowUser(user, username);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("!#user.inactive")
    @DeleteMapping("/me/deactivate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso")
    })
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal User user) {
        userService.deactivateUser(user);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("!#user.inactive")
    @PutMapping
    public ResponseEntity<Void> editProfile(@AuthenticationPrincipal User user, EditProfileRequest editProfileRequest, @RequestPart("file") MultipartFile file) throws IOException {
        userService.editProfile(user, editProfileRequest, file);
        return ResponseEntity.noContent().build();
    }
}

