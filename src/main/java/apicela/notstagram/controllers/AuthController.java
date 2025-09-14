package apicela.notstagram.controllers;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.models.requests.*;
import apicela.notstagram.models.responses.AuthResponse;
import apicela.notstagram.models.responses.DefaultApiResponse;
import apicela.notstagram.services.AuthService;
import apicela.notstagram.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso, retorna tokens de acesso e atualização",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
    }


    @PostMapping("/register/email")
    @Operation(
            summary = "Iniciar o processo de registro.",
            description = "Este endpoint é o primeiro passo para o registro de um novo usuário. Ele recebe um **email** no corpo da requisição (`EmailDTO`), cria um usuário pendente no banco de dados e envia um código de verificação para o email fornecido. Em seguida, retorna um **token temporário** com a autoridade `PENDING_USER_TOKEN` para permitir a próxima etapa de verificação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário pendente criado, código enviado por email",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email já está em uso",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid EmailDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createPendingUser(dto.email()));
    }


    @PreAuthorize("hasAuthority('PENDING_USER_TOKEN')")
    @PatchMapping("/register/confirm")
    @Operation(
            summary = "Confirmar o email do usuário.",
            description = "Segundo passo do processo de registro. Exige um **token temporário** (`PENDING_USER_TOKEN`) e um código de verificação recebido por email. Se válido, retorna um novo **token temporário** com autoridade `CONFIRMED_USER_TOKEN`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token inválido ou ausente",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código de verificação inválido",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> confirmEmail(@AuthenticationPrincipal User user, VerificationCode verificationCode) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.confirmPendingUser(user, verificationCode.code()));
    }


    @PreAuthorize("hasAuthority('CONFIRMED_USER_TOKEN')")
    @PatchMapping("/register/complete")
    @Operation(
            summary = "Concluir o registro do usuário.",
            description = "Etapa final do registro. Exige um **token temporário** (`CONFIRMED_USER_TOKEN`). O usuário envia nome de usuário, senha e preferência de perfil público (`CompleteRegisterRequest`). Retorna os tokens definitivos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro concluído com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nome de usuário já em uso",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token inválido ou ausente",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> completeRegister(@AuthenticationPrincipal User user, @RequestBody @Valid CompleteRegisterRequest completeRegisterRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.completeRegister(user, completeRegisterRequest));
    }


    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Gera um novo access token usando um refresh token válido. Pode retornar também um novo refresh token se usar rotação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Novo access token gerado",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.rotateToken(request));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Solicitar redefinição de senha",
            description = "Envia um e-mail com instruções para redefinir a senha do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de redefinição de senha enviado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "E-mail inválido ou usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody @Valid EmailDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.resetPassword(dto.email()));
    }

    @PreAuthorize("hasAuthority('RESET_PASSWORD')")
    @PutMapping("/change-password")
    @Operation(
            summary = "Alterar senha do usuário",
            description = "Permite que o usuário altere sua senha após solicitar a redefinição. "
                    + "Necessário possuir a permissão `RESET_PASSWORD`."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha não atende os requisitos mínimos",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui a permissão necessária para alterar senha",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class)))
    })
    public ResponseEntity<AuthResponse> changePassword(@AuthenticationPrincipal User user,
                                                       @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.changePassword(user, changePasswordRequest));
    }


    record EmailDTO(@Schema(example = "jamilnetobr@gmail.com", description = "O e-mail do usuário")
                    @Email(message = "E-mail invalid") String email) {
    }
}
