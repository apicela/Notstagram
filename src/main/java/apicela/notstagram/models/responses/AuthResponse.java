package apicela.notstagram.models.responses;

public record AuthResponse(String acessToken, String refreshToken, int access_expires_in) {
}
