package apicela.notstagram.models.requests;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(@Schema(example = "jamilnetobr@gmail.com", description = "O e-mail do usuário") String email,
                           @Schema(example = "123", description = "Senha do usuário") String password) {
}
