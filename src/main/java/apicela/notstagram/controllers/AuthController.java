package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.services.AuthService;
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

@RestController()
@RequestMapping("/auth")
@Log4j2
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "", description = "")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }

    @PostMapping("/register/email")
    @Operation(summary = "CREATE", description = "Here, you can create a new object for your entity")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid EmailDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createPendingUser(dto.email()));
    }

    @PreAuthorize("hasAuthority('PENDING_USER_TOKEN')")
    @PostMapping("/register/confirm")
    public ResponseEntity<AuthResponse> confirmEmail(@AuthenticationPrincipal User user, VerificationCode verificationCode) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.confirmPendingUser(user, verificationCode.code()));
    }

    @PreAuthorize("hasAuthority('CONFIRMED_USER_TOKEN')")
    @PostMapping("/register/complete")
    public ResponseEntity<AuthResponse> completeRegister(@AuthenticationPrincipal User user, @RequestBody @Valid CompleteRegisterRequest completeRegisterRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.completeRegister(user, completeRegisterRequest));
    }

    record EmailDTO(@Schema(example = "jamilnetobr@gmail.com", description = "O e-mail do usuário")
                    @Email(message = "E-mail invalid") String email) {
    }
}
