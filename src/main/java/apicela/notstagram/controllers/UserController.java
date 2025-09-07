package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.models.responses.DefaultApiResponse;
import apicela.notstagram.models.responses.TokenResponse;
import apicela.notstagram.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/email")
    @Operation(summary = "CREATE", description = "Here, you can create a new object for your entity")
    public ResponseEntity<DefaultApiResponse> registerUser(@RequestBody @Valid EmailDTO dto) {
        TokenResponse token = userService.createPendingUser(dto.email());
        DefaultApiResponse<TokenResponse> response = new DefaultApiResponse<>(
                "User created successfully",
                token,
                HttpStatus.CREATED.value()
        );
        log.info("{}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('PENDING_USER_TOKEN')")
    @PostMapping("/register/confirm")
    public ResponseEntity<DefaultApiResponse> confirmEmail(@AuthenticationPrincipal User user, VerificationCode verificationCode) {
        TokenResponse token = userService.confirmPendingUser(user, verificationCode.code());
        DefaultApiResponse<TokenResponse> response = new DefaultApiResponse<>(
                "User confirmed.",
                token,
                HttpStatus.CREATED.value()
        );
        log.info("{}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('CONFIRMED_USER_TOKEN')")
    @PostMapping("/register/complete")
    public ResponseEntity<DefaultApiResponse> completeRegister(@AuthenticationPrincipal User user, @RequestBody @Valid CompleteRegisterRequest completeRegisterRequest) {
        TokenResponse token = userService.completeRegister(user, completeRegisterRequest);

        DefaultApiResponse<TokenResponse> response = new DefaultApiResponse<>(
                "Register completed.",
                token,
                HttpStatus.CREATED.value()
        );
        log.info("{}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    record EmailDTO(@Schema(example = "jamilnetobr@gmail.com", description = "O e-mail do usuário")
                    @Email(message = "E-mail invalid") String email) {
    }
}

