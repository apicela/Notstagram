package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> followUser(@AuthenticationPrincipal User user, @PathVariable String username) {
        userService.followUser(user, username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal User user, @PathVariable String username) {
        userService.unfollowUser(user, username);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("!#user.inactive")
    @DeleteMapping("/me/deactivate")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal User user) {
        userService.deactivateUser(user);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#user.inactive")
    @PostMapping("/me/activate")
    public ResponseEntity<Void> activate(@AuthenticationPrincipal User user) {
        userService.activateUser(user);
        return ResponseEntity.noContent().build();
    }

}

