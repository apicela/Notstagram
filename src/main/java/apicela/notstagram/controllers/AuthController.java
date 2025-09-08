package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.CompleteRegisterRequest;
import apicela.notstagram.models.requests.LoginRequest;
import apicela.notstagram.models.requests.RefreshTokenRequest;
import apicela.notstagram.models.requests.VerificationCode;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.services.AuthService;
import apicela.notstagram.services.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário e gerar tokens de acesso.",
            description = "Este endpoint permite que um usuário existente faça login. Ele recebe o email e a senha no corpo da requisição (`LoginRequest`), autentica as credenciais e, se forem válidas, retorna um **token de acesso** e um **token de atualização** para uso em requisições futuras."
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }

    @PostMapping("/register/email")
    @Operation(
            summary = "Iniciar o processo de registro.",
            description = "Este endpoint é o primeiro passo para o registro de um novo usuário. Ele recebe um **email** no corpo da requisição (`EmailDTO`), cria um usuário pendente no banco de dados e envia um código de verificação para o email fornecido. Em seguida, retorna um **token temporário** com a autoridade `PENDING_USER_TOKEN` para permitir a próxima etapa de verificação."
    )
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid EmailDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createPendingUser(dto.email()));
    }

    @PreAuthorize("hasAuthority('PENDING_USER_TOKEN')")
    @PostMapping("/register/confirm")
    @Operation(
            summary = "Confirmar o email do usuário.",
            description = "Este endpoint é o segundo passo do processo de registro. Ele exige um **token temporário** de usuário pendente (`PENDING_USER_TOKEN`). O usuário deve fornecer o **código de verificação** recebido por email para confirmar sua identidade. Se o código for válido, o email do usuário é marcado como verificado e um novo **token temporário** com a autoridade `CONFIRMED_USER_TOKEN` é retornado para prosseguir com o registro."
    )
    public ResponseEntity<AuthResponse> confirmEmail(@AuthenticationPrincipal User user, VerificationCode verificationCode) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.confirmPendingUser(user, verificationCode.code()));
    }

    @PreAuthorize("hasAuthority('CONFIRMED_USER_TOKEN')")
    @PostMapping("/register/complete")
    @Operation(
            summary = "Concluir o registro do usuário.",
            description = "Este endpoint é a etapa final do processo de registro. Ele exige um **token temporário** de usuário confirmado (`CONFIRMED_USER_TOKEN`). O usuário envia o nome de usuário, senha e a preferência de perfil público (`CompleteRegisterRequest`) para finalizar o cadastro. O perfil do usuário é atualizado, a senha é criptografada e uma nova sessão é iniciada, retornando os **tokens de acesso e atualização** definitivos."
    )
    public ResponseEntity<AuthResponse> completeRegister(@AuthenticationPrincipal User user, @RequestBody @Valid CompleteRegisterRequest completeRegisterRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.completeRegister(user, completeRegisterRequest));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Gera um novo access token usando um refresh token válido. Pode retornar também um novo refresh token se usar rotação."
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.rotateToken(request));
    }

    record EmailDTO(@Schema(example = "jamilnetobr@gmail.com", description = "O e-mail do usuário")
                    @Email(message = "E-mail invalid") String email) {
    }
}
